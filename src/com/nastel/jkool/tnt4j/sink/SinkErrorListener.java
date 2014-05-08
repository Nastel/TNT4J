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
 * This interface can be implemented by classes that are interested in "error" events
 * caused when exceptions occur when writing to an event sink.</p>
 *
 * @see SinkError
 * @see EventSink
 *
 * @version $Revision: 2 $
 *
 */
public interface SinkErrorListener {
	/**
	 * Notifies this listener about an error when writing 
	 * to an event sink.
	 * 
	 * @param ev the event describing the error
	 * 
	 * @see SinkError
	 */
	public void sinkError(SinkError ev);
}
