/*
 * Copyright 2014-2018 JKOOL, LLC.
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import com.jkoolcloud.tnt4j.core.KeyValueStats;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.limiter.DefaultLimiterFactory;
import com.jkoolcloud.tnt4j.limiter.Limiter;
import com.jkoolcloud.tnt4j.sink.*;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.NamedThreadFactory;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements an asynchronous pooled logger which allows logging events asynchronously by a set of threads
 * defined by a pool size. Because events are logged by a set of threads the relative sequence of events logged to the
 * underlying sinks are not guaranteed. The sinks must handle events coming out of sequence. Event time stamps are
 * preserved and should be used to sequence events properly.
 * </p>
 *
 *
 * @version $Revision: 1 $
 *
 * @see EventSink
 * @see KeyValueStats
 * @see SinkLogEvent
 */
public class PooledLogger implements KeyValueStats, IOShutdown {
	protected static final EventSink logger = DefaultEventSinkFactory.defaultEventSink(PooledLogger.class);
	protected static final double ERROR_RATE = Double
			.valueOf(System.getProperty("tnt4j.pooled.logger.error.rate", "0.1"));
	protected static final int REOPEN_FREQ = Integer.getInteger("tnt4j.pooled.logger.reopen.freq.ms", 10000);

	static final String KEY_Q_SIZE = "pooled-queue-size";
	static final String KEY_Q_TASKS = "pooled-queue-tasks";
	static final String KEY_Q_CAPACITY = "pooled-queue-capacity";
	static final String KEY_DQ_SIZE = "pooled-delay-size";
	static final String KEY_OBJECTS_DROPPED = "pooled-objects-dropped";
	static final String KEY_OBJECTS_SKIPPED = "pooled-objects-skipped";
	static final String KEY_OBJECTS_REQUEUED = "pooled-objects-requeued";
	static final String KEY_OBJECTS_LOGGED = "pooled-objects-logged";
	static final String KEY_OBJECTS_COUNT = "pooled-objects-total";
	static final String KEY_EXCEPTION_COUNT = "pooled-exceptions";
	static final String KEY_SIGNAL_COUNT = "pooled-signals";
	static final String KEY_RECOVERY_COUNT = "pooled-recovery-count";
	static final String KEY_LAST_SERVICE_TIME_USEC = "pooled-last-service-time-usec";
	static final String KEY_TOTAL_TIME_USEC = "pooled-total-time-usec";
	static final String KEY_TOTAL_SERVICE_TIME_USEC = "pooled-total-service-time-usec";

	String poolName;
	int poolSize, capacity;
	int retryInterval = REOPEN_FREQ; // time in milliseconds
	boolean dropOnError = false;
	ExecutorService threadPool;
	Limiter errorLimiter;
	BlockingQueue<SinkLogEvent> eventQ;
	DelayQueue<DelayedElement<SinkLogEvent>> delayQ;

	volatile boolean started = false, shutdown = false;

	AtomicLong dropCount = new AtomicLong(0);
	AtomicLong skipCount = new AtomicLong(0);
	AtomicLong reQCount = new AtomicLong(0);
	AtomicLong signalCount = new AtomicLong(0);
	AtomicLong loggedCount = new AtomicLong(0);
	AtomicLong totalCount = new AtomicLong(0);
	AtomicLong exceptionCount = new AtomicLong(0);
	AtomicLong recoveryCount = new AtomicLong(0);
	AtomicLong totalUsec = new AtomicLong(0);
	AtomicLong lastServiceUsec = new AtomicLong(0);
	AtomicLong totalServiceUsec = new AtomicLong(0);

	/**
	 * Create a pooled logger instance.
	 *
	 * @param name
	 *            pool name to set
	 * @param threadPoolSize
	 *            number of threads that will be used to log all enqueued events.
	 * @param maxCapacity
	 *            maximum queue capacity to hold incoming events, exceeding capacity will drop incoming events.
	 */
	public PooledLogger(String name, int threadPoolSize, int maxCapacity) {
		poolName = name;
		poolSize = threadPoolSize;
		capacity = maxCapacity;
		eventQ = new ArrayBlockingQueue<>(capacity);
		delayQ = new DelayQueue<>();
		errorLimiter = DefaultLimiterFactory.getInstance().newLimiter(PooledLogger.ERROR_RATE, Limiter.MAX_RATE);
	}

