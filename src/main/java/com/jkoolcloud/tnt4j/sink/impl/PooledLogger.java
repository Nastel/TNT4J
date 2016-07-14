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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.core.KeyValueStats;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.limiter.DefaultLimiterFactory;
import com.jkoolcloud.tnt4j.limiter.Limiter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSink;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.SinkLogEvent;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements an asynchronous pooled logger which allows logging
 * events asynchronously by a set of threads defined by a pool size. Because
 * events are logged by a set of threads the relative sequence of events logged
 * to the underlying sinks are not guaranteed. The sinks must handle events coming
 * out of sequence. Event time stamps are preserved and should be used to sequence
 * events properly.
 * </p>
 *
 *
 * @version $Revision: 1 $
 *
 * @see EventSink
 * @see KeyValueStats
 * @see SinkLogEvent
 */
public class PooledLogger implements KeyValueStats {
	protected static final EventSink logger = DefaultEventSinkFactory.defaultEventSink(PooledLogger.class);
	protected static final double ERROR_RATE = Double.valueOf(System.getProperty("tnt4j.pooled.logger.error.rate", "0.1"));
	protected static final int REOPEN_FREQ = Integer.getInteger("tnt4j.pooled.logger.reopen.freq.ms", 30000);
			
	static final String KEY_Q_SIZE = "pooled-queue-size";
	static final String KEY_Q_TASKS = "pooled-queue-tasks";
	static final String KEY_Q_CAPACITY = "pooled-queue-capacity";
	static final String KEY_OBJECTS_DROPPED = "pooled-objects-dropped";
	static final String KEY_OBJECTS_SKIPPED = "pooled-objects-skipped";
	static final String KEY_OBJECTS_LOGGED = "pooled-objects-logged";
	static final String KEY_EXCEPTION_COUNT = "pooled-exceptions";
	static final String KEY_SIGNAL_COUNT = "pooled-signals";
	static final String KEY_RECOVERY_COUNT = "pooled-recovery-count";
	static final String KEY_TOTAL_TIME_USEC = "pooled-total-time-usec";
	static final String KEY_TOTAL_SERVICE_TIME_USEC = "pooled-total-service-time-usec";

	String poolName;
	int poolSize, capacity;
	ExecutorService threadPool;
	ArrayBlockingQueue<SinkLogEvent> eventQ;

	volatile boolean started = false;

	AtomicLong dropCount = new AtomicLong(0);
	AtomicLong skipCount = new AtomicLong(0);
	AtomicLong signalCount = new AtomicLong(0);
	AtomicLong loggedCount = new AtomicLong(0);
	AtomicLong exceptionCount = new AtomicLong(0);
	AtomicLong recoveryCount = new AtomicLong(0);
	AtomicLong totalNanos = new AtomicLong(0);
	AtomicLong totalServiceNanos = new AtomicLong(0);

    /**
     * Create a pooled logger instance.
     *
     * @param threadPoolSize number of threads that will be used to log all enqueued events.
     * @param maxCapacity maximum queue capacity to hold incoming events, exceeding capacity will drop incoming events.
     */
	public PooledLogger(String name, int threadPoolSize, int maxCapacity) {
		poolName = name;
		poolSize = threadPoolSize;
		capacity = maxCapacity;
		eventQ = new ArrayBlockingQueue<SinkLogEvent>(capacity);
	}
	
    /**
     * Obtain pool name
     * 
     * @return pool name
     */
	public String getName() {
		return poolName;
	}
	
	@Override
    public Map<String, Object> getStats() {
	    Map<String, Object> stats = new LinkedHashMap<String, Object>();
	    getStats(stats);
	    return stats;
    }

