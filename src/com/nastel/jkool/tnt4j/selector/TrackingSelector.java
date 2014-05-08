/*
 * Copyright (c) 2013 Nastel Technologies, Inc. All Rights Reserved.
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
package com.nastel.jkool.tnt4j.selector;

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.repository.TokenRepository;
import com.nastel.jkool.tnt4j.sink.Handle;

/**
 * <p>Classes that implement this interface provide implementation for 
 * the <code>TrackingSelector</code> which allows conditional logging based on given sev/key/value combination.</p>
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
	 * <code>TrackingLogger.isSet(OpLevel.INFO, "orderapp.order.id");</code>
	 * 
	 * @param sev severity of to be checked
	 * @param key key associated with tracking activity
	 * 
	 * @see OpLevel
	 */
	public boolean isSet(OpLevel sev, Object key);
	
	/**
	 * Determine if a particular sev/key/value combination is trackable
	 * Use this method to determine if tracking is enabled/disabled
	 * for a specific key/value pair. Example, checking if order id 
	 * "723772" is trackable:
	 * 
	 * <code>TrackingLogger.isSet(OpLevel.INFO, "orderapp.order.id", "723772");</code>
	 * 
	 * @param sev severity of to be checked
	 * @param key key associated with tracking activity
	 * @param value associated value with a given key
	 * 
	 * @see OpLevel
	 */
	public boolean isSet(OpLevel sev, Object key, Object value);
	
	/**
	 * Set sev/key/value combination for tracking
	 * 
	 * @param sev severity of to be checked
	 * @param key key associated with tracking activity
	 * @param value associated value with a given key
	 * 
	 * @see OpLevel
	 */
	public void set(OpLevel sev, Object key, Object value);

	/**
	 * Set sev/key combination for tracking. This is the same as calling
	 * <code>set(sev, key, null)</code>, where value is null.
	 * 
	 * @param sev severity of to be checked
	 * @param key key associated with tracking activity
	 * 
	 * @see OpLevel
	 */
	public void set(OpLevel sev, Object key);

	/**
	 * Get value of the specific key
	 * 
	 * @param key key associated with tracking activity
	 * @return value associated with a given key
	 * 
	 */
	public Object get(Object key);

	/**
	 * Clear value for the specific key
	 * 
	 * @param key key associated with tracking activity
	 * 
	 */
	public void remove(Object key);
	
	/**
	 * Obtain an instance of the token repository associated with this selector
	 * 
	 * @return handle to the token repository
	 * @see TokenRepository
	 */
	public TokenRepository getRepository();
	
	/**
	 * Set an instance of the token repository associated with this selector
	 * 
	 * @param repo token repository implementation
	 * @see TokenRepository
	 */
	public void setRepository(TokenRepository repo);
	
}
