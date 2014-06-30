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
package com.nastel.jkool.tnt4j.core;

import com.nastel.jkool.tnt4j.utils.Utils;


/**
 * <p>Implements a Operation entity.</p>
 *
 * <p>An <code>Operation</code> object is used to represent an intercepted operation
 * whose execution it to be recorded.  An <code>Operation</code> is associated
 * with either a <code>Message</code>, if the operation acts upon a message
 * (typically send/receive operations), or a <code>Activity</code> if the
 * operation does not act upon a message.</p>
 *
 * <p>An <code>Operation</code> is required to have its start time and end time set.</p>
 *
 * @see OpType
 * @see OpCompCode
 * @see Activity
 * @see Message
 * @see Trackable
 *
 * @version $Revision: 12 $
 */
public class Operation {

	/**
	 * Noop operation name
	 */
	public static final String NOOP = "NOOP";

	/**
	 * Maximum length of Operation Function Name.
	 * @since Revision 14
	 */
	public static final int MAX_FUNCTION_NAME_LENGTH = 256;

	/**
	 * Maximum length of Operation User Name.
	 * @since Revision 14
	 */
	public static final int MAX_USER_NAME_LENGTH = 64;

	/**
	 * Maximum length of Operation Exception string.
	 * @since Revision 25
	 */
	public static final int MAX_EXCEPTION_LENGTH = 512;

	/**
	 * Maximum length of Operation Location string.
	 * @since Revision 42
	 */
	public static final int MAX_LOCATION_LENGTH = 512;

	/**
	 * Maximum length of Operation Correlator.
	 * @since Revision 43
	 */
	public static final int MAX_CORRELATOR_LENGTH = 256;


	private String		opName;
	private OpType		opType;
	private OpCompCode	opCC = OpCompCode.SUCCESS;
	private OpLevel				opLevel = OpLevel.INFO;
	private UsecTimestamp		startTime;
	private UsecTimestamp		endTime;
	private String			    resource;
	private String				user;
	private long				elapsedTime;
	private long				elapsedTimeNano, startTimeNano, stopTimeNano;
	private long				messageAge;
	private long				waitTime;
	private int					opRC = 0;
	private String				exceptionStr;
	private Throwable			exHandle;
	private String				location;
	private String				correlator;
	private long          		pid;
	private long          		tid;

	/**
	 * Creates a Operation with the specified properties.
	 *
	 * @param opname function name triggering operation
	 * @param opType operation type
	 * @throws NullPointerException if any arguments are null
	 * @throws IllegalArgumentException if opName is empty
	 */
	public Operation(String opname, OpType opType) {
		setName(opname);
		setType(opType);
		setTID(Thread.currentThread().getId());
		setPID(Utils.getVMPID());
	}

	/**
	 * Gets the name of the method that triggered the operation.
	 *
	 * @return name triggering operation
	 */
	public String getName() {
		return opName;
	}

	/**
	 * Sets the name of the method that triggered the operation, truncating if necessary.
	 *
	 * @param opname function name triggering operation
	 * @throws NullPointerException if opName is <code>null</code>
	 * @throws IllegalArgumentException if opName is empty
	 * @see #MAX_FUNCTION_NAME_LENGTH
	 */
	public void setName(String opname) {
		if (opname == null)
			throw new NullPointerException("opName must be a non-empty string");
		if (opname.length() == 0)
			throw new IllegalArgumentException("opName must be a non-empty string");
		if (opname.length() > MAX_FUNCTION_NAME_LENGTH)
			opname = opname.substring(0, MAX_FUNCTION_NAME_LENGTH);
		this.opName = opname;
	}

	/**
	 * Gets process ID of process the Activity is running in.
	 *
	 * @return process ID for process running Activity
	 */
	public long getPID() {
		return pid;
	}

	/**
	 * Sets process ID for Activity, which should be the ID of the process the Activity is running in.
	 *
	 * @param pid process ID of process running Activity
	 */
	public void setPID(long pid) {
		this.pid = pid;
	}

	/**
	 * Gets thread ID of process thread the Activity is running in.
	 *
	 * @return thread ID for thread running Activity
	 */
	public long getTID() {
		return tid;
	}

	/**
	 * Sets thread ID for Activity, which should be the ID of the process thread the Activity is running in.
	 *
	 * @param tid thread ID of thread running Activity
	 */
	public void setTID(long tid) {
		this.tid = tid;
	}

	/**
	 * Gets the type of operation.
	 *
	 * @return operation type
	 */
	public OpType getType() {
		return opType;
	}

