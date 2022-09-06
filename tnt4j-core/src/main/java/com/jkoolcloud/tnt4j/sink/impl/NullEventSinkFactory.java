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
package com.jkoolcloud.tnt4j.sink.impl;

import java.util.Properties;

import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;

/**
 * <p>
 * Concrete implementation of {@link com.jkoolcloud.tnt4j.sink.EventSinkFactory} interface, which creates instances of
 * {@link com.jkoolcloud.tnt4j.sink.EventSink}. This factory uses {@link com.jkoolcloud.tnt4j.sink.impl.NullEventSink}.
 * </p>
 *
 *
 * @see EventSink
 * @see NullEventSink
 *
 * @version $Revision: 1 $
 *
 */
public class NullEventSinkFactory extends AbstractEventSinkFactory {

	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new NullEventSink(name));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return getEventSink(name);
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new NullEventSink(name, frmt));
	}
}
