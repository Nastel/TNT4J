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
package com.nastel.jkool.tnt4j.format;

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * Classes that implement this interface provide implementation for the <code>EventFormatter</code> interface.
 * This interface allows formatting of any object, tracking objects as well as log messages
 * to a string format.
 * </p>
 * 
 * 
 * @version $Revision: 2 $
 * 
 * @see Formatter
 */
public interface EventFormatter extends Formatter {
	/**
	 * Format a given <code>TrackingEvent</code> and return a string
	 *
	 * @param event tracking event instance to be formatted
	 * @see TrackingEvent
	 */
	public String format(TrackingEvent event);
	
	/**
	 * Format a given <code>TrackingActivity</code> and return a string
	 *
	 * @param activity tracking activity instance to be formatted
	 * @see TrackingActivity
	 */
	public String format(TrackingActivity activity);

	/**
	 * Format a given message and severity level combo
	 *
	 * @param level severity level
	 * @param msg message to be formatted
	 * @see OpLevel
	 */
	public String format(OpLevel level, Object msg);
	
	/**
	 * Format a given message, severity, exception combo
	 *
	 * @param level severity level
	 * @param msg message to be formatted
	 * @param ex exception to be formatted
	 * @see OpLevel
	 */
	public String format(OpLevel level, Object msg, Throwable ex);
}
