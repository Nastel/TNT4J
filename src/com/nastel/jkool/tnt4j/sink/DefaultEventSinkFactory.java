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

import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * This class provides a static way to get default event sink factory
 * </p>
 * 
 * <pre>
 * {@code
 * EventSinkFactory config = DefaultEventSinkFactory.getInstance();
 * ...
 * }
 * </pre>
 * 
 * @see EventSinkFactory
 * @see EventSink
 * 
 * @version $Revision: 1 $
 * 
 */
public class DefaultEventSinkFactory {
	private static final String DEFAULT_FACTORY_CLASS = "com.nastel.jkool.tnt4j.logger.Log4JEventSinkFactory";
	private static EventSinkFactory defaultFactory;

	static {
		LoadDefaultFactory();
	}
	
	private static void LoadDefaultFactory() {
		if (defaultFactory == null) {
			String defaultFactoryClass = System.getProperty("tnt4j.default.event.factory", DEFAULT_FACTORY_CLASS);
			try {
				defaultFactory = (EventSinkFactory) Utils.createInstance(defaultFactoryClass);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	private DefaultEventSinkFactory() {}
	
	/**
	 * Obtain a default event sink factory
	 * 
	 * @return default <code>EventSinkFactory</code> instance
	 */
	public static EventSinkFactory getInstance() {
		return defaultFactory;
	}

	/**
	 * Set a default event sink factory implementation
	 * 
	 * @return <code>EventSinkFactory</code> instance
	 */
	public static EventSinkFactory setDefaultEventSinkFactory(EventSinkFactory factory) {
		defaultFactory = factory != null? factory: defaultFactory;
		return defaultFactory;
	}
	
	/**
	 * Static method to obtain default event sink
	 * 
	 * @param name name of the application/event sink to get
	 *
	 */
	public static EventSink defaultEventSink(String name) {
		return defaultFactory.getEventSink(name);
	}

	/**
	 * Static method to obtain default event sink
	 * 
	 * @param clazz class for which to get the event sink
	 *
	 */
	public static EventSink defaultEventSink(Class<?> clazz) {
	    return defaultEventSink(clazz.getName());
    }
}
