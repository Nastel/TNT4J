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
package com.jkoolcloud.tnt4j.tracker;

import java.util.Collection;

import com.jkoolcloud.tnt4j.core.OpType;
import com.jkoolcloud.tnt4j.core.Property;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.config.TrackerConfig;
import com.jkoolcloud.tnt4j.core.Activity;
import com.jkoolcloud.tnt4j.core.Handle;
import com.jkoolcloud.tnt4j.core.KeyValueStats;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.ValueTypes;
import com.jkoolcloud.tnt4j.selector.TrackingSelector;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.uuid.UUIDFactory;


/**
 * <p>Classes that implement this interface provide implementation for
 * the {@link Tracker} logger.</p>
 *
 * <p>A {@link TrackingEvent} represents a specific tracking event that application creates for
 * every discrete activity such as JDBC, JMS, SOAP or any other relevant application activity.
 * Source developers must obtain a {@link Tracker} instance via {@code TrackingLogger}, create
 * instances of {@link TrackingEvent} and use {@code tnt(...)} method to report and 
 * log tracking activities and associated tracking events.
 *
 * <p>A {@link Tracker} {@code #newActivity()} method should be used to create application activities
 * {@link TrackingActivity}. {@link Tracker} instance should be obtained per thread or 
 * use a helper {@code TrackingLogger} class.
 * </p>
 *
 * @see TrackingEvent
 * @see OpLevel
 * @see DefaultTrackerFactory
 * @see Activity
 *
 * @version $Revision: 5 $
 *
 */
public interface Tracker extends Handle, KeyValueStats, UUIDFactory {
	String KEY_ACTIVITY_COUNT = "tracker-activities";
	String KEY_EVENT_COUNT = "tracker-events";
	String KEY_MSG_COUNT = "tracker-messages";
	String KEY_SNAPSHOT_COUNT = "tracker-snapshots";
	String KEY_ACTIVITIES_STARTED = "tracker-started";
	String KEY_ACTIVITIES_STOPPED = "tracker-stopped";
	String KEY_NOOP_COUNT = "tracker-noops";
	String KEY_ERROR_COUNT = "tracker-errors";
	String KEY_STACK_DEPTH = "tracker-stack-depth";
	String KEY_OVERHEAD_USEC = "tracker-overhead-usec";


	/**
	 * Obtains unique tracker id
	 *
	 * @return unique tracker id
	 */
	String getId();

	/**
	 * Obtains current/active {@link Source} handle associated
	 * with the current thread.
	 *
	 * @return current active source handle associated with this thread
	 * @see Source
	 */
	Source getSource();

	/**
	 * Obtains current {@link TrackingSelector} associated with this {@link Tracker}
	 * instance. Tracking selectors allow conditional logging based on a sev/key/value combinations
	 *
	 * @return current {@link TrackingSelector} instance associated with the current tracker
	 * @see TrackingSelector
	 */
	TrackingSelector getTrackingSelector();

	/**
	 * Obtains current {@link EventSink} associated with this {@link Tracker}
	 * instance.
	 *
	 * @return current {@link EventSink} instance
	 * @see EventSink
	 */
	EventSink getEventSink();

	/**
	 * Obtains current/active {@link TrackerConfig} configuration associated with
	 * the current tracker instance.
	 *
	 * @return current tracking configuration
	 * @see TrackerConfig
	 */
	TrackerConfig getConfiguration();

	/**
	 * Obtains the top most active {@link TrackingActivity} instance
	 * at the top of the stack, {@link NullActivity} if none is available.
	 * Current activity is within the scope of the current thread.
	 *
	 * @return current active tracking activity or {@link NullActivity} when no such activity exists.
	 * @see NullActivity
	 */
	TrackingActivity getCurrentActivity();

	/**
	 * Obtains the bottom most active {@link TrackingActivity} instance
	 * at the bottom of the stack, {@link NullActivity} if none is available.
	 * This represents the root (first) activity.
	 * Root activity is within the scope of the current thread.
	 *
	 * @return root tracking activity or {@link NullActivity} when no such activity exists.
	 * @see NullActivity
	 */
	TrackingActivity getRootActivity();

