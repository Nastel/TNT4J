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

import java.util.EventObject;

import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * An event class for reporting errors when writing to an instance of <code>EventSink</code>.
 * </p>
 * 
 * @see EventSink
 * 
 * @version $Revision: 6 $
 * 
 */
public class SinkError extends EventObject {
	private static final long serialVersionUID = 1L;

	private Throwable error;
	private Object sinkObj;

	/**
	 * Create a new event instance
	 * 
	 * @param source
	 *            sink associated with the event
	 * @param msg
	 *            message associated with the sink
	 * @param ex
	 *            exception associated with the event
	 */
	public SinkError(Sink source, Object msg, Throwable ex) {
		super(source);
		error = ex;
		sinkObj = msg;
	}

	/**
	 * Get message associated with the sink. Message that was being written to the sink.
	 * 
	 * @return sink message
	 * 
	 */
	public Object getSinkObject() {
		return sinkObj;
	}

	/**
	 * Get error associated with the event
	 * 
	 * @return error
	 * 
	 */
	public Throwable getCause() {
		return error;
	}

	@Override
	public String toString() {
		return super.toString() 
			+ "{sink.msg: " + Utils.quote(sinkObj)
			+ ", cause: " + Utils.quote(error)
			+ "}";
	}
}
