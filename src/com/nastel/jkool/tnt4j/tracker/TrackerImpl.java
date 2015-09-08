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
package com.nastel.jkool.tnt4j.tracker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.nastel.jkool.tnt4j.config.TrackerConfig;
import com.nastel.jkool.tnt4j.core.Handle;
import com.nastel.jkool.tnt4j.core.KeyValueStats;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.core.Operation;
import com.nastel.jkool.tnt4j.core.Property;
import com.nastel.jkool.tnt4j.core.PropertySnapshot;
import com.nastel.jkool.tnt4j.core.Snapshot;
import com.nastel.jkool.tnt4j.selector.TrackingSelector;
import com.nastel.jkool.tnt4j.sink.DefaultEventSinkFactory;
import com.nastel.jkool.tnt4j.sink.EventSink;
import com.nastel.jkool.tnt4j.sink.SinkError;
import com.nastel.jkool.tnt4j.sink.SinkErrorListener;
import com.nastel.jkool.tnt4j.source.Source;
import com.nastel.jkool.tnt4j.utils.LightStack;
import com.nastel.jkool.tnt4j.utils.Utils;


/**
 * <p>
 * Concrete class that implements <code>Tracker</code> interface. This class implements integration with
 * <code>EventSink</code>. Do not use this class directly. This class is instantiated by the
 * <code>DefaultTrackerFactory.getInstance(...)</code> or <code>TrackingLogger.getInstance(...)</code> calls.
 * Access to this class is thread safe. <code>TrackingLogger.tnt(...)</code> method will trigger
 * logging to <code>EventSink</code> configured in <code>TrackingConfig</code>.
 * </p>
 *
 * @see TrackerConfig
 * @see TrackingEvent
 * @see TrackingActivity
 * @see EventSink
 * @see OpLevel
 * @see Source
 *
 * @version $Revision: 21 $
 *
 */
public class TrackerImpl implements Tracker, SinkErrorListener {
	private static EventSink logger = DefaultEventSinkFactory.defaultEventSink(TrackerImpl.class);
	private static ThreadLocal<LightStack<TrackingActivity>> ACTIVITY_STACK = new ThreadLocal<LightStack<TrackingActivity>>();

	public static final String KEY_CONFIG_SOURCE = "config";
	public static final NullActivity NULL_ACTIVITY = new NullActivity();
	public static final NullEvent NULL_EVENT = new NullEvent();

	private String id;
	private EventSink eventSink;
	private TrackerConfig tConfig;
	private TrackingSelector selector;
	private TrackingFilter filter;
	private volatile boolean openFlag = false, keepContext = true;
	private AtomicLong activityCount = new AtomicLong(0);
	private AtomicLong eventCount = new AtomicLong(0);
	private AtomicLong msgCount = new AtomicLong(0);
	private AtomicLong snapCount = new AtomicLong(0);
	private AtomicLong errorCount = new AtomicLong(0);
	private AtomicLong pushCount = new AtomicLong(0);
	private AtomicLong popCount = new AtomicLong(0);
	private AtomicLong noopCount = new AtomicLong(0);
	private AtomicLong overheadNanos = new AtomicLong(0);

	protected TrackerImpl(TrackerConfig config) {
		id = Utils.newUUID();
		tConfig = config;
		selector = tConfig.getTrackingSelector();
		eventSink = tConfig.getEventSink();
		open();
	}

