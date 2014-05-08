package com.nastel.jkool.tnt4j.core;


/**
 * Provides list of valid operation message severity levels
 *
 * @see Operation
 * @version $Revision: 2 $
 */
public enum OpLevel {
	UNKNOWN, TRACE, DEBUG, INFO, SUCCESS, WARNING, ERROR, FAILURE, CRITICAL, FATAL, HALT;

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
	 * Converts the specified object to a member of the enumeration.
	 *
	 * @param value object to convert
	 * @return enumeration member
	 * @throws NullPointerException if value is <code>null</code>
	 * @throws IllegalArgumentException if object cannot be matched to a
	 *  member of the enumeration
	 */
	public static OpLevel valueOf(Object value) {
		if (value == null)
			throw new NullPointerException("object must be non-null");
		if (value instanceof Number)
			return valueOf(((Number)value).intValue());
		else if (value instanceof String)
			return valueOf(value.toString());
		throw new IllegalArgumentException("Cannot convert object of type '" + value.getClass().getName() + "' enum OpLevel");
	}
}
