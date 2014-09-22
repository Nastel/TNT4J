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
package com.nastel.jkool.tnt4j.tracker;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.UUID;

import com.nastel.jkool.tnt4j.TrackingLogger;
import com.nastel.jkool.tnt4j.core.Activity;
import com.nastel.jkool.tnt4j.core.ActivityListener;
import com.nastel.jkool.tnt4j.core.ActivityStatus;
import com.nastel.jkool.tnt4j.core.Message;
import com.nastel.jkool.tnt4j.core.OpCompCode;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.core.Operation;
import com.nastel.jkool.tnt4j.core.Property;
import com.nastel.jkool.tnt4j.core.PropertySnapshot;
import com.nastel.jkool.tnt4j.core.Snapshot;
import com.nastel.jkool.tnt4j.utils.Useconds;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * Implements application activity tracking and tracing entity, which keeps track of all related tracking events.
 * </p>
 * 
 * <p>
 * Represents a collection of related <code>TrackingEvent</code> instances considered to be an application activity.
 * These are generally delimited by START/STOP (or START/STOP(EXCEPTION)) calls. <code>TrackingEvent</code> instances
 * can be created using <code>TrackingLogger</code> or <code>Tracker</code>. Source activities should be started and
 * stopped before being reported using <code>TrackingLogger.tnt()</code> or <code>Tracker.tnt()</code> calls.
 * <code>TrackingEvent</code> instances should be registered with a given activity using
 * <code>TrackingActivity.tnt()</code> call which adds and reports a given <code>TrackingEvent</code> with the activity.
 * </p>
 * 
 * @see Activity
 * @see ActivityListener
 * @see ActivityStatus
 * @see Message
 * @see Operation
 * @see Property
 * @see Tracker
 * @see TrackingEvent
 * @see TrackingLogger
 * 
 * @version $Revision: 9 $
 */
public class TrackingActivity extends Activity {
	public static final String DEFAULT_SNAPSHOT_CATEGORY = "Java";
	public static final String SNAPSHOT_CATEGORY_GC = "GarbageCollector";

	public static final String SNAPSHOT_CPU = "CPU";
	public static final String SNAPSHOT_ACTIVITY = "Activity";
	public static final String SNAPSHOT_MEMORY = "Memory";
	public static final String SNAPSHOT_THREAD = "Thread";

	public static final String DEFAULT_PROPERTY_LOAD_AVG = "SystemLoadAvg";
	public static final String DEFAULT_PROPERTY_CPU_TIME = "TotalCpuUsec";
	public static final String DEFAULT_PROPERTY_TOTAL_USER_TIME = "TotalCpuUserUsec";
	public static final String DEFAULT_PROPERTY_SLACK_TIME = "SlackUsec";
	public static final String DEFAULT_PROPERTY_WALL_TIME = "WallUsec";
	public static final String DEFAULT_PROPERTY_OVERHEAD_TIME = "OverheadUsec";

	public static final String DEFAULT_PROPERTY_COUNT = "Count";
	public static final String DEFAULT_PROPERTY_DAEMON_COUNT = "DaemonCount";
	public static final String DEFAULT_PROPERTY_STARTED_COUNT = "StartedCount";
	public static final String DEFAULT_PROPERTY_PEAK_COUNT = "PeakCount";

	public static final String DEFAULT_PROPERTY_BLOCKED_COUNT = "BlockedCount";
	public static final String DEFAULT_PROPERTY_WAITED_COUNT = "WaitedCount";
	public static final String DEFAULT_PROPERTY_BLOCKED_TIME = "BlockedUsec";
	public static final String DEFAULT_PROPERTY_WAITED_TIME = "WaitUsec";

	public static final String DEFAULT_PROPERTY_MAX_BYTES = "MaxBytes";
	public static final String DEFAULT_PROPERTY_TOTAL_BYTES = "TotalBytes";
	public static final String DEFAULT_PROPERTY_FREE_BYTES = "FreeBytes";
	public static final String DEFAULT_PROPERTY_USED_BYTES = "UsedBytes";
	public static final String DEFAULT_PROPERTY_USAGE = "Usage";

	public static final String DEFAULT_PROPERTY_TIME = "Time";
	public static final String DEFAULT_PROPERTY_VALID = "isValid";

