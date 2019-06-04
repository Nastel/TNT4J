/*
 * Copyright 2014-2019 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.tracker;

import com.jkoolcloud.tnt4j.core.OpLevel;

/**
 * A simple event filter interface for filtering out tracking activities and events.
 * <p>
 * Implementations of this interface are registered with {@link Tracker} to filter out tracking activities and tracking
 * events. {@link #isTrackingEnabled(Tracker, OpLevel, Object...)} is called from {@link Tracker} instances when
 * activities and events are created using {@code Tracker.newEvent()} and {@code Tracker.newActivity} calls.
 * <p>
 * Any activity or event with {@code OpType.NOOP} is automatically filtered out.
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
	 *            a set of arguments passed to {@code Tracker.newEvent} and {@code Tracker.newActivity} calls
	 * @return true if tracking is enabled
	 * @see Tracker
	 */
	boolean isTrackingEnabled(Tracker tracker, OpLevel level, Object... args);
}