	@Override
	public void shutdown(Throwable ex) {
		if (shutdown) {
			return;
		}

		shutdown = true;

		// when ex is null it must be immediate shutdown request
		if (ex == null) {
			SinkLogEvent dieEvent = new SinkLogEvent(this, SinkLogEvent.SIGNAL_TERMINATE);
			for (int i = 0; i < poolSize; i++) {
				eventQ.offer(dieEvent);
			}
			delayQ.offer(new DelayedElement<>(dieEvent, 0));

			stop();

			eventQ.clear();
			delayQ.clear();
		}
	}

	/**
	 * Obtain pool name.
	 *
	 * @return pool name
	 */
	public String getName() {
		return poolName;
	}

	@Override
	public Map<String, Object> getStats() {
		Map<String, Object> stats = new LinkedHashMap<>();
		getStats(stats);
		return stats;
	}

	@Override
	public KeyValueStats getStats(Map<String, Object> stats) {
		stats.put(Utils.qualify(this, poolName, KEY_Q_SIZE), eventQ.size());
		stats.put(Utils.qualify(this, poolName, KEY_DQ_SIZE), delayQ.size());
		stats.put(Utils.qualify(this, poolName, KEY_Q_CAPACITY), capacity);
		stats.put(Utils.qualify(this, poolName, KEY_Q_TASKS), poolSize);
		stats.put(Utils.qualify(this, poolName, KEY_OBJECTS_DROPPED), dropCount.get());
		stats.put(Utils.qualify(this, poolName, KEY_OBJECTS_SKIPPED), skipCount.get());
		stats.put(Utils.qualify(this, poolName, KEY_OBJECTS_REQUEUED), reQCount.get());
		stats.put(Utils.qualify(this, poolName, KEY_OBJECTS_COUNT), totalCount.get());
		stats.put(Utils.qualify(this, poolName, KEY_OBJECTS_LOGGED), loggedCount.get());
		stats.put(Utils.qualify(this, poolName, KEY_EXCEPTION_COUNT), exceptionCount.get());
		stats.put(Utils.qualify(this, poolName, KEY_RECOVERY_COUNT), recoveryCount.get());
		stats.put(Utils.qualify(this, poolName, KEY_SIGNAL_COUNT), signalCount.get());
		stats.put(Utils.qualify(this, poolName, KEY_LAST_SERVICE_TIME_USEC), lastServiceUsec.get());
		stats.put(Utils.qualify(this, poolName, KEY_TOTAL_TIME_USEC), totalUsec.get());
		stats.put(Utils.qualify(this, poolName, KEY_TOTAL_SERVICE_TIME_USEC), totalServiceUsec.get());
		return this;
	}

	@Override
	public void resetStats() {
		dropCount.set(0);
		skipCount.set(0);
		reQCount.set(0);
		signalCount.set(0);
		totalCount.set(0);
		totalServiceUsec.set(0);
		lastServiceUsec.set(0);
		loggedCount.set(0);
		totalUsec.set(0);
		recoveryCount.set(0);
		exceptionCount.set(0);
	}

	/**
	 * Obtain total number of re-queued events.
	 *
	 * @return total number of re-queued events.
	 */
	public long getReQCount() {
		return reQCount.get();
	}

	/**
	 * Obtain total number of times sink recovered from exception(s).
	 *
	 * @return total number of times sink recovered from exceptions
	 */
	public long getRecoveryCount() {
		return recoveryCount.get();
	}

	/**
	 * Obtain total number of signal messages processed
	 *
	 * @return total number of signal messages processed
	 */
	public long getSignalCount() {
		return signalCount.get();
	}

	/**
	 * Obtain total number of events/log messages dropped since last reset. Dropped events occur when pooled queue is
	 * full and messages have nowhere to go.
	 *
	 * @return total number of dropped messages since last reset
	 */
	public long getDropCount() {
		return dropCount.get();
	}

