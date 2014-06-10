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
package com.nastel.jkool.tnt4j.tracker;

import com.nastel.jkool.tnt4j.config.TrackerConfig;
import com.nastel.jkool.tnt4j.core.Activity;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.selector.TrackingSelector;
import com.nastel.jkool.tnt4j.sink.EventSink;
import com.nastel.jkool.tnt4j.sink.Handle;
import com.nastel.jkool.tnt4j.source.Source;


/**
 * <p>Classes that implement this interface provide implementation for 
 * the <code>Tracker</code> logger.</p>
 *
 * <p>A <code>TrackingEvent</code> represents a specific tracking event that application creates for 
 * every discrete activity such as JDBC, JMS, SOAP or any other relevant application activity. 
 * Source developers must obtain a <code>Tracker</code> instance via <code>TrackingLogger</code>, create
 * instances of <code>TrackingEvent</code> and use <code>tnt()</code> method to report and log tracking activities
 * and associated tracking events.
 *
 * <p>A <code>Tracker</code> <code>newActivity()</code> method should be used to create application activities <code>TrackingActivity</code>.
 * <code>Tracker</code> instance should be obtained per thread or use a helper <code>TrackingLogger</code> class.</p>
 *
 * @see TrackingEvent
 * @see OpLevel
 * @see DefaultTrackerFactory
 * @see Activity
 *
 * @version $Revision: 5 $
 *
 */
public interface Tracker extends Handle {
	
	/**
	 * Obtains current/active <code>Source</code> handle associated 
	 * with the current thread. 
	 * 
	 * @return current active source handle associated with this thread
	 * @see Source
	 */
	public Source getSource();

	/**
	 * Obtains current <code>TrackingSelector</code> associated with this <code>Tracker</code>
	 * instance. Tracking selectors allow conditional logging based on a sev/key/value combinations
	 * 
	 * @return current <code>TrackingSelector</code> instance associated with the current tracker
	 * @see TrackingSelector
	 */
	public TrackingSelector getTrackingSelector();

	/**
	 * Obtains current <code>EventSink</code> associated with this <code>Tracker</code>
	 * instance.
	 * 
	 * @return current <code>EventSink</code> instance
	 * @see EventSink
	 */
	public EventSink getEventSink();

	/**
	 * Obtains current/active <code>TrackerConfig</code> configuration associated with 
	 * the current tracker instance. 
	 * 
	 * @return current tracking configuration
	 * @see TrackerConfig
	 */
	public TrackerConfig getConfiguration();

	/**
	 * Create a new application activity via <code>TrackingActivity</code> object instance.
	 * 
	 * @return a new application activity object instance
	 * @see TrackingActivity
	 */
	public TrackingActivity newActivity();

	/**
	 * Create a new application activity via <code>TrackingActivity</code> object instance.
	 * 
	 * @param signature user defined activity signature (should be unique)
	 * @return a new application activity object instance
	 * @see TrackingActivity
	 */
	public TrackingActivity newActivity(String signature);

	/**
	 * Track and Trace a single application tracking activity
	 * 
	 * @param activity application activity to be reported
	 * @see TrackingActivity
	 */
	public void tnt(TrackingActivity activity);
	
	/**
	 * Track and Trace a single application tracking event as 
	 * a separate application activity. This method creates a 
	 * new <code>TrackingActivity</code> and associates given
	 * event with the newly created activity. 
	 * 
	 * @param event application tracking event to be reported
	 * @see TrackingEvent
	 */
	public void tnt(TrackingEvent event);
	
	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 *
	 * @param severity severity level
	 * @param msg text message associated with this event
	 * @param opName operation name associated with this event (tracking event name)
	 * @see OpLevel
	 */
	public TrackingEvent newEvent(OpLevel severity, String msg, String opName);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 *
	 * @param severity severity level
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param msg text message associated with this event
	 * @param opName operation name associated with this event (tracking event name)
	 */
	public TrackingEvent newEvent(OpLevel severity, String correlator, String msg, String opName);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param msg text message associated with this event
	 * @param opName operation name associated with this event (tracking event name)
	 * @see OpType
	 * @see OpLevel
	 */
	public TrackingEvent newEvent(OpLevel severity, OpType opType, String msg, String opName);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param msg text message associated with this event
	 * @param opName operation name associated with this event (tracking event name)
	 * @see OpLevel
	 * @see OpType
	 */
	public TrackingEvent newEvent(OpLevel severity, OpType opType, String correlator, String msg, String opName);	
}
