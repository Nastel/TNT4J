/*
 * Copyright 2014-2023 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.sink.impl;

/**
 * Base class for handling and delivery of logging events to various event sinks.
 *
 * @version $Revision: 1 $
 *
 * @see PooledLogger
 */
public abstract class AbstractPoolLoggingTask implements Runnable {
	PooledLogger pooledLogger;

	private boolean canceled;

	/**
	 * Constructs a new AbstractPoolLoggingTask instance.
	 *
	 * @param logger
	 *            pooled logger instance to be used by this task
	 */
	protected AbstractPoolLoggingTask(PooledLogger logger) {
		pooledLogger = logger;
	}

	/**
	 * Sets this task as canceled.
	 */
	public void cancel() {
		canceled = true;
	}

	/**
	 * Checks if task is canceled.
	 *
	 * @return {@code true} if task is canceled, {@code false} - otherwise
	 */
	public boolean isCanceled() {
		return canceled;
	}
}