	private boolean reportStarts = false;
	private int startStopCount = 0;
	private long startCPUTime = 0, stopCPUTime = 0, startBlockTime = 0, stopBlockTime = 0, startWaitTime = 0,
	        stopWaitTime = 0, startBlockCount = 0, stopBlockCount = 0, startWaitCount = 0, stopWaitCount = 0,
	        overHeadTimeNano = 0, lastEventNanos = 0;
	private ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
	private ThreadInfo ownerThread = null;
	private boolean appendProps = true, cpuTimingSupported = false, contTimingSupported = false, enableTiming = false;
	private TrackerImpl tracker = null;

	/**
	 * Creates a logical application activity object with the specified signature.
	 * 
	 * @param level activity severity level
	 * @param name
	 *            activity name
	 * @throws NullPointerException
	 *             if the signature is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the signature is empty or is too long
	 * @see #setTrackingId(String)
	 */
	protected TrackingActivity(OpLevel level, String name) {
		super(UUID.randomUUID().toString(), name);
		setSeverity(level);
	}

	/**
	 * Creates a logical application activity object with the specified signature.
	 * 
	 * @param level activity severity level
	 * @param name
	 *            activity name
	 * @param trk
	 *            <code>Tracker</code> instance associated with this activity
	 * @throws NullPointerException
	 *             if the signature is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the signature is empty or is too long
	 * @see #setTrackingId(String)
	 */
	protected TrackingActivity(OpLevel level, String name, TrackerImpl trk) {
		super(UUID.randomUUID().toString(), name, trk.getSource());
		tracker = trk;
		setSeverity(level);
		initJavaTiming();
	}

	/**
	 * Creates a logical application activity object with the specified signature.
	 * 
	 * @param level activity severity level
	 * @param name
	 *            activity name
	 * @param signature
	 *            activity signature
	 * @param trk
	 *            <code>Tracker</code> instance associated with this activity
	 * @throws NullPointerException
	 *             if the signature is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the signature is empty or is too long
	 * @see #setTrackingId(String)
	 */
	protected TrackingActivity(OpLevel level, String name, String signature, TrackerImpl trk) {
		super(signature, name, trk.getSource());
		tracker = trk;
		setSeverity(level);
		initJavaTiming();
	}

	private void initJavaTiming() {
		cpuTimingSupported = tmbean.isThreadCpuTimeEnabled();
		contTimingSupported = tmbean.isThreadContentionMonitoringEnabled();
	}

	private long getLastElapsedUsec() {
		long elapsedUsec = lastEventNanos > 0? (System.nanoTime() - lastEventNanos)/1000: 0;
		return elapsedUsec;
	}
	
	/**
	 * Track and Trace given <code>TrackingEvent</code> instance correlated with current activity
	 * 
	 */
	public void tnt(TrackingEvent event) {
		if (isStopped()) {
			throw new IllegalStateException("Activity already stopped: name=" + getName() + ", id=" + this.getTrackingId());
		}
		add(event);
		lastEventNanos = System.nanoTime();
		tracker.tnt(event);
	}

	/**
	 * Track and Trace given <code>Snapshot</code> instance correlated with current activity
	 * 
	 */
	public void tnt(Snapshot snapshot) {
		if (isStopped()) {
			throw new IllegalStateException("Activity already stopped: name=" + getName() + ", id=" + this.getTrackingId());
		}
		add(snapshot);
		tracker.tnt(snapshot);
	}

