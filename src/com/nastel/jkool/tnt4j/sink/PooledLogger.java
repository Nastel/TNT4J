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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.nastel.jkool.tnt4j.core.KeyValueStats;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.Snapshot;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;
import com.nastel.jkool.tnt4j.utils.Utils;

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
	
	static final String KEY_Q_SIZE = "pooled-queue-size";
	static final String KEY_Q_CAPACITY = "pooled-queue-capacity";
	static final String KEY_OBJECTS_DROPPED = "pooled-objects-dropped";
	static final String KEY_OBJECTS_LOGGED = "pooled-objects-logged";
	static final String KEY_EXCEPTION_COUNT = "pooled-exceptions";
	static final String KEY_RECOVERY_COUNT = "pooled-recovery-count";
	static final String KEY_TOTAL_TIME_USEC = "pooled-total-time-usec";
	
	int poolSize, capacity;
	ArrayBlockingQueue<SinkLogEvent> eventQ;
	ExecutorService threadPool;
	
	AtomicLong dropCount = new AtomicLong(0);
	AtomicLong loggedCount = new AtomicLong(0);
	AtomicLong exceptionCount = new AtomicLong(0);
	AtomicLong recoveryCount = new AtomicLong(0);
	AtomicLong totalNanos = new AtomicLong(0);
	
    /**
     * Create a pooled logger instance.
     * 
     * @param threadPoolSize number of threads that will be used to log all enqueued events.
     * @param maxCapacity maximum queue capacity to hold incoming events, exceeding capacity will drop incoming events.
     */
	public PooledLogger(int threadPoolSize, int maxCapacity) {
		poolSize = threadPoolSize;
		capacity = maxCapacity;
		threadPool = Executors.newFixedThreadPool(poolSize, new LoggingThreadFactory("PooledLogger(" + threadPoolSize + "," + capacity + ")/task-"));
		eventQ = new ArrayBlockingQueue<SinkLogEvent>(capacity);
		start();
	}

	@Override
    public Map<String, Object> getStats() {
	    Map<String, Object> stats = new LinkedHashMap<String, Object>();
	    getStats(stats);
	    return stats;
    }

	@Override
    public KeyValueStats getStats(Map<String, Object> stats) {
	    stats.put(Utils.qualify(this, KEY_Q_SIZE), eventQ.size());
	    stats.put(Utils.qualify(this, KEY_Q_CAPACITY), capacity);
	    stats.put(Utils.qualify(this, KEY_OBJECTS_DROPPED), dropCount.get());
	    stats.put(Utils.qualify(this, KEY_OBJECTS_LOGGED), loggedCount.get());		
	    stats.put(Utils.qualify(this, KEY_EXCEPTION_COUNT), exceptionCount.get());		
	    stats.put(Utils.qualify(this, KEY_RECOVERY_COUNT), recoveryCount.get());		
	    stats.put(Utils.qualify(this, KEY_TOTAL_TIME_USEC), (long)(totalNanos.get()/1000));
	    return this;
    }

	@Override
    public void resetStats() {
		dropCount.set(0);
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
	 * Obtain total number of events/log messages dropped since last reset.
	 * 
	 * @return total number of dropped messages since last reset
	 */
	public long getDropCount() {
		return dropCount.get();
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
     * @throws InterruptedException 
     */
	public void put(SinkLogEvent event) throws InterruptedException {
		eventQ.put(event);
	}
	
    /**
     * Start the the thread pool and all threads in this pooled logger.
     */
	private void start() {
		for (int i = 0; i < poolSize; i++) {
			threadPool.execute(new LoggingTask(this, eventQ));
		}
	}
	
    /**
     * Stop the the thread pool and all threads in this pooled logger.
     */
	public void stop() {
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
	private static final int ERROR_REPORT_WINDOW = Integer.getInteger("tnt4j.logger.error.frequency", 30000);
	
	PooledLogger pooledLogger;
	BlockingQueue<SinkLogEvent> eventQ;
	
	public LoggingTask(PooledLogger logger, ArrayBlockingQueue<SinkLogEvent> eq) {
		pooledLogger = logger;
		eventQ = eq;
    }

	protected void logEvent(SinkLogEvent event, long start) {
		Object sinkO = event.getSinkObject();
		EventSink outSink = event.getEventSink();
		
		//check if the sink is in valid write state
		AbstractEventSink.checkState(outSink);
		
		if (sinkO instanceof TrackingEvent) {
			outSink.log((TrackingEvent)sinkO);
		} else if (sinkO instanceof TrackingActivity) {
			outSink.log((TrackingActivity)sinkO);
		}  else if (sinkO instanceof Snapshot) {
			outSink.log(event.getSnapshot());
		} else if (event.getEventSource() != null){
			outSink.log(event.getEventSource(), event.getSeverity(), 
					String.valueOf(sinkO), event.getArguments());
		} else {
			outSink.log(event.getSeverity(), String.valueOf(sinkO),
					event.getArguments());
		}
		pooledLogger.loggedCount.incrementAndGet();
		long elaspedNanos = System.nanoTime() - start;
		pooledLogger.totalNanos.addAndGet(elaspedNanos);		
	}
	
    @Override
    public void run() {
    	try {
    		long lastError = 0;
    		boolean errorState = false;
			while (true) {
				SinkLogEvent event = eventQ.take();
				try {
					long start = System.nanoTime();
					logEvent(event, start);
					if (errorState) {
						pooledLogger.recoveryCount.incrementAndGet();
						errorState = false;
					}
				} catch (Throwable err) {
					errorState = true;
					long thisError = System.currentTimeMillis();
					pooledLogger.exceptionCount.incrementAndGet();
					if ((thisError - lastError) >= ERROR_REPORT_WINDOW) {
						lastError = thisError;
						PooledLogger.logger.log(OpLevel.ERROR, 
							"Error during processing: error.count={0}, event.source={1}, event.sink={2}", 
							pooledLogger.exceptionCount.get(), event.getEventSource(), event.getEventSink(), err);
					}
				}
			}
		} catch (Throwable e) {
			PooledLogger.logger.log(OpLevel.WARNING, 
					"Interrupted during processing: shutting down: error.count={0}",
					pooledLogger.exceptionCount.get(), e);
		}
    }	
}