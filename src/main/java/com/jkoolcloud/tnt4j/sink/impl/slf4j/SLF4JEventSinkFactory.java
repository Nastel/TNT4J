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

	String name;

	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new SLF4JEventSink(this.name == null ? name : this.name, System.getProperties(),
				new DefaultFormatter()));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new SLF4JEventSink(this.name == null ? name : this.name, props, new DefaultFormatter()));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new SLF4JEventSink(this.name == null ? name : this.name, props, frmt));
	}

	/**
	 * Static method to obtain default event sink
	 *
	 * @param name
	 *            name of the application/event sink to get
	 * @return event sink
	 */
	public static EventSink defaultEventSink(String name) {
		return new SLF4JEventSink(name, System.getProperties(), new DefaultFormatter());
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
		name = Utils.getString("nameOverride", props, name);
		super.setConfiguration(props);
	}

}
