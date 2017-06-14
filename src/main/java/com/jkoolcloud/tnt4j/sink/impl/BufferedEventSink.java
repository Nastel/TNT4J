/*
 * Copyright 2014-2015 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jkoolcloud.tnt4j.sink.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import com.jkoolcloud.tnt4j.core.KeyValueStats;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.core.TTL;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.*;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements a buffered event sink, which buffers events into a memory queue and then flushes it to a
 * specified out sink using a separate thread. {@code BufferedEventSink} decouples writer from the actual sink write and
 * can improve performance during bursts.
 * </p>
 *
 *
 * @version $Revision: 1 $
 *
 * @see EventSink
 * @see SinkError
 * @see SinkErrorListener
 * @see SinkLogEvent
 * @see SinkLogEventListener
 */
public class BufferedEventSink implements EventSink, IOShutdown {
	static final String KEY_OBJECTS_TOTAL = "buffered-objects-total";
	static final String KEY_OBJECTS_DROPPED = "buffered-objects-dropped";
	static final String KEY_OBJECTS_SKIPPED = "buffered-objects-skipped";
	static final String KEY_OBJECTS_REQUEUED = "buffered-objects-requeued";
	static final String KEY_FLUSH_COUNT = "buffered-flush-count";
	static final String KEY_TOTAL_ERRORS = "buffered-errors-total";

	private long ttl = TTL.TTL_CONTEXT;
	private long signalTimeout = 5000;
	private boolean block = false;
	private Source source;
	private EventSink outSink = null;
	private BufferedEventSinkFactory factory;

	// sink stat counters
	private AtomicLong totalCount = new AtomicLong(0);
	private AtomicLong signalCount = new AtomicLong(0);
	private AtomicLong skipCount = new AtomicLong(0);
	private AtomicLong dropCount = new AtomicLong(0);
	private AtomicLong rqCount = new AtomicLong(0);
	private AtomicLong errorCount = new AtomicLong(0);

	/**
	 * Create a buffered sink instance with a specified out sink maximum capacity. Event will be dropped if capacity is
	 * exceeded. Obtain drop counts and queue sizes using {@link #getDropCount()} method.
	 * 
	 * @param f buffered event sink factory
	 * @param sink out sink where events/log message are written out
	 * @param blocking set to true to block if full, false to drop messages if full
	 * 
	 */
	public BufferedEventSink(BufferedEventSinkFactory f, EventSink sink, boolean blocking) {
		factory = f;
		outSink = sink;
		block = blocking;
		outSink.addSinkErrorListener(new BufferedSinkErrorListener(this));
		outSink.filterOnLog(false); // disable filtering on the underlying sink (prevent double filters)
	}

	/**
	 * Set maximum signal timeout.
	 * 
	 * @param duration time
	 * @param units time unit
	 */
	public void setSignalTimeout(long duration, TimeUnit units) {
		signalTimeout = units.toMillis(duration);
	}

