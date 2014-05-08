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

/**
 * <p>
 * A simple event filter interface. Implementations of this interface are used with <code>EventSink</code> to filter out
 * logging events.
 * </p>
 * 
 * @see EventSink
 * @see SinkLogEvent
 * 
 * @version $Revision: 2 $
 * 
 */
public interface SinkEventFilter {
	/**
	 * Returns true if a given logging event passes the filter, false otherwise
	 * 
	 * @param event
	 *            to be checked with registered filters
	 * @return true if event passed all filters, false otherwise
	 * @see SinkLogEvent
	 */
	public boolean acceptEvent(SinkLogEvent event);
}
