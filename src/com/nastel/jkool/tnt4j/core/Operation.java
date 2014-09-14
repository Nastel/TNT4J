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

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.nastel.jkool.tnt4j.utils.Useconds;
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
	 * Current stack frame class marker prefix
	 */
	public static final String OP_STACK_MARKER_PREFIX = "$";
	
	/**
	 * Noop operation name
	 */
	public static final String NOOP = "NOOP";


	private String				opName;
	private OpType				opType;
	private OpCompCode			opCC = OpCompCode.SUCCESS;
	private OpLevel				opLevel = OpLevel.INFO;
	private UsecTimestamp		startTime;
	private UsecTimestamp		endTime;
	private String			    resource;
	private String				user;
	private long				elapsedTime;
	private long				elapsedTimeNano, startTimeNano, stopTimeNano;
	private long				waitTime;
	private int					opRC = 0;
	private String				exceptionStr;
	private Throwable			exHandle;
	private String				location;
	private String				correlator;
	private long          		pid;
	private long          		tid;
	private HashMap<String, Snapshot> snapshots =  new HashMap<String, Snapshot>(32);

	/**
	 * Creates a Operation with the specified properties.
	 * Operation name can be any name or a relative name based 
	 * on the current thread stack trace. The relative operation name
	 * must be specified as follows: <code>$class-marker:offset</code>. 
	 * Example: <code>$com.nastel.jkool.tnt4j.tracker:0</code>
	 * This name results in the actual operation name computed at runtime based on
	 * current thread stack at the time when <code>getResolvedName</code> is called.
	 * 
	 * @param opname function name triggering operation
	 * @param opType operation type
	 * @see #getResolvedName()
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
	 * Gets resolved name of the operation. Runtime stack resolution
	 * occurs when the operation name is of the form:
	 * <code>$class-marker:offset</code>. 
	 * Example: <code>$com.nastel.jkool.tnt4j.tracker:0</code>
	 *
	 * @return name triggering operation
	 * @see #OP_STACK_MARKER_PREFIX
	 */
	public String getResolvedName() {
		return getResolvedName(opName);
	}

	/**
	 * Gets resolved name of the method that triggered the operation.
	 * 
	 * @return name triggering operation
	 */
	public static String getResolvedName(String opName) {
		if (!opName.startsWith(OP_STACK_MARKER_PREFIX)) {
			return opName;
		} else {
			String marker = opName.substring(1);
			String[] pair = marker.split(":");
			int offset = pair.length == 2? Integer.parseInt(pair[1]): 0;
			StackTraceElement item = Utils.getStackFrame(pair[0], offset);
			return item.toString();
		}
	}

	/**
	 * Gets resolved name of the method that triggered the operation.
	 * 
	 * @param marker class marker to be used to locate the stack frame
	 * @param offset offset from the located stack frame (must be >= 0)
	 * @return name triggering operation
	 */
	public static String getResolvedName(String marker, int offset) {
		StackTraceElement item = Utils.getStackFrame(marker, offset);
		return item.toString();
	}

	/**
	 * Gets resolved name of the method that triggered the operation.
	 * 
	 * @param classMarker class marker to be used to locate the stack frame
	 * @return name triggering operation
	 */
	public static String getResolvedName(Class<?> classMarker) {
		StackTraceElement item = Utils.getStackFrame(classMarker.getName(), 0);
		return item.toString();
	}

	/**
	 * Gets resolved name of the method that triggered the operation.
	 * 
	 * @param classMarker class marker to be used to locate the stack frame
	 * @param offset offset from the located stack frame (must be >= 0)
	 * @return name triggering operation
	 */
	public static String getResolvedName(Class<?> classMarker, int offset) {
		StackTraceElement item = Utils.getStackFrame(classMarker.getName(), offset);
		return item.toString();
	}

	/**
	 * Sets the name of the method that triggered the operation, truncating if necessary.
	 *
	 * @param opname function name triggering operation
	 */
	public void setName(String opname) {
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
	 * Determine if operation was ever started
	 *
	 * @return true if operation was started, false otherwise
	 */
	public boolean isStarted() {
		return (this.startTime != null);
	}

	/**
	 * Determine if operation was ever stopped
	 *
	 * @return true if operation was stopped, false otherwise
	 */
	public boolean isStopped() {
		return (this.endTime != null);
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
	 */
	public void setUser(String user) {
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
	 * Gets the wait time for the operation.
	 * This value represents the time the operation spent waiting.
	 *
	 * @return wait time for operation, in microseconds
	 */
	public long getWaitTime() {
		return waitTime;
	}

	/**
	 * Sets the wait time for the operation. 
	 * This value represents the time the operation spent waiting.
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
	 */
	public void setException(String exceptionStr) {
		if (exceptionStr != null && exceptionStr.length() == 0)
			exceptionStr = null;
		this.exceptionStr = exceptionStr;
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
	 */
	public void setCorrelator(String correlator) {
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
	 * @param startTimeUsec start time, in microseconds
	 * @throws IllegalArgumentException if startTime or startTimeUsec is negative
	 */
	public void start(long startTimeUsec) {
		this.startTimeNano = System.nanoTime();
		this.startTime = new UsecTimestamp(startTimeUsec);
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
		start(startTimestamp.getTimeUsec());
	}

	/**
	 * Indicates that the operation has started immediately.
	 */
	public void start() {
		start(Useconds.CURRENT.get());
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
	 * @param stopTimeUsec stop time, in microseconds
	 * @param elaspedUsec elapsed time in microseconds
	 * @throws IllegalArgumentException if stopTime or stopTimeUsec is negative,
	 *  or if the stop time is less than the previously specified start time
	 */
	public void stop(long stopTimeUsec, long elaspedUsec) {
		endTime = new UsecTimestamp(stopTimeUsec);

		if (startTime == null) {
			long startUsec = stopTimeUsec - elaspedUsec;
			startTime = new UsecTimestamp(startUsec);
		}

		if (endTime.compareTo(startTime) < 0)
			throw new IllegalArgumentException("stop time is less than start time");

		elapsedTime = endTime.difference(startTime);
		if (startTimeNano > 0) {
			stopTimeNano = System.nanoTime();	
			elapsedTimeNano = stopTimeNano - startTimeNano;
		}
	}

	/**
	 * Indicates that the operation has stopped at the specified stop time.
	 *
	 * @param stopTime stop time, in microseconds
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
	 * @param elaspedUsec elapsed time in microseconds
	 * @throws NullPointerException if stopTimestamp is <code>null</code>
	 * @throws IllegalArgumentException if stopTimestamp is invalid
	 */
	public void stop(UsecTimestamp stopTimestamp, long elaspedUsec) {
		if (stopTimestamp == null)
			throw new NullPointerException("stopTimestamp must be non-null");
		stop(stopTimestamp.getTimeUsec(), elaspedUsec);
	}

	/**
	 * Indicates that the operation has stopped immediately.
	 */
	public void stop() {
		stop(Useconds.CURRENT.get(), 0);
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
		   .append("SnapCount=").append(getSnapshotCount()).append(",")
		   .append("CompCode:").append(getCompCode()).append(",")
		   .append("ReasonCode:").append(getReasonCode()).append(",")
		   .append("PID:").append(getPID()).append(",")
		   .append("TID:").append(getTID()).append(",")
		   .append("ElapsedUsec:").append(getElapsedTime()).append(",")
		   .append("WaitUsec:").append(getWaitTime()).append(",")
		   .append("StartTime:[").append(sTime == null ? "null" : sTime.toString()).append("],")
		   .append("EndTime:[").append(eTime == null ? "null" : eTime.toString()).append("],")
		   .append("Exception:").append(getExceptionString()).append("}");

		return str.toString();
	}


	
	/**
	 * Gets all available snapshot keys associated with this operation
	 *
	 * @return a set of all available snapshot keys
	 */
	public Set<String> getSnapshotIds() {
		return snapshots.keySet();
	}

	/**
	 * Gets a snapshot associated with a specific key/id
	 *
	 * @return snapshot associated with a given id
	 * @see Snapshot
	 */
	public Snapshot getSnapshot(Object snapId) {
		return snapshots.get(snapId);
	}

	/**
	 * Associate a snapshot with this operation
	 *
	 * @param snapshot with a list of properties
	 * @see Snapshot
	 */
	public void addSnapshot(Snapshot snapshot) {
		snapshots.put(snapshot.getId(), snapshot);		
	}
	
	/**
	 * Gets the list of available snapshots
	 *
	 * @return list of available snapshots
	 * @see Snapshot
	 */
	public Collection<Snapshot> getSnapshots() {
		return snapshots.values();
	}

	/**
	 * Gets the number of available snapshots.
	 *
	 * @return number of available snapshots
	 */
	public int getSnapshotCount() {
		return snapshots != null ? snapshots.size() : 0;
	}
}
