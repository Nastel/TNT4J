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
package com.nastel.jkool.tnt4j.logger;

import java.util.Properties;

import com.nastel.jkool.tnt4j.format.DefaultFormatter;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.sink.EventSink;
import com.nastel.jkool.tnt4j.sink.EventSinkFactory;

/**
 * <p>Concrete implementation of <code>EventSinkFactory</code> interface, which
 * creates instances of <code>EventSink</code>. This factory uses <code>Log4jEventSink</code>
 * as the underlying logger provider.</p>
 *
 *
 * @see EventSink
 * @see Log4jEventSink
 *
 * @version $Revision: 1 $
 *
 */
public class Log4JEventSinkFactory implements EventSinkFactory {
	
	@Override
	public EventSink getEventSink(String name) {
		return new Log4jEventSink(name, System.getProperties(), new DefaultFormatter());
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return new Log4jEventSink(name, props, new DefaultFormatter());
	}

	@Override
    public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return new Log4jEventSink(name, props, frmt);
    }
	
	/**
	 * Static method to obtain default event sink
	 * 
	 * @param name name of the application/event sink to get
	 *
	 */
	public static EventSink defaultEventSink(String name) {
		return new Log4jEventSink(name, System.getProperties(), new DefaultFormatter());
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
