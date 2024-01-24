/*
 * Copyright 2014-2024 JKOOL, LLC.
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

/**
 * Provides list of valid Log types.
 *
 * @see LogEntry
 * 
 * @version $Revision: 1 $
 */
public enum LogType {
	GENERAL, ERROR, QUERY, SUBSCRIBE, TRIGGER, //
	@Deprecated
	TRACE, //
	AUDIT, ML, SCRIPT;

	private static LogType[] enumList = LogType.values();

	/**
	 * Converts the specified value to a member of the enumeration.
	 *
	 * @param value
	 *            enumeration value to convert
	 * @return enumeration member
	 * @throws IllegalArgumentException
	 *             if there is no member of the enumeration with the specified value
	 */
	public static LogType valueOf(int value) {
		int ordnl = value;
		if (ordnl < 0 || ordnl >= enumList.length) {
			throw new IllegalArgumentException("value '" + value + "' is not valid for enumeration LogType");
		}
		return enumList[ordnl];
	}

	/**
	 * Converts the specified object to a member of the enumeration.
	 *
	 * @param value
	 *            object to convert
	 * @return enumeration member
	 * @throws NullPointerException
	 *             if value is {@code null}
	 * @throws IllegalArgumentException
	 *             if object cannot be matched to a member of the enumeration
	 */
	public static LogType valueOf(Object value) {
		if (value == null) {
			throw new NullPointerException("object must be non-null");
		}
		if (value instanceof Number) {
			return valueOf(((Number) value).intValue());
		} else if (value instanceof String) {
			return valueOf(value.toString());
		}
		throw new IllegalArgumentException(
				"Cannot convert object of type '" + value.getClass().getName() + "' to enum LogType");
	}
}
