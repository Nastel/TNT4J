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
package com.jkoolcloud.tnt4j.selector;

import java.util.regex.Pattern;

import com.jkoolcloud.tnt4j.core.OpLevel;

/**
 * <p>
 * This class implements a property token that associates severity, key, value
 * and a matching pattern. Property tokens are used to match key/value/sev pairs with
 * a regexp pattern.
 * </p>
 * 
 * @see OpLevel
 *
 * @version $Revision: 3 $
 *
 */
class PropertyToken {
	Object key;
	String value;
	String vPattern;
	OpLevel sevLimit;
	Pattern valuePatten;

	/**
	 * Create a property token
	 *
	 * @param sev severity of to be checked
	 * @param k key associated with the token
	 * @param v value associated with the token
	 * @param vPtn value regexp pattern
	 * @see OpLevel
	 */
	public PropertyToken(OpLevel sev, Object k, String v, String vPtn) {
		key = k;
		value = v;
		sevLimit = sev;
		vPattern = vPtn;
		if (vPattern != null) {
			valuePatten = Pattern.compile(vPattern);
		}
	}

	/**
	 * Return value part of the token
	 * 
	 * @return value part of the token
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Return key part of the token
	 *
	 * @return key part of the token
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * Return regexp pattern part of the token
	 * 
	 * @return regexp pattern part of the token
	 */
	public String getPattern() {
		return vPattern;
	}

	/**
	 * Matches a given sev/key/value with this token
	 *
	 * @return true of matches, false otherwise
	 */
	public boolean isMatch(OpLevel sev, Object key, Object value) {
		boolean match;
		boolean sevMatch = (sev.ordinal() >= sevLimit.ordinal());
		match = sevMatch
		        && ((value != null && valuePatten != null)? valuePatten.matcher(value.toString()).matches(): true);		
		return match;
	}

	@Override
	public String toString() {
		return "Token{"
			+ key + ":" + value
			+ ", sev.level: " + sevLimit
			+ ", value.pattern: " + vPattern
			+ "}";
	}
}