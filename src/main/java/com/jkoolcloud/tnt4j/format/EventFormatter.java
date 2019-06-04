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
package com.jkoolcloud.tnt4j.format;

import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * Classes that implement this interface provide implementation for the {@link EventFormatter} interface.
 * This interface allows formatting of any object, tracking objects as well as log messages
 * to a string format.
 * </p>
 *
 *
 * @version $Revision: 2 $
 *
 * @see Formatter
 */
public interface EventFormatter extends Formatter {
	/**
	 * Format a given {@link TrackingEvent} and return a string
	 *
	 * @param event tracking event instance to be formatted
	 * @return formatted tracking event
	 * @see TrackingEvent
	 */
	String format(TrackingEvent event);

	/**
	 * Format a given {@link TrackingActivity} and return a string
	 *
	 * @param activity tracking activity instance to be formatted
	 * @return formatted tracking event
	 * @see TrackingActivity
	 */
	String format(TrackingActivity activity);

	/**
	 * Format a given {@link Snapshot} and return a string
	 *
	 * @param snapshot snapshot object to be formatted
	 * @return formatted snapshot
	 * @see Snapshot
	 */
	String format(Snapshot snapshot);

	/**
	 * Format a given message and severity level combo
	 *
	 * @param ttl time to live in seconds
	 * @param src event source
	 * @param level severity level
	 * @param msg message to be formatted
	 * @param args arguments associated with the object
	 * @return formatted message and severity
	 * @see OpLevel
	 */
	String format(long ttl, Source src, OpLevel level, String msg, Object...args);
}
