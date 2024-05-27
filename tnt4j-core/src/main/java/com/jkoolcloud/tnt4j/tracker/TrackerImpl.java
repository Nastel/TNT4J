/*
 * Copyright 2014-2023 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jkoolcloud.tnt4j.tracker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.jkoolcloud.tnt4j.config.TrackerConfig;
import com.jkoolcloud.tnt4j.core.*;
import com.jkoolcloud.tnt4j.selector.TrackingSelector;
import com.jkoolcloud.tnt4j.sink.*;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.utils.LightStack;
import com.jkoolcloud.tnt4j.utils.Utils;
import com.jkoolcloud.tnt4j.uuid.DefaultUUIDFactory;

/**
 * <p>
 * Concrete class that implements {@link Tracker} interface. This class implements integration with {@link EventSink}.
 * Do not use this class directly. This class is instantiated by the {@code DefaultTrackerFactory.getInstance(...)} or
 * {@code TrackingLogger.getInstance(...)} calls. Access to this class is thread safe. {@code TrackingLogger.tnt(...)}
 * method will trigger logging to {@link EventSink} configured in {@link TrackerConfig}.
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
public class TrackerImpl implements Tracker, SinkErrorListener, AutoCloseable {
	private static EventSink logger = DefaultEventSinkFactory.defaultEventSink(TrackerImpl.class);
	private static ThreadLocal<LightStack<TrackingActivity>> ACTIVITY_STACK = new ThreadLocal<>();

	public static final String DEFAULT_SNAPSHOT_CAT_KEY = "tracker.default.snapshot.category";
	public static final String DEFAULT_SNAPSHOT_CATEGORY = "None";

	public static final NullActivity NULL_ACTIVITY = new NullActivity();
	public static final NullEvent NULL_EVENT = new NullEvent(null);

	private final String id;
	private final EventSink eventSink;
	private final TrackerConfig tConfig;
	private final TrackingSelector selector;
	private TrackingFilter filter;

	// tracker statistics
	private AtomicLong activityCount = new AtomicLong(0);
	private AtomicLong eventCount = new AtomicLong(0);
	private AtomicLong msgCount = new AtomicLong(0);
	private AtomicLong snapCount = new AtomicLong(0);
	private AtomicLong errorCount = new AtomicLong(0);
	private AtomicLong dropCount = new AtomicLong(0);
	private AtomicLong pushCount = new AtomicLong(0);
	private AtomicLong popCount = new AtomicLong(0);
	private AtomicLong noopCount = new AtomicLong(0);
	private AtomicLong overheadNanos = new AtomicLong(0);
	private volatile boolean keepContext = false;

	protected TrackerImpl(TrackerConfig config) {
		this(config, false);
	}

	protected TrackerImpl(TrackerConfig config, boolean keepContext) {
		if (!config.isBuilt()) {
			throw new IllegalArgumentException("Uninitialized tracker configuration: use config.build()");
		}
		this.keepContext = keepContext;
		this.tConfig = config;
		this.id = newUUID(config);
		this.selector = tConfig.getTrackingSelector();
		this.eventSink = tConfig.getEventSink();
		open();
	}

	private void openIOHandle(Handle handle) {
		try {
			handle.open();
		} catch (IOException ioe) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR,
					"Failed to open handle={}, vm.name={}, tid={}, event.sink={}, source={}, reason={}", handle,
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(),
					Utils.getExceptionMessages(ioe), ioe);
		} catch (Throwable e) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR, "Failed to open handle={}, vm.name={}, tid={}, event.sink={}, source={}", handle,
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(), e);
		}
	}

	private synchronized void openEventSink() {
		closeEventSink();

		try {
			if (tConfig.getSinkErrorListener() != null) {
				eventSink.addSinkErrorListener(tConfig.getSinkErrorListener());
			}
			if (tConfig.getSinkLogEventListener() != null) {
				eventSink.addSinkLogEventListener(tConfig.getSinkLogEventListener());
			}
			if (tConfig.getSinkEventFilter() != null) {
				eventSink.addSinkEventFilter(tConfig.getSinkEventFilter());
			}
			eventSink.addSinkErrorListener(this);
			eventSink.open();
		} catch (IOException ioe) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR,
					"Failed to open event sink vm.name={}, tid={}, event.sink={}, source={}, reason={}",
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(),
					Utils.getExceptionMessages(ioe), ioe);
		} catch (Throwable e) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR, "Failed to open event sink vm.name={}, tid={}, event.sink={}, source={}",
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
				if (tConfig.getSinkErrorListener() != null) {
					eventSink.removeSinkErrorListener(tConfig.getSinkErrorListener());
				}
				eventSink.removeSinkErrorListener(this);

				if (eventSink.isOpen()) {
					eventSink.flush();
					eventSink.close();
				}
			}
		} catch (IllegalStateException exc) { // NOTE: sink is closed
		} catch (IOException ioe) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR,
					"Failed to close event sink vm.name={}, tid={}, event.sink={}, source={}, reason={}",
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(),
					Utils.getExceptionMessages(ioe), ioe);
		} catch (Throwable e) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR, "Failed to close event sink vm.name={}, tid={}, event.sink={}, source={}",
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(), e);
		}
	}

	private synchronized void resetEventSink() {
		try {
			if (Utils.isOpen(eventSink)) {
				eventSink.flush();
				eventSink.close();
			}
		} catch (IOException ioe) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR,
					"Failed to reset event sink vm.name={}, tid={}, event.sink={}, source={}, reason={}",
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(),
					Utils.getExceptionMessages(ioe), ioe);
		} catch (Throwable e) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR, "Failed to reset event sink vm.name={}, tid={}, event.sink={}, source={}",
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(), e);
		}
	}

	private void _checkSinkState() throws IOException, URISyntaxException {
		if (!eventSink.isOpen()) {
			eventSink.open();
		}
	}

	private void _reportItem(TrackingActivity activity) throws IOException, URISyntaxException {
		if (!activity.isStopped()) {
			activity.stop();
		}

		_checkSinkState();

		eventSink.log(activity);
		snapCount.addAndGet(activity.getSnapshotCount());
		activityCount.incrementAndGet();
	}

	private void _reportItem(TrackingEvent event) throws IOException, URISyntaxException {
		if (!event.isStopped()) {
			event.stop();
		}

		_checkSinkState();

		eventSink.log(event);
		eventCount.incrementAndGet();
	}

	private void _reportItem(Snapshot snap) throws IOException, URISyntaxException {
		_checkSinkState();

		eventSink.log(snap);
		snapCount.incrementAndGet();
	}

	private boolean isTrackingEnabled(OpLevel level, Object... args) {
		if (filter == null) {
			return true;
		}
		return filter.isTrackingEnabled(this, level, args);
	}

	/**
	 * Push an instance of {@link TrackingActivity} on top of the stack. Invoke this when activity starts. The stack is
	 * maintained per thread in thread local.
	 *
	 * @param item
	 *            activity to be pushed on the current stack
	 * @return current tracker instance
	 */
	protected Tracker push(TrackingActivity item) {
		if (!keepContext) {
			return this;
		}
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		if (stack == null) {
			stack = new LightStack<>();
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
	 * Pop an instance of {@link TrackingActivity} from the top the stack. Invoke this method when activity stops. The
	 * stack is maintained per thread in thread local.
	 *
	 * @param item
	 *            activity to be popped from the current stack
	 * @return current tracker instance
	 * @exception EmptyStackException
	 *                if this stack is empty.
	 * @exception IllegalStateException
	 *                if the top of the stack is not the item
	 */
	protected Tracker pop(TrackingActivity item) {
		if (!keepContext) {
			return this;
		}
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		if (stack != null) {
			stack.pop(item);
			popCount.incrementAndGet();
		}
		return this;
	}

	/**
	 * Add a given number of nanoseconds to overhead count. Should be called by package members to account for tracking
	 * overhead.
	 *
	 * @param delta
	 *            amount to add to overhead count
	 * @return current tracker instance
	 */
	protected long countOverheadNanos(long delta) {
		return overheadNanos.addAndGet(delta);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() //
				+ "{jid=" + Integer.toHexString(System.identityHashCode(this)) //
				+ ", name=" + getSource().getName() //
				+ ", keep.context=" + keepContext //
				+ ", sink=" + eventSink //
				+ "}";
	}

	@Override
	public Map<String, Object> getStats() {
		Map<String, Object> stats = new LinkedHashMap<>();
		getStats(stats);
		return stats;
	}

	@Override
	public KeyValueStats getStats(Map<String, Object> stats) {
		stats.put(Utils.qualify(this, KEY_ACTIVITY_COUNT), activityCount.get());
		stats.put(Utils.qualify(this, KEY_EVENT_COUNT), eventCount.get());
		stats.put(Utils.qualify(this, KEY_MSG_COUNT), msgCount.get());
		stats.put(Utils.qualify(this, KEY_SNAPSHOT_COUNT), snapCount.get());
		stats.put(Utils.qualify(this, KEY_ERROR_COUNT), errorCount.get());
		stats.put(Utils.qualify(this, KEY_NOOP_COUNT), noopCount.get());
		stats.put(Utils.qualify(this, KEY_DROP_COUNT), dropCount.get());
		stats.put(Utils.qualify(this, KEY_ACTIVITIES_STARTED), pushCount.get());
		stats.put(Utils.qualify(this, KEY_ACTIVITIES_STOPPED), popCount.get());
		stats.put(Utils.qualify(this, KEY_STACK_DEPTH), getStackSize());
		stats.put(Utils.qualify(this, KEY_OVERHEAD_USEC), overheadNanos.get() / 1000);
		if (eventSink != null) {
			eventSink.getStats(stats);
		}
		return this;
	}

	@Override
	public void resetStats() {
		activityCount.set(0);
		eventCount.set(0);
		msgCount.set(0);
		snapCount.set(0);
		errorCount.set(0);
		dropCount.set(0);
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
			return root != null ? root : NULL_ACTIVITY;
		} else {
			return NULL_ACTIVITY;
		}
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		StackTraceElement[] activityTrace = null;
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		if ((stack != null) && (!stack.isEmpty())) {
			activityTrace = new StackTraceElement[stack.size()];
			int index = 0;
			for (int i = (stack.size() - 1); i >= 0; i--) {
				TrackingActivity act = stack.get(i);
				activityTrace[index++] = new StackTraceElement(act.getSource().getName(), act.getResolvedName(),
						act.getTrackingId() + ":" + act.getParentId(), act.getIdCount());
			}
		}
		return activityTrace;
	}

	@Override
	public TrackingActivity[] getActivityStack() {
		TrackingActivity[] activityTrace = null;
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		if ((stack != null) && (!stack.isEmpty())) {
			activityTrace = new TrackingActivity[stack.size()];
			int index = 0;
			for (int i = (stack.size() - 1); i >= 0; i--) {
				TrackingActivity act = stack.get(i);
				activityTrace[index++] = act;
			}
		}
		return activityTrace;
	}

	@Override
	public int getStackSize() {
		LightStack<TrackingActivity> stack = ACTIVITY_STACK.get();
		return stack != null ? stack.size() : 0;
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
			signature = (signature == null) ? newUUID() : signature;
			TrackingActivity activity = new TrackingActivity(level, name, signature, this);
			activity.setPID(Utils.getVMPID());
			if (tConfig.getActivityListener() != null) {
				activity.addActivityListener(tConfig.getActivityListener());
			}
			return activity;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public void tnt(TrackingActivity activity) {
		long start = System.nanoTime();
		try {
			if (activity.isNoop()) {
				noopCount.incrementAndGet();
			} else {
				_reportItem(activity);
			}
		} catch (Throwable ex) {
			dropCount.incrementAndGet();
			trackerError(new SinkLogEvent(eventSink, activity), ex);
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public void tnt(TrackingEvent event) {
		long start = System.nanoTime();
		try {
			if (event.isNoop()) {
				noopCount.incrementAndGet();
			} else {
				_reportItem(event);
			}
		} catch (Throwable ex) {
			dropCount.incrementAndGet();
			trackerError(new SinkLogEvent(eventSink, event), ex);
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public void tnt(Snapshot snapshot) {
		long start = System.nanoTime();
		try {
			_reportItem(snapshot);
		} catch (Throwable ex) {
			dropCount.incrementAndGet();
			trackerError(new SinkLogEvent(eventSink, snapshot), ex);
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public void log(OpLevel sev, String msg, Object... args) {
		long start = System.nanoTime();
		try {
			_checkSinkState();
			eventSink.log(eventSink.getTTL(), getSource(), sev, msg, args);
			msgCount.incrementAndGet();
		} catch (Throwable ex) {
			dropCount.incrementAndGet();
			trackerError(new SinkLogEvent(eventSink, getSource(), sev, eventSink.getTTL(), msg, args), ex);
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
			TrackingEvent event = new TrackingEvent(this, getSource(), OpLevel.NONE, opName, (String) null, msg, args);
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
			TrackingEvent event = new TrackingEvent(this, getSource(), severity, opName, correlator, msg, args);
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
			TrackingEvent event = new TrackingEvent(this, getSource(), severity, opType, opName, correlator, tag, msg,
					args);
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
			TrackingEvent event = new TrackingEvent(this, getSource(), severity, opName, correlator, msg, args);
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
			TrackingEvent event = new TrackingEvent(this, getSource(), severity, opType, opName, correlator, tag, msg,
					args);
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
			TrackingEvent event = new TrackingEvent(this, getSource(), severity, opName, correlators, msg, args);
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
			TrackingEvent event = new TrackingEvent(this, getSource(), severity, opType, opName, correlators, tags, msg,
					args);
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
			TrackingEvent event = new TrackingEvent(this, getSource(), severity, opName, correlators, msg, args);
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
			TrackingEvent event = new TrackingEvent(this, getSource(), severity, opType, opName, correlators, tags, msg,
					args);
			event.getOperation().setUser(tConfig.getSource().getUser());
			return event;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
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
		return Utils.isOpen(eventSink);
	}

	@Override
	public synchronized void open() {
		if (!isOpen()) {
			openIOHandle(selector);
			openEventSink();
			logger.log(OpLevel.DEBUG, "Tracker opened vm.name={}, tid={}, event.sink={}, source={}", Utils.getVMName(),
					Thread.currentThread().getId(), eventSink, getSource());
		}
	}

	@Override
	public synchronized void close() {
		try {
			closeEventSink();
			Utils.close(selector);
			logger.log(OpLevel.DEBUG, "Tracker closed vm.name={}, tid={}, event.sink={}, source={}", Utils.getVMName(),
					Thread.currentThread().getId(), eventSink, getSource());
		} catch (Throwable e) {
			errorCount.incrementAndGet();
			logger.log(OpLevel.ERROR, "Failed to close tracker vm.name={}, tid={}, event.sink={}, source={}",
					Utils.getVMName(), Thread.currentThread().getId(), eventSink, getSource(), e);
		}
	}

	@Override
	public void sinkError(SinkError ev) {
		errorCount.incrementAndGet();
		if (logger.isSet(OpLevel.DEBUG)) {
			logger.log(OpLevel.ERROR, "Sink write error: count={}, vm.name={}, tid={}, event.sink={}, source={}",
					errorCount.get(), Utils.getVMName(), Thread.currentThread().getId(), ev.getSink(),
					ev.getSinkEvent().getEventSource(), ev.getCause());
		}
		resetEventSink();
	}

	/**
	 * Notifies this tracker about an error when writing to an event sink.
	 * 
	 * @param ev
	 *            sink event associated with the error
	 * @param ex
	 *            exception associated with the event
	 */
	protected void trackerError(SinkLogEvent ev, Throwable ex) {
		errorCount.incrementAndGet();
		if (logger.isSet(OpLevel.DEBUG)) {
			if (ev.getSinkObject() instanceof Trackable) {
				logger.log(OpLevel.ERROR, "Failed to track: count={}, signature={}, tid={}, event.sink={}, source={}",
						errorCount.get(), ((Trackable) ev.getSinkObject()).getTrackingId(),
						Thread.currentThread().getId(), ev.getEventSink(), ev.getEventSource(), ex);

			} else {
				logger.log(OpLevel.ERROR,
						"Failed to log message: count={}, severity={}, msg={}, tid={}, event.sink={}, source={}",
						errorCount.get(), ev.getSeverity(), ev.getSinkObject(), Thread.currentThread().getId(),
						ev.getEventSink(), ev.getEventSource(), ex);

			}
		}
	}

	@Override
	public Dataset newDataset(String name) {
		return newDataset(OpType.DATASET.name(), name);
	}

	@Override
	public Dataset newDataset(String cat, String name) {
		Dataset ds = new Dataset(cat, name);
		ds.setSource(getSource());
		return ds;
	}

	@Override
	public Snapshot newSnapshot(String name) {
		return newSnapshot(tConfig.getProperty(DEFAULT_SNAPSHOT_CAT_KEY, DEFAULT_SNAPSHOT_CATEGORY), name);
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

	@Override
	public String newUUID() {
		return newUUID(tConfig);
	}

	@Override
	public String newUUID(Object obj) {
		return newUUID(tConfig, obj);
	}

	public String newUUID(TrackerConfig tConfig) {
		if (tConfig.getUUIDFactory() == null) {
			tConfig.build();
		}
		return tConfig.getUUIDFactory() == null ? DefaultUUIDFactory.getFallbackUUID()
				: tConfig.getUUIDFactory().newUUID();
	}

	public String newUUID(TrackerConfig tConfig, Object obj) {
		if (tConfig.getUUIDFactory() == null) {
			tConfig.build();
		}
		return tConfig.getUUIDFactory() == null ? DefaultUUIDFactory.getFallbackUUID()
				: tConfig.getUUIDFactory().newUUID(obj);
	}

	@Override
	public LogEntry newLogEntry(OpLevel severity, String opName, String msg, Object... args) {
		long start = System.nanoTime();
		try {
			LogEntry entry = new LogEntry(this, getSource(), severity, opName, msg, args);
			entry.getOperation().setUser(tConfig.getSource().getUser());
			return entry;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public LogEntry newLogEntry(OpLevel severity, String opName, byte[] msg, Object... args) {
		long start = System.nanoTime();
		try {
			LogEntry entry = new LogEntry(this, getSource(), severity, opName, msg, args);
			entry.getOperation().setUser(tConfig.getSource().getUser());
			return entry;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public LogEntry newLogEntry(OpLevel severity, OpType opType, String opName, String tag, String msg,
			Object... args) {
		long start = System.nanoTime();
		try {
			LogEntry entry = new LogEntry(this, getSource(), severity, opType, opName, tag, msg, args);
			entry.getOperation().setUser(tConfig.getSource().getUser());
			return entry;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public LogEntry newLogEntry(OpLevel severity, OpType opType, String opName, Collection<String> tags, String msg,
			Object... args) {
		long start = System.nanoTime();
		try {
			LogEntry entry = new LogEntry(this, getSource(), severity, opType, opName, tags, msg, args);
			entry.getOperation().setUser(tConfig.getSource().getUser());
			return entry;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public LogEntry newLogEntry(OpLevel severity, OpType opType, String opName, Collection<String> tags, byte[] msg,
			Object... args) {
		long start = System.nanoTime();
		try {
			LogEntry entry = new LogEntry(this, getSource(), severity, opType, opName, tags, msg, args);
			entry.getOperation().setUser(tConfig.getSource().getUser());
			return entry;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

	@Override
	public LogEntry newLogEntry(OpLevel severity, OpType opType, String opName, String tag, byte[] msg,
			Object... args) {
		long start = System.nanoTime();
		try {
			LogEntry entry = new LogEntry(this, getSource(), severity, opType, opName, tag, msg, args);
			entry.getOperation().setUser(tConfig.getSource().getUser());
			return entry;
		} finally {
			countOverheadNanos(System.nanoTime() - start);
		}
	}

}
