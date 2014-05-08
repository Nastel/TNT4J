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
package com.nastel.jkool.tnt4j.core;

/**
 * <p>A simple event listener interface for tracking activity timing events.
 * This interface can be implemented by classes which need to be notified when
 * activities are started and stopped.</p>
 *
 * @see Activity
 *
 * @version $Revision: 1 $
 *
 */
public interface ActivityListener {
	/**
	 * Notifies this listener when activity is started.
	 * 
	 * @param activity activity which is just started
	 * 
	 */
	public void started(Activity activity);
	
	/**
	 * Notifies this listener when activity is stopped.
	 * 
	 * @param activity activity which is just stopped
	 * 
	 */
	public void stopped(Activity activity);
}
