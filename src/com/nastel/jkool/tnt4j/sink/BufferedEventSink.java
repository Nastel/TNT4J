/*
 * Copyright 2014 Nastel Technologies, Inc.
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.nastel.jkool.tnt4j.core.KeyValueStats;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

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
public class BufferedEventSink implements EventSink, Runnable {
	static final String KEY_Q_SIZE = "buffered-queue-size";
	static final String KEY_Q_CAPACITY = "buffered-queue-capacity";
	static final String KEY_OBJECTS_DROPPED = "buffered-objects-dropped";
	static final String KEY_OBJECTS_LOGGED = "buffered-objects-logged";
	static final String KEY_TOTAL_TIME_NANOS = "buffered-total-time-nanos";

	private volatile Thread runningThread = null;
	private volatile boolean stopRequest = false;
	private int capacity = 1000;
	private EventSink outSink = null;
	private AtomicLong dropCount = new AtomicLong(0),
			loggedCount = new AtomicLong(0),
			totalNanos = new AtomicLong(0);
	private ArrayBlockingQueue<SinkLogEvent> eventQ;
	
	/**
	 * Create a buffered sink instance with a specified out sink 
	 * and a default capacity of 1000.
	 * 
	 * @param sink out sink where events/log message are written out
	 */
	public BufferedEventSink(EventSink sink) {
		this(sink, 1000);
	}

	/**
	 * Create a buffered sink instance with a specified out sink 
	 * maximum capacity. Event will be dropped if capacity is exceeded.
	 * Obtain drop counts and queue sizes using <code>getDropCount()</code> method.
	 * 
	 * @param sink out sink where events/log message are written out
	 */
	public BufferedEventSink(EventSink sink, int size) {
		outSink = sink;
		capacity = size;
		eventQ = new ArrayBlockingQueue<SinkLogEvent>(capacity);
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
	public long getCount() {
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
		boolean flag = eventQ.offer(new SinkLogEvent(outSink, OpLevel.NONE, String.valueOf(msg), args));
		if (!flag) dropCount.incrementAndGet();
	}

	@Override
    public void log(TrackingActivity activity) {
		boolean flag = eventQ.offer(new SinkLogEvent(outSink, activity));
		if (!flag) dropCount.incrementAndGet();
   }

	@Override
    public void log(TrackingEvent event) {
		boolean flag = eventQ.offer(new SinkLogEvent(outSink, event));
		if (!flag) dropCount.incrementAndGet();
    }

	@Override
    public void log(OpLevel sev, String msg, Object... args) {
		boolean flag = eventQ.offer(new SinkLogEvent(outSink, sev, msg, args));
		if (!flag) dropCount.incrementAndGet();
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
	    return outSink.isOpen();
    }

	@Override
    public void open() throws IOException {
		stopRequest = false;
		outSink.open();
		runningThread = new Thread(this, "EventSink/" + this);
		runningThread.start();
	}

	@Override
    public void close() throws IOException {
		stopRequest = true;		
		if (runningThread != null) {
			runningThread.interrupt();
			try {
	            runningThread.join();
            } catch (InterruptedException e) {
            }
		}
		outSink.close();
    }

	@Override
    public Map<String, Object> getStats() {
	    Map<String, Object> stats = outSink.getStats();
		_getOwnStats(stats);
	    return stats;
    }

	@Override
    public KeyValueStats getStats(Map<String, Object> stats) {
		_getOwnStats(stats);
	    return outSink.getStats(stats);
    }

	@Override
    public void resetStats() {
		dropCount.set(0);
		loggedCount.set(0);		
		totalNanos.set(0);
		outSink.resetStats();
	}

	@Override
    public void run() {
		try {
			while (!stopRequest) {
				SinkLogEvent event = eventQ.take();
				long start = System.nanoTime();
				Object sinkO = event.getSinkObject();
				if (sinkO instanceof TrackingEvent) {
					outSink.log((TrackingEvent)sinkO);
				} else if (sinkO instanceof TrackingActivity) {
					outSink.log((TrackingActivity)sinkO);
				} else {
					outSink.log(event.getSeverity(), String.valueOf(sinkO), event.getArguments());
				}
				loggedCount.incrementAndGet();
				long elaspedNanos = System.nanoTime() - start;
				totalNanos.addAndGet(elaspedNanos);
			}
		} catch (InterruptedException e) {
		}
	}
	
	private void _getOwnStats(Map<String, Object> stats) {
	    stats.put(KEY_Q_SIZE, eventQ.size());
	    stats.put(KEY_Q_CAPACITY, capacity);
	    stats.put(KEY_OBJECTS_DROPPED, dropCount.get());
	    stats.put(KEY_OBJECTS_LOGGED, loggedCount.get());		
	    stats.put(KEY_TOTAL_TIME_NANOS, totalNanos.get());
	}

	@Override
    public String getName() {
	    return outSink.getName();
    }
}
