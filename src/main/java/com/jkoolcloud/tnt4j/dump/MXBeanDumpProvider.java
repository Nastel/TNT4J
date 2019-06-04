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
package com.jkoolcloud.tnt4j.dump;

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