	/**
	 * Sets the type of operation.
	 *
	 * @param opType operation type
	 * @throws NullPointerException if opType is <code>null</code>
	 */
	public void setType(OpType opType) {
		if (opType == null)
			throw new NullPointerException("opType must be non-null");
		this.opType = opType;
	}

	/**
	 * Returns true of operation is a NOOP
	 *
	 * @return true if operation is a NOOP, false otherwise
	 */
	public boolean isNoop() {
		return (this.opType == OpType.NOOP);
	}

	/**
	 * Gets the completion code for the operation.
	 *
	 * @return function completion code
	 */
	public OpCompCode getCompCode() {
		return opCC;
	}

	/**
	 * Sets the completion code for the operation.
	 * To provide a reason for a warning or failure, set a reason-code or
	 * exception message.
	 *
	 * @param opCC function completion code
	 * @throws NullPointerException if opCC is <code>null</code>
	 * @see #setReasonCode(int)
	 * @see #setException(String)
	 */
	public void setCompCode(OpCompCode opCC) {
		if (opCC == null)
			throw new NullPointerException("opCC must be non-null");
		this.opCC = opCC;
	}

	/**
	 * Gets the reason code for the operation.
	 *
	 * @return function return code
	 */
	public int getReasonCode() {
		return opRC;
	}

	/**
	 * Sets the reason code for the operation.
	 *
	 * @param opRC function return code
	 * @see #setCompCode(OpCompCode)
	 */
	public void setReasonCode(int opRC) {
		this.opRC = opRC;
	}

	/**
	 * Gets the resource associated with this operation.
	 *
	 * @return resource for operation
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * Sets the resource associated with this operation.
	 *
	 * @param resource resource for operation
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}

	/**
	 * Gets the user whose context the operation is running in.
	 *
	 * @return user name
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the user whose context the operation is running in, truncating if necessary.
	 *
	 * @param user name of user
	 * @see #MAX_USER_NAME_LENGTH
	 */
	public void setUser(String user) {
		if (user != null) {
			if (user.length() > MAX_USER_NAME_LENGTH)
				user = user.substring(0, MAX_USER_NAME_LENGTH);
			else if (user.length() == 0)
				user = null;
		}
		this.user = user;
	}

	/**
	 * Gets the total time for the operation.
	 *
	 * @return elapsed time for operation, in microseconds
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * Gets the total time for the operation in nanoseconds.
	 * Time is measured between a pair of start/stop calls.
	 *
	 * @return elapsed time for operation, in nanoseconds
	 */
	public long getElapsedTimeNano() {
		return elapsedTimeNano;
	}

	/**
	 * Gets the age of the message that the operation applies to.  This is only
	 * relevant for operations whose type is <code>OpType.RECEIVE</code>.
	 * This value represents the time between when the message was sent (put/write)
	 * and received (get/read).
	 *
	 * @return age of message, in microseconds
	 */
	public long getMessageAge() {
		return messageAge;
	}

	/**
	 * Sets the age of the message that the operation applies to.  This is only
	 * relevant for operations whose type is <code>OpType.RECEIVE</code>.
	 * This value represents the time between when the message was sent (put/write)
	 * and received (get/read).
	 *
	 * @param messageAge age of message, in microseconds
	 * @throws IllegalArgumentException if messageAge is negative
	 */
	public void setMessageAge(long messageAge) {
		if (messageAge < 0)
			throw new IllegalArgumentException("messageAge must be non-negative");
		this.messageAge = messageAge;
	}

	/**
	 * Gets the wait time for the operation.  This is only relevant for operations
	 * whose type is <code>OpType.RECEIVE</code>.
	 * This value represents the time the operation spent waiting for a message
	 * to be available.
	 *
	 * @return wait time for operation, in microseconds
	 */
	public long getWaitTime() {
		return waitTime;
	}

	/**
	 * Sets the wait time for the operation.  This is only relevant for operations
	 * whose type is <code>OpType.RECEIVE</code>.
	 * This value represents the time the operation spent waiting for a message
	 * to be available.
	 *
	 * @param wTime idle time for operation, in microseconds
	 * @throws IllegalArgumentException if waitTime is negative
	 */
	public void setWaitTime(long wTime) {
		if (wTime < 0)
			throw new IllegalArgumentException("waitTime must be non-negative");
		this.waitTime = wTime;
	}


	/**
	 * Gets the exception string message currently associated with the operation.
	 *
	 * @return operation's exception string message
	 */
	public String getExceptionString() {
		return exceptionStr;
	}

