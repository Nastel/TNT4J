/*
 * Copyright 2014-2022 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.sink;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.impl.FileEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.impl.NullEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.impl.slf4j.SLF4JEventSinkFactory;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * Abstract logged event sink factory class. Derived classes should call
 * {@link #getLogSink(String, java.util.Properties, com.jkoolcloud.tnt4j.format.EventFormatter)} to initialize logger
 * {@link com.jkoolcloud.tnt4j.sink.EventSink}. Logger sink can be defined using those configuration properties:
 * <ul>
 * <li>Filename - defines file name to be used with {@link com.jkoolcloud.tnt4j.sink.impl.FileEventSinkFactory}. NOTE:
 * this property use is deprecated. Use {@code LogSink} with {@code file:file_path} value instead.</li>
 * <li>LogSink - defines sink descriptor to be used for logging. Descriptor format is {@code sinkId:sinkParam}. Now
 * there are two sink types supported:
 * <ul>
 * <li>file:file_path - defines file name to be used with
 * {@link com.jkoolcloud.tnt4j.sink.impl.FileEventSinkFactory}.</li>
 * <li>slf4j:logger_name - defines SLF4J logger name to be used with
 * {@link com.jkoolcloud.tnt4j.sink.impl.slf4j.SLF4JEventSinkFactory}</li>
 * <li>null - means {@link com.jkoolcloud.tnt4j.sink.impl.NullEventSinkFactory} shall be used</li>
 * </ul>
 * </li>
 * <li>eventSinkFactory - defines logger sink factory class and configuration properties for that factory.</li>
 * </ul>
 *
 * @version $Revision: 1 $
 *
 * @see com.jkoolcloud.tnt4j.sink.impl.FileEventSinkFactory
 * @see com.jkoolcloud.tnt4j.sink.impl.slf4j.SLF4JEventSinkFactory
 * @see com.jkoolcloud.tnt4j.sink.impl.NullEventSinkFactory
 * @see com.jkoolcloud.tnt4j.sink.impl.SocketEventSinkFactory
 */
public abstract class LoggedEventSinkFactory extends AbstractEventSinkFactory {
	protected static final String SINK_PREF_FILE = "file:";
	protected static final String SINK_PREF_SLF4J = "slf4j:";
	protected static final String SINK_PREF_NULL = "null";

	private String sinkDescriptor = null;
	private EventSinkFactory eventSinkFactory = null;

	/**
	 * Obtain an instance of logger {@link com.jkoolcloud.tnt4j.sink.EventSink} by name and custom properties
	 *
	 * @param name
	 *            name of the category associated with the event log
	 * @param props
	 *            properties associated with the event logger (implementation specific).
	 * @param frmt
	 *            event formatter object to format events before writing to log
	 * @return logger event sink instance, or {@code null} if logger sink factory is {@code null}
	 *
	 * @see com.jkoolcloud.tnt4j.sink.EventSinkFactory#getEventSink(String, java.util.Properties,
	 *      com.jkoolcloud.tnt4j.format.EventFormatter)
	 */
	protected EventSink getLogSink(String name, Properties props, EventFormatter frmt) {
		return eventSinkFactory == null ? null
				: eventSinkFactory.getEventSink(name, props, getDefaultEventFormatter(frmt));
	}

	@Override
	public void setConfiguration(Map<String, ?> settings) throws ConfigException {
		super.setConfiguration(settings);

		String fileName = Utils.getString("Filename", settings, null);
		sinkDescriptor = Utils.getString("LogSink", settings, sinkDescriptor);
		eventSinkFactory = (EventSinkFactory) Utils.createConfigurableObject("eventSinkFactory", "eventSinkFactory.",
				settings);

		if (StringUtils.isEmpty(sinkDescriptor) && StringUtils.isNotEmpty(fileName)) {
			sinkDescriptor = SINK_PREF_FILE + fileName;
		}
		_applyConfig(settings);
	}

	private void _applyConfig(Map<String, ?> settings) throws ConfigException {
		if (eventSinkFactory == null && StringUtils.isNotEmpty(sinkDescriptor)) {
			if (sinkDescriptor.startsWith(SINK_PREF_SLF4J)) {
				eventSinkFactory = new SLF4JEventSinkFactory(sinkDescriptor.substring(SINK_PREF_SLF4J.length()));
			} else if (sinkDescriptor.startsWith(SINK_PREF_FILE)) {
				eventSinkFactory = new FileEventSinkFactory(sinkDescriptor.substring(SINK_PREF_FILE.length()));
			} else if (SINK_PREF_NULL.equals(sinkDescriptor)) {
				eventSinkFactory = new NullEventSinkFactory();
			} else {
				throw new ConfigException("Unknown LogSink descriptor: " + sinkDescriptor, settings);
			}

			if (eventSinkFactory != null) {
				eventSinkFactory.setTTL(getTTL());
			}
		}

		if (eventSinkFactory == null && doInitDefaultLogger()) {
			eventSinkFactory = DefaultEventSinkFactory.getInstance();
		}
	}

	/**
	 * Returns indicator flag if {@link com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory} shall be used if no other log
	 * sink factory is defined over configuration.
	 * 
	 * @return {@code true} if default logger event sink factory should be used to initialize log sink, {@code false} -
	 *         otherwise
	 *
	 * @see DefaultEventSinkFactory#getInstance()
	 * @see #setConfiguration(java.util.Map)
	 */
	protected boolean doInitDefaultLogger() {
		return false;
	}
}
