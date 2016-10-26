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
package com.jkoolcloud.tnt4j.sink;

import java.util.EventObject;

import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * An event class for reporting errors when writing to an instance of {@link EventSink}.
 * </p>
 * 
 * @see EventSink
 * 
 * @version $Revision: 6 $
 * 
 */
public class SinkError extends EventObject {
	private static final long serialVersionUID = 1L;

	private final Throwable error;
	private final SinkLogEvent sinkObj;

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
	public SinkError(Sink source, SinkLogEvent msg, Throwable ex) {
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
	public SinkLogEvent getSinkObject() {
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
