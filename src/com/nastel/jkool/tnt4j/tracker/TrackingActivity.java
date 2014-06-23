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
	public	static final String DEFAULT_SNAPSHOT_CATEGORY = "Java";
	public	static final String DEFAULT_SNAPSHOT_NAME = "System";
	
	public	static final String DEFAULT_PROPERTY_SYSTEM_LOAD_AVG = "SystemLoadAvg";
	public	static final String DEFAULT_PROPERTY_ACTIVITY_CPU_USED = "ActivityCpuUsedUsec";
	public	static final String DEFAULT_PROPERTY_CPU_TOTAL_TIME = "ThreadCpuTotalUsec";
	public	static final String DEFAULT_PROPERTY_CPU_TOTAL_USER_TIME = "ThreadCpuUserTotalUsec";

	public	static final String DEFAULT_PROPERTY_THREAD_COUNT = "ThreadCount";
	public	static final String DEFAULT_PROPERTY_THREAD_DAEMON_COUNT = "ThreadDaemonCount";
	public	static final String DEFAULT_PROPERTY_THREAD_STARTED_COUNT = "ThreadStartedCount";
	public	static final String DEFAULT_PROPERTY_THREAD_PEAK_COUNT = "ThreadPeakCount";
	
	public	static final String DEFAULT_PROPERTY_THREAD_BLOCKED_COUNT = "ThreadBlockedCount";
	public	static final String DEFAULT_PROPERTY_THREAD_WAITED_COUNT = "ThreadWaitedCount";
	public	static final String DEFAULT_PROPERTY_THREAD_BLOCKED_TIME = "ThreadBlockedTimeUsec";
	public	static final String DEFAULT_PROPERTY_THREAD_WAITED_TIME = "ThreadWaitTimeUsec";

	public	static final String DEFAULT_PROPERTY_MEMORY_MAX_BYTES = "MemoryMaxBytes";
	public	static final String DEFAULT_PROPERTY_MEMORY_TOTAL_BYTES = "MemoryTotalBytes";
	public	static final String DEFAULT_PROPERTY_MEMORY_FREE_BYTES = "MemoryFreeBytes";
	public	static final String DEFAULT_PROPERTY_MEMORY_USED_BYTES = "MemoryUsedBytes";
	public	static final String DEFAULT_PROPERTY_MEMORY_PERCENT_USAGE = "MemoryUsage";

	private long startCPUTime = 0, stopCPUTime = 0;
	private int startStopCount = 0;
	private ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
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

	/**
	 * Creates a logical application activity object with the specified signature.
	 * 
	 * @param signature
	 *            activity signature
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
	protected TrackingActivity(String signature, String name, Tracker trk) {
		super(signature, name, trk.getSource());
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
		tracker.tnt(event);
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
	 * activity stops. When set to true <code>takePropertySnapshot()</code>
	 * is called when activity is stopped using <code>stop()</code> method. 
	 * This can be useful when you want to append a defined set of default
	 * properties every time an activity is stopped.
	 * 
	 * @param flag append default snapshot with name "SYSTEM"
	 */
	public TrackingActivity appendDefaultSnapshot(boolean flag) {
		appendProps = flag;
		return this;
	}
	
	@Override
	public void stop(long stopTime, int stopTimeUsec) {
		stopCPUTime = getCurrentCpuTimeNanos();
		super.stop(stopTime, stopTimeUsec);
		if (appendProps && (startStopCount == 2)) {
			appendProperties();
		}
	}

	/**
	 * This method returns total CPU time in nanoseconds currently used by the 
	 * current thread.
	 */
	public long getCurrentCpuTimeNanos() {
		return (cpuTimingSupported? tmbean.getCurrentThreadCpuTime(): 0);
	}
	
	/**
	 * This method returns total CPU time in nanoseconds used since the start of this activity.
	 * If the activity has stopped the value returned is an elapsed CPU time since between
	 * activity start/stop calls. If the activity has not stopped yet, the value is the current used
	 * CPU time since the start until now.
	 */
	public long getUsedCpuTimeNanos() {
		if (stopCPUTime > 0) return (stopCPUTime - startCPUTime);
		else if (startCPUTime > 0) {
			return (getCurrentCpuTimeNanos() - startCPUTime);
		} else return 0;
	}
	
	/**
	 * This method appends a default set of properties when activity timing stops.
	 * Developers should override this method to add user defined set of properties.
	 * By default this method appends default set of properties defined by
	 * <code>DEFAULT_PROPERTY_XXX</code> property values. Example:
	 * <code>TrackingActivity.DEFAULT_PROPERTY_CPU_TOTAL_TIME</code>.
	 */
	protected void appendProperties() {
		PropertySnapshot snap = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, DEFAULT_SNAPSHOT_NAME);
		double load = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
		if (load >= 0) {
			snap.add(new Property(DEFAULT_PROPERTY_SYSTEM_LOAD_AVG, load));
		}
		if (cpuTimingSupported) {
			long cpuUsed = getUsedCpuTimeNanos();
			snap.add(new Property(DEFAULT_PROPERTY_ACTIVITY_CPU_USED, ((double)cpuUsed / 1000.0d)));
			snap.add(new Property(DEFAULT_PROPERTY_CPU_TOTAL_TIME, ((double)stopCPUTime / 1000.0d)));
			snap.add(new Property(DEFAULT_PROPERTY_CPU_TOTAL_USER_TIME, 
					((double)tmbean.getThreadUserTime(Thread.currentThread().getId())/ 1000.0d)));
		}
		ThreadInfo tinfo = tmbean.getThreadInfo(Thread.currentThread().getId());
		snap.add(new Property(DEFAULT_PROPERTY_THREAD_COUNT, tmbean.getThreadCount()));
		snap.add(new Property(DEFAULT_PROPERTY_THREAD_DAEMON_COUNT, tmbean.getDaemonThreadCount()));
		snap.add(new Property(DEFAULT_PROPERTY_THREAD_STARTED_COUNT, tmbean.getTotalStartedThreadCount()));
		snap.add(new Property(DEFAULT_PROPERTY_THREAD_PEAK_COUNT, tmbean.getPeakThreadCount()));
		snap.add(new Property(DEFAULT_PROPERTY_THREAD_BLOCKED_COUNT, tinfo.getBlockedCount()));
		snap.add(new Property(DEFAULT_PROPERTY_THREAD_WAITED_COUNT, tinfo.getWaitedCount()));
		if (contTimingSupported) {
			snap.add(new Property(DEFAULT_PROPERTY_THREAD_BLOCKED_TIME, tinfo.getBlockedTime()*1000));
			snap.add(new Property(DEFAULT_PROPERTY_THREAD_WAITED_TIME, tinfo.getWaitedTime()*1000));
		}
		long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long memPct = (long) (((double) usedMem / (double) Runtime.getRuntime().totalMemory()) * 100.0d);
		snap.add(new Property(DEFAULT_PROPERTY_MEMORY_MAX_BYTES, Runtime.getRuntime().maxMemory()));
		snap.add(new Property(DEFAULT_PROPERTY_MEMORY_TOTAL_BYTES, Runtime.getRuntime().totalMemory()));
		snap.add(new Property(DEFAULT_PROPERTY_MEMORY_FREE_BYTES, Runtime.getRuntime().freeMemory()));
		snap.add(new Property(DEFAULT_PROPERTY_MEMORY_USED_BYTES, usedMem));
		snap.add(new Property(DEFAULT_PROPERTY_MEMORY_PERCENT_USAGE, memPct));	
		this.add(snap);
		
		List<GarbageCollectorMXBean> gcList = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean gc: gcList) {
			PropertySnapshot gcSnap = new PropertySnapshot("GarbageCollector", gc.getName());
			gcSnap.add(new Property("GcCount", gc.getCollectionCount()));
			gcSnap.add(new Property("GcTime", gc.getCollectionTime()));
			gcSnap.add(new Property("isValid", gc.isValid()));
			this.add(gcSnap);
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
