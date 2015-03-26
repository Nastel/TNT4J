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

	private Source source;
	private EventSink outSink = null;
	private AtomicLong dropCount = new AtomicLong(0);	

	/**
	 * Create a buffered sink instance with a specified out sink 
	 * maximum capacity. Event will be dropped if capacity is exceeded.
	 * Obtain drop counts and queue sizes using <code>getDropCount()</code> method.
	 * 
	 * @param sink out sink where events/log message are written out
	 */
	public BufferedEventSink(EventSink sink) {
		outSink = sink;
	}

	/**
	 * Obtain total number of events/log messages dropped since last reset.
	 * 
	 * @return total number of dropped messages since last reset
	 */
	public long getDropCount() {
		return dropCount.get();
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
		boolean flag = BufferedEventSinkFactory.getPooledLogger().offer(new SinkLogEvent(outSink, null, OpLevel.NONE, String.valueOf(msg), args));
		if (!flag) dropCount.incrementAndGet();
	}

	@Override
    public void log(TrackingActivity activity) {
		_checkState();
		boolean flag = BufferedEventSinkFactory.getPooledLogger().offer(new SinkLogEvent(outSink, activity));
		if (!flag) dropCount.incrementAndGet();
	}

	@Override
    public void log(TrackingEvent event) {
		_checkState();
		boolean flag = BufferedEventSinkFactory.getPooledLogger().offer(new SinkLogEvent(outSink, event));
		if (!flag) dropCount.incrementAndGet();
    }

	@Override
    public void log(Snapshot props) {
		_checkState();
		boolean flag = BufferedEventSinkFactory.getPooledLogger().offer(new SinkLogEvent(outSink, props));
		if (!flag) dropCount.incrementAndGet();
    }
	
	@Override
    public void log(OpLevel sev, String msg, Object... args) {
		log(source, sev, msg, args);
    }

	@Override
    public void log(Source src, OpLevel sev, String msg, Object... args) {
		_checkState();
		boolean flag = BufferedEventSinkFactory.getPooledLogger().offer(new SinkLogEvent(outSink, src, sev, msg, args));
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
		outSink.open();
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
	    BufferedEventSinkFactory.getPooledLogger().getStats(stats);
	    return outSink.getStats(stats);
    }

	@Override
    public void resetStats() {
		dropCount.set(0);
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
	 * Override this method to check state of the sink before logging occurs. Throws <code>IllegalStateException</code>
	 * if sink is in wrong state.
	 *
	 * @throws IllegalStateException
	 */
    protected void _checkState() throws IllegalStateException {
    	AbstractEventSink.checkState(this);
    }	
}
