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
package com.jkoolcloud.tnt4j.dump;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * <p>
 * This class is a dump provider for dumping thread stack
 * 
 * </p>
 * 
 * @see DumpCollection
 * 
 * @version $Revision: 3 $
 * 
 */

public class ThreadDumpProvider extends DefaultDumpProvider {

	/**
	 * Create a new dump provider with a given name to dump thread stack
	 * 
	 *@param name
	 *            provider name
	 */
	public ThreadDumpProvider(String name) {
		super(name, "System");
	}

	@Override
	public DumpCollection getDump() {
		Dump dump = new Dump("JavaThreadStack", this);

		ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
		boolean contentionSupported = tmbean.isThreadContentionMonitoringSupported()
		        && tmbean.isThreadContentionMonitoringEnabled();
		boolean cputSupported = tmbean.isCurrentThreadCpuTimeSupported() && tmbean.isThreadCpuTimeEnabled();
		dump.add("java.thread.contention.supported", contentionSupported);
		dump.add("java.thread.cpu.supported", cputSupported);

		long[] dead = tmbean.findMonitorDeadlockedThreads();
		dump.add("java.thread.deadlock.count", dead == null ? 0 : dead.length);

		long[] tids = tmbean.getAllThreadIds();
		ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
		for (ThreadInfo ti : tinfos) {
			dump.add(ti.getThreadName() + "-" + String.valueOf(ti.getThreadId()), ti);
		}
		return dump;
	}
}