	@Override
    public KeyValueStats getStats(Map<String, Object> stats) {
	    stats.put(Utils.qualify(this, poolName, KEY_Q_SIZE), eventQ.size());
	    stats.put(Utils.qualify(this, poolName, KEY_Q_CAPACITY), capacity);
	    stats.put(Utils.qualify(this, poolName, KEY_Q_TASKS), poolSize);
	    stats.put(Utils.qualify(this, poolName, KEY_OBJECTS_DROPPED), dropCount.get());
	    stats.put(Utils.qualify(this, poolName, KEY_OBJECTS_SKIPPED), skipCount.get());
	    stats.put(Utils.qualify(this, poolName, KEY_OBJECTS_LOGGED), loggedCount.get());
	    stats.put(Utils.qualify(this, poolName, KEY_EXCEPTION_COUNT), exceptionCount.get());
	    stats.put(Utils.qualify(this, poolName, KEY_RECOVERY_COUNT), recoveryCount.get());
	    stats.put(Utils.qualify(this, poolName, KEY_SIGNAL_COUNT), signalCount.get());
	    stats.put(Utils.qualify(this, poolName, KEY_TOTAL_TIME_USEC), totalNanos.get()/1000);
	    stats.put(Utils.qualify(this, poolName, KEY_TOTAL_SERVICE_TIME_USEC), totalServiceNanos.get()/1000);
	    return this;
    }

	@Override
    public void resetStats() {
		dropCount.set(0);
		skipCount.set(0);
		signalCount.set(0);
		loggedCount.set(0);
		totalNanos.set(0);
		recoveryCount.set(0);
		exceptionCount.set(0);
	}

	/**
	 * Obtain total number of times sink recovered from
	 * exception(s).
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
	 * Obtain total number of events/log messages dropped since last reset.
	 * Dropped events occur when pooled queue is full and messages have nowhere
	 * to go.
	 *
	 * @return total number of dropped messages since last reset
	 */
	public long getDropCount() {
		return dropCount.get();
	}

	/**
	 * Obtain total number of events/log messages skipped since last reset
	 * due to underlying event sink either being in error state or 
	 * unavailable for whatever reason.
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
	 * Obtain total number nanoseconds spent in logging activities to the actual sink.
	 *
	 * @return total number of nanoseconds spent logging to the underlying sink
	 */
	public long getTimeNanos() {
		return totalNanos.get();
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
	 * Obtain maximum capacity of this sink instance. Events are dropped if
	 * capacity is reached 100%.
	 *
	 * @return maximum capacity of the sink
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
     * Inserts the specified log event at the tail of this pooled logger.
     *
     * @param event logging event
     * @return true if event is inserted/accepted false otherwise
     */
	public boolean offer(SinkLogEvent event) {
		boolean flag = eventQ.offer(event);
		if (!flag) dropCount.incrementAndGet();
		return flag;
	}

    /**
     * Inserts the specified log event at the tail of this pooled logger and block
     * until insert is completed.
     *
     * @param event logging event
     * @throws InterruptedException if interrupted waiting for space in logger
     */
	public void put(SinkLogEvent event) throws InterruptedException {
		eventQ.put(event);
	}

	
    /**
     * @return true if logger is started, false otherwise
     */
	public boolean isStarted() {
		return started;
	}
	
    /**
     * Start the the thread pool and all threads in this pooled logger.
     */
	protected synchronized void start() {
		if (started) return;
		threadPool = Executors.newFixedThreadPool(poolSize, new LoggingThreadFactory("PooledLogger(" + poolName + "," + poolSize + "," + capacity + ")/task-"));
		for (int i = 0; i < poolSize; i++) {
			threadPool.execute(new LoggingTask(this, eventQ));
		}
		started = true;
	}

    /**
     * Stop the the thread pool and all threads in this pooled logger.
     */
	protected synchronized void stop() {
		if (threadPool == null) return;
		threadPool.shutdown();
		try {
	        threadPool.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        } finally {
        	threadPool.shutdownNow();
        }
	}
}

class LoggingThreadFactory implements ThreadFactory {
	int count = 0;
	String prefix;

	LoggingThreadFactory(String pfix) {
		prefix = pfix;
	}

	@Override
    public Thread newThread(Runnable r) {
		Thread task = new Thread(r, prefix + count++);
		task.setDaemon(true);
		return task;
    }
}

class LoggingTask implements Runnable {
	PooledLogger pooledLogger;
	BlockingQueue<SinkLogEvent> eventQ;
	Limiter errorLimiter;

