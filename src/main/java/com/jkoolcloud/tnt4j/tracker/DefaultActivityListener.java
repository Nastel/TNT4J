/*
 * Copyright 2014-2019 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.tracker;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.jkoolcloud.tnt4j.core.*;

/**
 * This class implements a simple activity listener {@code ActivityListener} which enriches activities with JVM, thread
 * performance statistics when activity ends.
 *
 * @see ActivityListener
 * @see Activity
 *
 * @version $Revision: 5 $
 *
 */
public class DefaultActivityListener implements ActivityListener {
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

	private static ConcurrentHashMap<Activity, ThreadContext> THREAD_CONTEXT = new ConcurrentHashMap<>();
	protected static ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
	protected static boolean cpuTimingSupported = tmbean.isThreadCpuTimeEnabled() && tmbean.isThreadCpuTimeSupported();
	protected static boolean contTimingSupported = tmbean.isThreadContentionMonitoringSupported();

	public DefaultActivityListener() {
	}

	@Override
	public void started(Activity activity) {
		THREAD_CONTEXT.putIfAbsent(activity, new ThreadContext());
	}

	/**
	 * This method appends a default set of properties when activity timing stops. Developers should override this
	 * method to add user defined set of properties. By default this method appends default set of properties defined by
	 * {@code DEFAULT_PROPERTY_XXX} property values. Example: {@code DEFAULT_PROPERTY_CPU_TOTAL_TIME}.
	 */
	@Override
	public void stopped(Activity activity) {
		long start = System.nanoTime();
		ThreadContext ctx = THREAD_CONTEXT.remove(activity);
		if (ctx != null) {
			ctx.end();
		}

		PropertySnapshot cpu = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_CPU, activity.getSeverity());
		double load = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
		if (load >= 0) {
			cpu.add(new Property(DEFAULT_PROPERTY_LOAD_AVG, load, ValueTypes.VALUE_TYPE_GAUGE));
		}
		if (ctx != null && cpuTimingSupported) {
			cpu.add(DEFAULT_PROPERTY_COUNT, ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());
			cpu.add(new Property(DEFAULT_PROPERTY_CPU_TIME,
					((double) tmbean.getThreadCpuTime(ctx.ownerThread.getThreadId()) / 1000.0d),
					ValueTypes.VALUE_TYPE_AGE_USEC));
			cpu.add(new Property(DEFAULT_PROPERTY_TOTAL_USER_TIME,
					((double) tmbean.getThreadUserTime(ctx.ownerThread.getThreadId()) / 1000.0d),
					ValueTypes.VALUE_TYPE_AGE_USEC));
		}
		activity.add(cpu);

