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

import java.util.Map;
import java.util.Properties;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.utils.Utils;

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
	String poolFactoryClass;
	boolean blockWrites = false;
	long signalTimeout = 10000;
	EventSinkFactory sinkFactory;
	PooledLoggerFactory pooledFactory;

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
		return pooledFactory.getPooledLogger();
	}

	@Override
	protected EventSink configureSink(EventSink sink) {
		BufferedEventSink bsink = (BufferedEventSink) sink;
		bsink.setSignalTimeout(signalTimeout);
		return super.configureSink(bsink);	
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
		pooledFactory = (PooledLoggerFactory) Utils.createConfigurableObject("PooledLoggerFactory", "PooledLoggerFactory.", props);	
		blockWrites = Utils.getBoolean("BlockWrites", props, blockWrites);
		signalTimeout = Utils.getLong("SignalTimeout", props, signalTimeout);
		if (sinkFactory == null) {
			throw new ConfigException("Missing EventSinkFactory implementation", props);
		}
		if (pooledFactory == null) {
			throw new ConfigException("Missing PooledLoggerFactory implementation", props);
		}
	}	
}
