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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.nastel.jkool.tnt4j.core.KeyValueStats;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.Snapshot;
import com.nastel.jkool.tnt4j.core.TTL;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.source.Source;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements a default abstract class for <code>EventSink</code>. Developers should subclass from this class
 * for all event sinks.
 * </p>
 *
 *
 * @version $Revision: 9 $
 *
 * @see TTL
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
	private Source source;
	private boolean filterCheck = true;
	private long ttl = TTL.TTL_CONTEXT;
	private EventLimiter limiter;
	private EventFormatter formatter;
	private AtomicLong loggedActivities = new AtomicLong(0);
	private AtomicLong loggedEvents = new AtomicLong(0);
	private AtomicLong loggedMsgs = new AtomicLong(0);
	private AtomicLong sinkWrites = new AtomicLong(0);
	private AtomicLong lastTime = new AtomicLong(0);
	private AtomicLong loggedSnaps = new AtomicLong(0);
	private AtomicLong errorCount = new AtomicLong(0);
	private AtomicLong skipCount = new AtomicLong(0);

	/**
	 * Create an event sink with a given name
	 * 
	 * @param nm event sink name
	 */
	public AbstractEventSink(String nm) {
		this.name = nm;
	}

	/**
	 * Create an event sink with a given name
	 * and event formatter and default {@link TTL}
	 * 
	 * @param nm event sink name
	 * @param fmt event formatter instance
	 */
	public AbstractEventSink(String nm, EventFormatter fmt) {
		this.name = nm;
		this.formatter = fmt;
	}

	/**
	 * Create an event sink with a given name
	 * and event formatter
	 * 
	 * @param nm event sink name
	 * @param ttl time to live for events written to this sink
	 * @param fmt event formatter instance
	 * 
	 * @see TTL
	 */
	public AbstractEventSink(String nm, long ttl, EventFormatter fmt) {
		this.name = nm;
		this.ttl = ttl;
		this.formatter = fmt;
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
		stats.put(Utils.qualify(this, KEY_LOGGED_ACTIVITIES), loggedActivities.get());
		stats.put(Utils.qualify(this, KEY_LOGGED_EVENTS), loggedEvents.get());
		stats.put(Utils.qualify(this, KEY_LOGGED_SNAPSHOTS), loggedSnaps.get());
		stats.put(Utils.qualify(this, KEY_SINK_ERROR_COUNT), errorCount.get());
		stats.put(Utils.qualify(this, KEY_LOGGED_MSGS), loggedMsgs.get());
		stats.put(Utils.qualify(this, KEY_SINK_WRITES), sinkWrites.get());
		stats.put(Utils.qualify(this, KEY_SKIPPED_COUNT), skipCount.get());
		if (lastTime.get() > 0) {
			stats.put(Utils.qualify(this, KEY_LAST_TIMESTAMP), new Date(lastTime.get()));
			stats.put(Utils.qualify(this, KEY_LAST_AGE), (System.currentTimeMillis() - lastTime.get()));
		}
		if (limiter != null) {
			stats.put(Utils.qualify(this, "limiter-enabled"), limiter.getLimiter().isEnabled());
			stats.put(Utils.qualify(this, "limiter-mps"), limiter.getLimiter().getMPS());
			stats.put(Utils.qualify(this, "limiter-bps"), limiter.getLimiter().getBPS());
			stats.put(Utils.qualify(this, "limiter-max-mps"), limiter.getLimiter().getMaxMPS());
			stats.put(Utils.qualify(this, "limiter-max-bps"), limiter.getLimiter().getMaxBPS());
			stats.put(Utils.qualify(this, "limiter-total-msgs"), limiter.getLimiter().getTotalMsgs());
			stats.put(Utils.qualify(this, "limiter-total-bytes"), limiter.getLimiter().getTotalBytes());
			stats.put(Utils.qualify(this, "limiter-deny-count"), limiter.getLimiter().getDenyCount());
			stats.put(Utils.qualify(this, "limiter-delay-count"), limiter.getLimiter().getDelayCount());
			stats.put(Utils.qualify(this, "limiter-delay-last-sec"), limiter.getLimiter().getLastDelayTime());
			stats.put(Utils.qualify(this, "limiter-delay-time-sec"), limiter.getLimiter().getTotalDelayTime());
		}
		return this;
	}

	@Override
	public void resetStats() {
		loggedActivities.set(0);
		loggedEvents.set(0);
		errorCount.set(0);
		loggedMsgs.set(0);
		sinkWrites.set(0);
		skipCount.set(0);
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
		errorCount.incrementAndGet();
		if (errorListeners.size() > 0) {
			SinkError event = new SinkError(this, msg, ex);
			notifyListeners(event);
		} else {
			logger.log(OpLevel.ERROR, "Error when logging msg=''{0}''", msg, ex);
		}
	}

	@Override
	public EventSink filterOnLog(boolean flag) {
		filterCheck = flag;
		return this;
	}

	@Override
	public boolean isLoggable(OpLevel level, String msg, Object... args) {
		return isLoggable(getSource(), level, msg, args);
	}

	@Override
	public boolean isLoggable(Source source, OpLevel level, String msg, Object... args) {
		return isLoggable(getTTL(), source, level, msg, args);
	}

	@Override
	public boolean isLoggable(long ttl, Source source, OpLevel level, String msg, Object... args) {
		boolean pass = true;
		if (filters.size() == 0) return pass;
		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, ttl, source, level, msg, args));
			if (!pass) {
				skipCount.incrementAndGet();
				break;
			}
		}
		return pass;
	}

	@Override
	public boolean isLoggable(Snapshot snapshot) {
		boolean pass = true;
		if (filters.size() == 0) return pass;
		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, snapshot));
			if (!pass) {
				skipCount.incrementAndGet();
				break;
			}
		}
		return pass;
	}

	@Override
	public boolean isLoggable(TrackingActivity activity) {
		boolean pass = true;
		if (filters.size() == 0) return pass;
		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, activity));
			if (!pass) {
				skipCount.incrementAndGet();
				break;
			}
		}
		return pass;
	}

	@Override
	public boolean isLoggable(TrackingEvent event) {
		boolean pass = true;
		if (filters.size() == 0) return pass;
		for (SinkEventFilter filter : filters) {
			pass = (pass && filter.filter(this, event));
			if (!pass) {
				skipCount.incrementAndGet();
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
		boolean doLog = filterCheck? isLoggable(activity): true;
		if (doLog) {
			try {
				if (ttl != TTL.TTL_CONTEXT) {
					activity.setTTL(ttl);
				}
				if (!_limiter(1, 0)) return;
				_log(activity);
				loggedActivities.incrementAndGet();
				loggedSnaps.addAndGet(activity.getSnapshotCount());
				lastTime.set(System.currentTimeMillis());
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
		boolean doLog = filterCheck? isLoggable(event): true;
		if (doLog) {
			try {
				if (ttl != TTL.TTL_CONTEXT) {
					event.setTTL(ttl);
				}
				if (!_limiter(1, event.getSize())) return;
				_log(event);
				loggedEvents.incrementAndGet();
				loggedSnaps.addAndGet(event.getOperation().getSnapshotCount());
				lastTime.set(System.currentTimeMillis());
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
		boolean doLog = filterCheck? isLoggable(snapshot): true;
		if (doLog) {
			try {
				if (ttl != TTL.TTL_CONTEXT) {
					snapshot.setTTL(ttl);
				}
				if (!_limiter(1, 0)) return;
				_log(snapshot);
				loggedSnaps.incrementAndGet();
				lastTime.set(System.currentTimeMillis());
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
		log(ttl, src, sev, msg, args);
	}

	@Override
	public void log(long ttl_sec, Source src, OpLevel sev, String msg, Object... args) {
		_checkState();
		boolean doLog = filterCheck? isLoggable(ttl_sec, source, sev, msg): true;
		if (doLog) {
			try {
				long nttl = ((ttl_sec != TTL.TTL_CONTEXT)? ttl_sec: TTL.TTL_DEFAULT);
				if (!_limiter(1, msg.length())) return;
				_log(nttl, src, sev, msg, args);
				loggedMsgs.incrementAndGet();
				lastTime.set(System.currentTimeMillis());
				if (logListeners.size() > 0) {
					notifyListeners(new SinkLogEvent(this, src, sev, nttl, msg, args));
				}
			} catch (Throwable ex) {
				notifyListeners(msg, ex);
			}
		}
	}

	@Override
	public void write(Object msg, Object...args) throws IOException, InterruptedException {
		try {
			if (!_limiter(msg)) return;
			_write(msg, args);
			sinkWrites.incrementAndGet();
			lastTime.set(System.currentTimeMillis());
			if (logListeners.size() > 0) {
				notifyListeners(new SinkLogEvent(this, getSource(), OpLevel.NONE, (ttl != TTL.TTL_CONTEXT)? ttl: TTL.TTL_DEFAULT, msg, args));
			}
		} catch (Throwable ex) {
			notifyListeners(msg, ex);
		}
	}

	@Override
	public long getTTL() {
		return ttl;
	}

	@Override
	public void setTTL(long ttl) {
		this.ttl = ttl;
	}
	
	@Override
	public void setLimiter(EventLimiter limit) {
		this.limiter = limit;
	}
	
	@Override
	public EventLimiter getLimiter() {
		return limiter;
	}
	
	/**
	 * Check state of the sink before logging occurs.
	 *
	 * @param sink event sink
	 * @throws IllegalStateException if sink is in wrong state
	 */
    public static void checkState(EventSink sink) throws IllegalStateException {
		if (sink == null || !sink.isOpen())
			throw new IllegalStateException("Sink closed or unavailable: sink=" + sink);
    }

	/**
	 * Override this method to check state of the sink before logging occurs.
	 *
	 * @throws IllegalStateException if sink is in wrong state
	 */
    protected void _checkState() throws IllegalStateException {
    	checkState(this);
    }

	/**
	 * Applies rate limiting on mps/bps
	 * 
	 * @param msgCount messages sent
	 * @param byteCount bytes sent
	 * @return true if permit obtained, false otherwise
	 */
    protected boolean _limiter(int msgCount, int byteCount) {
    	if (limiter != null) {
    		return limiter.obtain(msgCount, byteCount);
    	}
    	return true;
    }

	/**
	 * Applies rate limiting on mps/bps
	 * 
	 * @param obj object to be sent
	 * @return true if permit obtained, false otherwise
	 */
    protected boolean _limiter(Object obj) {
    	if (limiter != null) {
    		return limiter.obtain(1, String.valueOf(obj).length());
    	} 
    	return true;
    }

	/**
	 * Override this method to add actual implementation for all subclasses.
	 *
	 * @param event to be sent to the sink
	 * @throws Exception if error logging tracking event
	 * @see TrackingEvent
	 */
	abstract protected void _log(TrackingEvent event) throws Exception;

	/**
	 * Override this method to add actual implementation for all subclasses.
	 *
	 * @param activity to be sent to the sink
	 * @throws Exception if error logging tracking activity
	 * @see TrackingActivity
	 */
	abstract protected void _log(TrackingActivity activity) throws Exception;;

	/**
	 * Override this method to add actual implementation for all subclasses.
	 *
	 * @param snapshot string message to be logged
	 * @throws Exception if error logging snapshot
	 * @see OpLevel
	 */
	abstract protected void _log(Snapshot snapshot) throws Exception;

	/**
	 * Override this method to add actual implementation for all subclasses.
	 *
	 * @param ttl
	 *            time to live in seconds {@link TTL}
	 * @param src
	 *            event source handle
	 * @param sev
	 *            message severity to log
	 * @param msg
	 *            string message to be logged
	 * @param args
	 *            arguments passed along the message
	 * @throws Exception if logging message
	 * @see OpLevel
	 */
	abstract protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) throws Exception;

	/**
	 * Override this method to add actual implementation for all subclasses.
	 *
	 * @param msg
	 *            string message to be logged
	 * @param args
	 *            arguments passed along the message
	 * @throws IOException if error writing to sink
	 * @throws InterruptedException if interrupted during write operation
	 */
	abstract protected void _write(Object msg, Object...args) throws IOException, InterruptedException;
}
