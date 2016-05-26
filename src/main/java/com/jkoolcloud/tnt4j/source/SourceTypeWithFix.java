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
package com.jkoolcloud.tnt4j.source;

/**
 * Provides list of valid source types. Source is a logical nested entity that expresses container
 * of applications, servers, application servers, runtimes, etc.
 *
 * @see Source
 * @version $Revision: 1 $
 */
public enum SourceTypeWithFix {
	GENERIC, USER, APPL, PROCESS, APPSERVER, SERVER, RUNTIME, VIRTUAL,
	NETWORK, DEVICE, NETADDR, GEOADDR, DATACENTER;

	private static SourceTypeWithFix[] enumList = SourceTypeWithFix.values();


	/**
	 * Returns the number of source types defined
	 *
	 * @return number of defined source types
	 */
	public static int length() {
		return enumList.length;		
	}
	
	/**
	 * Converts the specified value to a member of the enumeration.
	 *
	 * @param value enumeration value to convert
	 * @return enumeration member
	 * @throws IllegalArgumentException if there is no
	 *         member of the enumeration with the specified value
	 */
	public static SourceTypeWithFix valueOf(int value) {
		int ordnl = value;
		if (ordnl < 0 || ordnl >= enumList.length)
			throw new IllegalArgumentException("value '" + value + "' is not valid for enumeration SourceType");
		return enumList[ordnl];
	}

	/**
	 * Converts the specified object to a member of the enumeration.
	 *
	 * @param value object to convert
	 * @return enumeration member
	 * @throws NullPointerException if value is <code>null</code>
	 * @throws IllegalArgumentException if object cannot be matched to a
	 *  member of the enumeration
	 */
	public static SourceTypeWithFix valueOf(Object value) {
		if (value == null)
			throw new NullPointerException("object must be non-null");
		if (value instanceof Number)
			return valueOf(((Number)value).intValue());
		else if (value instanceof String)
			return valueOf(value.toString());
		throw new IllegalArgumentException("Cannot convert object of type '" + value.getClass().getName() + "' enum SourceType");
	}
}
