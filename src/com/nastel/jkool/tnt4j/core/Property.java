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
package com.nastel.jkool.tnt4j.core;

import java.util.Date;


/**
 * <p>Implements a Property entity.</p>
 *
 * <p>A <code>Property</code> object is associated with a <code>PropertySnapshot</code> and
 * represents a name=value pair.
 * </p>
 *
 * @see Activity
 *
 * @version $Revision: 7 $
 */
public class Property {
	private String  		key;
	private Object			value;


	/**
	 * Constructs a Property objects with the specified properties.
	 *
	 * @param key key of property
	 * @param value value for property
	 */
	public Property(String key, Object value) {
		set(key, value);
	}


	/**
	 * Sets the type of property.
	 *
	 * @param key of property
	 * @param val property value
	 */
	public void set(String key, Object val) {
		this.key = key;
		this.value = val;
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
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + key.length();

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Property))
			return false;

		final Property other = (Property) obj;
		return key.equals(other.key);
	}

	/**
	 * Obtain the language independent value type
	 * of the property
	 * 
	 * @return string representation of the value type
	 */
	public String getValueType() {
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
		} else {
			return "object";			
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final String key = getKey();
		StringBuilder str = new StringBuilder();

		str.append(getClass().getSimpleName()).append("{")
		   .append("Name:").append(key).append(",")
		   .append("Value:").append(getValue()).append(",")
		   .append("Type:").append(getValueType()).append("}");

		return str.toString();
	}
}
