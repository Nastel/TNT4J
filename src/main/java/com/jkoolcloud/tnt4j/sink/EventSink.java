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
package com.jkoolcloud.tnt4j.sink;

import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.core.TTL;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.core.KeyValueStats;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;

/**
 * <p>Classes that implement this interface provide implementation for
 * the <code>EventSink</code>, which provides an interface for underlying logging framework
 * storage destination. All event sink implementations should be wrapped with this interface.
 * </p>
 *
 * @see TTL
 * @see OpLevel
 * @see Source
 * @see TrackingEvent
 * @see TrackingActivity
 *
 * @version $Revision: 7 $
 *
 */
public interface EventSink extends Sink, TTL, KeyValueStats {
	
	/**
	 * Determine of sink is in error state -- meaning no successful write/log
	 * since last error;
	 *
	 * @return last error occurred when writing to sink, null if none
	 */
	boolean errorState();

	/**
	 * Set the sink into the error state due to some external condition.
	 * Error state to set to true if exception is not null, false otherwise.
	 *
	 * @param ex current error
	 * @return last error occurred when writing to sink, null if none
	 */
	Throwable setErrorState(Throwable ex);

	/**
	 * Get last error occurred when writing to sink
	 *
	 * @return last error occurred when writing to sink, null if none
	 */
	Throwable getLastError();

	/**
	 * Get time (ms) when last error occurred
	 *
	 * @return time (ms) when last error occurred
	 */
	long getLastErrorTime();

	/**
	 * Get total number of errors occurred on the sink
	 *
	 * @return total number of errors occurred on the sink
	 */
	long getErrorCount();
	
	/**
	 * Get rate limiter (throttle control)
	 *
	 * @return bps/mps rate limiter
	 * @see EventLimiter
	 */
	EventLimiter getLimiter();
	
	/**
	 * Set rate limiter (throttle control)
	 *
	 * @param limiter bps/mps rate limiter
	 * @see EventLimiter
	 */
	void setLimiter(EventLimiter limiter);
	
	/**
	 * Set current/active limiter with the sink
	 *
	 * @param src event source handle
	 * @see Source
	 */
	void setSource(Source src);

	/**
	 * Obtains current/active limiter handle associated
	 * with the current sink.
	 *
	 * @return current source handle
	 * @see Source
	 */
	Source getSource();

	/**
	 * Obtain name associated with this event sink instance
	 *
	 * @return name associated with the event sink instance
	 */
	String getName();

	/**
	 * Obtain event formatter instance associated with this sink
	 *
	 * @return event formatter instance
	 */
	EventFormatter getEventFormatter();


	/**
	 * Enable/disable filter checks on log() calls.
	 *
	 * @param flag {@code true} to enable filter checks, {@code false} to disable
	 * @return event sink instance
	 */
	EventSink filterOnLog(boolean flag);

	/**
	 * Check if a given event is loggable by the underlying sink -- passes all filters
	 *
	 * @param level
	 *            severity level of the event message
	 * @param msg
	 *            event message
	 * @param args
	 *            argument list passed along with the message
	 * @return true if event passed all filters, false otherwise
	 * @see OpLevel
	 */
	boolean isLoggable(OpLevel level, String msg, Object... args);

	/**
	 * Check if a given event is loggable by the underlying sink -- passes all filters
	 *
	 * @param source
	 *            message source
	 * @param level
	 *            severity level of the event message
	 * @param msg
	 *            event message
	 * @param args
	 *            argument list passed along with the message
	 * @return true if event passed all filters, false otherwise
	 * @see OpLevel
	 */
	boolean isLoggable(Source source, OpLevel level, String msg, Object... args);

	/**
	 * Check if a given event is loggable by the underlying sink -- passes all filters
	 *
	 * @param ttl
	 *            time to live in seconds {@link TTL}
	 * @param source
	 *            message source
	 * @param level
	 *            severity level of the event message
	 * @param msg
	 *            event message
	 * @param args
	 *            argument list passed along with the message
	 * @return true if event passed all filters, false otherwise
	 * @see OpLevel
	 */
	boolean isLoggable(long ttl, Source source, OpLevel level, String msg, Object... args);