	/**
	 * Track and Trace a given event and associate it with this activity
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param opName
	 *            operation name associated with the event message
	 * @param msg
	 *            event string message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, String opName, byte[] msg, Object...args) {
		tnt(severity, OpType.EVENT, opName, null, null, 0, msg, args);
	}

	/**
	 * Track and Trace a given event and associate it with this activity
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param type
	 *            operation type
	 * @param opName
	 *            operation name associated with the event message
	 * @param msg
	 *            event string message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, OpType type, String opName, byte[] msg, Object...args) {
		tnt(severity, type, opName, null, null, 0, msg, args);
	}

	/**
	 * Track and Trace a given event and associate it with this activity
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param type
	 *            operation type
	 * @param opName
	 *            operation name associated with the event message
	 * @param cid
	 *            event correlator
	 * @param elapsed
	 *            elapsed time of the event in microseconds.
	 * @param msg
	 *            event string message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, OpType type, String opName, String cid, long elapsed, byte[] msg, Object...args) {
		tnt(severity, type, opName, cid, null, elapsed, msg, args);
	}

	/**
	 * Track and Trace a given event and associate it with this activity
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param type
	 *            operation type
	 * @param opName
	 *            operation name associated with the event message
	 * @param cid
	 *            event correlator
	 * @param tag
	 *            message tag
	 * @param elapsed
	 *            elapsed time of the event in microseconds.
	 * @param msg
	 *            event binary message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, OpType type, String opName, String cid, String tag, long elapsed, byte[] msg, Object...args) {
		TrackingEvent event = tracker.newEvent(severity, type, opName, cid, tag, msg, args);
		Throwable ex = Utils.getThrowable(args);
		
		long elapsedUsec = elapsed > 0? elapsed: getLastElapsedUsec();
		event.stop(ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS, 0, 
					ex, Useconds.CURRENT.get(), elapsedUsec);
		tnt(event);
	}

	/**
	 * Track and Trace a given event and associate it with this activity
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param opName
	 *            operation name associated with the event message
	 * @param msg
	 *            event string message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, String opName, String msg, Object...args) {
		tnt(severity, OpType.EVENT, opName, null, null, 0, msg, args);
	}

	/**
	 * Track and Trace a given event and associate it with this activity
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param type
	 *            operation type
	 * @param opName
	 *            operation name associated with the event message
	 * @param msg
	 *            event string message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, OpType type, String opName, String msg, Object...args) {
		tnt(severity, type, opName, null, null, 0, msg, args);
	}

	/**
	 * Track and Trace a given event and associate it with this activity
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param type
	 *            operation type
	 * @param opName
	 *            operation name associated with the event message
	 * @param cid
	 *            event correlator
	 * @param elapsed
	 *            elapsed time of the event in microseconds.
	 * @param msg
	 *            event string message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, OpType type, String opName, String cid, long elapsed, String msg, Object...args) {
		tnt(severity, type, opName, cid, null, elapsed, msg, args);
	}

	/**
	 * Track and Trace a given event and associate it with this activity
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param type
	 *            operation type
	 * @param opName
	 *            operation name associated with the event message
	 * @param cid
	 *            event correlator
	 * @param tag
	 *            message tag
	 * @param elapsed
	 *            elapsed time of the event in microseconds.
	 * @param msg
	 *            event string message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, OpType type, String opName, String cid, String tag, long elapsed, String msg, Object...args) {
		TrackingEvent event = tracker.newEvent(severity, type, opName, cid, tag, msg, args);
		Throwable ex = Utils.getThrowable(args);
		
		long elapsedUsec = elapsed > 0? elapsed: getLastElapsedUsec();
		event.stop(ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS, 0, 
					ex, Useconds.CURRENT.get(), elapsedUsec);
		tnt(event);
	}

	/**
	 * Return thread handle that owns this activity. Owner is the tread that
	 * started this activity when <code>TrackingActivity.start()</code> is called.
	 * There can only be one thread that owns an activity. All thread/activity metrics
	 * are computed based on the owner thread.
	 * It is possible, but not recommended to use the same <code>TrackingActivity</code>
	 * instance across multiple threads, where start/stop are run across thread boundaries.
	 * 
	 * @return thread owner info
	 */	
	public ThreadInfo getThreadInfo() {
		return ownerThread;
	}
	
	private void initActivity() {
		if (startStopCount == 0) {
			long start = System.nanoTime();
			ownerThread = tmbean.getThreadInfo(Thread.currentThread().getId());
			startStopCount++;
			tracker.push(this);
			if (enableTiming) {
				startCPUTime = cpuTimingSupported ? tmbean.getThreadCpuTime(ownerThread.getThreadId()) : 0;
				startBlockCount = ownerThread.getBlockedCount();
				startWaitCount = ownerThread.getWaitedCount();
				if (contTimingSupported) {
					startBlockTime = ownerThread.getBlockedTime();
					startWaitTime = ownerThread.getWaitedTime();
				}
			}
			if (reportStarts) {
				tracker.tnt(this);
			}
			long delta = (System.nanoTime() - start);
			overHeadTimeNano += delta;
			tracker.countOverheadNanos(delta);
		}
	}

