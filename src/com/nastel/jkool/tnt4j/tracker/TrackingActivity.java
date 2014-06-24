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
	public	static final String SNAPSHOT_CATEGORY_GC = "GarbageCollector";

	public	static final String SNAPSHOT_CPU = "CPU";
	public	static final String SNAPSHOT_ACTIVITY = "Activity";
	public	static final String SNAPSHOT_MEMORY = "Memory";
	public	static final String SNAPSHOT_THREAD = "Thread";

	public	static final String DEFAULT_PROPERTY_LOAD_AVG = "SystemLoadAvg";
	public	static final String DEFAULT_PROPERTY_TOTAL_TIME = "TotalUsec";
	public	static final String DEFAULT_PROPERTY_TOTAL_USER_TIME = "TotalUserUsec";

	public	static final String DEFAULT_PROPERTY_COUNT = "Count";
	public	static final String DEFAULT_PROPERTY_DAEMON_COUNT = "DaemonCount";
	public	static final String DEFAULT_PROPERTY_STARTED_COUNT = "StartedCount";
	public	static final String DEFAULT_PROPERTY_PEAK_COUNT = "PeakCount";
	
	public	static final String DEFAULT_PROPERTY_BLOCKED_COUNT = "BlockedCount";
	public	static final String DEFAULT_PROPERTY_WAITED_COUNT = "WaitedCount";
	public	static final String DEFAULT_PROPERTY_BLOCKED_TIME = "BlockedTimeUsec";
	public	static final String DEFAULT_PROPERTY_WAITED_TIME = "WaitTimeUsec";

	public	static final String DEFAULT_PROPERTY_MAX_BYTES = "MaxBytes";
	public	static final String DEFAULT_PROPERTY_TOTAL_BYTES = "TotalBytes";
	public	static final String DEFAULT_PROPERTY_FREE_BYTES = "FreeBytes";
	public	static final String DEFAULT_PROPERTY_USED_BYTES = "UsedBytes";
	public	static final String DEFAULT_PROPERTY_USAGE = "Usage";
	
	public	static final String DEFAULT_PROPERTY_TIME = "Time";
	public	static final String DEFAULT_PROPERTY_VALID = "isValid";


	private long startCPUTime = 0, stopCPUTime = 0, 
		startBlockTime = 0, stopBlockTime = 0, startWaitTime = 0, stopWaitTime = 0,
		startBlockCount = 0, stopBlockCount = 0, startWaitCount = 0, stopWaitCount = 0;
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
		if (startStopCount == 1) {
			startCPUTime = cpuTimingSupported? tmbean.getCurrentThreadCpuTime(): 0;			
			ThreadInfo tinfo = tmbean.getThreadInfo(Thread.currentThread().getId());
			startBlockCount = tinfo.getBlockedCount();
			startWaitCount = tinfo.getWaitedCount();
			if (contTimingSupported) {
				startBlockTime = tinfo.getBlockedTime();
				startWaitTime = tinfo.getWaitedTime();
			}			
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
		super.stop(stopTime, stopTimeUsec);
		stopCPUTime = getCurrentCpuTimeNanos();
		
		if (appendProps && (startStopCount == 2)) {
			ThreadInfo tinfo = tmbean.getThreadInfo(Thread.currentThread().getId());
			stopBlockCount = tinfo.getBlockedCount();
			stopWaitCount = tinfo.getWaitedCount();
			if (contTimingSupported) {
				stopBlockTime = tinfo.getBlockedTime();
				stopWaitTime = tinfo.getWaitedTime();
			}
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
		PropertySnapshot activity = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_ACTIVITY);
		if (cpuTimingSupported) {
			long cpuUsed = getUsedCpuTimeNanos();
			activity.add(new Property(DEFAULT_PROPERTY_TOTAL_TIME, ((double)cpuUsed / 1000.0d)));
		}
		activity.add(new Property(DEFAULT_PROPERTY_BLOCKED_COUNT, (stopBlockCount - startBlockCount)));
		activity.add(new Property(DEFAULT_PROPERTY_WAITED_COUNT, (stopWaitCount - startWaitCount)));
		if (contTimingSupported) {
			activity.add(new Property(DEFAULT_PROPERTY_BLOCKED_TIME, ((stopBlockTime - startBlockTime)*1000)));
			activity.add(new Property(DEFAULT_PROPERTY_WAITED_TIME, ((stopWaitTime - startWaitTime)*1000)));
		}
		this.add(activity);
		
		PropertySnapshot cpu = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_CPU);
		double load = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
		if (load >= 0) {
			cpu.add(new Property(DEFAULT_PROPERTY_LOAD_AVG, load));
		}
		if (cpuTimingSupported) {
			cpu.add(DEFAULT_PROPERTY_COUNT, ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());
			cpu.add(new Property(DEFAULT_PROPERTY_TOTAL_TIME, ((double)stopCPUTime / 1000.0d)));
			cpu.add(new Property(DEFAULT_PROPERTY_TOTAL_USER_TIME, 
					((double)tmbean.getThreadUserTime(Thread.currentThread().getId())/ 1000.0d)));
		}
		this.add(cpu);
		
		PropertySnapshot thread = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_THREAD);		
		thread.add(new Property(DEFAULT_PROPERTY_COUNT, tmbean.getThreadCount()));
		thread.add(new Property(DEFAULT_PROPERTY_DAEMON_COUNT, tmbean.getDaemonThreadCount()));
		thread.add(new Property(DEFAULT_PROPERTY_STARTED_COUNT, tmbean.getTotalStartedThreadCount()));
		thread.add(new Property(DEFAULT_PROPERTY_PEAK_COUNT, tmbean.getPeakThreadCount()));
		thread.add(new Property(DEFAULT_PROPERTY_BLOCKED_COUNT, stopBlockCount));
		thread.add(new Property(DEFAULT_PROPERTY_WAITED_COUNT, stopWaitCount));
		if (contTimingSupported) {
			thread.add(new Property(DEFAULT_PROPERTY_BLOCKED_TIME, stopBlockTime*1000));
			thread.add(new Property(DEFAULT_PROPERTY_WAITED_TIME, stopWaitTime*1000));
		}
		this.add(thread);

		PropertySnapshot mem = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_MEMORY);
		long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long memPct = (long) (((double) usedMem / (double) Runtime.getRuntime().totalMemory()) * 100.0d);
		mem.add(new Property(DEFAULT_PROPERTY_MAX_BYTES, Runtime.getRuntime().maxMemory()));
		mem.add(new Property(DEFAULT_PROPERTY_TOTAL_BYTES, Runtime.getRuntime().totalMemory()));
		mem.add(new Property(DEFAULT_PROPERTY_FREE_BYTES, Runtime.getRuntime().freeMemory()));
		mem.add(new Property(DEFAULT_PROPERTY_USED_BYTES, usedMem));
		mem.add(new Property(DEFAULT_PROPERTY_USAGE, memPct));	
		this.add(mem);
		
		List<GarbageCollectorMXBean> gcList = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean gc: gcList) {
			PropertySnapshot gcSnap = new PropertySnapshot(SNAPSHOT_CATEGORY_GC, gc.getName());
			gcSnap.add(new Property(DEFAULT_PROPERTY_COUNT, gc.getCollectionCount()));
			gcSnap.add(new Property(DEFAULT_PROPERTY_TIME, gc.getCollectionTime()));
			gcSnap.add(new Property(DEFAULT_PROPERTY_VALID, gc.isValid()));
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
