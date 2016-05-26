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
package com.jkoolcloud.tnt4j.sink;

import java.util.Properties;

import com.jkoolcloud.tnt4j.core.TTL;
import com.jkoolcloud.tnt4j.format.EventFormatter;

/**
 * <p>Classes that implement this interface provide implementation for
 * the <code>EventSinkFactory</code>, which provides an interface to
 * create instances of event sinks bound to specific logging frameworks.</p>
 *
 *
 * @see EventSink
 * @see EventFormatter
 *
 * @version $Revision: 2 $
 *
 */
public interface EventSinkFactory extends TTL {
	/**
	 * Obtain an instance of <code>EventSink</code> by name
	 *
	 * @param name name of the category associated with the event log
	 * @return event sink instance
	 * @see EventSink
	 */
	EventSink getEventSink(String name);

	/**
	 * Obtain an instance of <code>EventSink</code> by name and
	 * custom properties
	 *
	 * @param name name of the category associated with the event log
	 * @param props properties associated with the event logger (implementation specific).
	 * @return event sink instance
	 * @see EventSink
	 */
	EventSink getEventSink(String name, Properties props);

	/**
	 * Obtain an instance of <code>EventSink</code> by name and
	 * custom properties
	 *
	 * @param name name of the category associated with the event log
	 * @param props properties associated with the event logger (implementation specific).
	 * @param frmt event formatter object to format events before writing to log
	 * @return event sink instance
	 * @see EventSink
	 * @see EventFormatter
	 */
	EventSink getEventSink(String name, Properties props, EventFormatter frmt);
}
