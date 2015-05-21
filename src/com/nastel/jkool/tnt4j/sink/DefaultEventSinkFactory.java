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

import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * This class provides a static way to get default event sink factory. Default event sink factory is
 * set to <code>com.nastel.jkool.tnt4j.logger.Log4JEventSinkFactory</code>.
 * Developers may initialize default event sink factory at runtime via code as follows:
 * <pre>
 * {@code
 * EventSinkFactory factory = new MyEventSinkFactory();
 * DefaultEventSinkFactory.setDefaultEventSinkFactory(factory);
 * ...
 * }
 * </pre>
 * Another way to initialize to via a java property <code>tnt4j.default.event.factory</code>
 * java property and setting to the name of the class that implements <code>EventSinkFactory</code> interface.
 * Example:
 * <pre>
 * {@code
 * tnt4j.default.event.factory="com.nastel.jkool.tnt4j.logger.Log4JEventSinkFactory"
 * }
 * </pre>
 * Below is an example of how to obtain default event sink factory.
 * <pre>
 * {@code
 * EventSinkFactory config = DefaultEventSinkFactory.getInstance();
 * ...
 * }
 * </pre>
 * Below is an example of how to obtain an event sink based on a default event sink factory.
 * Use this method for all default logging.
 * <pre>
 * {@code
 * EventSink sink = DefaultEventSinkFactory.defaultEventSink(MyClass.class);
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
	
	/**
	 * Initialize default even sink factory based on given environment.
	 * Default event sink factory can be specified using <code>tnt4j.default.event.factory</code>
	 * java property and setting to the name of the class that implements <code>EventSinkFactory</code>
	 * interface. The given event sink factory class must provide a default public constructor with
	 * no arguments.
	 * 
	 * @see EventSinkFactory
	 */
	private static void LoadDefaultFactory() {
		if (defaultFactory == null) {
			String defaultFactoryClass = System.getProperty("tnt4j.default.event.factory", DEFAULT_FACTORY_CLASS);
			defaultFactory = createEventSinkFactory(defaultFactoryClass);
			if (defaultFactory == null && !defaultFactoryClass.equals(DEFAULT_FACTORY_CLASS)) {
				defaultFactory = createEventSinkFactory(DEFAULT_FACTORY_CLASS);
			}
		}
	}
	
	/**
	 * Create an instance of <code>EventSinkFactory</code> based on
	 * a given class name. The class must provide a default public constructor
	 * with no arguments.
	 * 
	 * @param className class name that implements <code>EventSinkFactory</code> interface.
	 * @return event sink factory instance, null if not able to create an instance.
	 * @see EventSinkFactory
	 */
	private static EventSinkFactory createEventSinkFactory(String className) {
		try {
			EventSinkFactory factory = (EventSinkFactory) Utils.createInstance(className);
			return factory;
		} catch (Throwable e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	/**
	 * Private constructor to prevent object instantiation
	 * 
	 */
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
	 * @return new event sink instance associated with given name
	 */
	public static EventSink defaultEventSink(String name) {
		return defaultFactory.getEventSink(name);
	}

	/**
	 * Static method to obtain default event sink
	 * 
	 * @param clazz class for which to get the event sink
	 * @return new event sink instance associated with given class
	 */
	public static EventSink defaultEventSink(Class<?> clazz) {
	    return defaultEventSink(clazz.getName());
    }
}
