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
package com.nastel.jkool.tnt4j.repository;

import java.util.Iterator;

import com.nastel.jkool.tnt4j.core.Handle;


/**
 * <p>Classes that implement this interface provide implementation for
 * the <code>TokenRepository</code> which provides
 * interface to the underlying tokens(key/value pairs).</p>
 *
 *
 * @version $Revision: 2 $
 *
 */
public interface TokenRepository extends Handle {
	/**
	 * EVENT_EXCEPTION indicates that an exception has occurred in the underlying repository
	 */
	public static final int EVENT_EXCEPTION = 0;

	/**
	 * EVENT_ADD_KEY indicates that a new key/value pair has been added
	 */
	public static final int EVENT_ADD_KEY = 1;

	/**
	 * EVENT_SET_KEY indicates that an existing key/value pair has been set
	 */
	public static final int EVENT_SET_KEY = 2;

	/**
	 * EVENT_CLEAR_KEY indicates that an existing key/value pair has been removed
	 */
	public static final int EVENT_CLEAR_KEY = 3;

	/**
	 * EVENT_CLEAR indicates that repository has been cleared
	 */
	public static final int EVENT_CLEAR = 4;

	/**
	 * EVENT_CLEAR indicates that repository has been reloaded/refreshed
	 */
	public static final int EVENT_RELOAD = 5;

	/**
	 * Determine of the token repository is defined.
	 * Undefined <code>TokenRepository<code> will always fail to open.
	 *
	 * @return true if repository is defined, false otherwise
	 */
	boolean isDefined();

	/**
	 * Obtain a name associated with the repository.
	 *
	 * @return repository name
	 */
	String getName();

	/**
	 * Removes the mapping for the specified key from this repository if present.
	 *
	 * @param key key whose mapping is to be removed from the repository
	 */
	void remove(String key);

	/**
	 * Obtain the value associated with the given key
	 *
	 * @param key key whose mapping is to be obtained
	 * @return get the value associated with the specified key
	 */
	Object get(String key);

	/**
	 * Set the key/value pair within the repository
	 *
	 * @param key key whose mapping is to be obtained
	 * @param value value associated with the key
	 */
	void set(String key, Object value);

	/**
	 * Obtain a list of keys available in the repository
	 *
	 * @return iterator containing all available keys
	 */
	Iterator<? extends Object> getKeys();

	/**
	 * Register a repository listener for notifications in change of state of
	 * the underlying repository.
	 *
	 * @param listener token repository listener to register
	 * @see TokenRepositoryListener
	 */
	void addRepositoryListener(TokenRepositoryListener listener);

	/**
	 * Remove a repository listener for notifications in change of state of
	 * the underlying repository.
	 *
	 * @param listener token repository listener to remove
	 * @see TokenRepositoryListener
	 */
	void removeRepositoryListener(TokenRepositoryListener listener);
}
