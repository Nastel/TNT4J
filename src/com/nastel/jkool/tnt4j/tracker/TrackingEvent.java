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
import com.nastel.jkool.tnt4j.core.Trackable;
import com.nastel.jkool.tnt4j.core.UsecTimestamp;
import com.nastel.jkool.tnt4j.source.Source;
import com.nastel.jkool.tnt4j.utils.Useconds;
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
 * TrackingLogger tracker = TrackingLogger.getInstance("com.nastel.appl.name"); // register and obtain Tracker logger instance
 * TrackingActivity activity = tracker.newActivity(); // create a new application activity timing
 * TrackingEvent event = tracker.newEvent(OpLevel.INFO, OpType.RECEIVE, "RecvOrder", "Received order"); // create a receiver tracking event
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
 *	tracker.tnt(activity); // end activity timing
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
public class TrackingEvent extends Message implements Trackable {

	private Source	source;
	private String	parent;
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
	 * Returns true of operation is a NOOP
	 *
	 * @return true if operation is a NOOP, false otherwise
	 */
	public boolean isNoop() {
		return operation.isNoop();
	}

	/**
	 * Determine if operation was ever started
	 *
	 * @return true if operation was started, false otherwise
	 */
	public boolean isStarted() {
		return operation.isStarted();
	}

