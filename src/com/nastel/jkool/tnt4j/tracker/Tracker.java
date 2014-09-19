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
import com.nastel.jkool.tnt4j.core.KeyValueStats;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.core.Snapshot;
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
public interface Tracker extends Handle, KeyValueStats {
	static final String KEY_ACTIVITY_COUNT = "tracker-activities";
	static final String KEY_EVENT_COUNT = "tracker-events";
	static final String KEY_MSG_COUNT = "tracker-msgs";
	static final String KEY_SNAPSHOT_COUNT = "tracker-snapshots";
	static final String KEY_ACTIVITIES_STARTED = "tracker-started-activities";
	static final String KEY_ACTIVITIES_STOPPED = "tracker-stopped-activities";
	static final String KEY_NOOP_COUNT = "tracker-track-noops";
	static final String KEY_ERROR_COUNT = "tracker-track-errors";
	static final String KEY_STACK_DEPTH = "tracker-track-stack-depth";
	static final String KEY_OVERHEAD_NANOS = "tracker-total-overhead-nanos";
	
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
	 * Obtains the top most active <code>TrackingActivity</code> instance
	 * at the top of the stack, <code>NullActivity</code> if none is available.
	 * Current activity is within the scope of the current thread.
	 * 
	 * @return current active tracking activity or <code>NullActivity</code> when no such activity exists.
	 * @see NullActivity
	 */
	public TrackingActivity getCurrentActivity();

	/**
	 * Obtains the bottom most active <code>TrackingActivity</code> instance
	 * at the bottom of the stack, <code>NullActivity</code> if none is available.
	 * This represents the root (first) activity.
	 * Root activity is within the scope of the current thread.
	 * 
	 * @return root tracking activity or <code>NullActivity</code> when no such activity exists.
	 * @see NullActivity
	 */
	public TrackingActivity getRootActivity();

	/**
	 * Obtains current stack trace based on nested activity execution for the current thread.
	 * 
	 * @return stack trace of nested tracking activities
	 */
	public StackTraceElement[] getStackTrace();

	/**
	 * Obtains current stack of nested tracking activities for the current thread.
	 * 
	 * @return current stack of nested tracking activities
	 */
	public TrackingActivity[] getActivityStack();
	
	/**
	 * Obtains current size of nested activity stack for the current thread.
	 * 
	 * @return current size of nested activity stack.
	 */
	public int getStackSize();
	
	/**
	 * Create a new application activity via <code>TrackingActivity</code> object instance.
	 * NOOP activity instance <code>NullActivity</code> is returned 
	 * when <code>TrackingFilter</code> is set and returns false.
	 * 
	 * @return a new application activity object instance with severity set to OpLevel.INFO.
	 * @see TrackingActivity
	 */
	public TrackingActivity newActivity();

	/**
	 * Create a new application activity via <code>TrackingActivity</code> object instance.
	 * NOOP activity instance <code>NullActivity</code> is returned 
	 * when <code>TrackingFilter</code> is set and returns false.
	 * 
	 * @param level activity severity level
	 * @return a new application activity object instance
	 * @see TrackingActivity
	 */
	public TrackingActivity newActivity(OpLevel level);

	/**
	 * Create a new application activity via <code>TrackingActivity</code> object instance.
	 * NOOP activity instance <code>NullActivity</code> is returned 
	 * when <code>TrackingFilter</code> is set and returns false.
	 * 
	 * @param level activity severity level
	 * @param name user defined logical name of the activity
	 * @return a new application activity object instance
	 * @see TrackingActivity
	 */
	public TrackingActivity newActivity(OpLevel level, String name);

	/**
	 * Create a new application activity via <code>TrackingActivity</code> object instance.
	 * NOOP activity instance <code>NullActivity</code> is returned 
	 * when <code>TrackingFilter</code> is set and returns false.
	 * 
	 * @param level activity severity level
	 * @param name activity name
	 * @param signature user defined activity signature (should be unique)
	 * @return a new application activity object instance
	 * @see TrackingActivity
	 */
	public TrackingActivity newActivity(OpLevel level, String name, String signature);

	/**
	 * Create a new application snapshot via <code>Snapshot</code> object instance.
	 * 
	 * @param cat category name
	 * @param name snapshot name
	 * @return a new application metric snapshot
	 * @see Snapshot
	 */
	public Snapshot newSnapshot(String cat, String name);
	
	/**
	 * Create a new application snapshot via <code>Snapshot</code> object instance.
	 * 
	 * @param cat category name
	 * @param name snapshot name
	 * @param level activity severity level
	 * @return a new application metric snapshot
	 * @see Snapshot
	 */
	public Snapshot newSnapshot(String cat, String name, OpLevel level);

	/**
	 * Track and Trace a single application tracking activity
	 * Activities of type <code>OpType.NOOP</code> are ignored.
	 * 
	 * @param activity application activity to be reported
	 * @see TrackingActivity
	 */
	public void tnt(TrackingActivity activity);
	
	/**
	 * Track and Trace a single application tracking event as 
	 * a separate application activity.
	 * Events of type <code>OpType.NOOP</code> are ignored.
	 * 
	 * @param event application tracking event to be reported
	 * @see TrackingEvent
	 */
	public void tnt(TrackingEvent event);
	

	/**
	 * Log a given property snapshot with a given severity
	 *
	 * @param snapshot a set of properties
	 * @see Snapshot
	 */
	public void tnt(Snapshot snapshot);

	/**
	 * Log a given string message with a specified severity
	 *
	 * @param sev message severity to log
	 * @param msg string message to be logged
	 * @param args arguments passed along the message
	 * @see OpLevel
	 */
	public void log(OpLevel sev, String msg, Object...args);

	/**
	 * Register a tracking filter associated with the tracker.
	 * Tracking filter allows consolidation of all conditional tracking
	 * logic into a single class. Setting the value to null disables the tracking
	 * filter check and none of newly created activities and events are filtered out.
	 * 
	 * @see TrackingFilter
	 */
	public void setTrackingFilter(TrackingFilter filter);
	
	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 * NOOP event instance <code>NullEvent</code> is returned 
	 * when <code>TrackingFilter</code> is set and returns false.
	 *
	 * @param severity severity level
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param msg text message associated with this event
	 * @param args argument list passed along with the message
	 */
	public TrackingEvent newEvent(OpLevel severity, String opName, String correlator, String msg, Object...args);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * NOOP event instance <code>NullEvent</code> is returned 
	 * when <code>TrackingFilter</code> is set and returns false.
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param tag associated with this event 
	 * @param msg text message associated with this event
	 * @param args argument list passed along with the message
	 * @see OpLevel
	 * @see OpType
	 */
	public TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, String correlator, String tag, String msg, Object...args);	
	
	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 * NOOP event instance <code>NullEvent</code> is returned 
	 * when <code>TrackingFilter</code> is set and returns false.
	 *
	 * @param severity severity level
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param msg binary message associated with this event
	 * @param args argument list passed along with the message
	 */
	public TrackingEvent newEvent(OpLevel severity, String opName, String correlator, byte[] msg, Object...args);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * NOOP event instance <code>NullEvent</code> is returned 
	 * when <code>TrackingFilter</code> is set and returns false.
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param tag associated with this event 
	 * @param msg binary message associated with this event
	 * @param args argument list passed along with the message
	 * @see OpLevel
	 * @see OpType
	 */
	public TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, String correlator, String tag, byte[] msg, Object...args);	
}
