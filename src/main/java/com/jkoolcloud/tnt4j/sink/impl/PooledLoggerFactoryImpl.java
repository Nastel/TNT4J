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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.utils.Utils;

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

	public static final String DEFAULT_POOL_NAME = "default";
	private static final int MAX_POOL_SIZE = Integer.getInteger("tnt4j.pooled.logger.pool", 4);
	private static final int MAX_CAPACITY = Integer.getInteger("tnt4j.pooled.logger.capacity", 10000);
	private static final int RETRY_INTERVAL = Integer.getInteger("tnt4j.pooled.logger.retry.interval", 5000);
	private static final boolean DROP_ON_EXCEPTION = Boolean.getBoolean("tnt4j.pooled.logger.drop.on.error");

	private static final ConcurrentMap<String, PooledLogger> POOLED_LOGGERS = new ConcurrentHashMap<>();

	int poolSize = MAX_POOL_SIZE;
	int capacity = MAX_CAPACITY;
	int retryInterval = RETRY_INTERVAL;
	boolean dropOnError = DROP_ON_EXCEPTION;
	String poolName = DEFAULT_POOL_NAME;
	protected Map<String, ?> props;

	/**
	 * Create a default pooled logger factory.
	 */
	public PooledLoggerFactoryImpl() {
	}

	/**
	 * Obtain an instance of pooled logger, which allows logging of events asynchronously by a thread pool.
	 *
	 * @param pName
	 *            pool name
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
		Map<String, PooledLogger> copy = new HashMap<>();
		copy.putAll(POOLED_LOGGERS);
		return copy;
	}

	@Override
	public Map<String, ?> getConfiguration() {
		return props;
	}

	@Override
	public void setConfiguration(Map<String, ?> settings) throws ConfigException {
		this.props = settings;

		// obtain all optional attributes
		poolName = Utils.getString("Name", settings, DEFAULT_POOL_NAME);
		poolSize = Utils.getInt("Size", settings, MAX_POOL_SIZE);
		capacity = Utils.getInt("Capacity", settings, MAX_CAPACITY);
		retryInterval = Utils.getInt("RetryInterval", settings, RETRY_INTERVAL);
		dropOnError = Utils.getBoolean("DropOnError", settings, DROP_ON_EXCEPTION);
		// create and register pooled logger instance if not yet available
		PooledLogger pooledLogger = new PooledLogger(poolName, poolSize, capacity);
		pooledLogger.dropOnError(dropOnError);
		pooledLogger.setRetryInterval(retryInterval);
		if (POOLED_LOGGERS.putIfAbsent(poolName, pooledLogger) == null) {
			pooledLogger.start();
		}
	}

	/**
	 * Shuts down all pooled loggers.
	 */
	public static void shutdownAllLoggers() {
		for (PooledLogger pl : POOLED_LOGGERS.values()) {
			pl.shutdown(null);
		}
		POOLED_LOGGERS.clear();
	}
}
