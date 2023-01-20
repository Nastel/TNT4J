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
package com.jkoolcloud.tnt4j.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * Implements a Property entity.
 * <p>
 * A {@code Property} object represents a name=value pair.
 *
 * @see ValueTypes
 *
 * @version $Revision: 7 $
 */
public class Property {
	private String key;
	private Object value;
	private String valueType = ValueTypes.VALUE_TYPE_NONE;
	private boolean transient_;

	/**
	 * Constructs a Property objects with the specified properties.
	 *
	 * @param key
	 *            key of property
	 * @param value
	 *            value for property
	 */
	public Property(String key, Object value) {
		this(key, value, false);
	}

	/**
	 * Constructs a Property objects with the specified properties.
	 *
	 * @param key
	 *            key of property
	 * @param value
	 *            value for property
	 * @param transient_
	 *            flag indicating whether property is transient
	 */
	public Property(String key, Object value, boolean transient_) {
		set(key, value);
		this.transient_ = transient_;
	}

	/**
	 * Constructs a Property objects with the specified properties.
	 *
	 * @param key
	 *            key of property
	 * @param value
	 *            value for property
	 * @param valType
	 *            value type such as (currency, percent)
	 */
	public Property(String key, Object value, String valType) {
		this(key, value, valType, false);
	}

	/**
	 * Constructs a Property objects with the specified properties.
	 *
	 * @param key
	 *            key of property
	 * @param value
	 *            value for property
	 * @param valType
	 *            value type such as (currency, percent)
	 * @param transient_
	 *            flag indicating whether property is transient
	 */
	public Property(String key, Object value, String valType, boolean transient_) {
		set(key, value, valType);
		this.transient_ = transient_;
	}

	/**
	 * Sets the type of property.
	 *
	 * @param key
	 *            of property
	 * @param val
	 *            property value
	 */
	public void set(String key, Object val) {
		set(key, val, ValueTypes.VALUE_TYPE_NONE);
	}

	/**
	 * Sets the type of property.
	 *
	 * @param key
	 *            of property
	 * @param val
	 *            property value
	 * @param valType
	 *            value type such as (currency, percent). See {@link ValueTypes}.
	 */
	public void set(String key, Object val, String valType) {
		this.key = key;
		this.value = val;
		this.valueType = ((StringUtils.equalsAnyIgnoreCase(valType, null, ValueTypes.VALUE_TYPE_NONE)
				&& (val instanceof Boolean)) ? ValueTypes.VALUE_TYPE_FLAG : valType);
	}

	/**
	 * Gets current value for property.
	 *
	 * @return property value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Gets current key for property.
	 *
	 * @return property key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets flag value of this property indicating if it is transient. Transient properties are not processed by
	 * {@link com.jkoolcloud.tnt4j.format.Formatter}.
	 *
	 * @return {@code true} if this property is transient, {@code false} - otherwise
	 */
	public boolean isTransient() {
		return transient_;
	}

	/**
	 * Sets flag value of this property indicating if it is transient. Transient properties are not processed by
	 * {@link com.jkoolcloud.tnt4j.format.Formatter}.
	 * 
	 * @param transient_
	 *            flag indicating whether property is transient
	 */
	public void setTransient(boolean transient_) {
		this.transient_ = transient_;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Property)) {
			return false;
		}

		Property other = (Property) obj;
		return key.equals(other.key);
	}

	/**
	 * Obtain the language independent value data type of the property.
	 * 
	 * @return string representation of the value data type
	 */
	public String getDataType() {
		if (value instanceof String) {
			return "string";
		} else if (value instanceof Long) {
			return "long";
		} else if (value instanceof Integer) {
			return "int";
		} else if (value instanceof Double) {
			return "double";
		} else if (value instanceof Float) {
			return "float";
		} else if (value instanceof Boolean) {
			return "bool";
		} else if (value instanceof Byte) {
			return "byte";
		} else if (value instanceof Short) {
			return "short";
		} else if (value instanceof Character) {
			return "char";
		} else if (value instanceof Date) {
			return "date";
		} else if (value instanceof UsecTimestamp) {
			return "date.usec";
		} else if (value instanceof BigInteger) {
			return "bint";
		} else if (value instanceof BigDecimal) {
			return "bdec";
		} else if (value != null) {
			return value.getClass().getSimpleName();
		} else {
			return "none";
		}
	}

	/**
	 * Obtain value type such as currency, percent, number, timestamp, etc. See {@link ValueTypes}.
	 * 
	 * @return string representation of the value type. See {@link ValueTypes}
	 */
	public String getValueType() {
		return valueType;
	}

	@Override
	public String toString() {
		String key = getKey();
		StringBuilder str = new StringBuilder();

		str.append(getClass().getSimpleName()).append("{") //
				.append("Name:").append(key).append(",") //
				.append("Value:").append(getValue()).append(",") //
				.append("Type:").append(getDataType()).append(",") //
				.append("Value-Type:").append(getValueType()).append(",") //
				.append("Transient:").append(isTransient()).append("}");

		return str.toString();
	}
}