	/**
	 * Instruct activity to report start activity events into the underlying tracker associated with this activity. An
	 * tracking event will be logged when <code>start(..)</code> method call is made.
	 * 
	 * @param flag
	 *            enable reporting on start (default is false).
	 */
	public void reportStart(boolean flag) {
		reportStarts = flag;
	}

	@Override
	public void start() {
		enableTiming = true;
		initActivity();
		super.start();
	}

	@Override
	public void start(long startTimeUsec) {
		initActivity();
		super.start(startTimeUsec);
	}

	/**
	 * Indicates that application activity has ended normally without exception.
	 * 
	 */
	@Override
	public void stop() {
		stop(ActivityStatus.END, null, 0);
	}

	/**
	 * Indicates that application activity has ended normally without exception.
	 * 
	 * @param elapsedUsec elapsed time in microseconds
	 */
	@Override
	public void stop(long elapsedUsec) {
		stop(ActivityStatus.END, null, elapsedUsec);
	}

	/**
	 * Indicates that application activity has ended.
	 * 
	 * @param ex
	 *            exception associated with the activity or null if none.
	 * @see ActivityStatus
	 */
	public void stop(Throwable ex) {
		stop((ex != null ? ActivityStatus.EXCEPTION : ActivityStatus.END), (ex != null ? OpCompCode.WARNING
		        : OpCompCode.SUCCESS), ex, 0);
	}

	/**
	 * Indicates that application activity has ended.
	 * 
	 * @param ex
	 *            exception associated with the activity or null if none.
	 * @param elapsedUsec elapsed time in microseconds
	 * @see ActivityStatus
	 */
	public void stop(Throwable ex, long elapsedUsec) {
		stop((ex != null ? ActivityStatus.EXCEPTION : ActivityStatus.END), (ex != null ? OpCompCode.WARNING
		        : OpCompCode.SUCCESS), ex, elapsedUsec);
	}

	/**
	 * Indicates that application activity has ended.
	 * 
	 * @param status
	 *            status with which activity ended.
	 * @param ex
	 *            exception associated with the activity or null if none.
	 * @see ActivityStatus
	 */
	public void stop(ActivityStatus status, Throwable ex) {
		stop(status, (ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS), ex, 0);
	}

	/**
	 * Indicates that application activity has ended.
	 * 
	 * @param status
	 *            status with which activity ended.
	 * @param ex
	 *            exception associated with the activity or null if none.
	 * @param elapsedUsec elapsed time in microseconds
	 * @see ActivityStatus
	 */
	public void stop(ActivityStatus status, Throwable ex, long elapsedUsec) {
		stop(status, (ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS), ex, elapsedUsec);
	}

	/**
	 * Indicates that application activity has ended.
	 * 
	 * @param status
	 *            status with which activity ended.
	 * @param ccode
	 *            completion code of the activity.
	 * @param ex
	 *            exception associated with the activity or null if none.
	 * @see ActivityStatus
	 * @see OpCompCode
	 */
	public void stop(ActivityStatus status, OpCompCode ccode, Throwable ex) {
		stop(status, ccode, ex, 0);
	}

	/**
	 * Indicates that application activity has ended.
	 * 
	 * @param status
	 *            status with which activity ended.
	 * @param ccode
	 *            completion code of the activity.
	 * @param ex
	 *            exception associated with the activity or null if none.
	 * @param elapsedUsec elapsed time in microseconds
	 * @see ActivityStatus
	 * @see OpCompCode
	 */
	public void stop(ActivityStatus status, OpCompCode ccode, Throwable ex, long elapsedUsec) {
		setException(ex);
		setStatus(status);
		setCompCode(ccode);
		finishTiming();
		stop(Useconds.CURRENT.get(), elapsedUsec);
	}

	@Override
	public void stop(long stopTimeUsec, long elapsedUsec) {
		finishTiming();
		super.stop(stopTimeUsec, elapsedUsec);
		finishActivity();
	}

	/**
	 * Enable/disable appending of default snapshot when activity stops. When set to true
	 * <code>takePropertySnapshot()</code> is called when activity is stopped using <code>stop()</code> method. This can
	 * be useful when you want to append a defined set of default properties every time an activity is stopped.
	 * 
	 * @param flag
	 *            append default snapshot with name "SYSTEM"
	 */
	public TrackingActivity appendDefaultSnapshot(boolean flag) {
		appendProps = flag;
		return this;
	}