		PropertySnapshot thread = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_THREAD,
				activity.getSeverity());
		thread.add(new Property(DEFAULT_PROPERTY_COUNT, tmbean.getThreadCount(), ValueTypes.VALUE_TYPE_GAUGE));
		thread.add(new Property(DEFAULT_PROPERTY_DAEMON_COUNT, tmbean.getDaemonThreadCount(),
				ValueTypes.VALUE_TYPE_GAUGE));
		thread.add(new Property(DEFAULT_PROPERTY_STARTED_COUNT, tmbean.getTotalStartedThreadCount(),
				ValueTypes.VALUE_TYPE_COUNTER));
		thread.add(new Property(DEFAULT_PROPERTY_PEAK_COUNT, tmbean.getPeakThreadCount(), ValueTypes.VALUE_TYPE_GAUGE));
		if (ctx != null) {
			thread.add(new Property(DEFAULT_PROPERTY_BLOCKED_COUNT, ctx.ownerThread.getBlockedCount(),
					ValueTypes.VALUE_TYPE_COUNTER));
			thread.add(new Property(DEFAULT_PROPERTY_WAITED_COUNT, ctx.ownerThread.getWaitedCount(),
					ValueTypes.VALUE_TYPE_COUNTER));
		}
		if (ctx != null && contTimingSupported) {
			thread.add(new Property(DEFAULT_PROPERTY_BLOCKED_TIME, ctx.ownerThread.getBlockedTime() * 1000,
					ValueTypes.VALUE_TYPE_AGE_USEC));
			thread.add(new Property(DEFAULT_PROPERTY_WAITED_TIME, ctx.ownerThread.getWaitedTime() * 1000,
					ValueTypes.VALUE_TYPE_AGE_USEC));
		}
		activity.add(thread);

		PropertySnapshot mem = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_MEMORY, activity.getSeverity());
		long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		double memPct = (double) ((double) usedMem / (double) Runtime.getRuntime().totalMemory());
		mem.add(new Property(DEFAULT_PROPERTY_MAX_BYTES, Runtime.getRuntime().maxMemory(),
				ValueTypes.VALUE_TYPE_SIZE_BYTE));
		mem.add(new Property(DEFAULT_PROPERTY_TOTAL_BYTES, Runtime.getRuntime().totalMemory(),
				ValueTypes.VALUE_TYPE_SIZE_BYTE));
		mem.add(new Property(DEFAULT_PROPERTY_FREE_BYTES, Runtime.getRuntime().freeMemory(),
				ValueTypes.VALUE_TYPE_SIZE_BYTE));
		mem.add(new Property(DEFAULT_PROPERTY_USED_BYTES, usedMem, ValueTypes.VALUE_TYPE_SIZE_BYTE));
		mem.add(new Property(DEFAULT_PROPERTY_USAGE, memPct, ValueTypes.VALUE_TYPE_PERCENT));
		activity.add(mem);

		List<GarbageCollectorMXBean> gcList = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean gc : gcList) {
			PropertySnapshot gcSnap = new PropertySnapshot(SNAPSHOT_CATEGORY_GC, gc.getName(), activity.getSeverity());
			gcSnap.add(new Property(DEFAULT_PROPERTY_COUNT, gc.getCollectionCount(), ValueTypes.VALUE_TYPE_COUNTER));
			gcSnap.add(new Property(DEFAULT_PROPERTY_TIME, gc.getCollectionTime(), ValueTypes.VALUE_TYPE_AGE_MSEC));
			gcSnap.add(new Property(DEFAULT_PROPERTY_VALID, gc.isValid()));
			activity.add(gcSnap);
		}

		if (ctx != null && ctx.startCPUTime > 0) {
			PropertySnapshot snapshot = new PropertySnapshot(DEFAULT_SNAPSHOT_CATEGORY, SNAPSHOT_ACTIVITY,
					activity.getSeverity());
			if (cpuTimingSupported) {
				long cpuUsed = getUsedCpuTimeNanos(ctx);
				double cpuUsec = ((double) cpuUsed / 1000.0d);
				snapshot.add(new Property(DEFAULT_PROPERTY_CPU_TIME, cpuUsec));
				long slackTime = (long) (activity.getElapsedTimeUsec() - activity.getWaitTimeUsec() - cpuUsec);
				snapshot.add(new Property(DEFAULT_PROPERTY_SLACK_TIME, slackTime, ValueTypes.VALUE_TYPE_AGE_USEC));
				snapshot.add(new Property(DEFAULT_PROPERTY_WALL_TIME, (cpuUsec + activity.getWaitTimeUsec()),
						ValueTypes.VALUE_TYPE_AGE_USEC));
			}
			snapshot.add(new Property(DEFAULT_PROPERTY_BLOCKED_COUNT, (ctx.stopBlockCount - ctx.startBlockCount),
					ValueTypes.VALUE_TYPE_GAUGE));
			snapshot.add(new Property(DEFAULT_PROPERTY_WAITED_COUNT, (ctx.stopWaitCount - ctx.startWaitCount),
					ValueTypes.VALUE_TYPE_GAUGE));
			if (contTimingSupported) {
				snapshot.add(new Property(DEFAULT_PROPERTY_BLOCKED_TIME,
						((ctx.stopBlockTime - ctx.startBlockTime) * 1000), ValueTypes.VALUE_TYPE_AGE_USEC));
				snapshot.add(new Property(DEFAULT_PROPERTY_WAITED_TIME, ((ctx.stopWaitTime - ctx.startWaitTime) * 1000),
						ValueTypes.VALUE_TYPE_AGE_USEC));
			}
			ctx.overHeadTimeNano += (System.nanoTime() - start);
			snapshot.add(new Property(DEFAULT_PROPERTY_OVERHEAD_TIME, ((double) ctx.overHeadTimeNano / 1000.0d),
					ValueTypes.VALUE_TYPE_AGE_USEC));
			activity.add(snapshot);
		}
	}

	/**
	 * This method returns total CPU time in nanoseconds currently used by the current thread context. run this method
	 * only after activity is started.
	 * 
	 * @param ctx
	 *            thread context
	 * @return total currently used CPU time in nanoseconds
	 */
	protected static long getCurrentCpuTimeNanos(ThreadContext ctx) {
		return (cpuTimingSupported && (ctx.ownerThread != null) ? tmbean.getThreadCpuTime(ctx.ownerThread.getThreadId())
				: -1);
	}

	/**
	 * This method returns total CPU time in nanoseconds used since the start of this activity. If the activity has
	 * stopped the value returned is an elapsed CPU time since between activity start/stop calls. If the activity has
	 * not stopped yet, the value is the current used CPU time since the start until now.
	 * 
	 * @param ctx
	 *            thread context
	 * @return total used CPU time in nanoseconds
	 */
	protected static long getUsedCpuTimeNanos(ThreadContext ctx) {
		if (ctx.stopCPUTime > 0) {
			return (ctx.stopCPUTime - ctx.startCPUTime);
		} else if (ctx.startCPUTime > 0) {
			return (getCurrentCpuTimeNanos(ctx) - ctx.startCPUTime);
		} else {
			return -1;
		}
	}
}

