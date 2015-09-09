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
 * <p>Buffered implementation of <code>EventSinkFactory</code> interface, which
 * creates instances of <code>BufferedEventSink</code>. This factory relies on the
 * specified concrete <code>EventSinkFactory</code> instance specified by <code>EventSinkFactory</code>
 * configuration attribute. This factory uses specified event sink factory to create event sinks and wraps
 * then with instances of <code>BufferedEventSink</code>.
 *
 *
 * @see EventSink
 * @see BufferedEventSink
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
	
	/**
	 * Create a default buffered sink factory
	 *
	 */
	public BufferedEventSinkFactory() {
	}

	/**
	 * Create a default buffered sink factory with a given sink factory
	 * used to create concrete event sinks.
	 *
	 * @param factory concrete event sink factory instance
	 */
	public BufferedEventSinkFactory(EventSinkFactory factory) {
		sinkFactory = factory;
	}

	/**
	 * Obtain an instance of pooled logger, which allows logging of events
	 * asynchronously by a thread pool.
	 *
	 * @return pooled logger instance
	 */
	protected PooledLogger getPooledLogger() {
		return pooledLogger;
	}

	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new BufferedEventSink(this, sinkFactory.getEventSink(name)));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new BufferedEventSink(this, sinkFactory.getEventSink(name, props)));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new BufferedEventSink(this, sinkFactory.getEventSink(name, props, frmt)));
	}

	@Override
	public void setConfiguration(Map<String, Object> props) throws ConfigException {
		super.setConfiguration(props);
		sinkFactory = (EventSinkFactory) Utils.createConfigurableObject("EventSinkFactory", "EventSinkFactory.", props);
		
		String loggerKey = Integer.toHexString(System.identityHashCode(this));
		Object nameObj = props.get("PoolName");
		String loggerName = nameObj == null? loggerKey: nameObj.toString();
		
		Object threadPool = props.get("PoolSize");
		int poolSize = threadPool == null? MAX_POOL_SIZE: Integer.parseInt(threadPool.toString());
		
		Object qCapacity = props.get("PoolCapacity");
		int capacity = qCapacity == null? MAX_CAPACITY: Integer.parseInt(qCapacity.toString());
		pooledLogger = new PooledLogger(loggerName, poolSize, capacity);
		POOLED_LOGGERS.putIfAbsent(loggerName, new PooledLogger(loggerName, poolSize, capacity));
		
		pooledLogger = POOLED_LOGGERS.get(loggerName);
		pooledLogger.start();
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			String loggerKey = Integer.toHexString(System.identityHashCode(this));
			PooledLogger lg = POOLED_LOGGERS.get(loggerKey);
			if (lg != null) {
				POOLED_LOGGERS.remove(loggerKey);
				lg.stop();
			}
		} finally {
			super.finalize();
		}
	}
}