	private void openIOHandle(Handle handle) {
		try {
			handle.open();
		} catch (Throwable e) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR,
					"Failed to open handle={4}, vm.name={0}, tid={1}, event.sink={2}, source={3}",
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(), handle, e);
		}
	}

	private synchronized void openEventSink() {
		try {
			if (tConfig.getSinkLogEventListener() != null) {
				eventSink.addSinkLogEventListener(tConfig.getSinkLogEventListener());
			}
			if (tConfig.getSinkEventFilter() != null) {
				eventSink.addSinkEventFilter(tConfig.getSinkEventFilter());
			}
			eventSink.addSinkErrorListener(this);
			eventSink.open();
		} catch (Throwable e) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR,
					"Failed to open event sink vm.name={0}, tid={1}, event.sink={2}, source={3}",
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(), e);
		}
	}

	private synchronized void closeEventSink() {
		try {
			if (eventSink != null) {
				if (tConfig.getSinkLogEventListener() != null) {
					eventSink.removeSinkLogEventListener(tConfig.getSinkLogEventListener());
				}
				if (tConfig.getSinkEventFilter() != null) {
					eventSink.removeSinkEventFilter(tConfig.getSinkEventFilter());
				}
				eventSink.removeSinkErrorListener(this);
				eventSink.close();
			}
		} catch (Throwable e) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR,
					"Failed to close event sink vm.name={0}, tid={1}, event.sink={2}, source={3}",
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(), e);
		}
	}

	private synchronized void resetEventSink() {
		try {
			if (eventSink != null) {
				eventSink.close();
			}
		} catch (Throwable e) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR,
					"Failed to reset event sink vm.name={0}, tid={1}, event.sink={2}, source={3}",
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(), e);
		}
	}

	private void reportActivity(TrackingActivity activity) throws IOException, URISyntaxException {
		try {
			if (!eventSink.isOpen()) {
				eventSink.open();
			}
		} finally {
			if (!activity.isStopped()) {
				activity.stop();
			}
			eventSink.log(activity);
			snapCount.addAndGet(activity.getSnapshotCount());
			activityCount.incrementAndGet();
		}
	}

	private void reportEvent(TrackingEvent event) throws IOException, URISyntaxException {
		try {
			if (!eventSink.isOpen()) {
				eventSink.open();
			}
		} finally {
			if (!event.isStopped()) {
				event.stop();
			}
			eventSink.log(event);
			eventCount.incrementAndGet();
		}
	}

	private boolean isTrackingEnabled(OpLevel level, Object...args) {
		if (filter == null) return true;
		return filter.isTrackingEnabled(this, level, args);
	}

	/**
	 * Push an instance of <code>TrackingActivity</code> on top of the stack.
	 * Invoke this when activity starts. The stack is maintained per thread in
	 * thread local.
	 *
	 * @param item activity to be pushed on the current stack
	 * @return current tracker instance
	 */
	protected Tracker push(TrackingActivity item) {
		if (!keepContext) return this;
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		if (stack == null) {
			stack = new LightStack<TrackingActivity>();
			ACTIVITY_STACK.set(stack);
		}
		// associate with the parent activity if there is any
		TrackingActivity parent = stack.peek(null);
		if (parent != null) {
			parent.add(item);
		}
		stack.push(item);
		pushCount.incrementAndGet();
		return this;
	}

	/**
	 * Pop an instance of <code>TrackingActivity</code> from the top the stack.
	 * Invoke this method when activity stops. The stack is maintained per thread in
	 * thread local.
	 *
	 * @param item activity to be popped from the current stack
	 * @return current tracker instance
	 * @exception EmptyStackException
	 *                if this stack is empty.
	 * @exception IllegalStateException
	 *                if the top of the stack is not the item
	 */
	protected Tracker pop(TrackingActivity item) {
		if (!keepContext) return this;
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		if (stack != null) {
			stack.pop(item);
			popCount.incrementAndGet();
		}
		return this;
	}

	/**
	 * Add a given number of nanoseconds to overhead count.
	 * Should be called by package members to account for tracking
	 * overhead.
	 *
	 * @param delta amount to add to overhead count
	 * @return current tracker instance
	 */
	protected long countOverheadNanos(long delta) {
		return overheadNanos.addAndGet(delta);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()
				+ "{jid=" + Integer.toHexString(System.identityHashCode(this))
				+ ", name=" + getSource().getName()
				+ ", sink=" + eventSink
				+ "}";
	}

	@Override
	public Map<String, Object> getStats() {
		Map<String, Object> stats = new LinkedHashMap<String, Object>();
		getStats(stats);
		return stats;
	}

	@Override
	public  KeyValueStats getStats(Map<String, Object> stats) {
		stats.put(Utils.qualify(this, KEY_ACTIVITY_COUNT), activityCount.get());
		stats.put(Utils.qualify(this, KEY_EVENT_COUNT), eventCount.get());
		stats.put(Utils.qualify(this, KEY_MSG_COUNT), msgCount.get());
		stats.put(Utils.qualify(this, KEY_SNAPSHOT_COUNT), snapCount.get());
		stats.put(Utils.qualify(this, KEY_ERROR_COUNT), errorCount.get());
		stats.put(Utils.qualify(this, KEY_NOOP_COUNT), noopCount.get());
		stats.put(Utils.qualify(this, KEY_ACTIVITIES_STARTED), pushCount.get());
		stats.put(Utils.qualify(this, KEY_ACTIVITIES_STOPPED), popCount.get());
		stats.put(Utils.qualify(this, KEY_STACK_DEPTH), getStackSize());
		stats.put(Utils.qualify(this, KEY_OVERHEAD_USEC), overheadNanos.get()/1000);
		if (eventSink != null) eventSink.getStats(stats);
		return this;
	}

	@Override
	public void resetStats() {
		activityCount.set(0);
		eventCount.set(0);
		msgCount.set(0);
		snapCount.set(0);
		errorCount.set(0);
		pushCount.set(0);
		popCount.set(0);
		noopCount.set(0);
		overheadNanos.set(0);
		if (eventSink != null) {
			eventSink.resetStats();
		}
	}

	@Override
	public TrackingActivity getCurrentActivity() {
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		if (stack != null) {
			return stack.peek(NULL_ACTIVITY);
		} else {
			return NULL_ACTIVITY;
		}
	}

	@Override
	public TrackingActivity getRootActivity() {
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		if (stack != null) {
			TrackingActivity root = stack.get(0);
			return root != null? root: NULL_ACTIVITY;
		} else {
			return NULL_ACTIVITY;
		}
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		StackTraceElement[] activityTrace = null;
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		if ((stack != null) && (stack.size() > 0)) {
			activityTrace = new StackTraceElement[stack.size()];
			int index = 0;
			for (int i = (stack.size()-1); i >=0; i--) {
				TrackingActivity act = stack.get(i);
				activityTrace[index++] = new StackTraceElement(act.getSource().getName(),
						act.getResolvedName(),
						act.getTrackingId() + ":" + act.getParentId(),
						act.getIdCount());
			}
		}
		return activityTrace;
	}

	@Override
	public TrackingActivity[] getActivityStack() {
		TrackingActivity[] activityTrace = null;
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		if ((stack != null) && (stack.size() > 0)) {
			activityTrace = new TrackingActivity[stack.size()];
			int index = 0;
			for (int i = (stack.size()-1); i >=0; i--) {
				TrackingActivity act = stack.get(i);
				activityTrace[index++] = act;
			}
		}
		return activityTrace;
	}

	@Override
	public int getStackSize() {
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		return stack != null? stack.size(): 0;
	}

	@Override
	public void setTrackingFilter(TrackingFilter tfilt) {
		filter = tfilt;
	}

	@Override
	public Source getSource() {
		return tConfig.getSource();
	}

	@Override
    public EventSink getEventSink() {
	    return eventSink;
    }


	@Override
	public TrackingActivity newActivity() {
		return newActivity(OpLevel.INFO, Operation.NOOP);
	}

	@Override
	public TrackingActivity newActivity(OpLevel level) {
		return newActivity(level, Operation.NOOP);
	}

	@Override
	public TrackingActivity newActivity(OpLevel level, String name) {
		return newActivity(level, name, null);
	}

	@Override
	public TrackingActivity newActivity(OpLevel level, String name, String signature) {
		long start = System.nanoTime();
		try {
			if (!isTrackingEnabled(level, name, signature)) {
				return NULL_ACTIVITY;
			}
			signature = (signature == null)? Utils.newUUID(): signature;
			TrackingActivity luw = new TrackingActivity(level, name, signature, this);
			luw.setPID(Utils.getVMPID());
			if (tConfig.getActivityListener() != null) {
				luw.addActivityListener(tConfig.getActivityListener());
			}
			return luw;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
    public void tnt(TrackingActivity activity) {
		long start = System.nanoTime();
		try  {
			if (!activity.isNoop()) {
				reportActivity(activity);
			} else {
				noopCount.incrementAndGet();
			}
		}
		catch (Throwable ex) {
			if (logger.isSet(OpLevel.DEBUG)) {
				logger.log(OpLevel.ERROR,
					"Failed to track activity: signature={0}, tid={1}, event.sink={2}, source={3}",
					activity.getTrackingId(), Thread.currentThread().getId(), eventSink, getSource(), ex);
			}
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public void tnt(TrackingEvent event) {
		long start = System.nanoTime();
		try  {
			if (!event.isNoop()) {
				reportEvent(event);
			} else {
				noopCount.incrementAndGet();
			}
		} catch (Throwable ex) {
			if (logger.isSet(OpLevel.DEBUG)) {
				logger.log(OpLevel.ERROR,
						"Failed to track event: signature={0}, tid={1}, event.sink={2}, source={3}",
						event.getTrackingId(), Thread.currentThread().getId(), eventSink, getSource(), ex);
			}
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}


	@Override
    public void tnt(Snapshot snapshot) {
		long start = System.nanoTime();
		try {
			eventSink.log(snapshot);
			snapCount.incrementAndGet();
		} catch (Throwable ex) {
			if (logger.isSet(OpLevel.DEBUG)) {
				logger.log(OpLevel.ERROR,
					"Failed to track snapshot: signature={0}, tid={1}, event.sink={2}, snapshot={3}",
					snapshot.getTrackingId(), Thread.currentThread().getId(), eventSink, snapshot, ex);
			}
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
    }

	@Override
    public void log(OpLevel sev, String msg, Object... args) {
		long start = System.nanoTime();
		try {
			eventSink.log(eventSink.getTTL(), getSource(), sev, msg, args);
			msgCount.incrementAndGet();
		} catch (Throwable ex) {
			if (logger.isSet(OpLevel.DEBUG)) {
				logger.log(OpLevel.ERROR,
					"Failed to log message: severity={0}, msg={1}", sev, msg, ex);
			}
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
    public TrackingEvent newEvent(String opName, String msg, Object... args) {
		long start = System.nanoTime();
		try {
			if (!isTrackingEnabled(OpLevel.NONE, opName, msg, args)) {
				return NULL_EVENT;
			}
			TrackingEvent event = new TrackingEvent(getSource(), OpLevel.NONE, opName, (String)null, msg, args);
			event.getOperation().setUser(tConfig.getSource().getUser());
			return event;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
    }

	@Override
	public TrackingEvent newEvent(OpLevel severity, String opName, String correlator, String msg, Object... args) {
		long start = System.nanoTime();
		try {
			if (!isTrackingEnabled(severity, opName, correlator, msg, args)) {
				return NULL_EVENT;
			}
			TrackingEvent event = new TrackingEvent(getSource(), severity, opName, correlator, msg, args);
			event.getOperation().setUser(tConfig.getSource().getUser());
			return event;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}


	@Override
	public TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, String correlator, String tag,
	        String msg, Object... args) {
		long start = System.nanoTime();
		try {
			if (!isTrackingEnabled(severity, opName, correlator, tag, msg, args)) {
				return NULL_EVENT;
			}
			TrackingEvent event = new TrackingEvent(getSource(), severity, opType, opName, correlator, tag, msg, args);
			event.getOperation().setUser(tConfig.getSource().getUser());
			return event;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public TrackingEvent newEvent(OpLevel severity, String opName, String correlator, byte[] msg, Object... args) {
		long start = System.nanoTime();
		try {
			if (!isTrackingEnabled(severity, opName, correlator, msg, args)) {
				return NULL_EVENT;
			}
			TrackingEvent event = new TrackingEvent(getSource(), severity, opName, correlator, msg, args);
			event.getOperation().setUser(tConfig.getSource().getUser());
			return event;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}


	@Override
	public TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, String correlator, String tag,
	        byte[] msg, Object... args) {
		long start = System.nanoTime();
		try {
			if (!isTrackingEnabled(severity, opName, correlator, tag, msg, args)) {
				return NULL_EVENT;
			}
			TrackingEvent event = new TrackingEvent(getSource(), severity, opType, opName, correlator, tag, msg, args);
			event.getOperation().setUser(tConfig.getSource().getUser());
			return event;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
    public TrackingEvent newEvent(OpLevel severity, String opName, Collection<String> correlators, String msg,
            Object... args) {
		long start = System.nanoTime();
		try {
			if (!isTrackingEnabled(severity, opName, correlators, msg, args)) {
				return NULL_EVENT;
			}
			TrackingEvent event = new TrackingEvent(getSource(), severity, opName, correlators, msg, args);
			event.getOperation().setUser(tConfig.getSource().getUser());
			return event;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
    }

	@Override
    public TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, Collection<String> correlators,
            Collection<String> tags, String msg, Object... args) {
		long start = System.nanoTime();
		try {
			if (!isTrackingEnabled(severity, opName, correlators, tags, msg, args)) {
				return NULL_EVENT;
			}
			TrackingEvent event = new TrackingEvent(getSource(), severity, opType, opName, correlators, tags, msg, args);
			event.getOperation().setUser(tConfig.getSource().getUser());
			return event;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
    }

	@Override
    public TrackingEvent newEvent(OpLevel severity, String opName, Collection<String> correlators, byte[] msg,
            Object... args) {
		long start = System.nanoTime();
		try {
			if (!isTrackingEnabled(severity, opName, correlators, msg, args)) {
				return NULL_EVENT;
			}
			TrackingEvent event = new TrackingEvent(getSource(), severity, opName, correlators, msg, args);
			event.getOperation().setUser(tConfig.getSource().getUser());
			return event;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
    }

	@Override
    public TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, Collection<String> correlators,
            Collection<String> tags, byte[] msg, Object... args) {
		long start = System.nanoTime();
		try {
			if (!isTrackingEnabled(severity, opName, correlators, tags, msg, args)) {
				return NULL_EVENT;
			}
			TrackingEvent event = new TrackingEvent(getSource(), severity, opType, opName, correlators, tags, msg, args);
			event.getOperation().setUser(tConfig.getSource().getUser());
			return event;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
    }

	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	@Override
    public TrackingSelector getTrackingSelector() {
	    return selector;
    }

	@Override
    public TrackerConfig getConfiguration() {
	    return tConfig;
    }

	@Override
    public boolean isOpen() {
	    return openFlag;
    }

	@Override
    public synchronized void open() {
		if (!isOpen()) {
			openIOHandle(selector);
			openEventSink();
			openFlag = true;
			logger.log(OpLevel.DEBUG,
				"Tracker opened vm.name={0}, tid={1}, event.sink={2}, source={3}",
				Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource());
		}
    }

	@Override
	public synchronized void close() {
		if (!isOpen()) return;
		try {
			closeEventSink();
			Utils.close(selector);
			logger.log(OpLevel.DEBUG,
				"Tracker closed vm.name={0}, tid={1}, event.sink={2}, source={3}",
				Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource());
		} catch (Throwable e) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR,
				"Failed to close tracker vm.name={0}, tid={1}, event.sink={2}, source={3}",
				Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(), e);
		} finally {
			openFlag = false;
		}
	}

	@Override
    public void sinkError(SinkError ev) {
		errorCount.incrementAndGet();
		if (logger.isSet(OpLevel.DEBUG)) {
			logger.log(OpLevel.ERROR,
				"Sink write error: count={4}, vm.name={0}, tid={1}, event.sink={2}, source={3}",
				Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(), errorCount.get(), ev.getCause());
		}
		resetEventSink();
	}

	@Override
    public Snapshot newSnapshot(String cat, String name) {
	    PropertySnapshot snapshot = new PropertySnapshot(cat, name);
	    snapshot.setSource(getSource());
	    return snapshot;
    }

	@Override
    public Snapshot newSnapshot(String cat, String name, OpLevel level) {
	    PropertySnapshot snapshot = new PropertySnapshot(cat, name, level);
	    snapshot.setSource(getSource());
	    return snapshot;
    }

	@Override
    public Property newProperty(String key, Object val) {
	    return new Property(key, val);
    }

	@Override
    public Property newProperty(String key, Object val, String valType) {
	    return new Property(key, val, valType);
    }

	@Override
    public Tracker setKeepThreadContext(boolean flag) {
		keepContext = flag;
	    return this;
    }

	@Override
    public boolean getKeepThreadContext() {
		return keepContext;
    }

	@Override
    public String getId() {
	    return id;
    }
}
