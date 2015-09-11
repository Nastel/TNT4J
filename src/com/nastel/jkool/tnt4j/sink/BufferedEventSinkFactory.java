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
package com.nastel.jkool.tnt4j.sink;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.nastel.jkool.tnt4j.config.ConfigException;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>Buffered implementation of {@link EventSinkFactory} interface, which
 * creates instances of {@link BufferedEventSink}. This factory relies on the
 * specified concrete {@link EventSinkFactory} instance specified by {@link EventSinkFactory}
 * configuration attribute. This factory uses specified event sink factory to create event sinks and wraps
 * then with instances of {@link BufferedEventSink}.
 *
 *
 * @see EventSink
 * @see BufferedEventSink
 * @see PooledLogger
 * 
 * @version $Revision: 1 $
 *
 */
public class BufferedEventSinkFactory extends AbstractEventSinkFactory {
	private static int MAX_POOL_SIZE = Integer.getInteger("tnt4j.pooled.logger.pool", 5);
	private static int MAX_CAPACITY = Integer.getInteger("tnt4j.pooled.logger.capacity", 10000);
	
	private static final ConcurrentMap<String, PooledLogger> POOLED_LOGGERS = new ConcurrentHashMap<String, PooledLogger>();
	
	EventSinkFactory sinkFactory;
	PooledLogger pooledLogger;
	String factoryName;
	boolean blockWrites = false;

	/**
	 * Create a default buffered sink factory
	 *
	 */
	public BufferedEventSinkFactory() {
		this(null);
	}

	/**
	 * Create a default buffered sink factory with a given sink factory
	 * used to create concrete event sinks.
	 *
	 * @param factory concrete event sink factory instance
	 */
	public BufferedEventSinkFactory(EventSinkFactory factory) {
		factoryName = Integer.toHexString(System.identityHashCode(this));
		sinkFactory = factory;
	}

	/**
	 * Obtain an instance of pooled logger, which allows logging of events
	 * asynchronously by a thread pool.
	 *
	 * @return pooled logger instance
	 * @see PooledLogger
	 */
	protected PooledLogger getPooledLogger() {
		return pooledLogger;
	}

	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new BufferedEventSink(this, sinkFactory.getEventSink(name), blockWrites));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new BufferedEventSink(this, sinkFactory.getEventSink(name, props), blockWrites));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new BufferedEventSink(this, sinkFactory.getEventSink(name, props, frmt), blockWrites));
	}

	@Override
	public void setConfiguration(Map<String, Object> props) throws ConfigException {
		super.setConfiguration(props);
		sinkFactory = (EventSinkFactory) Utils.createConfigurableObject("EventSinkFactory", "EventSinkFactory.", props);
		
		// obtain all optional attributes
		Object nameObj = props.get("PoolName");
		String loggerName = nameObj == null? factoryName: nameObj.toString();
		
		Object threadPool = props.get("PoolSize");
		int poolSize = threadPool == null? MAX_POOL_SIZE: Integer.parseInt(threadPool.toString());
		
		Object qCapacity = props.get("PoolCapacity");
		int capacity = qCapacity == null? MAX_CAPACITY: Integer.parseInt(qCapacity.toString());
		
		Object blockMode = props.get("BlockWrites");
		blockWrites = blockMode == null? blockWrites: Boolean.parseBoolean(blockMode.toString());
		
		// create and register pooled logger instance if not yet available
		pooledLogger = new PooledLogger(loggerName, poolSize, capacity);
		POOLED_LOGGERS.putIfAbsent(loggerName, new PooledLogger(loggerName, poolSize, capacity));
		
		// obtain the required logger and attempt to start
		pooledLogger = POOLED_LOGGERS.get(loggerName);
		pooledLogger.start();
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			PooledLogger lg = POOLED_LOGGERS.get(factoryName);
			if (lg != null) {
				POOLED_LOGGERS.remove(factoryName);
				lg.stop();
			}
		} finally {
			super.finalize();
		}
	}
}
