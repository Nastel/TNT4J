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
public abstract class DefaultEventSink implements EventSink, SinkEventFilter {

	private ArrayList<SinkErrorListener> errorListeners = new ArrayList<SinkErrorListener>(10);
	private ArrayList<SinkLogEventListener> logListeners = new ArrayList<SinkLogEventListener>(10);
	private ArrayList<SinkEventFilter> filters = new ArrayList<SinkEventFilter>(10);

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
	 * unwanted log events.
	 * 
	 * @param event
	 *            to be checked with registered filters
	 * @return true if event passed all filters, false otherwise           
	 * @see SinkLogEvent
	 */
	public boolean acceptEvent(SinkLogEvent event) {
		boolean pass = true;
		synchronized (filters) {
			for (SinkEventFilter filter : filters) {
				pass = (pass && filter.acceptEvent(event));
				if (!pass) break;
			}
		}
		return pass;
	}

	/**
	 * Subclasses should use this helper class to filter out
	 * unwanted log activities.
	 * 
	 * @param activity
	 *            to be checked with registered filters
	 * @return true if tracking activity passed all filters, false otherwise           
	 * @see TrackingActivity
	 */
	public boolean acceptEvent(TrackingActivity activity) {
		if (filters.size() > 0) {
			return acceptEvent(new SinkLogEvent(this, activity));
		}
		return true;
	}
	
	
	/**
	 * Subclasses should use this helper class to filter out
	 * unwanted log events.
	 * 
	 * @param event
	 *            to be checked with registered filters
	 * @return true if trackign event passed all filters, false otherwise           
	 * @see TrackingEvent
	 */
	public boolean acceptEvent(TrackingEvent event) {
		if (filters.size() > 0) {
			return acceptEvent(new SinkLogEvent(this, event));
		}
		return true;
	}
	
	
	/**
	 * Subclasses should use this helper class to filter out
	 * unwanted log messages
	 * 
	 * @param sev message severity to log
	 * @param msg string message to be logged
	 * @return true if log event passed all filters, false otherwise           
	 * @see OpLevel
	 */
	public boolean acceptEvent(OpLevel sev, String msg) {
		if (filters.size() > 0) {
			return acceptEvent(new SinkLogEvent(this, sev, msg));
		}
		return true;
	}
	
	/**
	 * Subclasses should use this helper class to filter out
	 * unwanted log messages
	 * 
	 * @param sev message severity to log
	 * @param msg string message to be logged
	 * @param ex exception associated with this message
	 * @return true if log event passed all filters, false otherwise           
	 * @see OpLevel
	 */
	public boolean acceptEvent(OpLevel sev, String msg, Throwable ex) {
		if (filters.size() > 0) {
			return acceptEvent(new SinkLogEvent(this, sev, msg, ex));
		}
		return true;
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
	public void log(OpLevel sev, String msg) {
		if (logListeners.size() > 0) {
			notifyListeners(new SinkLogEvent(this, sev, msg));
		}
	}

	@Override
	public void log(OpLevel sev, String msg, Throwable ex) {
		if (logListeners.size() > 0) {
			notifyListeners(new SinkLogEvent(this, sev, msg, ex));
		}
	}
}
