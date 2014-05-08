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


/**
 * <p>A simple event listener interface for configuration observers.
 * This interface can be implemented by classes that are interested in "raw" events caused by repository objects. 
 * Each manipulation on a token repository object will generate such an event. 
 * There is only a single method that is invoked when an event occurs..</p>
 *
 * @see TokenRepositoryEvent
 *
 * @version $Revision: 1 $
 *
 */
public interface TokenRepositoryListener {
	/**
	 * Notifies this listener about a manipulation on a monitored repository object.
	 * 
	 * @param event the event describing the manipulation
	 * 
	 * @see TokenRepositoryEvent
	 */
	public void repositoryChanged(TokenRepositoryEvent event);
	
	/**
	 * Notifies this listener about an error on a monitored repository object.
	 * 
	 * @param event the event describing the error
	 * 
	 * @see TokenRepositoryEvent
	 */
	public void repositoryError(TokenRepositoryEvent event);
	
}
