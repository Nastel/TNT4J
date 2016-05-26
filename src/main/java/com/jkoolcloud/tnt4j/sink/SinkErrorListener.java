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
	void sinkError(SinkError ev);
}
