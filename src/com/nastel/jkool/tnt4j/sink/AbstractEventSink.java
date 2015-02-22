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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.nastel.jkool.tnt4j.core.KeyValueStats;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.Snapshot;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.source.Source;
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
public abstract class AbstractEventSink implements EventSink {
	private static final EventSink logger = DefaultEventSinkFactory.defaultEventSink(AbstractEventSink.class);

	protected ArrayList<SinkErrorListener> errorListeners = new ArrayList<SinkErrorListener>(10);
	protected ArrayList<SinkLogEventListener> logListeners = new ArrayList<SinkLogEventListener>(10);
	protected ArrayList<SinkEventFilter> filters = new ArrayList<SinkEventFilter>(10);

	private String name;
	private EventFormatter formatter;
	private Source source;
	private AtomicLong loggedActivities = new AtomicLong(0);
	private AtomicLong loggedEvents = new AtomicLong(0);
	private AtomicLong loggedMsgs = new AtomicLong(0);
	private AtomicLong loggedSnaps = new AtomicLong(0);
	private AtomicLong errorCount = new AtomicLong(0);
	private AtomicLong filteredCount = new AtomicLong(0);

	public AbstractEventSink(String nm) {
		name = nm;
	}

	public AbstractEventSink(String nm, EventFormatter fmt) {
		name = nm;
		formatter = fmt;
	}

