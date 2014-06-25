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

import java.util.UUID;

import com.nastel.jkool.tnt4j.core.Message;
import com.nastel.jkool.tnt4j.core.OpCompCode;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.core.Operation;
import com.nastel.jkool.tnt4j.core.UsecTimestamp;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p><code>TrackingEvent</code> implements tracking for a specific application sub-activity. Applications create
 * instances of <code>TrackingEvent</code> after initiating an application activity using <code>Tracker</code>
 * logger instance or <code>TrackingLogger</code>. Each <code>TrackingEvent</code> can be timed independently using
 * start()/stop() method calls and reported and logged using <code>Tracker.reportEvent()</code> method call.</p>
 *
 * <p>A <code>TrackingEvent</code> can represent a specific tracking event that application creates for
 * every discrete activity such as JDBC, JMS, SOAP or any other relevant application activity.
 * Source developers must obtain a <code>Tracker</code> instance via <code>DefaultTrackerFactory</code>, create
 * instances of <code>TrackingEvent</code> and use <code>TrackingActivity.tnt()</code> to associate and report
 * tracking events.
 *
 * <p>A <code>Tracker</code> start()/end() used to mark application activity boundaries.</p>
 *
 * <p>Any pair of <code>TrackingEvent</code> instances are related when event correlators match.
 * <code>TrackingEvent</code> A is related to <code>TrackingEvent</code> B when {@code A.getCorrelator() == B.getCorrelator()}.
 * Developers may use signatures and or correlators to correlate tracking events across application/server boundaries.
 * <code>TrackingEvent</code> A is the same as <code>TrackingEvent</code> B if the corresponding event signature are identical: meaning
 * {@code A.getSignature() == B.getSignature()}. Currently each <code>TrackingEvent</code> gets assigned a unique event signature using
 * <code>TrackingEvent.newUUID()</code> call. Signatures can be changed using {@code TrackingEvent.setSiganture()} method call.
 * Signatures and correlators must be set before making {@code TrackingActivity.tnt()} call.
 *
 * <p>Below is example of how to set <code>TrackingEvent</code> correlator and operation type for a sender application:
 * <pre>
 * {@code
 * TrackingLogger.register("com.nastel.appl.name", "myserver"); // register and obtain Tracker logger instance
 * TrackingActivity activity = TrackingLogger.newActivity(); // create a new application activity timing
 * TrackingEvent event = TrackingLogger.newEvent(OpLevel.INFO, OpType.SEND, "SendOrder", "Sending order"); // create a sender tracking event
 * activity.start(); // start application activity timing
 * event.start(); // start timing a tracking event
 * String order_id = null;
 * try {
 *	...
 *	...
 *	order_id = request.getOrderId(); // sample code to obtain order id which will be used as a correlator
 *	event.setCorrelator(order_id); // set event correlator obtained from order_id
 *	event.stop(); // stop timing tracking event
 * } catch (SQLException e) {
 *	event.stop(e); // stop timing tracking event and associate an exception
 *	...
 * } finally {
 *	activity.stop();
 *	activity.tnt(event); // report a tracking event
 *	TrackingLogger.tnt(activity); // end activity timing
 * }
 * }
 * </pre>
 * <p>Below is example of corresponding receiver application:
 * <pre>
 * {@code
 * TrackingLogger.register("com.nastel.appl.name", "myserver"); // register and obtain Tracker logger instance
 * TrackingActivity activity = TrackingLogger.newActivity(); // create a new application activity timing
 * TrackingEvent event = TrackingLogger.newEvent(OpLevel.INFO, OpType.RECEIVE, "RecvOrder", "Received order"); // create a receiver tracking event
 * activity.start(); // start application activity timing
 * event.start(); // start timing a tracking event
 * String order_id = null;
 * try {
 *	...
 *	...
 *	order_id = response.getOrderId(); // sample code to obtain order id which will be used as a correlator
 *	event.setCorrelator(order_id); // set event correlator obtained from the received order_id
 *	event.stop(); // stop timing tracking event
 * } catch (SQLException e) {
 *	event.stop(e); // stop timing tracking event and associate an exception
 *	...
 * } finally {
 *	activity.stop();
 *	activity.tnt(event); // report a tracking event
 *	TrackingLogger.tnt(activity); // end activity timing
 * }
 * }
 * </pre>
 * @see Message
 * @see OpLevel
 * @see DefaultTrackerFactory
 * @see OpCompCode
 * @see OpType
 *
 * @version $Revision: 7 $
 *
 */
public class TrackingEvent extends Message {
	Operation operation;

	/**
	 * Return string representation of this tracking event
	 *
	 * @return string representation of the tracking event
	 */
	@Override
	public String toString() {
		return 	"{" + operation.getSeverity()
				+ ",[" + getMessage() + "],"
				+ "[" + operation.getName() + "],"
				+ super.toString()
				+ "," + operation + "}";
	}

	/**
	 * Return current severity level associated with this event
	 *
	 * @return severity level
	 */
	public OpLevel getSeverity() {
		return operation.getSeverity();
	}

