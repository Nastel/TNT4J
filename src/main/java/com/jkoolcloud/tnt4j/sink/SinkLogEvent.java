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
package com.jkoolcloud.tnt4j.sink;

import java.util.EventObject;

import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.core.TTL;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * An event class for reporting logging activities generated by an {@link EventSink} instance.
 * </p>
 * 
 * @see EventSink
 * @see TrackingEvent
 * @see TrackingActivity
 * @see OpLevel
 * 
 * @version $Revision: 3 $
 * 
 */
public class SinkLogEvent extends EventObject implements TTL {
	private static final long serialVersionUID = 1L;

	public static final int SIGNAL_PROCESS = 0;
	public static final int SIGNAL_FLUSH = 1;
	public static final int SIGNAL_CLOSE = 5;
	public static final int SIGNAL_SHUTDOWN = 10;
	
	private Object logObj = null;
	private Snapshot snapshot = null;
	private Throwable error = null;
	private Source evSrc = null;
	private OpLevel level = OpLevel.NONE;
	private Object[] argList = null;
	private int signalType = SIGNAL_PROCESS;
	private long ttl;
	private long startTimeNanos =  System.nanoTime();
	private long stopTimeNanos = 0;

	/**
	 * Create a new log event instance designed as a signal
	 * 
	 * @param sink
	 *            sink associated with the event
	 * @param th
	 *            thread associated with this event
	 * @param signalType
	 *            signal type
	 */
	public SinkLogEvent(EventSink sink, Thread th, int signalType) {
		super(sink);
		logObj = th;
	}

	/**
	 * Create a new log event instance
	 * 
	 * @param sink
	 *            sink associated with the event
	 * @param msg
	 *            tracking event instance
	 */
	public SinkLogEvent(EventSink sink, TrackingEvent msg) {
		super(sink);
		logObj = msg;
		error = msg.getOperation().getThrowable();
		level = msg.getSeverity();
		evSrc = msg.getSource();
		argList = msg.getMessageArgs();
		ttl = msg.getTTL();
	}

	/**
	 * Create a new log event instance
	 * 
	 * @param sink
	 *            sink associated with the event
	 * @param msg
	 *            tracking activity instance
	 */
	public SinkLogEvent(EventSink sink, TrackingActivity msg) {
		super(sink);
		logObj = msg;
		error = msg.getThrowable();
		evSrc = msg.getSource();
		ttl = msg.getTTL();	
	}

	/**
	 * Create a new log event instance.
	 * 
	 * @param sink
	 *            sink associated with the event
	 * @param snap
	 *            a set of properties
	 */
	public SinkLogEvent(EventSink sink, Snapshot snap) {
		super(sink);
		level = snap.getSeverity();
		logObj = snap;
		snapshot = snap;
		evSrc = snap.getSource();
		ttl = snap.getTTL();	
	}

	/**
	 * Create a new log event instance.
	 * 
	 * @param sink
	 *            sink associated with the event
	 * @param evSource
	 *            source associated with the event
	 * @param sev
	 *            log severity
	 * @param ttl
	 *            time to live in seconds
	 * @param msg
	 *            log message object
	 * @param args
	 *            argument list associated with the message
	 */
	public SinkLogEvent(EventSink sink, Source evSource, OpLevel sev, long ttl, Object msg, Object... args) {
		super(sink);
		logObj = msg;
		if (args != null && args.length > 0) {
			argList = args;
			error = Utils.getThrowable(args);
		}
		level = sev;
		evSrc = evSource;
		this.ttl = ttl;
	}

	/**
	 * Return Thread associated with event producer, null if not available
	 * 
	 * @return Thread associated with event producer, null if not available
	 */
	public Thread getSignal() {
		return logObj instanceof Thread? (Thread)logObj: null;
	}


	/**
	 * Return signal type associated with this event
	 * 
	 * @return signal type associated with this event
	 */
	public int getSignalType() {
		return signalType;
	}


	/**
	 * Return associated event sink with this event
	 * 
	 * @return event sink handle associated with this event
	 */
	public EventSink getEventSink() {
		return (EventSink) getSource();
	}

	/**
	 * Return list of arguments supplied with the logging message
	 * 
	 * @return array of objects
	 */
	public Object[] getArguments() {
		return argList;
	}

	/**
	 * Return current event source associated with this event
	 * 
	 * @return source associated with the event
	 */
	public Source getEventSource() {
		return evSrc;
	}

	/**
	 * Return current severity level associated with this event
	 * 
	 * @return severity level
	 */
	public OpLevel getSeverity() {
		return level;
	}

	/**
	 * Return log exception
	 * 
	 * @return severity level
	 */
	public Throwable getException() {
		return error;
	}

	/**
	 * Set log exception
	 * 
	 * @param ex exception instance
	 */
	public void setException(Throwable ex) {
		error = ex;
	}

	/**
	 * Return log object
	 * 
	 * @return log object
	 */
	public Object getSinkObject() {
		return logObj;
	}

	/**
	 * Return log object
	 * 
	 * @return log object
	 */
	public Snapshot getSnapshot() {
		return snapshot;
	}

	/**
	 * This method is called when event processing is complete and
	 * service time is calculated.
	 * 
	 * @return service time in nanoseconds
	 */
	public long complete() {
		stopTimeNanos = System.nanoTime();
		return stopTimeNanos - startTimeNanos;
	}
	
	/**
	 * Compute event service time
	 * 
	 * @return service time in nanoseconds
	 */
	public long getServiceTimeNanos() {
		return stopTimeNanos > 0? stopTimeNanos - startTimeNanos: System.nanoTime() - startTimeNanos;
	}
	
	@Override
	public String toString() {
		return super.toString() 
			+ "{source: " + getSource()
			+ ", sev: " + level
			+ ", ttl: " + ttl
			+ ", log.obj: " + Utils.quote(logObj)
			+ ", ev.source: " + Utils.quote(evSrc)
		    + ", exception: " + Utils.quote(error)
		    + "}";
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
