/*
 * Copyright 2014-2015 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.sink;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.config.Configurable;

/**
 * <p>
 * A pooled logger factory manages access to {@link PooledLogger} instances.
 * </p>
 * 
 * @see PooledLogger
 * 
 * @version $Revision: 1 $
 * 
 */
public class PooledLoggerFactoryImpl implements PooledLoggerFactory, Configurable {

	public static String DEFAULT_POOL_NAME = "default";
	private static int MAX_POOL_SIZE = Integer.getInteger("tnt4j.pooled.logger.pool", 5);
	private static int MAX_CAPACITY = Integer.getInteger("tnt4j.pooled.logger.capacity", 10000);

	private static final ConcurrentMap<String, PooledLogger> POOLED_LOGGERS = new ConcurrentHashMap<String, PooledLogger>();

	int poolSize, capacity;
	String poolName = DEFAULT_POOL_NAME;
	protected Map<String, Object> props = null;

	/**
	 * Create a default pooled logger factory
	 *
	 */
	public PooledLoggerFactoryImpl() {
	}
		
	/**
	 * Obtain an instance of pooled logger, which allows logging of events
	 * asynchronously by a thread pool.
	 *
	 * @param pName pool name
	 * @return pooled logger instance
	 * @see PooledLogger
	 */
	public static PooledLogger getPooledLogger(String pName) {
		return POOLED_LOGGERS.get(pName);
	}

	@Override
	public PooledLogger getPooledLogger() {
		return POOLED_LOGGERS.get(poolName);
	}

	@Override
    public Map<String, PooledLogger> getPooledLoggers() {
		Map<String, PooledLogger> copy = new HashMap<String, PooledLogger>();
		copy.putAll(POOLED_LOGGERS);
	    return POOLED_LOGGERS;
    }

	@Override
    public Map<String, Object> getConfiguration() {
	    return props;
    }

	@Override
    public void setConfiguration(Map<String, Object> settings) throws ConfigException {
		// obtain all optional attributes
		Object nameObj = props.get("Name");
		poolName = nameObj == null? DEFAULT_POOL_NAME: nameObj.toString();
		
		Object threadPool = props.get("Size");
		int poolSize = threadPool == null? MAX_POOL_SIZE: Integer.parseInt(threadPool.toString());
		
		Object qCapacity = props.get("Capacity");
		int capacity = qCapacity == null? MAX_CAPACITY: Integer.parseInt(qCapacity.toString());
		
		// create and register pooled logger instance if not yet available
		PooledLogger pooledLogger = new PooledLogger(poolName, poolSize, capacity);
		if (POOLED_LOGGERS.putIfAbsent(poolName, pooledLogger) == null) {
			pooledLogger.start();
		}		
    }
}
