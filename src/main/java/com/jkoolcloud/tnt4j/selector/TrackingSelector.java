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

import java.util.Iterator;

import com.jkoolcloud.tnt4j.core.Handle;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.repository.TokenRepository;

/**
 * <p>Classes that implement this interface provide implementation for
 * the {@link TrackingSelector} which allows conditional logging based on given sev/key/value combination.</p>
 *
 * @see OpLevel
 *
 * @version $Revision: 3 $
 *
 */
public interface TrackingSelector extends Handle {
	/**
	 * Determine if a particular sev/key combination is trackable
	 * Use this method to determine if tracking is enabled/disabled
	 * for a specific sev/key pair.
	 *
	 * {@code TrackingLogger.isSet(OpLevel.INFO, "orderapp.order.id");}
	 *
	 * @param sev severity of to be checked
	 * @param key key associated with tracking activity
	 * @return true if severity level set for given key
	 * @see OpLevel
	 */
	boolean isSet(OpLevel sev, Object key);

	/**
	 * Determine if a particular sev/key/value combination is trackable
	 * Use this method to determine if tracking is enabled/disabled
	 * for a specific key/value pair. Example, checking if order id
	 * "723772" is trackable:
	 *
	 * {@code TrackingLogger.isSet(OpLevel.INFO, "orderapp.order.id", "723772");}
	 *
	 * @param sev severity of to be checked
	 * @param key key associated with tracking activity
	 * @param value associated value with a given key
	 * @return true if severity level set for given key with specified value
	 * @see OpLevel
	 */
	boolean isSet(OpLevel sev, Object key, Object value);

	/**
	 * Set sev/key/value combination for tracking
	 *
	 * @param sev severity of to be checked
	 * @param key key associated with tracking activity
	 * @param value associated value with a given key
	 *
	 * @see OpLevel
	 */
	void set(OpLevel sev, Object key, Object value);

	/**
	 * Set sev/key combination for tracking. This is the same as calling
	 * {@code set(sev, key, null)}, where value is null.
	 *
	 * @param sev severity of to be checked
	 * @param key key associated with tracking activity
	 *
	 * @see OpLevel
	 */
	void set(OpLevel sev, Object key);

	/**
	 * Get value of the specific key
	 *
	 * @param key key associated with tracking activity
	 * @return value associated with a given key
	 *
	 */
	Object get(Object key);

	/**
	 * Determine of tracking selector is valid and defined
	 * Undefined {@link TrackingSelector} does not have
	 * defined token repository and isSet() calls will either
	 * return all true or false depending on the value of
	 * <code>tnt4j.selector.undefined.isset=true</code>
	 * property. Default is true.
	 *
	 * @return true if repository is defined, false otherwise
	 */
	boolean isDefined();

	/**
	 * Determine if a specific key exists
	 *
	 * @param key key associated with tracking activity
	 * @return true if exists, false otherwise
	 *
	 */
	boolean exists(Object key);

	/**
	 * Clear value for the specific key
	 *
	 * @param key key associated with tracking activity
	 *
	 */
	void remove(Object key);

	/**
	 * Obtain a list of keys available in the selector
	 *
	 * @return iterator containing all available keys
	 */
	Iterator<? extends Object> getKeys();

	/**
	 * Obtain an instance of the token repository associated with this selector
	 *
	 * @return handle to the token repository
	 * @see TokenRepository
	 */
	TokenRepository getRepository();

	/**
	 * Set an instance of the token repository associated with this selector
	 *
	 * @param repo token repository implementation
	 * @see TokenRepository
	 */
	void setRepository(TokenRepository repo);
}