	/**
	 * Determine if operation was ever stopped
	 *
	 * @return true if operation was stopped, false otherwise
	 */
	public boolean isStopped() {
		return operation.isStopped();
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
	 * Create a new NOOP tracking event
	 * This constructor will assign a unique event signature using newUUID() call
	 *
	 */
	protected TrackingEvent() {
		this(null, OpLevel.NONE, OpType.NOOP, Operation.NOOP, null, null, (String)null);
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
	protected TrackingEvent(Source src, OpLevel severity, String opName, String correlator, String msg, Object...args) {
		this(src, severity, OpType.EVENT, opName, correlator, null, msg, args);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 * This constructor will assign a unique event signature using newUUID() call
	 *
	 * @param severity severity level
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param msg binary message associated with this event
	 * @param args argument list passed along side the message
	 */
	protected TrackingEvent(Source src, OpLevel severity, String opName, String correlator, byte[] msg, Object...args) {
		this(src, severity, OpType.EVENT, opName, correlator, null, msg, args);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param tag associated with this event
	 * @param msg text message associated with this event
	 * @param args argument list passed along side the message
	 * @see OpLevel
	 * @see OpType
	 */
	protected TrackingEvent(Source src, OpLevel severity, OpType opType, String opName, String correlator, String tag, String msg, Object...args) {
		super(newUUID(), msg, args);
		operation = new Operation(opName, opType);
		operation.setSeverity(severity);
		operation.setCorrelator(correlator);
		operation.setResource(Utils.getVMName());
		operation.setException(Utils.getThrowable(args));
		setSource(src);
		setTag(tag);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 *
	 * @param severity severity level
	 * @param opType operation type
	 * @param opName operation name associated with this event (tracking event name)
	 * @param correlator associated with this event (could be unique or passed from a correlated activity)
	 * @param tag associated with this event
	 * @param msg binary message associated with this event
	 * @param args argument list passed along side the message
	 * @see OpLevel
	 * @see OpType
	 */
	protected TrackingEvent(Source src, OpLevel severity, OpType opType, String opName, String correlator, String tag, byte[] msg, Object...args) {
		super(newUUID(), msg, args);
		operation = new Operation(opName, opType);
		operation.setSeverity(severity);
		operation.setCorrelator(correlator);
		operation.setResource(Utils.getVMName());
		operation.setException(Utils.getThrowable(args));
		setSource(src);
		setTag(tag);
	}

	@Override
	public void setParentId(Trackable parentObject) {
		parent = parentObject.getTrackingId();
		source = parentObject.getSource();
	}

	@Override
	public String getParentId() {
		return parent;
	}

	@Override
    public Source getSource() {
	    return source;
    }

	/**
	 * Sets the operation correlator, which is a user-defined value to relate two separate
	 * operations as belonging to the same transaction, truncating if necessary.
	 *
	 * @param cid user-defined operation correlator
	 * @throws IllegalArgumentException if correlator is too long
	 */
	public void setCorrelator(String cid) {
		operation.setCorrelator(cid);
	}

	/**
	 * Gets the operation correlator, which is a user-defined value to relate two separate
	 * operations as belonging to the same transaction.
	 *
	 * @return user-defined operation correlator
	 */
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
	 * @param startTimeUsc start time of the tracking event (usec)
	 */
	public void start(long startTimeUsc) {
		operation.start(startTimeUsc);
	}

	/**
	 * Indicates that application tracking event has started.
	 *
	 */
	public void start() {
		operation.start();
	}

	/**
	 * Indicates that application tracking event has started given a
	 * specific time stamp.
	 *
	 * @param time when operation started
	 */
	public void start(UsecTimestamp time) {
		operation.start(time);
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
	 * @param time when operation stopped
	 * @param elapsedUsec elapsed time of the event in microseconds
	 */
	public void stop(UsecTimestamp time, long elapsedUsec) {
		operation.stop(time, elapsedUsec);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param elaspedTime elapsed time of this event in (usec)
	 */
	public void stop(long elaspedTime) {
		operation.stop(Useconds.CURRENT.get(), elaspedTime);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param endTimeUsec ending time associated with the event (usec)
	 * @param elapsedUsec elapsed time in (usec)
	 */
	public void stop(long endTimeUsec, long elapsedUsec) {
		operation.stop(endTimeUsec, elapsedUsec);
	}

	/**
	 * Indicates that application tracking event has ended.
	 * Event completion code is set to <code>OpCompCode.WARNING</code>
	 *
	 * @param opEx exception associated with this tracking event
	 */
	public void stop(Throwable opEx) {
		operation.setException(opEx);
		operation.stop();
		operation.setCompCode(opEx != null? OpCompCode.WARNING: OpCompCode.SUCCESS);
	}

	/**
	 * Indicates that application tracking event has ended.
	 * Event completion code is set to <code>OpCompCode.WARNING</code>
	 *
	 * @param opEx exception associated with this tracking event
	 * @param elapsedUsec elapsed time in (usec)
	 */
	public void stop(Throwable opEx, long elapsedUsec) {
		operation.setException(opEx);
		operation.setCompCode(opEx != null? OpCompCode.WARNING: OpCompCode.SUCCESS);
		operation.stop(Useconds.CURRENT.get(), elapsedUsec);
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
		operation.setException(opEx);
		operation.setCompCode(ccode);
		operation.setReasonCode(rcode);
		operation.stop();
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param ccode completion code of the tracking event
	 * @param rcode reason code associated with this tracking event
	 * @param opEx exception associated with this tracking event
	 * @param endTimeUsec time when the tracking event has ended (usec)
	 * @see OpCompCode
	 */
	public void stop(OpCompCode ccode, int rcode, Throwable opEx, long endTimeUsec) {
		operation.setException(opEx);
		operation.setCompCode(ccode);
		operation.setReasonCode(rcode);
		operation.stop(endTimeUsec);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param ccode completion code of the tracking event
	 * @param rcode reason code associated with this tracking event
	 * @param opEx exception associated with this tracking event
	 * @param endTimeUsec time when the tracking event has ended (usec)
	 * @param elpasedUsec elapsed time in (usec)
	 * @see OpCompCode
	 */
	public void stop(OpCompCode ccode, int rcode, Throwable opEx, long endTimeUsec, long elpasedUsec) {
		operation.setException(opEx);
		operation.setCompCode(ccode);
		operation.setReasonCode(rcode);
		operation.stop(endTimeUsec, elpasedUsec);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param ccode completion code of the tracking event
	 * @param rcode reason code associated with this tracking event
	 * @param opEx exception associated with this tracking event
	 * @param endTime time when the tracking event has ended (usec)
	 * @param elpasedUsec elapsed time in (usec)
	 * @see OpCompCode
	 */
	public void stop(OpCompCode ccode, int rcode, Throwable opEx, UsecTimestamp endTime, long elpasedUsec) {
		operation.setException(opEx);
		operation.setCompCode(ccode);
		operation.setReasonCode(rcode);
		operation.stop(endTime, elpasedUsec);
	}

	/**
	 * Obtain a handle to the <code>Operation</code> associated with this tracking event
	 *
	 *@return operation handle associated with this event
	 */
	public Operation getOperation() {
		return operation;
	}

	@Override
    public OpType getType() {
	    return operation.getType();
    }

	@Override
    public void setSource(Source src) {
		source = src;
	}
}
