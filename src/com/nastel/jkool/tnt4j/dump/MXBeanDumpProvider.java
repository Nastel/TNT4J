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

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

/**
 * <p>
 * This class is a dump provider for dumping java statistics based on ManagementFactory and includes:
 * OperatingSystemMXBean, MemoryMXBean, MemoryPoolMXBean, GarbageCollectorMXBean, ClassLoadingMXBean, ThreadMXBean,
 * CompilationMXBean
 * 
 * </p>
 * 
 * @see DumpCollection
 * 
 * @version $Revision: 2 $
 * 
 */
public class MXBeanDumpProvider extends DefaultDumpProvider {

	/**
	 * Create a new java statistics dump provider with a given name.
	 * 
	 *@param name
	 *            provider name
	 */
	public MXBeanDumpProvider(String name) {
		super(name, "System");
	}

	@Override
	public DumpCollection getDump() {
		Dump dump = new Dump("JavaMXStats", this);
		OperatingSystemMXBean oxBean = ManagementFactory.getOperatingSystemMXBean();

		dump.add("os.xbean.name", oxBean.getName());
		dump.add("os.xbean.version", oxBean.getVersion());
		dump.add("os.xbean.arch", oxBean.getArch());
		dump.add("os.xbean.cpus", oxBean.getAvailableProcessors());

		dump.add("memory.free.bytes", Runtime.getRuntime().freeMemory());
		dump.add("memory.total.bytes", Runtime.getRuntime().totalMemory());
		dump.add("memory.max.bytes", Runtime.getRuntime().maxMemory());

		MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
		dump.add("memory.xbean.heap.usage", mxBean.getHeapMemoryUsage());
		dump.add("memory.xbean.nonheap.usage", mxBean.getNonHeapMemoryUsage());
		dump.add("memory.xbean.pending.finalize.count", mxBean.getObjectPendingFinalizationCount());

		CompilationMXBean cpBean = ManagementFactory.getCompilationMXBean();
		dump.add(cpBean.getName() + ".xbean.total.compile.time", cpBean.getTotalCompilationTime());

		List<MemoryPoolMXBean> mxPool = ManagementFactory.getMemoryPoolMXBeans();
		for (MemoryPoolMXBean mpool : mxPool) {
			dump.add(mpool.getName() + ".xbean.type", mpool.getType());
			dump.add(mpool.getName() + ".xbean.usage", mpool.getUsage());
			dump.add(mpool.getName() + ".xbean.peak.usage", mpool.getPeakUsage());
			dump.add(mpool.getName() + ".xbean.collection.usage", mpool.getCollectionUsage());
		}

		List<GarbageCollectorMXBean> mf = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean gcbean : mf) {
			dump.add(gcbean.getName() + ".xbean.valid", gcbean.isValid());
			dump.add(gcbean.getName() + ".xbean.collection.count", gcbean.getCollectionCount());
			dump.add(gcbean.getName() + ".xbean.collection.time", gcbean.getCollectionTime());
		}

		ClassLoadingMXBean cxBean = ManagementFactory.getClassLoadingMXBean();
		dump.add("classloader.xbean.loaded.count", cxBean.getLoadedClassCount());
		dump.add("classloader.xbean.total.loaded.count", cxBean.getTotalLoadedClassCount());
		dump.add("classloader.xbean.total.unloaded.count", cxBean.getUnloadedClassCount());

		ThreadMXBean txBean = ManagementFactory.getThreadMXBean();
		dump.add("thread.xbean.count", txBean.getThreadCount());
		dump.add("thread.xbean.daemon.count", txBean.getDaemonThreadCount());
		dump.add("thread.xbean.peak.count", txBean.getPeakThreadCount());
		dump.add("thread.xbean.started.count", txBean.getTotalStartedThreadCount());

		return dump;
	}
}