	/**
	 * Obtain total number of events/log messages skipped since last reset due to underlying event sink either being in
	 * error state or unavailable for whatever reason.
	 *
	 * @return total number of skipped messages since last reset
	 */
	public long getSkipCount() {
		return skipCount.get();
	}

	/**
	 * Obtain total number of events/log messages logged since last reset.
	 *
	 * @return total number of logged messages since last reset
	 */
	public long getLoggedCount() {
		return loggedCount.get();
	}

	/**
	 * Obtain total number of processed messages since last reset.
	 *
	 * @return total number of processed messages since last reset
	 */
	public long getTotalCount() {
		return totalCount.get();
	}

	/**
	 * Obtain total number microseconds spent in logging activities to underlying sinks.
	 *
	 * @return total number of microseconds spent logging to the underlying sink
	 */
	public long getTotalUsec() {
		return totalUsec.get();
	}

	/**
	 * Obtain total number of events buffered in a queue waiting to be flushed
	 *
	 * @return total number of messages waiting to be flushed
	 */
	public int getQSize() {
		return eventQ.size();
	}

	/**
	 * Obtain total number of events buffered in a delay queue waiting to be re-delivered due to error during processing
	 *
	 * @return total number of messages waiting to be re-delivered
	 */
	public int getDQSize() {
		return delayQ.size();
	}

	/**
	 * Determine if the delay queue is full.
	 *
	 * @return {@code true} if delay queue is full, {@code false} otherwise
	 */
	public boolean isDQfull() {
		return delayQ.size() >= getCapacity();
	}

	/**
	 * Determine if event queue is full.
	 *
	 * @return {@code true} if event queue is full, {@code false} otherwise
	 */
	public boolean isQFull() {
		return eventQ.size() >= getCapacity();
	}

	/**
	 * Determine if event queue is empty
	 *
	 * @return {@code true} if event queue is empty, {@code false} otherwise
	 */
	public boolean isEmpty() {
		return eventQ.size() <= 0;
	}

	/**
	 * Obtain maximum capacity of this sink instance. Events are dropped if capacity is reached 100%.
	 *
	 * @return maximum capacity of the sink
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Inserts the specified log event at the tail of this pooled logger.
	 *
	 * @param event
	 *            logging event
	 * @return {@code true} if event is inserted/accepted {@code false} otherwise
	 */
	public boolean offer(SinkLogEvent event) {
		boolean flag = false;
		if (!shutdown || (event.getSignal() != null)) {
			flag = eventQ.offer(event);
		}
		if (!flag) {
			dropCount.incrementAndGet();
		}
		return flag;
	}

	/**
	 * Inserts the specified log event at the tail of this pooled logger and block until insert is completed.
	 *
	 * @param event
	 *            logging event
	 * @throws InterruptedException
	 *             if interrupted waiting for space in logger
	 */
	public void put(SinkLogEvent event) throws InterruptedException {
		if (!shutdown || (event.getSignal() != null)) {
			eventQ.put(event);
		} else {
			dropCount.incrementAndGet();
			throw new InterruptedException("Unable to accept events: " + getName() + " is shutdown");
		}
	}

	/**
	 * Checks if logger is started.
	 *
	 * @return {@code true} if logger is started, {@code false} otherwise
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Checks if logger is shot down.
	 *
	 * @return {@code true} if logger has been shutdown
	 */
	public boolean isShut() {
		return shutdown;
	}

	/**
	 * Allow the pool to drop queued events when exception occur. default behavior is to re-queue messages back on the
	 * queue for retry.
	 *
	 * @param dropOnError
	 *            {@code true} to allow drops, {@code false} otherwise
	 */
	public void dropOnError(boolean dropOnError) {
		this.dropOnError = dropOnError;
	}

	/**
	 * Obtain how failed messages are handled.
	 *
	 * @return {@code true} means messaged can be dropped, {@code false} re-queued
	 */
	public boolean isDropOnError() {
		return dropOnError;
	}

