/*
 * Copyright 2014 Nastel Technologies, Inc.
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
package com.nastel.jkool.tnt4j.format;

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * Classes that implement this interface provide implementation for the <code>EventFormatter</code> interface.
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
	 * Format a given <code>TrackingEvent</code> and return a string
	 *
	 * @param event tracking event instance to be formatted
	 * @see TrackingEvent
	 */
	public String format(TrackingEvent event);
	
	/**
	 * Format a given <code>TrackingActivity</code> and return a string
	 *
	 * @param activity tracking activity instance to be formatted
	 * @see TrackingActivity
	 */
	public String format(TrackingActivity activity);

	/**
	 * Format a given message and severity level combo
	 *
	 * @param level severity level
	 * @param msg message to be formatted
	 * @param args arguments associated with the object
	 * @see OpLevel
	 */
	public String format(OpLevel level, String msg, Object...args);
}
