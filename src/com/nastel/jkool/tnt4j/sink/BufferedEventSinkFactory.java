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

import com.nastel.jkool.tnt4j.config.ConfigurationException;
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
	private EventSinkFactory sinkFactory;
	private int sinkMaxCapacity = 1000;
	
	/**
	 * Create a default buffered sink factory 
	 * 
	 * @param sink out sink where events/log message are written out
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
	
	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new BufferedEventSink(sinkFactory.getEventSink(name), sinkMaxCapacity));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new BufferedEventSink(sinkFactory.getEventSink(name, props), sinkMaxCapacity));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new BufferedEventSink(sinkFactory.getEventSink(name, props, frmt), sinkMaxCapacity));
	}

	@Override
	public void setConfiguration(Map<String, Object> props) throws ConfigurationException {
		Object capacity = props.get("MaxCapacity");
		sinkMaxCapacity = capacity != null? Integer.valueOf(capacity.toString()): sinkMaxCapacity;
		sinkFactory = (EventSinkFactory) Utils.createConfigurableObject("EventSinkFactory", "EventSinkFactory.", props);
		super.setConfiguration(props);
	}
}