	@Override
	public void setSource(Source src) {
		source = src;
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public EventFormatter getEventFormatter() {
		return formatter;
	}

	@Override
	public Map<String, Object> getStats() {
		LinkedHashMap<String, Object> stats = new LinkedHashMap<String, Object>();
		getStats(stats);
		return stats;
	}

	@Override
	public KeyValueStats getStats(Map<String, Object> stats) {
		stats.put(KEY_LOGGED_ACTIVITIES, loggedActivities.get());
		stats.put(KEY_LOGGED_EVENTS, loggedEvents.get());
		stats.put(KEY_LOGGED_SNAPSHOTS, loggedSnaps.get());
		stats.put(KEY_SINK_ERROR_COUNT, errorCount.get());
		stats.put(KEY_LOGGED_MSGS, loggedMsgs.get());
		stats.put(KEY_SKIPPED_COUNT, filteredCount.get());
		return this;
	}

	@Override
	public void resetStats() {
		loggedActivities.set(0);
		loggedEvents.set(0);
		errorCount.set(0);
		loggedMsgs.set(0);
		filteredCount.set(0);
	}

	/**
	 * Register an event sink listener for notifications when logging events occur when writing to event sink.
	 *
	 * @see SinkLogEventListener
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	private void notifyListeners(SinkError event) {
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
		errorCount.incrementAndGet();
		if (errorListeners.size() > 0) {
			SinkError event = new SinkError(this, msg, ex);
			notifyListeners(event);
		} else {
			logger.log(OpLevel.ERROR, "Error when logging msg=''{0}''", msg, ex);
		}
	}

	/**
	 * Subclasses should use this helper class to filter out unwanted log events before writing to the underlying sink
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
	protected boolean passEvent(OpLevel level, String msg, Object... args) {
		boolean pass = true;
		if (filters.size() == 0) return pass;
		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, level, msg, args));
			if (!pass) {
				filteredCount.incrementAndGet();
				break;
			}
		}
		return pass;
	}

	/**
	 * Subclasses should use this helper class to filter out unwanted log events before writing to the underlying sink
	 *
	 * @param snapshot
	 *            snapshot
	 * @return true if event passed all filters, false otherwise
	 * @see OpLevel
	 */
	protected boolean filterEvent(Snapshot snapshot) {
		boolean pass = true;
		if (filters.size() == 0)
			return pass;

		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, snapshot));
			if (!pass) {
				filteredCount.incrementAndGet();
				break;
			}
		}
		return pass;
	}

	/**
	 * Subclasses should use this helper class to filter out unwanted log events before writing to the underlying sink
	 *
	 * @param activity
	 *            to be checked with registered filters
	 * @return true if tracking activity passed all filters, false otherwise
	 * @see TrackingActivity
	 */
	protected boolean filterEvent(TrackingActivity activity) {
		boolean pass = true;
		if (filters.size() == 0)
			return pass;

		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, activity));
			if (!pass) {
				filteredCount.incrementAndGet();
				break;
			}
		}
		return pass;
	}

	/**
	 * Subclasses should use this helper class to filter out unwanted log events before writing to the underlying sink
	 *
	 * @param event
	 *            to be checked with registered filters
	 * @return true if tracking event passed all filters, false otherwise
	 * @see TrackingEvent
	 */
	protected boolean filterEvent(TrackingEvent event) {
		boolean pass = true;
		if (filters.size() == 0)
			return pass;

		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, event));
			if (!pass) {
				filteredCount.incrementAndGet();
				break;
			}
		}
		return pass;
	}

	@Override
	public void addSinkEventFilter(SinkEventFilter filter) {
		synchronized (filters) {
			filters.add(filter);
		}
	}

	@Override
	public void removeSinkEventFilter(SinkEventFilter filter) {
		synchronized (filters) {
			filters.remove(filter);
		}
	}

	@Override
	public void log(TrackingActivity activity) {
		_checkState();
		if (!filterEvent(activity))
			return;
		if (isSet(activity.getSeverity())) {
			try {
				_log(activity);
				loggedEvents.incrementAndGet();
				if (logListeners.size() > 0) {
					notifyListeners(new SinkLogEvent(this, activity));
				}
			} catch (Throwable ex) {
				notifyListeners(activity, ex);
			}
		}
	}

	@Override
	public void log(TrackingEvent event) {
		_checkState();
		if (!filterEvent(event))
			return;
		if (isSet(event.getSeverity())) {
			try {
				_log(event);
				loggedEvents.incrementAndGet();
				if (logListeners.size() > 0) {
					notifyListeners(new SinkLogEvent(this, event));
				}
			} catch (Throwable ex) {
				notifyListeners(event, ex);
			}
		}
	}

	@Override
	public void log(Snapshot snapshot) {
		_checkState();
		if (!filterEvent(snapshot))
			return;
		if (isSet(snapshot.getSeverity())) {
			try {
				_log(snapshot);
				loggedSnaps.incrementAndGet();
				if (logListeners.size() > 0) {
					notifyListeners(new SinkLogEvent(this, snapshot));
				}
			} catch (Throwable ex) {
				notifyListeners(snapshot, ex);
			}
		}
	}

	@Override
	public void log(OpLevel sev, String msg, Object... args) {
		log(source, sev, msg, args);
	}

	@Override
	public void log(Source src, OpLevel sev, String msg, Object... args) {
		_checkState();
		if (!passEvent(sev, msg)) return;
		if (isSet(sev)) {
			try {
				_log(src, sev, msg, args);
				loggedMsgs.incrementAndGet();
				if (logListeners.size() > 0) {
					notifyListeners(new SinkLogEvent(this, src, sev, msg, args));
				}
			} catch (Throwable ex) {
				notifyListeners(msg, ex);
			}
		}
	}

	/**
	 * Check state of the sink before logging occurs. Throws <code>IllegalStateException</code>
	 * if sink is in wrong state.
	 *
	 * @throws IllegalStateException
	 */
    public static void checkState(EventSink sink) throws IllegalStateException {
		if (sink == null || !sink.isOpen())
			throw new IllegalStateException("Sink closed or unavailable: sink=" + sink);
    }	

	/**
	 * Override this method to check state of the sink before logging occurs. Throws <code>IllegalStateException</code>
	 * if sink is in wrong state.
	 *
	 * @throws IllegalStateException
	 */
    protected void _checkState() throws IllegalStateException {
    	checkState(this);
    }

	/**
	 * Override this method to add actual implementation for all subclasses.
	 *
	 * @param event
	 *            to be sent to the sink
	 * @see TrackingEvent
	 */
	abstract protected void _log(TrackingEvent event) throws Exception;

	/**
	 * Override this method to add actual implementation for all subclasses.
	 *
	 * @param activity
	 *            to be sent to the sink
	 * @see TrackingActivity
	 */
	abstract protected void _log(TrackingActivity activity) throws Exception;;

	/**
	 * Override this method to add actual implementation for all subclasses.
	 *
	 * @param snapshot
	 *            string message to be logged
	 * @see OpLevel
	 */
	abstract protected void _log(Snapshot snapshot) throws Exception;

	/**
	 * Override this method to add actual implementation for all subclasses.
	 *
	 * @param src
	 *            event source handle
	 * @param sev
	 *            message severity to log
	 * @param msg
	 *            string message to be logged
	 * @param args
	 *            arguments passed along the message
	 * @see OpLevel
	 */
	abstract protected void _log(Source src, OpLevel sev, String msg, Object... args) throws Exception;;
}
