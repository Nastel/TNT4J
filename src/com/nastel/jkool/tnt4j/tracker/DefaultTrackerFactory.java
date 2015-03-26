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
