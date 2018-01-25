/*
 * Copyright 2014-2018 JKOOL, LLC.
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

/**
 * <p>
 * Classes that implement this interface provide implementation for the {@code Formatter} interface. This interface
 * allows formatting of any object to a string.
 * </p>
 *
 *
 * @version $Revision: 1 $
 *
 * @see DefaultFormatter
 */

public interface Formatter {
	/**
	 * Format a given object and return a string
	 *
	 * @param obj
	 *            object to be formatted as string
	 * @param args
	 *            arguments associated with the object
	 * @return formatted object
	 */
	String format(Object obj, Object... args);
}