	/**
	 * Return newly generated random/unique UUID that can be used as event signature and or correlator
	 *
	 * @return UUID in string form
	 */
	public static String newUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 *
	 * @param severity severity level
	 * @param opName operation name associated with this event (tracking event name)
	 * @param msg text message associated with this event
	 * @param args argument list passed along side the message
	 * @see OpLevel
	 */
	protected TrackingEvent(OpLevel severity, String opName, String msg, Object...args) {
		this(severity, OpType.EVENT, opName, null, msg, args);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 *
	 * @param severity severity level
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param msg text message associated with this event
	 * @param args argument list passed along side the message
	 */
	protected TrackingEvent(OpLevel severity, String opName, String correlator, String msg, Object...args) {
		this(severity, OpType.EVENT, opName, correlator, msg, args);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param opName operation name associated with this event (tracking event name)
	 * @param msg text message associated with this event
	 * @param args argument list passed along side the message
	 * @see OpType
	 * @see OpLevel
	 */
	protected TrackingEvent(OpLevel severity, OpType opType, String opName, String msg, Object...args) {
		this(severity, opType, opName, null, msg, args);
	}


	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param msg text message associated with this event
	 * @param args argument list passed along side the message
	 * @see OpLevel
	 * @see OpType
	 */
	protected TrackingEvent(OpLevel severity, OpType opType, String opName, String correlator, String msg, Object...args) {
		super(newUUID(), msg, args);
		operation = new Operation(opName, opType);
		operation.setSeverity(severity);
		operation.setCorrelator(correlator);
		operation.setResource(Utils.getVMName());
		operation.setException(Utils.getThrowable(args));
	}

	public void setCorrelator(String cid) {
		operation.setCorrelator(cid);
	}

	public String getCorrelator() {
		return operation.getCorrelator();
	}

	/**
	 * Gets the location string associated with the tracking event such as GPS locator.
	 *
	 * @return location string for tracking event
	 */
	public String getLocation() {
		return operation.getLocation();
	}

	/**
	 * Sets the location associated with the tracking event such as GPS locator.
	 *
	 * @param location location string for tracking event
	 */
	public void setLocation(String location) {
		operation.setLocation(location);
	}

	/**
	 * Indicates that application tracking event has started at the specified startTime
	 *
	 * @param startTime start time of the tracking event (ms)
	 */
	public void start(long startTime) {
		operation.start(startTime);
	}

	/**
	 * Indicates that application tracking event has started at the specified startTime
	 *
	 * @param startTime start time of the tracking event (usec)
	 */
	public void start(UsecTimestamp startTime) {
		operation.start(startTime);
	}

	/**
	 * Indicates that application tracking event has started.
	 *
	 */
	public void start() {
		operation.start();
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 */
	public void stop() {
		operation.stop();
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param endTime ending time associated with the event (ms)
	 */
	public void stop(long endTime) {
		operation.stop(endTime);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param endTime ending time associated with the event (usec)
	 */
	public void stop(UsecTimestamp endTime) {
		operation.stop(endTime);
	}

	/**
	 * Indicates that application tracking event has ended.
	 * Event completion code is set to <code>OpCompCode.WARNING</code>
	 *
	 * @param opEx exception associated with this tracking event
	 * @see OpCompCode
	 */
	public void stop(Throwable opEx) {
		operation.stop();
		operation.setCompCode(opEx != null? OpCompCode.WARNING: OpCompCode.SUCCESS);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param ccode completion code of the tracking event
	 * @param rcode reason code associated with this tracking event
	 * @see OpCompCode
	 */
	public void stop(OpCompCode ccode, int rcode) {
		operation.stop();
		operation.setCompCode(ccode);
		operation.setReasonCode(rcode);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param ccode completion code of the tracking event
	 * @param opEx exception associated with this tracking event
	 * @see OpCompCode
	 */
	public void stop(OpCompCode ccode, Throwable opEx) {
		operation.stop();
		operation.setException(opEx);
		operation.setCompCode(ccode);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param ccode completion code of the tracking event
	 * @param rcode reason code associated with this tracking event
	 * @param opEx exception associated with this tracking event
	 * @see OpCompCode
	 */
	public void stop(OpCompCode ccode, int rcode, Throwable opEx) {
		operation.stop();
		operation.setException(opEx);
		operation.setCompCode(ccode);
		operation.setReasonCode(rcode);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param ccode completion code of the tracking event
	 * @param rcode reason code associated with this tracking event
	 * @param opEx exception associated with this tracking event
	 * @param endTime time when the tracking event has ended (ms)
	 * @see OpCompCode
	 */
	public void stop(OpCompCode ccode, int rcode, Throwable opEx, long endTime) {
		operation.stop(endTime);
		operation.setException(opEx);
		operation.setCompCode(ccode);
		operation.setReasonCode(rcode);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param ccode completion code of the tracking event
	 * @param rcode reason code associated with this tracking event
	 * @param opEx exception associated with this tracking event
	 * @param endTime time when the tracking event has ended (usec)
	 * @see OpCompCode
	 */
	public void stop(OpCompCode ccode, int rcode, Throwable opEx, UsecTimestamp endTime) {
		operation.stop(endTime);
		operation.setException(opEx);
		operation.setCompCode(ccode);
		operation.setReasonCode(rcode);
	}

	/**
	 * Obtain a handle to the <code>Operation</code> associated with this tracking event
	 *
	 *@return operation handle associated with this event
	 */
	public Operation getOperation() {
		return operation;
	}
}
