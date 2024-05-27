/*
 * Copyright 2014-2023 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jkoolcloud.tnt4j.tracker;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Set;

import com.jkoolcloud.tnt4j.core.*;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.utils.Useconds;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * {@code TrackingEvent} implements tracking for a specific application sub-activity. Applications create instances of
 * {@code TrackingEvent} after initiating an application activity using {@link com.jkoolcloud.tnt4j.tracker.Tracker}
 * logger instance or {@link com.jkoolcloud.tnt4j.TrackingLogger}. Each {@code TrackingEvent} can be timed independently
 * using start()/stop() method calls and reported and logged using {@code Tracker.reportEvent()} method call.
 * </p>
 *
 * <p>
 * A {@code TrackingEvent} can represent a specific tracking event that application creates for every discrete activity
 * such as JDBC, JMS, SOAP or any other relevant application activity. Source developers must obtain a
 * {@link com.jkoolcloud.tnt4j.tracker.Tracker} instance via {@link com.jkoolcloud.tnt4j.tracker.DefaultTrackerFactory},
 * create instances of {@code TrackingEvent} and use {@code TrackingActivity.tnt()} to associate and report tracking
 * events.
 *
 * <p>
 * A {@link com.jkoolcloud.tnt4j.tracker.Tracker} start()/end() used to mark application activity boundaries.
 * </p>
 *
 * <p>
 * Any pair of {@code TrackingEvent} instances are related when event correlators match. {@code TrackingEvent} A is
 * related to {@code TrackingEvent} B when {@code A.getCorrelator() == B.getCorrelator()}. Developers may use signatures
 * and or correlators to correlate tracking events across application/server boundaries. {@code TrackingEvent} A is the
 * same as {@code TrackingEvent} B if the corresponding event signature are identical: meaning
 * {@code A.getSignature() == B.getSignature()}. Currently each {@code TrackingEvent} gets assigned a unique event
 * signature using {@link TrackerImpl#newUUID()} call. Signatures can be changed using
 * {@link TrackingEvent#setSignature(String)} method call. Signatures and correlators must be set before making
 * {@code TrackingActivity.tnt()} call.
 *
 * <p>
 * Below is example of how to set {@code TrackingEvent} correlator and operation type for a sender application:
 * 
 * <pre>
 * TrackingLogger logger = TrackingLogger.getInstance("com.jkoolcloud.appl.name"); // register and obtain Tracker logger instance
 * TrackingActivity activity = logger.newActivity(); // create a new application activity timing
 * TrackingEvent event = logger.newEvent(OpLevel.INFO, OpType.SEND, "SendOrder", "Sending order"); // create a sender tracking event
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
 *	logger.tnt(activity); // end activity timing
 * }
 * </pre>
 * <p>
 * Below is example of corresponding receiver application:
 * 
 * <pre>
 * TrackingLogger tracker = TrackingLogger.getInstance("com.jkoolcloud.appl.name"); // register and obtain Tracker logger instance
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
 * </pre>
 * 
 * @see Message
 * @see OpLevel
 * @see DefaultTrackerFactory
 * @see OpCompCode
 * @see OpType
 *
 * @version $Revision: 7 $
 *
 */
public class TrackingEvent extends Message implements Trackable, Relate2<Source> {

	private Source source;
	private String parent;
	private String sign;
	Operation operation;
	private TrackerImpl tracker;

	private final Source[] relation = new Source[2];
	private OpType relationType = OpType.NOOP;