	public LoggingTask(PooledLogger logger, ArrayBlockingQueue<SinkLogEvent> eq) {
		eventQ = eq;
		pooledLogger = logger;
		errorLimiter = DefaultLimiterFactory.getInstance().newLimiter(PooledLogger.ERROR_RATE, Limiter.MAX_RATE);
	}

	protected void openSink(EventSink sink) throws IOException {
		// check for open again
		if (!sink.isOpen()) {
			try {
				boolean lastErrorState = sink.errorState();
				sink.open();
				if (lastErrorState) {
					sink.setErrorState(null);
					pooledLogger.recoveryCount.incrementAndGet();
				}
			} catch (IOException e) {
				sink.setErrorState(e);
				throw e;
			}
		}
	}

	protected boolean isLoggable(EventSink sink) throws IOException {
		if (!sink.isOpen()) {
			synchronized (sink) {
				if (sink.errorState()) {
					long lastErrorTime = sink.getLastErrorTime();
					long errorElapsed = System.currentTimeMillis() - lastErrorTime;
					if (errorElapsed < PooledLogger.REOPEN_FREQ) {
						return false;
					}
				}
				openSink(sink);
			}
		}
		//check if the sink is in valid write state
		AbstractEventSink.checkState(sink);
		return true;
	}
	
	protected void sendEvent(SinkLogEvent event) {
		Object sinkObject = event.getSinkObject();
		EventSink outSink = event.getEventSink();

		if (sinkObject instanceof TrackingEvent) {
			outSink.log((TrackingEvent) sinkObject);
		} else if (sinkObject instanceof TrackingActivity) {
			outSink.log((TrackingActivity) sinkObject);
		} else if (sinkObject instanceof Snapshot) {
			outSink.log(event.getSnapshot());
		} else if (event.getEventSource() != null) {
			outSink.log(event.getTTL(), 
					event.getEventSource(),
					event.getSeverity(), 
					String.valueOf(sinkObject),
			        event.getArguments());
		} else {
			outSink.log(event.getTTL(),
					outSink.getSource(),
					event.getSeverity(),
					String.valueOf(sinkObject),
			        event.getArguments());
		}
		pooledLogger.loggedCount.incrementAndGet();		
	}
	
	protected void processEvent(SinkLogEvent event) throws IOException {
		if (event.getSignal() != null) {
			pooledLogger.signalCount.incrementAndGet();
			Thread signal = event.getSignal();
			if (event.getSignalType() == SinkLogEvent.SIGNAL_CLOSE) {
				event.getEventSink().close();
			}
			LockSupport.unpark(signal);
		} else if (isLoggable(event.getEventSink())) {
			sendEvent(event);
		} else {
			pooledLogger.skipCount.incrementAndGet();
		}
	}

	protected void handleError(SinkLogEvent event, Throwable err) {
		try {
			pooledLogger.skipCount.incrementAndGet();
			pooledLogger.exceptionCount.incrementAndGet();
			if (errorLimiter.tryObtain(1, 0)) {
				PooledLogger.logger.log(OpLevel.ERROR,
						"Error during processing: total.error.count={0}, sink.error.count={1}, event.source={2}, event.sink={3}",
				        pooledLogger.exceptionCount.get(), event.getEventSink().getErrorCount(),
				        event.getEventSource(), event.getEventSink(), err);
			}
		} catch (Throwable ex) {
			PooledLogger.logger.log(OpLevel.FAILURE, 
					"Oops, error during error handling? error.count={0}, event={1}",
			        pooledLogger.exceptionCount.get(), event, ex);
		}
	}
	
    @Override
    public void run() {
    	try {
			while (true) {
				SinkLogEvent event = eventQ.take();
				long start = System.nanoTime();
				try {
					processEvent(event);
				} catch (Throwable err) {
					handleError(event, err);
				} finally {
					pooledLogger.totalServiceNanos.addAndGet(event.complete());
					long elaspedNanos = System.nanoTime() - start;
					pooledLogger.totalNanos.addAndGet(elaspedNanos);					
				}
			}
		} catch (Throwable e) {
			PooledLogger.logger.log(OpLevel.WARNING,
					"Interrupted during processing: shutting down: error.count={0}",
					pooledLogger.exceptionCount.get(), e);
		}
    }
}
