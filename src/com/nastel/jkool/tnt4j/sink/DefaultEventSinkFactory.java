/*
 * Copyright (c) 2013 Nastel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Nastel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with Nastel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *
 */
package com.nastel.jkool.tnt4j.sink;

import com.nastel.jkool.tnt4j.logger.Log4JEventSinkFactory;

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
	private static EventSinkFactory defaultFactory = new Log4JEventSinkFactory();

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