	private void finishTiming() {
		if (startStopCount == 1) {
			long start = System.nanoTime();
			startStopCount++;
			if (startCPUTime > 0) {
				stopBlockCount = ownerThread.getBlockedCount();
				stopWaitCount = ownerThread.getWaitedCount();
				if (contTimingSupported) {
					stopBlockTime = ownerThread.getBlockedTime();
					stopWaitTime = ownerThread.getWaitedTime();
					setWaitTime(((stopWaitTime - startWaitTime) + (stopBlockTime - startBlockTime)) * 1000);
				}
				stopCPUTime = getCurrentCpuTimeNanos();
			}
			tracker.pop(this);
			long delta = (System.nanoTime() - start);
			overHeadTimeNano += delta;
			tracker.countOverheadNanos(delta);
		}
	}

	private void finishActivity() {
		if (appendProps && (startStopCount == 2)) {
			startStopCount++;
			appendProperties();
		}
	}

	/**
	 * This method returns total CPU time in nanoseconds currently used by the current thread.
	 * run this method only after activity is started.
	 */
	public long getCurrentCpuTimeNanos() {
		return (cpuTimingSupported && (ownerThread != null)? tmbean.getThreadCpuTime(ownerThread.getThreadId()) : -1);
	}

	/**
	 * This method returns total CPU time in nanoseconds used since the start of this activity. If the activity has
	 * stopped the value returned is an elapsed CPU time since between activity start/stop calls. If the activity has
	 * not stopped yet, the value is the current used CPU time since the start until now.
	 */
	public long getUsedCpuTimeNanos() {
		if (stopCPUTime > 0)
			return (stopCPUTime - startCPUTime);
		else if (startCPUTime > 0) {
			return (getCurrentCpuTimeNanos() - startCPUTime);
		} else {
			return -1;
		}
	}

	/**
	 * This method returns total wall time computed after activity has stopped.
	 * wall-time is computed as total used cpu + blocked time + wait time.
	 */
	public long getWallTimeUsec() {
		long wallTime = -1;
		if (stopCPUTime > 0) {
			long cpuUsed = getUsedCpuTimeNanos();
			double cpuUsec = ((double) cpuUsed / 1000.0d);
			wallTime = (long) (cpuUsec + getWaitTime());
		}
		return wallTime;
	}

	/**
	 * This method returns total block time computed after activity has stopped.
	 * @return total blocked time in microseconds, -1 if not stopped yet
	 */
	public long getBlockedTimeUsec() {
		return stopBlockTime > 0? ((stopBlockTime - startBlockTime) * 1000): -1;
	}

	/**
	 * This method returns total wait time computed after activity has stopped.
	 * @return total waited time in microseconds, -1 if not stopped yet
	 */
	public long getWaitedTimeUsec() {
		return stopWaitTime > 0? ((stopWaitTime - startWaitTime) * 1000): -1;
	}

	/**
	 * This method appends a default set of properties when activity timing stops. Developers should override this
	 * method to add user defined set of properties. By default this method appends default set of properties defined by
	 * <code>DEFAULT_PROPERTY_XXX</code> property values. Example:
	 * <code>TrackingActivity.DEFAULT_PROPERTY_CPU_TOTAL_TIME</code>.
	 */
	protected void appendProperties() {
		long start = System.nanoTime();
		PropertySnapshot cpu = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_CPU, getSeverity());
		double load = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
		if (load >= 0) {
			cpu.add(new Property(DEFAULT_PROPERTY_LOAD_AVG, load));
		}
		if (cpuTimingSupported) {
			cpu.add(DEFAULT_PROPERTY_COUNT, ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());
			cpu.add(new Property(DEFAULT_PROPERTY_CPU_TIME, ((double) tmbean.getThreadCpuTime(ownerThread.getThreadId()) / 1000.0d)));
			cpu.add(new Property(DEFAULT_PROPERTY_TOTAL_USER_TIME, ((double) tmbean.getThreadUserTime(ownerThread.getThreadId()) / 1000.0d)));
		}
		this.add(cpu);

