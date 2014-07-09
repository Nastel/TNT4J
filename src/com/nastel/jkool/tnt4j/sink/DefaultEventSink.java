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

import java.util.ArrayList;

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * This class implements a default abstract class for <code>EventSink</code>. Developers should subclass from this class
 * for all event sinks.
 * </p>
 * 
 * 
 * @version $Revision: 9 $
 * 
 * @see EventSink
 * @see SinkError
 * @see SinkErrorListener
 * @see SinkLogEvent
 * @see SinkLogEventListener
 */
public abstract class DefaultEventSink implements EventSink {

	protected ArrayList<SinkErrorListener> errorListeners = new ArrayList<SinkErrorListener>(10);
	protected ArrayList<SinkLogEventListener> logListeners = new ArrayList<SinkLogEventListener>(10);
	protected ArrayList<SinkEventFilter> filters = new ArrayList<SinkEventFilter>(10);

	/**
	 * Register an event sink listener for notifications when logging events occur when writing to event sink.
	 * 
	 * @see SinkLogEventListener
	 */
	public void addSinkLogEventListener(SinkLogEventListener listener) {
		synchronized (logListeners) {
			logListeners.add(listener);
		}
	}

	/**
	 * Remove an event sink listener for notifications when logging events occur when writing to event sink.
	 * 
	 * @see SinkLogEventListener
	 */
	public void removeSinkLogEventListener(SinkLogEventListener listener) {
		synchronized (logListeners) {
			logListeners.remove(listener);
		}
	}

	/**
	 * Register an event sink listener for notifications when errors occur when writing to event sink.
	 * 
	 * @see SinkErrorListener
	 */
	public void addSinkErrorListener(SinkErrorListener listener) {
		synchronized (errorListeners) {
			errorListeners.add(listener);
		}
	}

	/**
	 * Remove an event sink listener for notifications when errors occur when writing to event sink.
	 * 
	 * @see SinkErrorListener
	 */
	public void removeSinkErrorListener(SinkErrorListener listener) {
		synchronized (errorListeners) {
			errorListeners.remove(listener);
		}
	}

	/**
	 * Subclasses should use this helper class to trigger log event notifications during logging process.
	 * 
	 * @param event
	 *            sink logging event to be sent to all listeners
	 * @see SinkLogEvent
	 */
	protected void notifyListeners(SinkLogEvent event) {
		synchronized (logListeners) {
			for (SinkLogEventListener listener : logListeners) {
				listener.sinkLogEvent(event);
			}
		}
	}

	/**
	 * Subclasses should use this helper class to trigger error notifications during logging process.
	 * 
	 * @param event
	 *            sink error event to be sent to all listeners
	 * @see SinkError
	 */
	protected void notifyListeners(SinkError event) {
		synchronized (errorListeners) {
			for (SinkErrorListener listener : errorListeners) {
				listener.sinkError(event);
			}
		}
	}

	/**
	 * Subclasses should use this helper class to trigger error notifications during logging process.
	 * 
	 * @param msg
	 *            sink message associated with the sink operation
	 * @param ex
	 *            exception to be reported to all registered event listeners
	 */
	protected void notifyListeners(Object msg, Throwable ex) {
		if (errorListeners.size() > 0) {
			SinkError event = new SinkError(this, msg, ex);
			notifyListeners(event);
		}
	}

	/**
	 * Subclasses should use this helper class to filter out
	 * unwanted log events before writing to the underlying sink
	 * 
	 * @param level
	 *            severity level of the event message
	 * @param msg
	 *            event message
	 * @param args
	 *            argument list passed along with the message
	 * @return true if event passed all filters, false otherwise           
	 * @see OpLevel
	 */
	protected boolean filterEvent(OpLevel level, String msg, Object...args) {
		boolean pass = true;
		if (filters.size() == 0) return pass;
		
		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, level, msg, args));
			if (!pass) break;
		}
		return pass;
	}

	/**
	 * Subclasses should use this helper class to filter out
	 * unwanted log events before writing to the underlying sink
	 * 
	 * @param activity
	 *            to be checked with registered filters
	 * @return true if tracking activity passed all filters, false otherwise           
	 * @see TrackingActivity
	 */
	protected boolean filterEvent(TrackingActivity activity) {
		boolean pass = true;
		if (filters.size() == 0) return pass;
		
		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, activity));
			if (!pass) break;
		}
		return pass;
	}
	
	
	/**
	 * Subclasses should use this helper class to filter out
	 * unwanted log events before writing to the underlying sink
	 * 
	 * @param event
	 *            to be checked with registered filters
	 * @return true if tracking event passed all filters, false otherwise           
	 * @see TrackingEvent
	 */
	protected boolean filterEvent(TrackingEvent event) {
		boolean pass = true;
		if (filters.size() == 0) return pass;
		
		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, event));
			if (!pass) break;
		}
		return pass;
	}
	
	
	
	@Override
	public void addSinkEventFilter(SinkEventFilter filter){
		synchronized (filters) {
			filters.add(filter);
		}	
	}
	
	@Override
	public void removeSinkEventFilter(SinkEventFilter filter){
		synchronized (filters) {
			filters.remove(filter);
		}		
	}

	@Override
	public void log(TrackingActivity activity) {
		if (logListeners.size() > 0) {
			notifyListeners(new SinkLogEvent(this, activity));
		}
	}

	@Override
	public void log(TrackingEvent event) {
		if (logListeners.size() > 0) {
			notifyListeners(new SinkLogEvent(this, event));
		}
	}

	@Override
	public void log(OpLevel sev, String msg, Object...args) {
		if (logListeners.size() > 0) {
			notifyListeners(new SinkLogEvent(this, sev, msg, args));
		}
	}
}
