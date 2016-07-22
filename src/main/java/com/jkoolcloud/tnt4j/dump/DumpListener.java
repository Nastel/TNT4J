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
package com.jkoolcloud.tnt4j.dump;


/**
 * <p>
 * A simple event listener interface for observers of dump generation.
 * This interface can be implemented by classes that are interested in "raw" events caused by <code>TrackerLogger.dump()</code> method. 
 * Each dump generation will generate such an event before, after, complete and error invocations into the listener instance.
 * </p>
 *
 * @see DumpEvent
 * @see DumpProvider
 *
 * @version $Revision: 1 $
 *
 */

public interface DumpListener {
	/**
	 * Notifies when a dump event is generated. Dump events are generated
	 * on before, after, complete and error defined in <code>DumpProvider</code>
	 * 
	 * @param event dump event instance
	 * 
	 * @see DumpEvent
	 * @see DumpProvider
	 */
	void onDumpEvent(DumpEvent event);
}
