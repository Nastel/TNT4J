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
package com.jkoolcloud.tnt4j.utils;

import java.util.concurrent.ThreadFactory;


/**
 * This class implements a thread factory with a naming convention assigned
 * to each new thread.
 *
 * @version $Revision: 1 $
 */
public class NamedThreadFactory implements ThreadFactory {
	int count = 0;
	String prefix;
	boolean daemon = true;

    /**
     * Create a thread factory
     *
     * @param pfix thread name prefix
     */
	public NamedThreadFactory(String pfix) {
		this(pfix, true);
	}

    /**
     * Create a thread factory
     *
     * @param pfix thread name prefix
     * @param daemon status assigned to each thread
     */
	NamedThreadFactory(String pfix, boolean daemon) {
		this.prefix = pfix;
		this.daemon = daemon;
	}

	@Override
    public Thread newThread(Runnable r) {
		Thread task = new Thread(r, prefix + count++);
		task.setDaemon(daemon);
		return task;
    }
}