/*
 * Copyright 2014-2018 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.sink.impl;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.SinkLogEvent;

/**
 * This class implements a task for asynchronous handling and delivery of delayed logging events to various event sinks.
 * Delayed events are those that could not be handled due to error or some other non-deliverable condition.
 *
 * @version $Revision: 1 $
 *
 * @see com.jkoolcloud.tnt4j.sink.impl.PooledLogger#takeDelayedEvent()
 * @see com.jkoolcloud.tnt4j.sink.impl.PooledLogger#put(com.jkoolcloud.tnt4j.sink.SinkLogEvent)
 */
class DelayedLoggingTask extends AbstractPoolLoggingTask {
	protected DelayedLoggingTask(PooledLogger logger) {
		super(logger);
	}

	@Override
	public void run() {
		try {
			while (!isCanceled()) {
				SinkLogEvent event = pooledLogger.takeDelayedEvent();
				if (event.getSignalType() == SinkLogEvent.SIGNAL_TERMINATE) {
					cancel();
				} else {
					pooledLogger.put(event);
				}
			}
		} catch (Throwable e) {
			PooledLogger.logger.log(OpLevel.WARNING,
					"Interrupted during delayed processing: shutting down: error.count={0}",
					pooledLogger.exceptionCount.get(), e);
		}
	}
}