	/**
	 * Interval wait time before retrying send of failed messages.
	 *
	 * @param retryInterval
	 *            time interval in milliseconds
	 */
	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}

	/**
	 * Obtain event message from the queue
	 *
	 * @return sink event instance
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	protected SinkLogEvent takeEvent() throws InterruptedException {
		return eventQ.take();
	}

	/**
	 * Obtain a delayed event message from a delay queue
	 *
	 * @return sink event instance
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	protected SinkLogEvent takeDelayedEvent() throws InterruptedException {
		DelayedElement<SinkLogEvent> elm = delayQ.take();
		return elm.getElement();
	}

	/**
	 * Inserts the specified log event at the tail of this pooled logger and block until insert is completed.
	 *
	 * @param event
	 *            logging event
	 */
	public void putDelayed(SinkLogEvent event) {
		putDelayed(event, retryInterval, TimeUnit.MILLISECONDS);
	}

	/**
	 * Inserts the specified log event at the tail of this pooled logger and block until insert is completed.
	 *
	 * @param event
	 *            logging event
	 * @param delay
	 *            time duration
	 * @param unit
	 *            time unit for duration
	 */
	public void putDelayed(SinkLogEvent event, long delay, TimeUnit unit) {
		reQCount.incrementAndGet();
		delayQ.put(new DelayedElement<>(event, unit.toMillis(delay)));
	}

	/**
	 * Handle event that could not be processed.
	 *
	 * @param event
	 *            skipped sink log event
	 * @param ex
	 *            skip reason
	 */
	private void skipEvent(SinkLogEvent event, Throwable ex) {
		// add logic to handle skipped event
		event.setException(ex);
		if ((!dropOnError) && (delayQ.size() < capacity)) {
			putDelayed(event);
		} else {
			skipCount.incrementAndGet();
		}
	}

	/**
	 * Handle event error during event processing
	 *
	 * @param event
	 *            event instance
	 * @param err
	 *            exception
	 */
	private void eventError(SinkLogEvent event, Throwable err) {
		try {
			exceptionCount.incrementAndGet();
			skipEvent(event, err);
			boolean errorPermit = errorLimiter.tryObtain(1, 0);
			if (errorPermit) {
				PooledLogger.logger.log(OpLevel.ERROR,
						"Error during processing: total.error.count={0}, sink.error.count={1}, queue.size={2}, delay.size={3}, skip.count={4}, req.count={5}, event.source={6}, event.sink={7}",
						exceptionCount.get(), event.getEventSink().getErrorCount(), getQSize(), getDQSize(),
						skipCount.get(), reQCount.get(), event.getEventSource(), event.getEventSink(), err);
			}
		} catch (Throwable ex) {
			PooledLogger.logger.log(OpLevel.FAILURE,
					"Oops, error during error handling? total.error.count={0}, sink.error.count={1}, queue.size={2}, skip.count={3}, requeue.count={4}, event={5}",
					exceptionCount.get(), event.getEventSink().getErrorCount(), getQSize(), skipCount.get(),
					reQCount.get(), event, ex);
		}
	}

	/**
	 * Determine if event sink is ready to accept events
	 *
	 * @param sink
	 *            event sink
	 * @throws IOException
	 */
	private boolean isLoggable(EventSink sink) throws IOException {
		if (!sink.isOpen()) {
			synchronized (sink) {
				if (sink.errorState()) {
					long lastErrorTime = sink.getLastErrorTime();
					long errorElapsed = System.currentTimeMillis() - lastErrorTime;
					if (errorElapsed < retryInterval) {
						return false;
					}
				}
				openSink(sink);
			}
		}
		// check if the sink is in valid write state
		AbstractEventSink.checkState(sink);
		return true;
	}

	/**
	 * Handle event signal processing
	 *
	 * @param event
	 *            event instance
	 * @throws IOException
	 */
	private void handleSignal(SinkLogEvent event) throws IOException {
		Thread signal = event.getSignal();
		try {
			signalCount.incrementAndGet();
			if (event.getSignalType() == SinkLogEvent.SIGNAL_CLOSE) {
				close(event.getEventSink());
			} else if (event.getSignalType() == SinkLogEvent.SIGNAL_FLUSH) {
				event.getEventSink().flush();
			} else if (event.getSignalType() == SinkLogEvent.SIGNAL_SHUTDOWN) {
				shutdown(event.getException());
				close(event.getEventSink());
			}
		} finally {
			LockSupport.unpark(signal);
		}
	}

	private static void close(EventSink eSink) throws IOException {
		try {
			eSink.flush();
		} finally {
			eSink.close();
		}
	}

	/**
	 * Handle event processing
	 *
	 * @param event
	 *            event instance
	 * @throws IOException
	 */
	private void onEvent(SinkLogEvent event) throws IOException {
		totalCount.incrementAndGet();
		if (event.getSignal() != null) {
			handleSignal(event);
		} else if (isLoggable(event.getEventSink())) {
			sendEvent(event);
		} else {
			skipEvent(event, null);
		}
	}

	/**
	 * Write event to the underlying event sink
	 *
	 * @param event
	 *            event instance
	 * @throws IOException
	 */
	private void sendEvent(SinkLogEvent event) {
		Object sinkObject = event.getSinkObject();
		EventSink outSink = event.getEventSink();

		if (sinkObject instanceof TrackingEvent) {
			outSink.log((TrackingEvent) sinkObject);
		} else if (sinkObject instanceof TrackingActivity) {
			outSink.log((TrackingActivity) sinkObject);
		} else if (sinkObject instanceof Snapshot) {
			outSink.log(event.getSnapshot());
		} else if (event.getEventSource() != null) {
			outSink.log(event.getTTL(), event.getEventSource(), event.getSeverity(), event.getResourceBundle(),
					String.valueOf(sinkObject), event.getArguments());
		} else {
			outSink.log(event.getTTL(), outSink.getSource(), event.getSeverity(), event.getResourceBundle(),
					String.valueOf(sinkObject), event.getArguments());
		}
		loggedCount.incrementAndGet();
	}

	/**
	 * Open event sink
	 *
	 * @param sink
	 *            event sink
	 * @throws IOException
	 */
	private void openSink(EventSink sink) throws IOException {
		// check for open again
		if (!sink.isOpen()) {
			try {
				boolean lastErrorState = sink.errorState();
				sink.open();
				if (lastErrorState) {
					sink.setErrorState(null);
					recoveryCount.incrementAndGet();
				}
			} catch (IOException e) {
				sink.setErrorState(e);
				throw e;
			}
		}
	}

	/**
	 * Event processing completed
	 *
	 * @param event
	 *            event instance
	 * @param start
	 *            timer in nanoseconds
	 */
	private long eventComplete(long start, SinkLogEvent event) {
		long elaspedUsec = (System.nanoTime() - start) / 1000;
		totalServiceUsec.addAndGet(event.complete() / 1000);
		lastServiceUsec.set(elaspedUsec);
		totalUsec.addAndGet(elaspedUsec);
		return elaspedUsec;
	}

	/**
	 * Fully process a single event
	 *
	 * @param event
	 *            event instance
	 */
	protected void processEvent(SinkLogEvent event) {
		long start = System.nanoTime();
		try {
			onEvent(event);
		} catch (Throwable err) {
			eventError(event, err);
		} finally {
			eventComplete(start, event);
		}
	}

	/**
	 * Start the the thread pool and all threads in this pooled logger.
	 */
	protected synchronized void start() {
		if (started) {
			return;
		}
		NamedThreadFactory tFactory = new NamedThreadFactory(
				"PooledLoggingTask(" + poolName + "," + poolSize + "," + capacity + ")/task-");
		threadPool = Executors.newFixedThreadPool((poolSize + 1), tFactory);
		for (int i = 0; i < poolSize; i++) {
			threadPool.execute(new PooledLoggingTask(this));
		}
		threadPool.execute(new DelayedLoggingTask(this));
		started = true;
	}

	/**
	 * Stop the the thread pool and all threads in this pooled logger.
	 */
	protected synchronized void stop() {
		if (threadPool == null) {
			return;
		}
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			threadPool.shutdownNow();
			started = false;
		}
	}
}