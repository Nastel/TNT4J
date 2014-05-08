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
 * <p>A simple event listener interface for event sink observers.
 * This interface can be implemented by classes that are interested in sink logging events
 * caused sink is called to log activities, messages, events.</p>
 *
 * @see SinkLogEvent
 * @see EventSink
 *
 * @version $Revision: 3 $
 *
 */
public interface SinkLogEventListener {
	/**
	 * Notifies this listener about a logging activity
	 * 
	 * @param ev sink activity event
	 * 
	 * @see SinkLogEvent
	 */
	public void sinkLogEvent(SinkLogEvent ev);
}
