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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

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
	private static ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();

	private boolean reportStarts = false;
	private int startStopCount = 0;
	private long startCPUTime = 0;
	private long stopCPUTime = 0;
	private long startBlockTime = 0;
	private long stopBlockTime = 0;
	private long startWaitTime = 0;
	private long stopWaitTime = 0;
	private long lastEventNanos = 0;
	private boolean cpuTimingSupported = false;
	private boolean contTimingSupported = false;
	private boolean enableTiming = false;
	private ThreadInfo ownerThread = null;
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
		super(Utils.newUUID(), name);
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
		super(Utils.newUUID(), name, trk.getSource());
		tracker = trk;
		setSeverity(level);
		setLocation(trk.getSource());
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
		setLocation(trk.getSource());
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
	 * @param event tracking instance to be tracked
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
	 * @param snapshot snapshot instance to be tracked
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
				if (contTimingSupported) {
					startBlockTime = ownerThread.getBlockedTime();
					startWaitTime = ownerThread.getWaitedTime();
				}
			}
			if (reportStarts) {
				tracker.tnt(this);
			}
			long delta = (System.nanoTime() - start);
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
	}

	private void finishTiming() {
		if (startStopCount == 1) {
			long start = System.nanoTime();
			startStopCount++;
			if (startCPUTime > 0) {
				if (contTimingSupported) {
					stopBlockTime = ownerThread.getBlockedTime();
					stopWaitTime = ownerThread.getWaitedTime();
					setWaitTime(((stopWaitTime - startWaitTime) + (stopBlockTime - startBlockTime)) * 1000);
				}
				stopCPUTime = getCurrentCpuTimeNanos();
			}
			tracker.pop(this);
			long delta = (System.nanoTime() - start);
			tracker.countOverheadNanos(delta);
		}
	}

	/**
	 * This method returns total CPU time in nanoseconds currently used by the current thread.
	 * run this method only after activity is started.
	 * 
	 * @return total currently used CPU time in nanoseconds
	 */
	public long getCurrentCpuTimeNanos() {
		return (cpuTimingSupported && (ownerThread != null)? tmbean.getThreadCpuTime(ownerThread.getThreadId()) : -1);
	}

	/**
	 * This method returns total CPU time in nanoseconds used since the start of this activity. If the activity has
	 * stopped the value returned is an elapsed CPU time since between activity start/stop calls. If the activity has
	 * not stopped yet, the value is the current used CPU time since the start until now.
	 * 
	 * @return total used CPU time in nanoseconds
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
	 * 
	 * @return total wall time of this activity
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

	@Override
	public String toString() {
		if (getSnapshotCount() > 0) {
			return super.toString() + "," + getSnapshots();
		} else {
			return super.toString();
		}
	}
}