	/**
	 * Gets the actual <code>Throwable</code> exception associated with this operation.
	 *
	 * @return operation's exception
	 */
	public Throwable getThrowable() {
		return exHandle;
	}

	/**
	 * <p>Sets the exception message to associate with the operation based on the
	 * specified exception.</p>
	 *
	 * <p>If an exception is associated with the operation, then the completion
	 * code should be set to either <code>WARNING</code> or <code>ERROR</code>.</p>
	 *
	 * @param t error thrown by operation
	 * @see #setCompCode(OpCompCode)
	 */
	public void setException(Throwable t) {
		exHandle = t;
		setException(t == null ? null : t.toString());
	}

	/**
	 * <p>Sets the exception message to associate with the operation.</p>
	 *
	 * <p>If an exception is associated with the operation, then the completion
	 * code should be set to either <code>WARNING</code> or <code>ERROR</code>.</p>
	 *
	 * @param exceptionStr operation's exception message
	 * @see #setCompCode(OpCompCode)
	 * @see #MAX_EXCEPTION_LENGTH
	 */
	public void setException(String exceptionStr) {
		if (exceptionStr != null && exceptionStr.length() == 0)
			exceptionStr = null;
		this.exceptionStr = exceptionStr;
		if (this.exceptionStr != null && this.exceptionStr.length() > MAX_EXCEPTION_LENGTH)
			this.exceptionStr = this.exceptionStr.substring(0, MAX_EXCEPTION_LENGTH);
	}

	/**
	 * Sets the current severity level to associate with the operation.
	 *
	 * @param level operation severity level to associate with operation
	 */
	public void setSeverity(OpLevel level) {
		opLevel = level;
	}

	/**
	 * Gets the current severity level to associate with the operation.
	 *
	 *@return current severity level
	 */
	public OpLevel getSeverity() {
		return opLevel;
	}

	/**
	 * Gets the location string identifying the location the operation was executed
	 * (e.g. GPS locator, source file line, etc.).
	 *
	 * @return location string for operation
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Gets the location string identifying the location the operation was executed
	 * (e.g. GPS locator, source file line, etc.).
	 *
	 * @param location location string for operation
	 */
	public void setLocation(String location) {
		if (location != null && location.length() == 0)
			location = null;
		this.location = location;
		if (this.location != null && this.location.length() > MAX_LOCATION_LENGTH)
			this.location = this.location.substring(0, MAX_LOCATION_LENGTH);
	}

	/**
	 * Gets the operation correlator, which is a user-defined value to relate two separate
	 * operations as belonging to the same transaction.
	 *
	 * @return user-defined operation correlator
	 */
	public String getCorrelator() {
		return correlator;
	}

	/**
	 * Sets the operation correlator, which is a user-defined value to relate two separate
	 * operations as belonging to the same transaction, truncating if necessary.
	 *
	 * @param correlator user-defined operation correlator
	 * @throws IllegalArgumentException if correlator is too long
	 * @see #MAX_CORRELATOR_LENGTH
	 */
	public void setCorrelator(String correlator) {
		if (correlator != null) {
			if (correlator.length() > MAX_CORRELATOR_LENGTH)
				throw new IllegalArgumentException("correlator length must be <= " + MAX_CORRELATOR_LENGTH);
			else if (correlator.length() == 0)
				correlator = null;
		}
		this.correlator = correlator;
	}

	/**
	 * Gets the time the operation started.
	 *
	 * @return operation start time
	 */
	public UsecTimestamp getStartTime() {
		return startTime;
	}

	/**
	 * Indicates that the operation has started at the specified start time.
	 *
	 * @param startTime start time, in milliseconds
	 * @param startTimeUsec microsecond fractional portion of start time
	 * @throws IllegalArgumentException if startTime or startTimeUsec is negative
	 */
	public void start(long startTime, long startTimeUsec) {
		this.startTimeNano = System.nanoTime();
		this.startTime = new UsecTimestamp(startTime, startTimeUsec);
	}

	/**
	 * Indicates that the operation has started at the specified start time.
	 *
	 * @param startTime start time, in milliseconds
	 * @throws IllegalArgumentException if startTime is negative
	 */
	public void start(long startTime) {
		start(startTime, 0);
	}

