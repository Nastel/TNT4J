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
package com.jkoolcloud.tnt4j;

/**
 * <p> 
 * This class implements a shutdown hook that automatically
 * dumps internal state of all registered dump providers.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 */
public class DumpHook extends Thread implements Thread.UncaughtExceptionHandler {
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		TrackingLogger.dumpState(e);
	}

	@Override
	public void run() {
		setName("TrackingLogger/DumpHook");
		TrackingLogger.dumpState(new Exception("VM-Shutdown"));
	}
}