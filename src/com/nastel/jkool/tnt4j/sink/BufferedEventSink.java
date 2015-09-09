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
package com.nastel.jkool.tnt4j.sink;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.nastel.jkool.tnt4j.core.KeyValueStats;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.Snapshot;
import com.nastel.jkool.tnt4j.core.TTL;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.source.Source;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements a buffered event sink, which buffers events into a memory queue and then
 * flushes it to a specified out sink using a separate thread. <code>BufferedSink</code> decouples
 * writer from the actual sink write and can improve performance during bursts.
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
public class BufferedEventSink implements EventSink {
	static final String KEY_OBJECTS_DROPPED = "buffered-objects-dropped";
	static final String KEY_OBJECTS_SKIPPED = "buffered-objects-skipped";

	private long ttl = TTL.TTL_CONTEXT;
	private Source source;
	private EventSink outSink = null;
	private AtomicLong dropCount = new AtomicLong(0), skipCount = new AtomicLong(0);

	/**
	 * Create a buffered sink instance with a specified out sink
	 * maximum capacity. Event will be dropped if capacity is exceeded.
	 * Obtain drop counts and queue sizes using <code>getDropCount()</code> method.
	 *
	 * @param sink out sink where events/log message are written out
	 */
	public BufferedEventSink(EventSink sink) {
		outSink = sink;
		sink.filterOnLog(false); // disable filtering on the underlying sink (prevent double filters)
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
	 * Obtain total number of events/log messages skipped since last reset.
	 * Events are skipped when don't pass sink filters.
	 *
	 * @return total number of skipped messages since last reset
	 */
	public long getSkipCount() {
		return skipCount.get();
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
			boolean flag = BufferedEventSinkFactory.getPooledLogger().offer(new SinkLogEvent(outSink, getSource(), OpLevel.NONE, (ttl != TTL.TTL_CONTEXT)? ttl: TTL.TTL_DEFAULT, txtMsg, resolveArguments(args)));
			if (!flag) dropCount.incrementAndGet();
		} else {
			skipCount.incrementAndGet();
		}
	}

	@Override
    public void log(TrackingActivity activity) {
		_checkState();
		if (isLoggable(activity)) {
			if (ttl != TTL.TTL_CONTEXT) activity.setTTL(ttl);
			boolean flag = BufferedEventSinkFactory.getPooledLogger().offer(new SinkLogEvent(outSink, activity));
			if (!flag) dropCount.incrementAndGet();
		} else {
			skipCount.incrementAndGet();
		}
	}

	@Override
    public void log(TrackingEvent event) {
		_checkState();
		if (isLoggable(event)) {
			if (ttl != TTL.TTL_CONTEXT) event.setTTL(ttl);
			boolean flag = BufferedEventSinkFactory.getPooledLogger().offer(new SinkLogEvent(outSink, event));
			if (!flag) dropCount.incrementAndGet();
		} else {
			skipCount.incrementAndGet();
		}
    }

	@Override
    public void log(Snapshot props) {
		_checkState();
		if (isLoggable(props)) {
			if (ttl != TTL.TTL_CONTEXT) props.setTTL(ttl);
			boolean flag = BufferedEventSinkFactory.getPooledLogger().offer(new SinkLogEvent(outSink, props));
			if (!flag) dropCount.incrementAndGet();
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
		log((ttl != TTL.TTL_CONTEXT)? ttl: TTL.TTL_DEFAULT, src, sev, msg, args);
    }

	@Override
    public void log(long ttl_sec, Source src, OpLevel sev, String msg, Object... args) {
		_checkState();
		if (isLoggable(sev, msg, args)) {
			boolean flag = BufferedEventSinkFactory.getPooledLogger().offer(new SinkLogEvent(outSink, src, sev, ttl_sec, msg, resolveArguments(args)));
			if (!flag) dropCount.incrementAndGet();
		} else {
			skipCount.incrementAndGet();
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
		outSink.close();
    }

	@Override
    public Map<String, Object> getStats() {
	    Map<String, Object> stats = outSink.getStats();
	    getStats(stats);
	    return stats;
    }

	@Override
    public KeyValueStats getStats(Map<String, Object> stats) {
	    stats.put(Utils.qualify(this, KEY_OBJECTS_DROPPED), dropCount.get());
	    stats.put(Utils.qualify(this, KEY_OBJECTS_SKIPPED), skipCount.get());
	    BufferedEventSinkFactory.getPooledLogger().getStats(stats);
	    return outSink.getStats(stats);
    }

	@Override
    public void resetStats() {
		dropCount.set(0);
		skipCount.set(0);
		outSink.resetStats();
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
}
