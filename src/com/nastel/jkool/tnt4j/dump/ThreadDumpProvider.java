/*
 * Copyright (c) 2014 Nastel Technologies, Inc. All Rights Reserved.
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
package com.nastel.jkool.tnt4j.dump;

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
