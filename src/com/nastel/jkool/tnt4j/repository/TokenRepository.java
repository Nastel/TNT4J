/*
 * Copyright (c) 2014 Nastel Technologies, Inc. All Rights Reserved.
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
 *
 */
package com.nastel.jkool.tnt4j.repository;

import java.util.Iterator;

import com.nastel.jkool.tnt4j.sink.Handle;


/**
 * <p>Classes that implement this interface provide implementation for 
 * the <code>TokenRepository</code> which provides 
 * interface to the underlying tokens(key/value pairs).</p>
 *
 *
 * @version $Revision: 2 $
 *
 */
public interface TokenRepository  extends Handle {
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
	 * Obtain a name associated with the repository.
	 * 
	 * @return repository name
	 */
	public String getName();

	/**
	 * Removes the mapping for the specified key from this repository if present.
	 * 
	 * @param key key whose mapping is to be removed from the repository
	 */
	public void remove(String key);

	/**
	 * Obtain the value associated with the given key
	 * 
	 * @param key key whose mapping is to be obtained
	 * @return get the value associated with the specified key
	 */
	public Object get(String key);

	/**
	 * Set the key/value pair within the repository
	 * 
	 * @param key key whose mapping is to be obtained
	 * @param value value associated with the key
	 */
	public void set(String key, Object value);

	/**
	 * Obtain a list of keys available in the repository
	 * 
	 * @return iterator containing all available keys
	 */
	public Iterator<String> getKeys();

	/**
	 * Register a repository listener for notifications in change of state of
	 * the underlying repository.
	 * 
	 * @see TokenRepositoryListener
	 */
	public void addRepositoryListener(TokenRepositoryListener listener);	
	
	/**
	 * Remove a repository listener for notifications in change of state of
	 * the underlying repository.
	 * 
	 * @see TokenRepositoryListener
	 */
	public void removeRepositoryListener(TokenRepositoryListener listener);	

}
