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
package com.nastel.jkool.tnt4j.tracker;

import com.nastel.jkool.tnt4j.config.TrackerConfig;
import com.nastel.jkool.tnt4j.utils.Utils;


/**
 * <p><code>DefaultTrackerFactory</code> lets developers obtain an instance to the <code>Tracker</code> logger.
 * Source should obtain a logger instance per thread</p>
 *
 * <p>A <code>TrackingEvent</code> represents a specific tracking event that application creates for 
 * every discrete activity such as JDBC, JMS, SOAP or any other relevant application activity. 
 * Source developers must obtain a <code>Tracker</code> instance via <code>DefaultTrackerFactory</code>, create
 * instances of <code>TrackingActivity</code> and use <code>Tracker.tnt()</code> to report application activities.
 *
 * <p><code>TrackingActivity start()/stop()</code> calls are used to mark application activity boundaries.</p>
 *
 * @see Tracker
 * @see TrackingEvent
 * @see TrackingActivity
 * @see TrackerConfig
 *
 * @version $Revision: 9 $
 *
 */
public class DefaultTrackerFactory implements TrackerFactory {
	
	/**
	 * Create a new instance of <code>DefaultTrackerFactory</code> with a default 
	 * tracker configuration <code>TrackerConfig</code> instance.
	 * 
	 * @see TrackerConfig
	 */
	public DefaultTrackerFactory() {
	}
		
	@Override
    public Tracker getInstance(TrackerConfig tconfig) {
	    return new TrackerImpl(tconfig);
    }	
	
	@Override
	public void close(Tracker tr) {
		Utils.close(tr);
	}
}
