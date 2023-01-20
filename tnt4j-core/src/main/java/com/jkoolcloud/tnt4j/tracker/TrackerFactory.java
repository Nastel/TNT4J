/*
 * Copyright 2014-2023 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.tracker;

import com.jkoolcloud.tnt4j.config.TrackerConfig;

/**
 * <p>
 * {@code TrackerFactory} interface allows creation of {@link com.jkoolcloud.tnt4j.tracker.Tracker} logger instances.
 * Developers should implement this interface when creating custom {@link com.jkoolcloud.tnt4j.tracker.Tracker} logger
 * factories.
 * </p>
 *
 * @see DefaultTrackerFactory
 *
 * @version $Revision: 4 $
 *
 */
public interface TrackerFactory {
	/**
	 * Obtain an instance to a {@link com.jkoolcloud.tnt4j.tracker.Tracker} logger. Each thread must obtain a logger
	 * instance. {@link com.jkoolcloud.tnt4j.tracker.Tracker} logger is not thread safe.
	 * 
	 * @param tconfig
	 *            tracking configuration associated with the tracking instance
	 * @return {@link com.jkoolcloud.tnt4j.tracker.Tracker} logger instance associated with this thread
	 */
	Tracker getInstance(TrackerConfig tconfig);

	/**
	 * Close and release resources associated with {@link com.jkoolcloud.tnt4j.tracker.Tracker} instance
	 * 
	 * @param tr
	 *            tracker instance to be closed
	 */
	void close(Tracker tr);
}
