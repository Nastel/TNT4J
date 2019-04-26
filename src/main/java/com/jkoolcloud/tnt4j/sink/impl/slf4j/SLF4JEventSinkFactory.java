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
package com.jkoolcloud.tnt4j.sink.impl.slf4j;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.format.DefaultFormatter;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Concrete implementation of {@link com.jkoolcloud.tnt4j.sink.EventSinkFactory} interface over SLF4J, which creates
 * instances of {@link com.jkoolcloud.tnt4j.sink.EventSink}. This factory uses
 * {@link com.jkoolcloud.tnt4j.sink.impl.slf4j.SLF4JEventSink} as the underlying logger provider.
 * </p>
 *
 *
 * @see EventSink
 * @see SLF4JEventSink
 *
 * @version $Revision: 1 $
 *
 */
public class SLF4JEventSinkFactory extends AbstractEventSinkFactory {

	private static final String FORMAT_PATTERN = "{2} | {3}";

	private String loggerName;

	/**
	 * Create a default sink factory with no custom predefined logger name.
	 */
	public SLF4JEventSinkFactory() {
	}

	/**
	 * Create a sink factory with a custom predefined logger name.
	 *
	 * @param loggerName
	 *            custom predefined logger name to use
	 */
	public SLF4JEventSinkFactory(String loggerName) {
		this.loggerName = loggerName;
	}

	@Override
	public EventSink getEventSink(String name) {
		return getEventSink(name, System.getProperties());
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return getEventSink(name, props, new DefaultFormatter(FORMAT_PATTERN));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new SLF4JEventSink(getLoggerName(name), props, frmt));
	}

	private String getLoggerName(String name) {
		return StringUtils.isEmpty(loggerName) ? name : loggerName;
	}

	/**
	 * Static method to obtain default event sink
	 *
	 * @param name
	 *            name of the application/event sink to get
	 * @return event sink
	 */
	public static EventSink defaultEventSink(String name) {
		return new SLF4JEventSink(name, System.getProperties(), new DefaultFormatter(FORMAT_PATTERN));
	}

	/**
	 * Static method to obtain default event sink
	 *
	 * @param clazz
	 *            class for which to get the event sink
	 * @return event sink
	 */
	public static EventSink defaultEventSink(Class<?> clazz) {
		return defaultEventSink(clazz.getName());
	}

	@Override
	public void setConfiguration(Map<String, ?> props) throws ConfigException {
		super.setConfiguration(props);
		loggerName = Utils.getString("LoggerName", props, loggerName);
	}
}
