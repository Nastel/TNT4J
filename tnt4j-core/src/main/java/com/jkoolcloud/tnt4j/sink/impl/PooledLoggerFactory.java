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

import java.util.Map;

/**
 * Interface defines creation and management of {@link PooledLogger} instances.
 * 
 * @see PooledLogger
 * @version $Revision: 1 $
 * 
 */
public interface PooledLoggerFactory {
	/**
	 * Obtain a default instance of pooled logger, which allows logging of events asynchronously by a thread pool.
	 *
	 * @return pooled logger instance associated with this factory
	 * @see PooledLogger
	 */
	PooledLogger getPooledLogger();

	/**
	 * Obtain a map of all registered pooled loggers
	 *
	 * @return map of all registered pooled loggers
	 * @see PooledLogger
	 */
	Map<String, PooledLogger> getPooledLoggers();
}
