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
package com.jkoolcloud.tnt4j.dump;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * <p>
 * This class is a dump provider for dumping deadlocked threads
 * 
 * </p>
 * 
 * @see DumpCollection
 * 
 * @version $Revision: 3 $
 * 
 */
public class ThreadDeadlockDumpProvider extends ThreadDumpProvider {

	/**
	 * Create a new dump provider with a given name to dump out deadlocked threads if any found.
	 * 
	 * @param name
	 *            provider name
	 */
	public ThreadDeadlockDumpProvider(String name) {
		super(name);
	}

	@Override
	public DumpCollection getDump() {
		Dump dump = new Dump("JavaDeadlockedThreads", this);
		ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();

		long[] dead = tmbean.findMonitorDeadlockedThreads();
		dump.add("java.thread.deadlock.count", dead == null ? 0 : dead.length);

		if (dead != null) {
			ThreadInfo[] tinfos = tmbean.getThreadInfo(dead, Integer.MAX_VALUE);
			for (ThreadInfo ti : tinfos) {
				dump.add(ti.getThreadName() + "-" + ti.getThreadId(), ti);
			}
		}
		return dump;
	}
}