	/**
	 * Obtains current stack trace based on nested activity execution for the current thread.
	 *
	 * @return stack trace of nested tracking activities
	 */
	StackTraceElement[] getStackTrace();

	/**
	 * Obtains current stack of nested tracking activities for the current thread.
	 *
	 * @return current stack of nested tracking activities
	 */
	TrackingActivity[] getActivityStack();

	/**
	 * Obtains current size of nested activity stack for the current thread.
	 *
	 * @return current size of nested activity stack.
	 */
	int getStackSize();

	/**
	 * Maintain thread context for all newly created activities.
	 * Activities are kept in the thread local context if set to true.
	 *
	 * @param flag {@code true} to maintain thread context, {@code false} to not
	 * @return itself
	 */
	Tracker setKeepThreadContext(boolean flag);

	/**
	 * Return true if thread context for all newly created activities is enabled.
	 * Activities are kept in the thread local context if set to true.
	 *
	 * @return true of thread context is enabled, false otherwise
	 */
	boolean getKeepThreadContext();

	/**
	 * Create a new application activity via {@link TrackingActivity} object instance.
	 * NOOP activity instance {@link NullActivity} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @return a new application activity object instance with severity set to OpLevel.INFO.
	 * @see TrackingActivity
	 */
	TrackingActivity newActivity();

	/**
	 * Create a new application activity via {@link TrackingActivity} object instance.
	 * NOOP activity instance {@link NullActivity} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param level activity severity level
	 * @return a new application activity object instance
	 * @see TrackingActivity
	 */
	TrackingActivity newActivity(OpLevel level);

	/**
	 * Create a new application activity via {@link TrackingActivity} object instance.
	 * NOOP activity instance {@link NullActivity} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param level activity severity level
	 * @param name user defined logical name of the activity
	 * @return a new application activity object instance
	 * @see TrackingActivity
	 */
	TrackingActivity newActivity(OpLevel level, String name);

	/**
	 * Create a new application activity via {@link TrackingActivity} object instance.
	 * NOOP activity instance {@link NullActivity} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param level activity severity level
	 * @param name activity name
	 * @param signature user defined activity signature (should be unique)
	 * @return a new application activity object instance
	 * @see TrackingActivity
	 */
	TrackingActivity newActivity(OpLevel level, String name, String signature);

	/**
	 * Create a new application snapshot via {@link Snapshot} object instance
	 * with default category and name as per tracker implementation.
	 *
	 * @param name snapshot name
	 * @return a new application metric snapshot
	 * @see Snapshot
	 */
	Snapshot newSnapshot(String name);

	/**
	 * Create a new application snapshot via {@link Snapshot} object instance.
	 *
	 * @param cat category name
	 * @param name snapshot name
	 * @return a new application metric snapshot
	 * @see Snapshot
	 */
	Snapshot newSnapshot(String cat, String name);

	/**
	 * Create a new application snapshot via {@link Snapshot} object instance.
	 *
	 * @param cat category name
	 * @param name snapshot name
	 * @param level activity severity level
	 * @return a new application metric snapshot
	 * @see Snapshot
	 */
	Snapshot newSnapshot(String cat, String name, OpLevel level);

	/**
	 * Create a new property via {@link Property} instance.
	 *
	 * @param key property key
	 * @param val property value
	 * @return valType value type {@link ValueTypes}
	 * @see Property
	 * @see ValueTypes
	 */
	Property newProperty(String key, Object val);

	/**
	 * Create a new property via {@link Property} instance.
	 *
	 * @param key property key
	 * @param val property value
	 * @return valType value type {@link ValueTypes}
	 * @see Property
	 * @see ValueTypes
	 */
	Property newProperty(String key, Object val, String valType);

	/**
	 * Track and Trace a single application tracking activity
	 * Activities of type {@code OpType.NOOP} are ignored.
	 *
	 * @param activity application activity to be reported
	 * @see TrackingActivity
	 */
	void tnt(TrackingActivity activity);