class ThreadContext {
	protected ThreadInfo ownerThread;
	protected boolean ended = false;
	protected long startCPUTime = 0;
	protected long stopCPUTime = 0;
	protected long startBlockTime = 0;
	protected long stopBlockTime = 0;
	protected long startWaitTime = 0;
	protected long stopWaitTime = 0;
	protected long startBlockCount = 0;
	protected long stopBlockCount = 0;
	protected long startWaitCount = 0;
	protected long stopWaitCount = 0;
	protected long overHeadTimeNano = 0;

	protected ThreadContext() {
		long start = System.nanoTime();
		ownerThread = DefaultActivityListener.tmbean.getThreadInfo(Thread.currentThread().getId());
		startCPUTime = DefaultActivityListener.cpuTimingSupported
				? DefaultActivityListener.tmbean.getThreadCpuTime(ownerThread.getThreadId()) : 0;
		startBlockCount = ownerThread.getBlockedCount();
		startWaitCount = ownerThread.getWaitedCount();
		if (DefaultActivityListener.contTimingSupported) {
			startBlockTime = ownerThread.getBlockedTime();
			startWaitTime = ownerThread.getWaitedTime();
		}
		overHeadTimeNano += (System.nanoTime() - start);
	}

	protected void end() {
		if (!ended) {
			ended = true;
			long start = System.nanoTime();
			if (startCPUTime > 0) {
				if (DefaultActivityListener.contTimingSupported) {
					stopBlockTime = ownerThread.getBlockedTime();
					stopWaitTime = ownerThread.getWaitedTime();
				}
				stopCPUTime = DefaultActivityListener.getCurrentCpuTimeNanos(this);
			}
			stopBlockCount = ownerThread.getBlockedCount();
			stopWaitCount = ownerThread.getWaitedCount();
			overHeadTimeNano += (System.nanoTime() - start);
		}
	}
}