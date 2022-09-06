/*
 * Copyright 2014-2022 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jkoolcloud.tnt4j.sink;

/**
 * <p>
 * A simple event listener interface for event sink observers. This interface can be implemented by classes that are
 * interested in sink logging events caused sink is called to log activities, messages, events.
 * </p>
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
	 * @param ev
	 *            sink activity event
	 * 
	 * @see SinkLogEvent
	 */
	void sinkLogEvent(SinkLogEvent ev);
}
