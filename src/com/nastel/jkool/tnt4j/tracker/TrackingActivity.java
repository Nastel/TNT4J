/*
 * Copyright (c) 2013 Nastel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Nastel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with Nastel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *
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
import com.nastel.jkool.tnt4j.core.Operation;
import com.nastel.jkool.tnt4j.core.Property;
import com.nastel.jkool.tnt4j.core.PropertySnapshot;

/**
 * <p>
 * Implements application activity tracking and tracing entity, which keeps track of all related tracking events.
 * </p>
 * 
 * <p>
 * Represents a collection of related <code>TrackingEvent</code> instances considered to be an
 * application activity. These are generally delimited by START/STOP (or START/STOP(EXCEPTION)) calls. 
 * <code>TrackingEvent</code> instances can be created using <code>TrackingLogger</code> or <code>Tracker</code>.
 * Source activities should be started and stopped before being reported using 
 * <code>TrackingLogger.tnt()</code> or <code>Tracker.tnt()</code> calls. <code>TrackingEvent</code> instances
 * should be registered with a given activity using <code>TrackingActivity.tnt()</code> call which adds and reports
 * a given <code>TrackingEvent</code> with the activity.
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
	private long startCPUTime = 0, stopCPUTime = 0;
	private int startStopCount = 0;
	ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
	private boolean appendProps = true, cpuTimingSupported = false, contTimingSupported = false;
	private Tracker tracker = null;

	/**
	 * Creates a logical application activity object with the specified signature.
	 * 
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
	protected TrackingActivity(String signature, Tracker trk) {
		super(signature, trk.getSource());
		tracker = trk;
		initJavaTiming();
	}

	private void initJavaTiming() {
		cpuTimingSupported = tmbean.isCurrentThreadCpuTimeSupported();
		if (cpuTimingSupported)
			tmbean.setThreadCpuTimeEnabled(cpuTimingSupported);
		contTimingSupported = tmbean.isThreadContentionMonitoringSupported();
		if (contTimingSupported)
			tmbean.setThreadContentionMonitoringEnabled(contTimingSupported);
	}

	/**
	 * Track and Trace given <code>TrackingEvent</code> instance within
	 * current activity
	 * 
	 */
	public void tnt(TrackingEvent event) {
		add(event);
		tracker.getEventSink().log(event);
	}

	@Override
	public void start(long startTime, int startTimeUsec) {
		super.start(startTime, startTimeUsec);
		startStopCount++;
		startCPUTime = cpuTimingSupported? tmbean.getCurrentThreadCpuTime(): 0;
		if (startStopCount == 1) {
			tracker.getEventSink().log(this);
		}
	}

	
	/**
	 * Indicates that application activity has ended
	 * normally without exception.
	 * 
	 */
    public void stop() {
	    stop(ActivityStatus.END, null);
    }

	
	/**
	 * Indicates that application activity has ended. 
	 * 
	 * @param status status with which activity ended.
	 * @param ex exception associated with the activity or null if none.
	 * @see ActivityStatus
	 */
   public void stop(ActivityStatus status, Throwable ex) {
		setException(ex);
		setStatus(status);
		startStopCount++;
		super.stop();
	}
	
	/**
	 * Indicates that application activity has ended. 
	 * 
	 * @param status status with which activity ended.
	 * @param ccode completion code of the activity.
	 * @param ex exception associated with the activity or null if none.
	 * @see ActivityStatus
	 * @see OpCompCode
	 */
  public void stop(ActivityStatus status, OpCompCode ccode, Throwable ex) {
		setException(ex);
		setStatus(status);
		this.setCompCode(ccode);
		startStopCount++;
		super.stop();
	}
	
	/**
	 * Enable/disable appending of default snapshot when
	 * activity stops.
	 * 
	 * @param flag append default snapshot ("DEFAULT")
	 */
	public TrackingActivity appendDefaultSnapshot(boolean flag) {
		appendProps = flag;
		return this;
	}
	
	@Override
	public void stop(long stopTime, int stopTimeUsec) {
		stopCPUTime = cpuTimingSupported? tmbean.getCurrentThreadCpuTime(): 0;
		super.stop(stopTime, stopTimeUsec);
		if (appendProps && (startStopCount == 2)) {
			takePropertySnapshot();
		}
	}

	/**
	 * This method appends default set of properties when activity timing stops.
	 * Developers should override this method to add user defined set of properties.
	 * 
	 */
	protected void takePropertySnapshot() {
		PropertySnapshot snap = new PropertySnapshot("JAVA", "SYSTEM");
		if (cpuTimingSupported) {
			double cpuUsed = (double) (stopCPUTime - startCPUTime) / 1000.0f;
			double cpuPctUsed = getElapsedTime() > 0? ((((double) cpuUsed) / (double)getElapsedTime()) * 100.0f): 0;
			snap.add(new Property("CPU_USED_USEC", cpuUsed));
			snap.add(new Property("CPU_TOTAL_TIME_USEC", stopCPUTime / 1000.0f));
			snap.add(new Property("CPU_PERCENT_USAGE", cpuPctUsed));
		}
		ThreadInfo tinfo = tmbean.getThreadInfo(Thread.currentThread().getId());
		snap.add(new Property("THREAD_BLOCKED_COUNT", tinfo.getBlockedCount()));
		snap.add(new Property("THREAD_WAITED_COUNT", tinfo.getWaitedCount()));
		if (contTimingSupported) {
			snap.add(new Property("THREAD_BLOCKED_TIME_USEC", tinfo.getBlockedTime()*1000));
			snap.add(new Property("THREAD_WAITED_TIME_USEC", tinfo.getWaitedTime()*1000));
		}
		long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long memPct = (long) (((double) usedMem / (double) Runtime.getRuntime().totalMemory()) * 100.0f);
		snap.add(new Property("MEMORY_MAX_BYTES", Runtime.getRuntime().maxMemory()));
		snap.add(new Property("MEMORY_TOTAL_BYTES", Runtime.getRuntime().totalMemory()));
		snap.add(new Property("MEMORY_FREE_BYTES", Runtime.getRuntime().freeMemory()));
		snap.add(new Property("MEMORY_USED_BYTES", usedMem));
		snap.add(new Property("MEMORY_PERCENT_USAGE", memPct));	
		this.add(snap);
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