	/**
	 * Check if a given event is loggable by the underlying sink -- passes all filters
	 *
	 * @param snapshot
	 *            snapshot
	 * @return true if event passed all filters, false otherwise
	 * @see OpLevel
	 */
	boolean isLoggable(Snapshot snapshot);

	/**
	 * Check if a given event is loggable by the underlying sink -- passes all filters
	 *
	 * @param activity
	 *            to be checked with registered filters
	 * @return true if tracking activity passed all filters, false otherwise
	 * @see TrackingActivity
	 */
	 boolean isLoggable(TrackingActivity activity);

	/**
	 * Check if a given event is loggable by the underlying sink -- passes all filters
	 *
	 * @param event
	 *            to be checked with registered filters
	 * @return true if tracking event passed all filters, false otherwise
	 * @see TrackingEvent
	 */
	boolean isLoggable(TrackingEvent event);

	/**
	 * Check of a certain log level is set/enabled
	 *
	 * @param sev severity level
	 * @return {@code true} if severity level set, {@code false} if not
	 * @see OpLevel
	 */
	boolean isSet(OpLevel sev);

	/**
	 * Log a given tracking event
	 *
	 * @param event tracking event to log
	 * @see TrackingEvent
	 */
	void log(TrackingEvent event);

	/**
	 * This method allows writing of <code>TrackingActivity</code> objects
	 * to the underlying destination.
	 *
	 * @param activity to be sent to the sink
	 * @see TrackingActivity
	 */
	void log(TrackingActivity activity);

	/**
	 * This method allows writing of <code>Snapshot<Property></code> objects
	 * to the underlying destination.
	 *
	 * @param snapshot a set of properties
	 * @see Snapshot
	 */
	void log(Snapshot snapshot);

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
	 * Log a given string message with a specified severity
	 *
	 * @param src log message source info
	 * @param sev message severity to log
	 * @param msg string message to be logged
	 * @param args arguments passed along the message
	 * @see OpLevel
	 */
	void log(Source src, OpLevel sev, String msg, Object...args);

	/**
	 * Log a given string message with a specified severity
	 *
	 * @param ttl time to live in seconds {@link TTL}
	 * @param src log message source info
	 * @param sev message severity to log
	 * @param msg string message to be logged
	 * @param args arguments passed along the message
	 * @see OpLevel
	 */
	void log(long ttl, Source src, OpLevel sev, String msg, Object...args);

	/**
	 * Register an event sink listener for notifications when errors
	 * occur when writing to event sink.
	 *
	 * @param listener event sink listener to register
	 * @see SinkErrorListener
	 */
	void addSinkErrorListener(SinkErrorListener listener);

	/**
	 * Remove an event sink listener for notifications when errors
	 * occur when writing to event sink.
	 *
	 * @param listener event sink listener to remove
	 * @see SinkErrorListener
	 */
	void removeSinkErrorListener(SinkErrorListener listener);

	/**
	 * Register an event sink listener for notifications when logging events
	 * occur when writing to event sink.
	 *
	 * @param listener event sink listener to register
	 * @see SinkLogEventListener
	 */
	void addSinkLogEventListener(SinkLogEventListener listener);

	/**
	 * Remove an event sink listener for notifications when logging events
	 * occur when writing to event sink.
	 *
	 * @param listener event sink listener to remove
	 * @see SinkLogEventListener
	 */
	void removeSinkLogEventListener(SinkLogEventListener listener);

	/**
	 * Register an event sink filter to selectively filter out certain events.
	 *
	 * @param listener event sink listener to register
	 * @see SinkEventFilter
	 */
	void addSinkEventFilter(SinkEventFilter listener);

	/**
	 * Remove an event sink filter.
	 *
	 * @param listener event sink listener to remove
	 * @see SinkEventFilter
	 */
	void removeSinkEventFilter(SinkEventFilter listener);
}