		PropertySnapshot thread = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_THREAD, getSeverity());
		thread.add(new Property(DEFAULT_PROPERTY_COUNT, tmbean.getThreadCount()));
		thread.add(new Property(DEFAULT_PROPERTY_DAEMON_COUNT, tmbean.getDaemonThreadCount()));
		thread.add(new Property(DEFAULT_PROPERTY_STARTED_COUNT, tmbean.getTotalStartedThreadCount()));
		thread.add(new Property(DEFAULT_PROPERTY_PEAK_COUNT, tmbean.getPeakThreadCount()));
		thread.add(new Property(DEFAULT_PROPERTY_BLOCKED_COUNT, ownerThread.getBlockedCount()));
		thread.add(new Property(DEFAULT_PROPERTY_WAITED_COUNT, ownerThread.getWaitedCount()));
		if (contTimingSupported) {
			thread.add(new Property(DEFAULT_PROPERTY_BLOCKED_TIME, ownerThread.getBlockedTime() * 1000));
			thread.add(new Property(DEFAULT_PROPERTY_WAITED_TIME, ownerThread.getWaitedTime() * 1000));
		}
		this.add(thread);

		PropertySnapshot mem = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_MEMORY, getSeverity());
		long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long memPct = (long) (((double) usedMem / (double) Runtime.getRuntime().totalMemory()) * 100.0d);
		mem.add(new Property(DEFAULT_PROPERTY_MAX_BYTES, Runtime.getRuntime().maxMemory()));
		mem.add(new Property(DEFAULT_PROPERTY_TOTAL_BYTES, Runtime.getRuntime().totalMemory()));
		mem.add(new Property(DEFAULT_PROPERTY_FREE_BYTES, Runtime.getRuntime().freeMemory()));
		mem.add(new Property(DEFAULT_PROPERTY_USED_BYTES, usedMem));
		mem.add(new Property(DEFAULT_PROPERTY_USAGE, memPct));
		this.add(mem);

		List<GarbageCollectorMXBean> gcList = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean gc : gcList) {
			PropertySnapshot gcSnap = new PropertySnapshot(SNAPSHOT_CATEGORY_GC, gc.getName(), getSeverity());
			gcSnap.add(new Property(DEFAULT_PROPERTY_COUNT, gc.getCollectionCount()));
			gcSnap.add(new Property(DEFAULT_PROPERTY_TIME, gc.getCollectionTime()));
			gcSnap.add(new Property(DEFAULT_PROPERTY_VALID, gc.isValid()));
			this.add(gcSnap);
		}

		if (startCPUTime > 0) {
			PropertySnapshot activity = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_ACTIVITY, getSeverity());
			if (cpuTimingSupported) {
				long cpuUsed = getUsedCpuTimeNanos();
				double cpuUsec = ((double) cpuUsed / 1000.0d);
				activity.add(new Property(DEFAULT_PROPERTY_CPU_TIME, cpuUsec));
				long slackTime = (long) (getElapsedTime() - getWaitTime() - cpuUsec);
				activity.add(new Property(DEFAULT_PROPERTY_SLACK_TIME, slackTime));
				activity.add(new Property(DEFAULT_PROPERTY_WALL_TIME, (cpuUsec + getWaitTime())));
			}
			activity.add(new Property(DEFAULT_PROPERTY_BLOCKED_COUNT, (stopBlockCount - startBlockCount)));
			activity.add(new Property(DEFAULT_PROPERTY_WAITED_COUNT, (stopWaitCount - startWaitCount)));
			if (contTimingSupported) {
				activity.add(new Property(DEFAULT_PROPERTY_BLOCKED_TIME, ((stopBlockTime - startBlockTime) * 1000)));
				activity.add(new Property(DEFAULT_PROPERTY_WAITED_TIME, ((stopWaitTime - startWaitTime) * 1000)));
			}
			overHeadTimeNano += (System.nanoTime() - start);
			activity.add(new Property(DEFAULT_PROPERTY_OVERHEAD_TIME, ((double) overHeadTimeNano / 1000.0d)));
			this.add(activity);
		}
	}

	@Override
	public String toString() {
		if (getSnapshotCount() > 0) {
			return super.toString() + "," + getSnapshots();
		} else {
			return super.toString();
		}
	}

}
