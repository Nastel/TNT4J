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

import java.util.Properties;

import com.nastel.jkool.tnt4j.format.EventFormatter;

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
public interface EventSinkFactory {
	/**
	 * Obtain an instance of <code>EventSink</code> by name
	 *
	 * @param name name of the category associated with the event log
	 * @see EventSink
	 */
	public EventSink getEventSink(String name);
	
	/**
	 * Obtain an instance of <code>EventSink</code> by name and 
	 * custom properties
	 *
	 * @param name name of the category associated with the event log
	 * @param props properties associated with the event logger (implementation specific).
	 * @see EventSink
	 */
	public EventSink getEventSink(String name, Properties props);

	/**
	 * Obtain an instance of <code>EventSink</code> by name and 
	 * custom properties
	 *
	 * @param name name of the category associated with the event log
	 * @param props properties associated with the event logger (implementation specific).
	 * @param frmt event formatter object to format events before writing to log
	 * @see EventSink
	 * @see EventFormatter
	 */
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt);
	
}
