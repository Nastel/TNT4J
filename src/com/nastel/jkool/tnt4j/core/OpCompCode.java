/*
 * Copyright (c) 2009 Nastel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Nastel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with Nastel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 *
 */
package com.nastel.jkool.tnt4j.core;

/**
 * Provides list of valid Operation completion codes.
 *
 * @see Operation
 * @version $Revision: 2 $
 */
public enum OpCompCode  {
	SUCCESS, WARNING, ERROR;

	private static OpCompCode[] enumList = OpCompCode.values();


	/**
	 * Converts the specified value to a member of the enumeration.
	 *
	 * @param value enumeration value to convert
	 * @return enumeration member
	 * @throws IllegalArgumentException if there is no
	 *         member of the enumeration with the specified value
	 */
	public static OpCompCode valueOf(int value) {
		int ordnl = value;
		if (ordnl < 0 || ordnl >= enumList.length)
			throw new IllegalArgumentException("value '" + value + "' is not valid for enum OpCompCode");
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
	public static OpCompCode valueOf(Object value) {
		if (value == null)
			throw new NullPointerException("object must be non-null");
		if (value instanceof Number)
			return valueOf(((Number)value).intValue());
		else if (value instanceof String)
			return valueOf(value.toString());
		throw new IllegalArgumentException("Cannot convert object of type '" + value.getClass().getName() + "' enum OpCompCode");
	}
}