	/**
	 * Track and Trace a single application tracking event as
	 * a separate application activity.
	 * Events of type {@code OpType.NOOP} are ignored.
	 *
	 * @param event application tracking event to be reported
	 * @see TrackingEvent
	 */
	void tnt(TrackingEvent event);


	/**
	 * Log a given property snapshot with a given severity
	 *
	 * @param snapshot a set of properties
	 * @see Snapshot
	 */
	void tnt(Snapshot snapshot);

	/**
	 * Log a given string message with a specified severity
	 *
	 * @param sev message severity to log
	 * @param msg string message to be logged
	 * @param args arguments passed along the message
	 * @see OpLevel
	 */
	void log(OpLevel sev, String msg, Object...args);

	/**
	 * Register a tracking filter associated with the tracker.
	 * Tracking filter allows consolidation of all conditional tracking
	 * logic into a single class. Setting the value to null disables the tracking
	 * filter check and none of newly created activities and events are filtered out.
	 *
	 * @param filter tracking filter to register
	 * @see TrackingFilter
	 */
	void setTrackingFilter(TrackingFilter filter);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 * NOOP event instance {@code NullEvent} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param opName operation name associated with this event (tracking event name)
	 * @param msg text message associated with this event
	 * @param args argument list passed along with the message
	 * @return tracking event instance
	 */
	TrackingEvent newEvent(String opName, String msg, Object...args);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 * NOOP event instance {@code NullEvent} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param severity severity level
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param msg text message associated with this event
	 * @param args argument list passed along with the message
	 * @return tracking event instance
	 */
	TrackingEvent newEvent(OpLevel severity, String opName, String correlator, String msg, Object...args);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 * NOOP event instance {@code NullEvent} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param severity severity level
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlators associated with this event (could be unique or passed from a correlated activity)
	 * @param msg text message associated with this event
	 * @param args argument list passed along with the message
	 * @return tracking event instance
	 */
	TrackingEvent newEvent(OpLevel severity, String opName, Collection<String> correlators, String msg, Object...args);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * NOOP event instance {@code NullEvent} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param tag associated with this event
	 * @param msg text message associated with this event
	 * @param args argument list passed along with the message
	 * @return tracking event instance
	 * @see OpLevel
	 * @see OpType
	 */
	TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, String correlator, String tag, String msg, Object...args);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * NOOP event instance {@code NullEvent} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlators associated with this event (could be unique or passed from a correlated activity)
	 * @param tags associated with this event
	 * @param msg text message associated with this event
	 * @param args argument list passed along with the message
	 * @return tracking event instance
	 * @see OpLevel
	 * @see OpType
	 */
	TrackingEvent newEvent(OpLevel severity, OpType opType, String opName,  Collection<String> correlators,  Collection<String> tags, String msg, Object...args);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 * NOOP event instance {@code NullEvent} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param severity severity level
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param msg binary message associated with this event
	 * @param args argument list passed along with the message
	 * @return tracking event instance
	 */
	TrackingEvent newEvent(OpLevel severity, String opName, String correlator, byte[] msg, Object...args);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 * NOOP event instance {@code NullEvent} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param severity severity level
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlators associated with this event (could be unique or passed from a correlated activity)
	 * @param msg binary message associated with this event
	 * @param args argument list passed along with the message
	 * @return tracking event instance
	 */
	TrackingEvent newEvent(OpLevel severity, String opName, Collection<String> correlators, byte[] msg, Object...args);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * NOOP event instance {@code NullEvent} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param tag associated with this event
	 * @param msg binary message associated with this event
	 * @param args argument list passed along with the message
	 * @return tracking event instance
	 * @see OpLevel
	 * @see OpType
	 */
	TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, String correlator, String tag, byte[] msg, Object...args);

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * NOOP event instance {@code NullEvent} is returned
	 * when {@link TrackingFilter} is set and returns false.
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlators associated with this event (could be unique or passed from a correlated activity)
	 * @param tags associated with this event
	 * @param msg binary message associated with this event
	 * @param args argument list passed along with the message
	 * @return tracking event instance
	 * @see OpLevel
	 * @see OpType
	 */
	TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, Collection<String> correlators, Collection<String> tags, byte[] msg, Object...args);
}
