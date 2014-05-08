/*
 * Copyright (c) 2014 Nastel Technologies, Inc. All Rights Reserved.
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

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * <p>Classes that implement this interface provide implementation for 
 * the <code>EventSink</code>, which provides an interface for underlying logging framework
 * storage destination. All event sink implementations should be wrapped with this interface.
 * </p>
 *
 *
 * @see OpLevel
 * @see TrackingEvent
 * @see TrackingActivity
 *
 * @version $Revision: 7 $
 *
 */
public interface EventSink extends Sink {
	/**
	 * This method allows writing of <code>TrackingActivity</code> objects
	 * to the underlying destination.
	 * 
	 * @param activity to be sent to the sink
	 * @see TrackingActivity
	 */
	public void log(TrackingActivity activity);

	/**
	 * Check of a certain log level is set/enabled
	 * 
	 * @param sev severity level
	 * @see OpLevel
	 */
	public boolean isSet(OpLevel sev);

	/**
	 * Log a given tracking event
	 *
	 * @param event tracking event to log
	 * @see TrackingEvent
	 */
	public void log(TrackingEvent event);

	/**
	 * Log a given string message with a specified severity
	 *
	 * @param sev message severity to log
	 * @param msg string message to be logged
	 * @see OpLevel
	 */
	public void log(OpLevel sev, String msg);
	
	/**
	 * Log a given string message with a specified severity
	 *
	 * @param sev message severity to log
	 * @param msg string message to be logged
	 * @param ex exception associated with this message
	 * @see OpLevel
	 */
	public void log(OpLevel sev, String msg, Throwable ex);
	
	/**
	 * Register an event sink listener for notifications when errors 
	 * occur when writing to event sink. 
	 * 
	 * @see SinkErrorListener
	 */
	public void addSinkErrorListener(SinkErrorListener listener);
	
	/**
	 * Remove an event sink listener for notifications when errors 
	 * occur when writing to event sink. 
	 * 
	 * @see SinkErrorListener
	 */
	public void removeSinkErrorListener(SinkErrorListener listener);
	
	/**
	 * Register an event sink listener for notifications when logging events
	 * occur when writing to event sink. 
	 * 
	 * @see SinkLogEventListener
	 */
	public void addSinkLogEventListener(SinkLogEventListener listener);
	
	/**
	 * Remove an event sink listener for notifications when logging events 
	 * occur when writing to event sink. 
	 * 
	 * @see SinkLogEventListener
	 */
	public void removeSinkLogEventListener(SinkLogEventListener listener);
	
	/**
	 * Register an event sink filter to selectively filter out certain events.
	 * 
	 * @see SinkEventFilter
	 */
	public void addSinkEventFilter(SinkEventFilter listener);
	
	/**
	 * Remove an event sink filter.
	 * 
	 * @see SinkEventFilter
	 */
	public void removeSinkEventFilter(SinkEventFilter listener);
	
}
