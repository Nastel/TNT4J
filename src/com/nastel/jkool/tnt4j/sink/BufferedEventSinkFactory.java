/*
 * Copyright 2014 Nastel Technologies, Inc.
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
	private static PooledLogger pooledLogger = new PooledLogger(Integer.getInteger("tnt4j.pooled.logger.pool", 5),
			Integer.getInteger("tnt4j.pooled.logger.capacity", 5000));
	
	private EventSinkFactory sinkFactory;
		
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
	 */
	public static PooledLogger getPooledLogger() {
		return pooledLogger;
	}
	
	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new BufferedEventSink(sinkFactory.getEventSink(name)));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new BufferedEventSink(sinkFactory.getEventSink(name, props)));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new BufferedEventSink(sinkFactory.getEventSink(name, props, frmt)));
	}

	@Override
	public void setConfiguration(Map<String, Object> props) throws ConfigException {
		sinkFactory = (EventSinkFactory) Utils.createConfigurableObject("EventSinkFactory", "EventSinkFactory.", props);
		super.setConfiguration(props);
	}
}
