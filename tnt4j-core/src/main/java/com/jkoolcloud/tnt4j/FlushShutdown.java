/*
 * Copyright 2014-2022 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j;

/**
 * <p>
 * This class implements a shutdown hook that automatically flushes and shuts down all registered trackers/sinks.
 * </p>
 * 
 * @version $Revision: 2 $
 */
public class FlushShutdown extends Thread {

	private boolean flush = false;

	/**
	 * Constructs a new instance of FlushShutdown hook thread.
	 */
	public FlushShutdown() {
		setName("TrackingLogger/FlushShutdownHook");
	}

	/**
	 * Sets flag indicating whether to run tracking loggers flush on JVM shutdown.
	 * 
	 * @param flush
	 *            flag indicating whether to run tracking loggers flush on JVM shutdown
	 */
	public void setFlush(boolean flush) {
		this.flush = flush;
	}

	@Override
	public void run() {
		if (flush) {
			TrackingLogger.flushAll();
		}
		TrackingLogger.shutdownAll();
	}
}