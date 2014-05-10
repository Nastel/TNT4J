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

import java.util.EventObject;

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * An event class for reporting logging activities generated by an <code>EventSink</code> instance.
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
public class SinkLogEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private Object logObj = null;
	private Throwable error = null;
	private OpLevel level = OpLevel.UNKNOWN;

	/**
	 * Create a new log event instance
	 * 
	 * @param source
	 *            sink associated with the event
	 * @param msg
	 *            tracking event instance
	 */
	public SinkLogEvent(EventSink source, TrackingEvent msg) {
	    super(source);
	    logObj = msg;
	    error = msg.getOperation().getThrowable();
	    level = msg.getSeverity();
   }
	
	/**
	 * Create a new log event instance
	 * 
	 * @param source
	 *            sink associated with the event
	 * @param msg
	 *            tracking activity instance
	 */
	public SinkLogEvent(EventSink source, TrackingActivity msg) {
	    super(source);
	    logObj = msg;
	    error = msg.getThrowable();
    }
	
	/**
	 * Create a new log event instance
	 * 
	 * @param source
	 *            sink associated with the event
	 * @param sev
	 *            log severity
	 * @param msg
	 *            log message
	 */
	public SinkLogEvent(EventSink source, OpLevel sev, String msg) {
		this(source, sev, msg, null);
	}
	
	/**
	 * Create a new log event instance
	 * 
	 * @param source
	 *            sink associated with the event
	 * @param sev
	 *            log severity
	 * @param msg
	 *            log message
	 * @param ex
	 *            exception associated with the log message
	 */
	public SinkLogEvent(EventSink source, OpLevel sev, String msg, Throwable ex) {
	    super(source);
	    logObj = msg;
	    error = ex;
	    level = sev;
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
	 * Return log object
	 *
	 * @return log object
	 */
	public Object getSinkObject() {
		return logObj;
	}
	
	@Override
	public String toString() {
		return super.toString() 
			+ "{source: " + getSource() 
			+ ", sev=" + level + ", log.obj=" + Utils.quote(logObj) + ", exception=" +  Utils.quote(error) + "}";
	}
}
