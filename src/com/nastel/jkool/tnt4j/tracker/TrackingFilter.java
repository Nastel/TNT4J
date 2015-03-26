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
package com.nastel.jkool.tnt4j.tracker;

import com.nastel.jkool.tnt4j.core.OpLevel;


/**
 * <p>
 * A simple event filter interface for filtering out tracking activities and events. 
 * Implementations of this interface are registered with <code>Tracker</code> to filter out
 * tracking activities and tracking events. <code>isTrackingEnabled</code> is called from <code>Tracker</code> instances
 * when activities and events are created using <code>Tracker.newEvent</code> and <code>Tracker.newActvity</code> calls.
 * Any activity or event with <code>OpType.NOOP</code> is automatically filtered out.
 * </p>
 * 
 * @see Tracker
 * @see TrackingEvent
 * @see TrackingActivity
 * 
 * @version $Revision: 1 $
 * 
 */

public interface TrackingFilter {
	/**
	 * Returns true if tracking is enabled
	 * 
	 * @param tracker
	 *            a specific tracker instance for which tracking is be determined
	 * @param level
	 *            severity level of the tracking event/activity
	 * @param args
	 *            a set of arguments passed to <code>Tracker.newEvent</code> and <code>Tracker.newActvity</code> calls
	 * @return true if tracking is enabled
	 * @see Tracker
	 */
	boolean isTrackingEnabled(Tracker tracker, OpLevel level, Object...args);
}