	/**
	 * Create a new NOOP tracking event This constructor will assign a unique event signature using newUUID() call
	 *
	 * @param tr
	 *            tracker instance
	 */
	protected TrackingEvent(TrackerImpl tr) {
		this(tr, null, OpLevel.NONE, OpType.NOOP, Operation.NOOP, null, (String) null, (String) null);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported. This constructor will assign a unique
	 * event signature using newUUID() call
	 *
	 * @param tr
	 *            tracker instance
	 * @param src
	 *            event source
	 * @param severity
	 *            severity level
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @param correlator
	 *            associated with this event (could be unique or passed from a correlated activity)
	 * @param msg
	 *            text message associated with this event
	 * @param args
	 *            argument list passed along side the message
	 */
	protected TrackingEvent(TrackerImpl tr, Source src, OpLevel severity, String opName, String correlator, String msg,
			Object... args) {
		this(tr, src, severity, OpType.EVENT, opName, correlator, null, msg, args);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported. This constructor will assign a unique
	 * event signature using newUUID() call
	 *
	 * @param tr
	 *            tracker instance
	 * @param src
	 *            event source
	 * @param severity
	 *            severity level
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @param correlators
	 *            associated with this event (could be unique or passed from a correlated activity)
	 * @param msg
	 *            text message associated with this event
	 * @param args
	 *            argument list passed along side the message
	 */
	protected TrackingEvent(TrackerImpl tr, Source src, OpLevel severity, String opName, Collection<String> correlators,
			String msg, Object... args) {
		this(tr, src, severity, OpType.EVENT, opName, correlators, null, msg, args);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported. This constructor will assign a unique
	 * event signature using newUUID() call
	 *
	 * @param tr
	 *            tracker instance
	 * @param src
	 *            event source
	 * @param severity
	 *            severity level
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @param correlator
	 *            associated with this event (could be unique or passed from a correlated activity)
	 * @param msg
	 *            binary message associated with this event
	 * @param args
	 *            argument list passed along side the message
	 */
	protected TrackingEvent(TrackerImpl tr, Source src, OpLevel severity, String opName, String correlator, byte[] msg,
			Object... args) {
		this(tr, src, severity, OpType.EVENT, opName, correlator, null, msg, args);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported. This constructor will assign a unique
	 * event signature using newUUID() call
	 *
	 * @param tr
	 *            tracker instance
	 * @param src
	 *            event source
	 * @param severity
	 *            severity level
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @param correlators
	 *            associated with this event (could be unique or passed from a correlated activity)
	 * @param msg
	 *            binary message associated with this event
	 * @param args
	 *            argument list passed along side the message
	 */
	protected TrackingEvent(TrackerImpl tr, Source src, OpLevel severity, String opName, Collection<String> correlators,
			byte[] msg, Object... args) {
		this(tr, src, severity, OpType.EVENT, opName, correlators, null, msg, args);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 *
	 * @param tr
	 *            tracker instance
	 * @param src
	 *            event source
	 * @param severity
	 *            severity level
	 * @param opType
	 *            operation type
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @param correlator
	 *            associated with this event (could be unique or passed from a correlated activity)
	 * @param tag
	 *            associated with this event
	 * @param msg
	 *            text message associated with this event
	 * @param args
	 *            argument list passed along side the message
	 * @see OpLevel
	 * @see OpType
	 */
	protected TrackingEvent(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName,
			String correlator, String tag, String msg, Object... args) {
		super(null, msg, args);
		tracker = tr;
		operation = new Operation(opName, opType);
		operation.setSeverity(severity);
		operation.setCorrelator(correlator);
		operation.setException(Utils.getThrowable(args));
		setSource(src);
		setLocation(src);
		setTag(tag);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 *
	 * @param tr
	 *            tracker instance
	 * @param src
	 *            event source
	 * @param severity
	 *            severity level
	 * @param opType
	 *            operation type
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @param correlators
	 *            associated with this event (could be unique or passed from a correlated activity)
	 * @param tags
	 *            associated with this event
	 * @param msg
	 *            text message associated with this event
	 * @param args
	 *            argument list passed along side the message
	 * @see OpLevel
	 * @see OpType
	 */
	protected TrackingEvent(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName,
			Collection<String> correlators, Collection<String> tags, String msg, Object... args) {
		super(null, msg, args);
		tracker = tr;
		operation = new Operation(opName, opType);
		operation.setSeverity(severity);
		operation.setCorrelator(correlators);
		operation.setException(Utils.getThrowable(args));
		setSource(src);
		setLocation(src);
		setTag(tags);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 *
	 * @param tr
	 *            tracker instance
	 * @param src
	 *            event source
	 * @param severity
	 *            severity level
	 * @param opType
	 *            operation type
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @param correlators
	 *            associated with this event (could be unique or passed from a correlated activity)
	 * @param tags
	 *            associated with this event
	 * @param msg
	 *            binary message associated with this event
	 * @param args
	 *            argument list passed along side the message
	 * @see OpLevel
	 * @see OpType
	 */
	protected TrackingEvent(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName,
			Collection<String> correlators, Collection<String> tags, byte[] msg, Object... args) {
		super(null, msg, args);
		tracker = tr;
		operation = new Operation(opName, opType);
		operation.setSeverity(severity);
		operation.setCorrelator(correlators);
		operation.setException(Utils.getThrowable(args));
		setSource(src);
		setLocation(src);
		setTag(tags);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported.
	 *
	 * @param tr
	 *            tracker instance
	 * @param src
	 *            event source
	 * @param severity
	 *            severity level
	 * @param opType
	 *            operation type
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @param correlator
	 *            associated with this event (could be unique or passed from a correlated activity)
	 * @param tag
	 *            associated with this event
	 * @param msg
	 *            binary message associated with this event
	 * @param args
	 *            argument list passed along side the message
	 * @see OpLevel
	 * @see OpType
	 */
	protected TrackingEvent(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName,
			String correlator, String tag, byte[] msg, Object... args) {
		super(null, msg, args);
		tracker = tr;
		operation = new Operation(opName, opType);
		operation.setSeverity(severity);
		operation.setCorrelator(correlator);
		operation.setException(Utils.getThrowable(args));
		setSource(src);
		setLocation(src);
		setTag(tag);
	}

	/**
	 * Return string representation of this tracking event
	 *
	 * @return string representation of the tracking event
	 */
	@Override
	public String toString() {
		return "{" + operation.getSeverity() + ",[" + getMessage() + "]," + "[" + operation.getName() + "],"
				+ super.toString() + "," + operation + "}";
	}

	@Override
	public void addProperty(Property property) {
		operation.addProperty(property);
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

	@Override
	public void setParentId(Trackable parentObject) {
		parent = parentObject.getTrackingId();
		if (source == null) {
			source = parentObject.getSource();
		}
	}

	@Override
	public void setParentId(String parentId) {
		parent = parentId;
	}

	@Override
	public String getParentId() {
		return parent;
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public void setCorrelator(String... cids) {
		operation.setCorrelator(cids);
	}

	@Override
	public void setCorrelator(Collection<String> cids) {
		operation.setCorrelator(cids);
	}

	@Override
	public Set<String> getCorrelator() {
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
	 * @param location
	 *            location string for tracking event
	 */
	public void setLocation(String location) {
		operation.setLocation(location);
	}

	/**
	 * Sets the location associated with the tracking event such as GPS locator.
	 *
	 * @param location
	 *            location string for tracking event
	 */
	public void setLocation(Source location) {
		operation.setLocation(location);
	}

	/**
	 * Indicates that application tracking event has started at the specified startTime
	 *
	 * @param startTimeUsc
	 *            start time of the tracking event (usec)
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
	 * Indicates that application tracking event has started given a specific time stamp.
	 *
	 * @param time
	 *            when operation started
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
	 * @param time
	 *            when operation stopped
	 * @param elapsedUsec
	 *            elapsed time of the event in microseconds
	 */
	public void stop(UsecTimestamp time, long elapsedUsec) {
		operation.stop(time, elapsedUsec);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param elapsedTime
	 *            elapsed time of this event in (usec)
	 */
	public void stop(long elapsedTime) {
		operation.stop(Useconds.CURRENT.get(), elapsedTime);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param endTimeUsec
	 *            ending time associated with the event (usec)
	 * @param elapsedUsec
	 *            elapsed time in (usec)
	 */
	public void stop(long endTimeUsec, long elapsedUsec) {
		operation.stop(endTimeUsec, elapsedUsec);
	}

	/**
	 * Indicates that application tracking event has ended. Event completion code is set to {@link OpCompCode#WARNING}
	 *
	 * @param opEx
	 *            exception associated with this tracking event
	 */
	public void stop(Throwable opEx) {
		operation.setException(opEx);
		operation.stop();
		operation.setCompCode(opEx != null ? OpCompCode.WARNING : OpCompCode.SUCCESS);
	}

	/**
	 * Indicates that application tracking event has ended. Event completion code is set to
	 * {@link com.jkoolcloud.tnt4j.core.OpCompCode#WARNING}
	 *
	 * @param opEx
	 *            exception associated with this tracking event
	 * @param elapsedUsec
	 *            elapsed time in (usec)
	 */
	public void stop(Throwable opEx, long elapsedUsec) {
		operation.setException(opEx);
		operation.setCompCode(opEx != null ? OpCompCode.WARNING : OpCompCode.SUCCESS);
		operation.stop(Useconds.CURRENT.get(), elapsedUsec);
	}

	/**
	 * Indicates that application tracking event has ended.
	 *
	 * @param ccode
	 *            completion code of the tracking event
	 * @param rcode
	 *            reason code associated with this tracking event
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
	 * @param ccode
	 *            completion code of the tracking event
	 * @param opEx
	 *            exception associated with this tracking event
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
	 * @param ccode
	 *            completion code of the tracking event
	 * @param rcode
	 *            reason code associated with this tracking event
	 * @param opEx
	 *            exception associated with this tracking event
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
	 * @param ccode
	 *            completion code of the tracking event
	 * @param rcode
	 *            reason code associated with this tracking event
	 * @param opEx
	 *            exception associated with this tracking event
	 * @param endTimeUsec
	 *            time when the tracking event has ended (usec)
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
	 * @param ccode
	 *            completion code of the tracking event
	 * @param rcode
	 *            reason code associated with this tracking event
	 * @param opEx
	 *            exception associated with this tracking event
	 * @param endTimeUsec
	 *            time when the tracking event has ended (usec)
	 * @param elpasedUsec
	 *            elapsed time in (usec)
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
	 * @param ccode
	 *            completion code of the tracking event
	 * @param rcode
	 *            reason code associated with this tracking event
	 * @param opEx
	 *            exception associated with this tracking event
	 * @param endTime
	 *            time when the tracking event has ended (usec)
	 * @param elpasedUsec
	 *            elapsed time in (usec)
	 * @see OpCompCode
	 */
	public void stop(OpCompCode ccode, int rcode, Throwable opEx, UsecTimestamp endTime, long elpasedUsec) {
		operation.setException(opEx);
		operation.setCompCode(ccode);
		operation.setReasonCode(rcode);
		operation.stop(endTime, elpasedUsec);
	}

	/**
	 * Obtain a handle to the {@link Operation} associated with this tracking event
	 *
	 * @return operation handle associated with this event
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Sign current event with hash/signature based on trackers configured signature factory.
	 * 
	 * @return signed self
	 * @throws NoSuchAlgorithmException
	 *             if signature calculation algorithm is not provided by environment
	 */
	public TrackingEvent sign() throws NoSuchAlgorithmException {
		if (tracker != null) {
			setSignature(tracker.getConfiguration().getSignFactory().sign(this));
		}
		return this;
	}

	@Override
	public OpType getType() {
		return operation.getType();
	}

	@Override
	public void setSource(Source src) {
		source = src;
	}

	@Override
	public long getTTL() {
		return operation.getTTL();
	}

	@Override
	public void setTTL(long ttl) {
		operation.setTTL(ttl);
	}

	@Override
	public Relate2<Source> relate2(Source srcA, Source srcB, OpType type) {
		relation[OBJ_ONE] = srcA;
		relation[OBJ_TWO] = srcB;
		relationType = type;
		return this;
	}

	@Override
	public Relate2<Source> relate2(Source srcB, OpType type) {
		return relate2(getSource(), srcB, type);
	}

	@Override
	public OpType get2Type() {
		return relationType;
	}

	@Override
	public Source get2(int index) {
		return relation[index];
	}

	@Override
	public Relate2<Source> clear2() {
		relation[OBJ_ONE] = null;
		relation[OBJ_TWO] = null;
		relationType = OpType.NOOP;
		return this;
	}

	@Override
	public String getSignature() {
		return sign;
	}

	@Override
	public void setSignature(String sign) {
		this.sign = sign;
	}

	@Override
	public String getName() {
		return operation.getName();
	}

	/**
	 * Returns value of {@code fieldName} defined field/property for this tracking event.
	 * <p>
	 * List of supported field names (in common with {@link com.jkoolcloud.tnt4j.core.Operation#getFieldValue(String)},
	 * {@link com.jkoolcloud.tnt4j.source.Source#getFieldValue(String)} and
	 * {@link com.jkoolcloud.tnt4j.core.Message#getFieldValue(String)}):
	 * <ul>
	 * <li>Source</li>
	 * <li>ParentId</li>
	 * <li>Signature</li>
	 * <li>Severity</li>
	 * <li>Relation1</li>
	 * <li>Relation2</li>
	 * <li>RelationType</li>
	 * </ul>
	 *
	 * @param fieldName
	 *            event field or property name
	 * @return field/property contained value
	 *
	 * @see com.jkoolcloud.tnt4j.core.Operation#getFieldValue(String)
	 */
	@Override
	public Object getFieldValue(String fieldName) {
		if ("Source".equalsIgnoreCase(fieldName)) {
			return source;
		}
		if ("ParentId".equalsIgnoreCase(fieldName)) {
			return parent;
		}
		if ("Signature".equalsIgnoreCase(fieldName)) {
			return sign;
		}
		if ("Severity".equalsIgnoreCase(fieldName)) {
			return getSeverity();
		}
		if ("Relation1".equalsIgnoreCase(fieldName)) {
			return get2(OBJ_ONE);
		}
		if ("Relation2".equalsIgnoreCase(fieldName)) {
			return get2(OBJ_TWO);
		}
		if ("RelationType".equalsIgnoreCase(fieldName)) {
			return relationType;
		}

		if (source != null) {
			Object sfValue = source.getFieldValue(fieldName);
			if (sfValue != null) {
				return sfValue;
			}
		}
		if (operation != null) {
			Object opFieldValue = operation.getFieldValue(fieldName);
			if (opFieldValue != null) {
				return opFieldValue;
			}
		}

		return super.getFieldValue(fieldName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jkoolcloud.tnt4j.core.Trackable#getGUID()
	 */
	@Override
	public String getGUID() {
		return operation.getGUID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jkoolcloud.tnt4j.core.Trackable#setGUID(java.lang.String)
	 */
	@Override
	public void setGUID(String uid) {
		operation.setGUID(uid);
	}
}