	/**
	 * Set maximum signal timeout in milliseconds
	 * 
	 * @param duration time
	 */
	public void setSignalTimeout(long duration) {
		setSignalTimeout(duration, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get maximum signal timeout in milliseconds
	 * 
	 * @return maximum flush timeout in milliseconds
	 */
	public long getSignalTimeout() {
		return signalTimeout;
	}

	/**
	 * Obtain total number of events/log messages dropped since last reset.
	 *
	 * @return total number of dropped messages since last reset
	 */
	public long getDropCount() {
		return dropCount.get();
	}

	/**
	 * Obtain total number of events/log messages re-queued since last reset.
	 *
	 * @return total number of re-queued messages since last reset
	 */
	public long getRqCount() {
		return rqCount.get();
	}

	/**
	 * Obtain total number of events/log messages skipped since last reset. Events are skipped when don't pass sink
	 * filters.
	 *
	 * @return total number of skipped messages since last reset
	 */
	public long getSkipCount() {
		return skipCount.get();
	}

	@Override
	public void setLimiter(EventLimiter limiter) {
		outSink.setLimiter(limiter);
	}

	@Override
	public EventLimiter getLimiter() {
		return outSink.getLimiter();
	}

	@Override
	public String getName() {
		return outSink.getName();
	}

	@Override
	public void addSinkErrorListener(SinkErrorListener listener) {
		outSink.addSinkErrorListener(listener);
	}

	@Override
	public void addSinkEventFilter(SinkEventFilter listener) {
		outSink.addSinkEventFilter(listener);
	}

	@Override
	public void addSinkLogEventListener(SinkLogEventListener listener) {
		outSink.addSinkLogEventListener(listener);
	}

	@Override
	public boolean isSet(OpLevel sev) {
		return outSink.isSet(sev);
	}

	@Override
	public void write(Object msg, Object... args) throws IOException, InterruptedException {
		_checkState();
		String txtMsg = String.valueOf(msg);
		if (isLoggable(OpLevel.NONE, txtMsg, args)) {
			SinkLogEvent sinkEvent = new SinkLogEvent(outSink, getSource(), OpLevel.NONE,
					(ttl != TTL.TTL_CONTEXT) ? ttl : TTL.TTL_DEFAULT, txtMsg, resolveArguments(args));
			_writeEvent(sinkEvent, block);
		} else {
			skipCount.incrementAndGet();
		}
	}

	@Override
	public void log(TrackingActivity activity) {
		_checkState();
		if (isLoggable(activity)) {
			if (ttl != TTL.TTL_CONTEXT) activity.setTTL(ttl);
			SinkLogEvent sinkEvent = new SinkLogEvent(outSink, activity);
			_writeEvent(sinkEvent, block);
		} else {
			skipCount.incrementAndGet();
		}
	}

	@Override
	public void log(TrackingEvent event) {
		_checkState();
		if (isLoggable(event)) {
			if (ttl != TTL.TTL_CONTEXT) event.setTTL(ttl);
			SinkLogEvent sinkEvent = new SinkLogEvent(outSink, event);
			_writeEvent(sinkEvent, block);
		} else {
			skipCount.incrementAndGet();
		}
	}

	@Override
	public void log(Snapshot snapshot) {
		_checkState();
		if (isLoggable(snapshot)) {
			if (ttl != TTL.TTL_CONTEXT) snapshot.setTTL(ttl);
			SinkLogEvent sinkEvent = new SinkLogEvent(outSink, snapshot);
			_writeEvent(sinkEvent, block);
		} else {
			skipCount.incrementAndGet();
		}
	}

	@Override
	public void log(OpLevel sev, String msg, Object... args) {
		log(source, sev, msg, args);
	}

	@Override
	public void log(Source src, OpLevel sev, String msg, Object... args) {
		log((ttl != TTL.TTL_CONTEXT) ? ttl : TTL.TTL_DEFAULT, src, sev, msg, args);
	}

	@Override
	public void log(long ttl_sec, Source src, OpLevel sev, String msg, Object... args) {
		_checkState();
		if (isLoggable(sev, msg, args)) {
			SinkLogEvent sinkEvent = new SinkLogEvent(outSink, src, sev, ttl_sec, msg, resolveArguments(args));
			_writeEvent(sinkEvent, block);
		} else {
			skipCount.incrementAndGet();
		}
	}

	private void _writeEvent(SinkLogEvent sinkEvent, boolean sync) {
		totalCount.incrementAndGet();
		if (sync) {
			try {
				factory.getPooledLogger().put(sinkEvent);
			} catch (Throwable ex) {
				dropCount.incrementAndGet();
			}
		} else {
			boolean flag = factory.getPooledLogger().offer(sinkEvent);
			if (!flag) {
				dropCount.incrementAndGet();
			}
		}
	}

	@Override
	public void removeSinkErrorListener(SinkErrorListener listener) {
		outSink.removeSinkErrorListener(listener);
	}

	@Override
	public void removeSinkEventFilter(SinkEventFilter listener) {
		outSink.removeSinkEventFilter(listener);
	}

	@Override
	public void removeSinkLogEventListener(SinkLogEventListener listener) {
		outSink.removeSinkLogEventListener(listener);
	}

	@Override
	public Object getSinkHandle() {
		return outSink;
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void open() {
		// open asynchronously PooledLogger should handle it
		// outSink.open();
	}

	@Override
	public void close() throws IOException {
		signal(SinkLogEvent.SIGNAL_CLOSE, signalTimeout, TimeUnit.MILLISECONDS);
	}

	@Override
	public Map<String, Object> getStats() {
		Map<String, Object> stats = outSink.getStats();
		getStats(stats);
		return stats;
	}

	@Override
	public KeyValueStats getStats(Map<String, Object> stats) {
		stats.put(Utils.qualify(this, KEY_OBJECTS_TOTAL), totalCount.get());
		stats.put(Utils.qualify(this, KEY_OBJECTS_DROPPED), dropCount.get());
		stats.put(Utils.qualify(this, KEY_OBJECTS_SKIPPED), skipCount.get());
		stats.put(Utils.qualify(this, KEY_OBJECTS_REQUEUED), rqCount.get());
		stats.put(Utils.qualify(this, KEY_FLUSH_COUNT), signalCount.get());
		stats.put(Utils.qualify(this, KEY_TOTAL_ERRORS), errorCount.get());
		factory.getPooledLogger().getStats(stats);
		return outSink.getStats(stats);
	}

	@Override
	public void resetStats() {
		totalCount.set(0);
		signalCount.set(0);
		dropCount.set(0);
		rqCount.set(0);
		errorCount.set(0);
		skipCount.set(0);
		outSink.resetStats();
	}

	@Override
	public void setEventFormatter(EventFormatter formatter) {
		outSink.setEventFormatter(formatter);
	}

	@Override
	public EventFormatter getEventFormatter() {
		return outSink.getEventFormatter();
	}

	@Override
	public void setSource(Source src) {
		source = src;
		if (outSink != null) {
			outSink.setSource(source);
		}
	}

	@Override
	public Source getSource() {
		return source;
	}

	/**
	 * Convert object array into an array of strings
	 *
	 * @param args array of objects
	 * @return array of string objects
	 */
	protected Object [] resolveArguments(Object...args) {
		if (args == null || args.length == 0) return null;
		for (int i = 0; i < args.length; i++) {
			if (!(args[i] instanceof Throwable)) {
				args[i] = String.valueOf(args[i]);
			}
		}
		return args;
	}

	/**
	 * Obtain {@link EventSinkFactory} associated with this sink.
	 *
	 * @return sink factory instance
	 */
	public EventSinkFactory getFactory() {
		return this.factory;
	}

	/**
	 * Override this method to check state of the sink before logging occurs.
	 *
	 * @throws IllegalStateException if sink is in wrong state
	 */
	protected void _checkState() throws IllegalStateException {
		AbstractEventSink.checkState(this);
	}

	@Override
	public boolean isLoggable(OpLevel level, String msg, Object... args) {
		return outSink.isLoggable(level, msg, args);
	}

	@Override
	public boolean isLoggable(Source source, OpLevel level, String msg, Object... args) {
		return outSink.isLoggable(getTTL(), source, level, msg, args);
	}

	@Override
	public boolean isLoggable(long ttl_sec, Source source, OpLevel level, String msg, Object... args) {
		return outSink.isLoggable(ttl_sec, source, level, msg, args);
	}

	@Override
	public boolean isLoggable(Snapshot snapshot) {
		return outSink.isLoggable(snapshot);
	}

	@Override
	public boolean isLoggable(TrackingActivity activity) {
		return outSink.isLoggable(activity);
	}

	@Override
	public boolean isLoggable(TrackingEvent event) {
		return outSink.isLoggable(event);
	}

	@Override
	public EventSink filterOnLog(boolean flag) {
		return outSink.filterOnLog(false);
	}

	@Override
	public long getTTL() {
		return ttl;
	}

	@Override
	public void setTTL(long ttl) {
		this.ttl = ttl;
	}

	@Override
	public boolean errorState() {
		return outSink.errorState();
	}

	@Override
	public Throwable getLastError() {
		return outSink.getLastError();
	}

	@Override
	public long getLastErrorTime() {
		return outSink.getLastErrorTime();
	}

	@Override
	public long getErrorCount() {
		return outSink.getErrorCount();
	}

	@Override
	public Throwable setErrorState(Throwable e) {
		return outSink.setErrorState(e);
	}

	@Override
	public void flush() throws IOException {
		signal(SinkLogEvent.SIGNAL_FLUSH, signalTimeout, TimeUnit.MILLISECONDS);
	}

	@Override
	public void shutdown(Throwable ex) throws IOException {
		factory.getPooledLogger().shutdown(ex);
		signal(SinkLogEvent.SIGNAL_SHUTDOWN, signalTimeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Send a signal to the underlying sink and wait for signal to be processed up to a max time.
	 *
	 * @param signalType type of signal to send
	 * @param wait max time to wait
	 * @param tunit time unit for wait
	 * @return sink factory instance
	 */
	protected BufferedEventSink signal(int signalType, long wait, TimeUnit tunit) throws IOException {
		signalCount.incrementAndGet();
		_writeEvent(new SinkLogEvent(outSink, Thread.currentThread(), signalType), true);
		LockSupport.parkNanos(this, tunit.toNanos(wait));
		return this;
	}

	/**
	 * Handle failed event by retrying to process it or dropping it depending on if it is possible to retry processing.
	 *
	 * @param ev sink error event
	 * @return sink factory instance
	 */
	protected BufferedEventSink handleError(SinkError ev) {
		errorCount.incrementAndGet();
		if (!factory.getPooledLogger().isDQfull()) {
			SinkLogEvent event = ev.getSinkEvent();
			factory.getPooledLogger().putDelayed(event);
			rqCount.incrementAndGet();
		} else {
			dropCount.incrementAndGet();
		}
		return this;
	}

	@Override
	public void reopen() throws IOException {
		this.close();
		this.open();
	}
}
