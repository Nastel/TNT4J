/*
 * Copyright 2014-2021 JKOOL, LLC.
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

import com.jkoolcloud.tnt4j.TrackingLogger;
import com.jkoolcloud.tnt4j.core.*;
import com.jkoolcloud.tnt4j.utils.Useconds;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Implements application activity tracking and tracing entity, which keeps track of all related tracking events.
 * </p>
 * 
 * <p>
 * Represents a collection of related {@link TrackingEvent} instances considered to be an application activity. These
 * are generally delimited by START/STOP (or START/STOP(EXCEPTION)) calls. {@link TrackingEvent} instances can be
 * created using {@link TrackingLogger} or {@link Tracker}. Source activities should be started and stopped before being
 * reported using {@code TrackingLogger.tnt()} or {@code Tracker.tnt()} calls. {@link TrackingEvent} instances should be
 * registered with a given activity using {@code TrackingActivity.tnt()} call which adds and reports a given
 * {@link TrackingEvent} with the activity.
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
	private boolean reportStarts = false;
	private long lastEventNanos = 0;
	private TrackerImpl tracker = null;

	/**
	 * Creates a logical application activity object with the specified signature.
	 * 
	 * @param level
	 *            activity severity level
	 * @param name
	 *            activity name
	 * @throws NullPointerException
	 *             if the signature is {@code null}
	 * @throws IllegalArgumentException
	 *             if the signature is empty or is too long
	 * @see #setTrackingId(String)
	 */
	protected TrackingActivity(OpLevel level, String name) {
		super(null, name);
		setSeverity(level);
	}

	/**
	 * Creates a logical application activity object with the specified signature.
	 * 
	 * @param level
	 *            activity severity level
	 * @param name
	 *            activity name
	 * @param trk
	 *            {@link Tracker} instance associated with this activity
	 * @throws NullPointerException
	 *             if the signature is {@code null}
	 * @throws IllegalArgumentException
	 *             if the signature is empty or is too long
	 * @see #setTrackingId(String)
	 */
	protected TrackingActivity(OpLevel level, String name, TrackerImpl trk) {
		super(trk.newUUID(), name, trk.getSource());
		tracker = trk;
		setSeverity(level);
		setLocation(trk.getSource());
	}

	/**
	 * Creates a logical application activity object with the specified signature.
	 * 
	 * @param level
	 *            activity severity level
	 * @param name
	 *            activity name
	 * @param signature
	 *            activity signature
	 * @param trk
	 *            {@link Tracker} instance associated with this activity
	 * @throws NullPointerException
	 *             if the signature is {@code null}
	 * @throws IllegalArgumentException
	 *             if the signature is empty or is too long
	 * @see #setTrackingId(String)
	 */
	protected TrackingActivity(OpLevel level, String name, String signature, TrackerImpl trk) {
		super(signature, name, trk.getSource());
		tracker = trk;
		setSeverity(level);
		setLocation(trk.getSource());
	}

	/**
	 * Obtain {@link Tracker} instance associated with this activity
	 * 
	 * @return {@link Tracker} instance associated with this activity
	 */
	public Tracker getTracker() {
		return tracker;
	}

	private long getLastElapsedUsec() {
		return lastEventNanos > 0 ? (System.nanoTime() - lastEventNanos) / 1000 : 0;
	}

	/**
	 * Track and Trace given {@link TrackingEvent} instance correlated with current activity
	 * 
	 * @param event
	 *            tracking instance to be tracked
	 */
	public void tnt(TrackingEvent event) {
		if (isStopped()) {
			throw new IllegalStateException(
					"Activity already stopped: name=" + getName() + ", id=" + this.getTrackingId());
		}
		add(event);
		lastEventNanos = System.nanoTime();
		tracker.tnt(event);
	}

	/**
	 * Track and Trace given {@link com.jkoolcloud.tnt4j.core.Snapshot} instance correlated with current activity
	 * 
	 * @param snapshot
	 *            snapshot instance to be tracked
	 */
	public void tnt(Snapshot snapshot) {
		if (isStopped()) {
			throw new IllegalStateException(
					"Activity already stopped: name=" + getName() + ", id=" + this.getTrackingId());
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
	public void tnt(OpLevel severity, String opName, byte[] msg, Object... args) {
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
	public void tnt(OpLevel severity, OpType type, String opName, byte[] msg, Object... args) {
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
	public void tnt(OpLevel severity, OpType type, String opName, String cid, long elapsed, byte[] msg,
			Object... args) {
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
	public void tnt(OpLevel severity, OpType type, String opName, String cid, String tag, long elapsed, byte[] msg,
			Object... args) {
		TrackingEvent event = tracker.newEvent(severity, type, opName, cid, tag, msg, args);
		Throwable ex = Utils.getThrowable(args);

		long elapsedUsec = elapsed > 0 ? elapsed : getLastElapsedUsec();
		event.stop(ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS, 0, ex, Useconds.CURRENT.get(), elapsedUsec);
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
	public void tnt(OpLevel severity, String opName, String msg, Object... args) {
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
	public void tnt(OpLevel severity, OpType type, String opName, String msg, Object... args) {
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
	public void tnt(OpLevel severity, OpType type, String opName, String cid, long elapsed, String msg,
			Object... args) {
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
	public void tnt(OpLevel severity, OpType type, String opName, String cid, String tag, long elapsed, String msg,
			Object... args) {
		TrackingEvent event = tracker.newEvent(severity, type, opName, cid, tag, msg, args);
		Throwable ex = Utils.getThrowable(args);

		long elapsedUsec = elapsed > 0 ? elapsed : getLastElapsedUsec();
		event.stop(ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS, 0, ex, Useconds.CURRENT.get(), elapsedUsec);
		tnt(event);
	}

	/**
	 * Instruct activity to report start activity events into the underlying tracker associated with this activity. An
	 * tracking event will be logged when {@code start(..)} method call is made.
	 * 
	 * @param flag
	 *            enable reporting on start (default is false).
	 */
	public void reportStart(boolean flag) {
		reportStarts = flag;
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
	 * @param elapsedUsec
	 *            elapsed time in microseconds
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
		stop((ex != null ? ActivityStatus.EXCEPTION : ActivityStatus.END),
				(ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS), ex, 0);
	}

	/**
	 * Indicates that application activity has ended.
	 * 
	 * @param ex
	 *            exception associated with the activity or null if none.
	 * @param elapsedUsec
	 *            elapsed time in microseconds
	 * @see ActivityStatus
	 */
	public void stop(Throwable ex, long elapsedUsec) {
		stop((ex != null ? ActivityStatus.EXCEPTION : ActivityStatus.END),
				(ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS), ex, elapsedUsec);
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
	 * @param elapsedUsec
	 *            elapsed time in microseconds
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
	 * @param elapsedUsec
	 *            elapsed time in microseconds
	 * @see ActivityStatus
	 * @see OpCompCode
	 */
	public void stop(ActivityStatus status, OpCompCode ccode, Throwable ex, long elapsedUsec) {
		setException(ex);
		setStatus(status);
		setCompCode(ccode);
		super.stop(Useconds.CURRENT.get(), elapsedUsec);
	}

	@Override
	protected void onStart(long start) {
		tracker.push(this);
		if (reportStarts) {
			tracker.tnt(this);
		}
		long delta = (System.nanoTime() - start);
		super.onStart(start);
		tracker.countOverheadNanos(delta);
	}

	@Override
	protected void onStop(long start) {
		tracker.pop(this);
		super.onStop(start);
		long delta = (System.nanoTime() - start);
		tracker.countOverheadNanos(delta);
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
