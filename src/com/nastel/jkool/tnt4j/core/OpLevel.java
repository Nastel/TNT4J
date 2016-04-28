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
package com.nastel.jkool.tnt4j.core;

import com.nastel.jkool.tnt4j.utils.Utils;


/**
 * Provides list of valid operation message severity levels
 *
 * @see Operation
 * @version $Revision: 2 $
 */
public enum OpLevel {
	NONE, TRACE, DEBUG, INFO, NOTICE, WARNING, ERROR, FAILURE, CRITICAL, FATAL, HALT;

	public static final String ANY_LEVEL = "ANY";
	
	private static OpLevel[] enumList = OpLevel.values();

	/**
	 * Converts the specified value to a member of the enumeration.
	 *
	 * @param value enumeration value to convert
	 * @return enumeration member
	 * @throws IllegalArgumentException if there is no
	 *         member of the enumeration with the specified value
	 */
	public static OpLevel valueOf(int value) {
		int ordnl = value;
		if (ordnl < 0 || ordnl >= enumList.length)
			throw new IllegalArgumentException("value '" + value + "' is not valid for enumeration OpLevel");
		return enumList[ordnl];
	}

	/**
	 * Randomly select a level between TRACE and last level
	 *
	 * @return randomly selected level within the range
	 * @throws IllegalArgumentException if maxLevel < minLevel
	 */
	public static OpLevel anyLevel() {
		return valueOf(Utils.randomRange(TRACE.ordinal(), (enumList.length-1)));
	}
	
	/**
	 * Randomly select a level based on a given level range
	 *
	 * @param minLevel minimum level number
	 * @param maxLevel maximum level number
	 * @return randomly selected level within specified range
	 * @throws IllegalArgumentException if maxLevel < minLevel
	 */
	public static OpLevel anyLevel(int minLevel, int maxLevel) {
		return valueOf(Utils.randomRange(minLevel, maxLevel));
	}
	
	/**
	 * Converts the specified object to a member of the enumeration.
	 *
	 * @param value object to convert
	 * @return enumeration member
	 * @throws NullPointerException if value is {@code null}
	 * @throws IllegalArgumentException if object cannot be matched to a
	 *  member of the enumeration
	 */
	public static OpLevel valueOf(Object value) {
		if (value == null)
			throw new NullPointerException("object must be non-null");
		if (value instanceof Number) {
			return valueOf(((Number)value).intValue());
		} else if (value instanceof String) {
			if (value.toString().equalsIgnoreCase(ANY_LEVEL)) {
				return anyLevel();
			}
			return valueOf(value.toString());
		}
		throw new IllegalArgumentException("Cannot convert object of type '" + value.getClass().getName() + "' enum OpLevel");
	}
}