	/**
	 * Indicates that the operation has started at the specified start time.
	 *
	 * @param startTimestamp start time
	 * @throws NullPointerException if startTimestamp is <code>null</code>
	 * @throws IllegalArgumentException if startTimestamp is invalid
	 */
	public void start(UsecTimestamp startTimestamp) {
		if (startTimestamp == null)
			throw new NullPointerException("startTimestamp must be non-null");
		start(startTimestamp.getTimeMillis(), startTimestamp.getUsecPart());
	}

	/**
	 * Indicates that the operation has started immediately.
	 */
	public void start() {
		start(System.currentTimeMillis(), 0);
	}

	/**
	 * Gets the time the operation ended.
	 *
	 * @return operation end time
	 */
	public UsecTimestamp getEndTime() {
		return endTime;
	}

	/**
	 * Indicates that the operation has stopped at the specified stop time.
	 *
	 * @param stopTime stop time, in milliseconds
	 * @param stopTimeUsec microsecond fractional portion of stop time
	 * @throws IllegalStateException if operation was never started (start time not set)
	 * @throws IllegalArgumentException if stopTime or stopTimeUsec is negative,
	 *  or if the stop time is less than the previously specified start time
	 */
	public void stop(long stopTime, long stopTimeUsec) {
		endTime = new UsecTimestamp(stopTime, stopTimeUsec);

		if (startTime == null)
			throw new IllegalStateException("stopping operation that was never started");

		if (endTime.compareTo(startTime) < 0)
			throw new IllegalArgumentException("stop time is less than start time");

		elapsedTime = endTime.difference(startTime);
		stopTimeNano = System.nanoTime();	
		elapsedTimeNano = stopTimeNano - startTimeNano;
	}

	/**
	 * Indicates that the operation has stopped at the specified stop time.
	 *
	 * @param stopTime stop time, in milliseconds
	 * @throws IllegalStateException if operation was never started (start time not set)
	 * @throws IllegalArgumentException if stopTime is negative,
	 *  or if the stop time is less than the previously specified start time
	 */
	public void stop(long stopTime) {
		stop(stopTime, 0);
	}

	/**
	 * Indicates that the operation has stopped at the specified stop time.
	 *
	 * @param stopTimestamp stop time
	 * @throws NullPointerException if stopTimestamp is <code>null</code>
	 * @throws IllegalArgumentException if stopTimestamp is invalid
	 */
	public void stop(UsecTimestamp stopTimestamp) {
		if (stopTimestamp == null)
			throw new NullPointerException("stopTimestamp must be non-null");
		stop(stopTimestamp.getTimeMillis(), stopTimestamp.getUsecPart());
	}

	/**
	 * Indicates that the operation has stopped immediately.
	 */
	public void stop() {
		stop(System.currentTimeMillis(), 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		final UsecTimestamp ts = getStartTime();

		result = prime * result
				+ ((opName == null) ? 0 : opName.hashCode());

		if (ts != null)
			result = prime * result + ts.hashCode();

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Operation))
			return false;

		final Operation other = (Operation) obj;

		if (opName == null) {
			if (other.opName != null)
				return false;
		}
		else if (!opName.equals(other.opName)) {
			return false;
		}

		if (startTime == null) {
			if (other.startTime != null)
				return false;
		}
		else if (!startTime.equals(other.startTime)) {
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final OpType type = getType();
		final String res = getResource();
		final UsecTimestamp sTime = getStartTime();
		final UsecTimestamp eTime = getEndTime();
		StringBuilder str = new StringBuilder();

		str.append(getClass().getSimpleName()).append("{")
		   .append("Name:").append(getName()).append(",")
		   .append("Type:").append(type == null ? "null" : type.toString()).append(",")
		   .append("Correlator:").append(getCorrelator()).append(",")
		   .append("Location:").append(getLocation()).append(",")
		   .append("Resource:").append(res == null ? "null" : res.toString()).append(",")
		   .append("User:").append(getUser()).append(",")
		   .append("CompCode:").append(getCompCode()).append(",")
		   .append("PID:").append(getPID()).append(",")
		   .append("TID:").append(getTID()).append(",")
		   .append("ReasonCode:").append(getReasonCode()).append(",")
		   .append("ElapsedUsec:").append(getElapsedTime()).append(",")
		   .append("WaitUsec:").append(getWaitTime()).append(",")
		   .append("MsgAgeUsec:").append(getMessageAge()).append(",")
		   .append("StartTime:[").append(sTime == null ? "null" : sTime.toString()).append("],")
		   .append("EndTime:[").append(eTime == null ? "null" : eTime.toString()).append("],")
		   .append("Exception:").append(getExceptionString()).append("}");

		return str.toString();
	}
}
