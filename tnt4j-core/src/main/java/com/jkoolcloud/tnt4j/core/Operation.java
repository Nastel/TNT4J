/*
 * Copyright 2014-2022 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.core;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.jkoolcloud.tnt4j.source.DefaultSourceFactory;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.source.SourceFactory;
import com.jkoolcloud.tnt4j.source.SourceType;
import com.jkoolcloud.tnt4j.utils.Useconds;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Implements a Operation entity.
 * </p>
 *
 * <p>
 * An {@code Operation} object is used to represent an intercepted operation whose execution it to be recorded. An
 * {@code Operation} is associated with either a {@link Message}, if the operation acts upon a message (typically
 * send/receive operations), or a {@link Activity} if the operation does not act upon a message.
 * </p>
 *
 * <p>
 * An {@code Operation} is required to have its start time and end time set.
 * </p>
 *
 * @see OpType
 * @see OpCompCode
 * @see Activity
 * @see Message
 * @see Trackable
 *
 * @version $Revision: 12 $
 */
public class Operation implements TTL, GlobalID {

	/**
	 * Noop operation name
	 */
	public static final String NOOP = "NOOP";

	private long elapsedTimeUsec;
	private long elapsedTimeNano, startTimeNano, stopTimeNano;
	private long waitTimeUsec;
	private long pid;
	private long tid;
	private int opRC = 0;

	private String guid; // unique id
	private String opName;
	private String resource;
	private String user;
	private String exceptionStr;
	private String location;

	private OpType opType;
	private OpCompCode opCC = OpCompCode.SUCCESS;
	private OpLevel opLevel = OpLevel.INFO;
	private long ttlSec = TTL.TTL_DEFAULT; // 0 is default time to live
	private long startTimeUs;
	private long endTimeUs;
	private Throwable exHandle;

	private HashSet<String> correlators = new HashSet<>(89);
	private HashMap<String, Snapshot> snapshots = new HashMap<>(89);
	private HashMap<String, Property> properties = new HashMap<>(89);

	// timing attributes
	private int startStopCount = 0;
	private long startCPUTime = 0;
	private long stopCPUTime = 0;
	private long startBlockTime = 0;
	private long stopBlockTime = 0;
	private long startWaitTime = 0;
	private long stopWaitTime = 0;
	private boolean enableTiming = false;
	private ThreadInfo ownerThread = null;
	protected static ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
	private boolean cpuTimingSupported = tmbean.isThreadCpuTimeSupported() && tmbean.isThreadCpuTimeEnabled();
	private boolean contTimingSupported = tmbean.isThreadContentionMonitoringSupported();

	/**
	 * Creates a Operation with the specified properties. Operation name can be any name or a relative name based on the
	 * current thread stack trace. The relative operation name must be specified as follows:
	 * {@code $class-marker:offset}. Example: {@code $com.jkoolcloud.tnt4j.tracker:0} This name results in the actual
	 * operation name computed at runtime based on current thread stack at the time when {@link #getResolvedName()} is
	 * called.
	 *
	 * @param opname
	 *            function name triggering operation
	 * @param opType
	 *            operation type
	 * @see #getResolvedName()
	 */
	public Operation(String opname, OpType opType) {
		this(opname, opType, true);
	}

	/**
	 * Creates a Operation with the specified properties. Operation name can be any name or a relative name based on the
	 * current thread stack trace. The relative operation name must be specified as follows:
	 * {@code $class-marker:offset}. Example: {@code $com.jkoolcloud.tnt4j.tracker:0} This name results in the actual
	 * operation name computed at runtime based on current thread stack at the time when {@link #getResolvedName()} is
	 * called.
	 *
	 * @param opname
	 *            function name triggering operation
	 * @param opType
	 *            operation type
	 * @param threadTiming
	 *            enable/disable cpu, wait, block timing between start/stop
	 * @see #getResolvedName()
	 */
	public Operation(String opname, OpType opType, boolean threadTiming) {
		setName(opname);
		setType(opType);
		setPID(Utils.getVMPID());
		setTID(Thread.currentThread().getId());
		enableTiming = threadTiming;
	}

	/**
	 * Gets globally unique identifier for this operation
	 *
	 * @return globally unique identifier for this operation
	 */
	@Override
	public String getGUID() {
		return guid;
	}

	/**
	 * Sets the globally unique id for this operation (optional)
	 *
	 * @param uid
	 *            globally unique id
	 */
	@Override
	public void setGUID(String uid) {
		this.guid = uid;
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
	 * Gets resolved name of the operation. Runtime stack resolution occurs when the operation name is of the form:
	 * {@code $class-marker:offset}. Example: {@code $com.jkoolcloud.tnt4j.tracker:0}
	 *
	 * @return name triggering operation
	 */
	public String getResolvedName() {
		return opName != null ? Utils.getMethodNameFromStack(opName) : opName;
	}

	/**
	 * Sets the name of the method that triggered the operation, truncating if necessary.
	 *
	 * @param opname
	 *            function name triggering operation
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
	 * @param pid
	 *            process ID of process running Activity
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
	 * @param tid
	 *            thread ID of thread running Activity
	 */
	public void setTID(long tid) {
		this.tid = tid;
	}

	@Override
	public long getTTL() {
		return ttlSec;
	}

	@Override
	public void setTTL(long ttl) {
		this.ttlSec = ttl;
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
	 * @param opType
	 *            operation type
	 * @throws NullPointerException
	 *             if opType is {@code null}
	 */
	public void setType(OpType opType) {
		if (opType == null) {
			throw new NullPointerException("opType must be non-null");
		}
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
		return (this.startTimeUs > 0);
	}

	/**
	 * Determine if operation was ever stopped
	 *
	 * @return true if operation was stopped, false otherwise
	 */
	public boolean isStopped() {
		return (this.endTimeUs > 0);
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
	 * Sets the completion code for the operation. To provide a reason for a warning or failure, set a reason-code or
	 * exception message.
	 *
	 * @param opCC
	 *            function completion code
	 * @throws NullPointerException
	 *             if opCC is {@code null}
	 * @see #setReasonCode(int)
	 * @see #setException(String)
	 */
	public void setCompCode(OpCompCode opCC) {
		if (opCC == null) {
			throw new NullPointerException("opCC must be non-null");
		}
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
	 * @param opRC
	 *            function return code
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
	 * Gets the resource associated with this operation as {@link Source} implementation using default
	 * {@link DefaultSourceFactory} source factory.
	 *
	 * @return resource as a {@link Source}
	 */
	public Source getResourceAsSource() {
		return getResourceAsSource(DefaultSourceFactory.getInstance());
	}

	/**
	 * Gets the resource associated with this operation as {@link Source} implementation.
	 *
	 * @param factory
	 *            source factory implementation
	 * @return resource as a {@link Source}
	 */
	public Source getResourceAsSource(SourceFactory factory) {
		return factory.fromFQN(resource);
	}

	/**
	 * Sets the resource associated with this operation. The name should conform with {@link Source} FQN name
	 * convention.
	 *
	 * @param resource
	 *            name for operation
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}

	/**
	 * Sets the resource associated with this operation.
	 *
	 * @param source
	 *            resource name as a {@link Source}
	 */
	public void setResource(Source source) {
		this.resource = source.getFQName();
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
	 * @param user
	 *            name of user
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets the total time for the operation.
	 *
	 * @return elapsed time for operation, in microseconds
	 */
	public long getElapsedTimeUsec() {
		return elapsedTimeUsec;
	}

	/**
	 * Gets the total time for the operation in nanoseconds. Time is measured between a pair of start/stop calls.
	 *
	 * @return elapsed time for operation, in nanoseconds
	 */
	public long getElapsedTimeNano() {
		return elapsedTimeNano;
	}

	/**
	 * Gets the wait time for the operation. This value is only valid after stop and represents the time the operation
	 * spent waiting/blocked
	 *
	 * @return wait time for operation, in microseconds
	 */
	public long getWaitTimeUsec() {
		return waitTimeUsec;
	}

	/**
	 * Sets the wait time for the operation. This value represents the time the operation spent waiting.
	 *
	 * @param wTime
	 *            idle time for operation, in microseconds
	 * @throws IllegalArgumentException
	 *             if waitTime is negative
	 */
	public void setWaitTimeUsec(long wTime) {
		if (wTime < 0) {
			throw new IllegalArgumentException("waitTime must be non-negative");
		}
		this.waitTimeUsec = wTime;
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
	 * Gets the actual {@link java.lang.Throwable} exception associated with this operation.
	 *
	 * @return operation's exception
	 */
	public Throwable getThrowable() {
		return exHandle;
	}

	/**
	 * <p>
	 * Sets the exception message to associate with the operation based on the specified exception.
	 * </p>
	 *
	 * <p>
	 * If an exception is associated with the operation, then the completion code should be set to either
	 * {@code WARNING} or {@code ERROR}.
	 * </p>
	 *
	 * @param t
	 *            error thrown by operation
	 * @see #setCompCode(OpCompCode)
	 */
	public void setException(Throwable t) {
		exHandle = t;
		setException(Utils.printThrowable(exHandle));
	}

	/**
	 * <p>
	 * Sets the exception message to associate with the operation.
	 * </p>
	 *
	 * <p>
	 * If an exception is associated with the operation, then the completion code should be set to either
	 * {@code WARNING} or {@code ERROR}.
	 * </p>
	 *
	 * @param exceptionStr
	 *            operation's exception message
	 * @see #setCompCode(OpCompCode)
	 */
	public void setException(String exceptionStr) {
		if (exceptionStr != null && exceptionStr.isEmpty()) {
			exceptionStr = null;
		}
		this.exceptionStr = exceptionStr;
	}

	/**
	 * Sets the current severity level to associate with the operation.
	 *
	 * @param level
	 *            operation severity level to associate with operation
	 */
	public void setSeverity(OpLevel level) {
		opLevel = level;
	}

	/**
	 * Gets the current severity level to associate with the operation.
	 *
	 * @return current severity level
	 */
	public OpLevel getSeverity() {
		return opLevel;
	}

	/**
	 * Gets the location string identifying the location the operation was executed (e.g. GPS locator, source file line,
	 * etc.).
	 *
	 * @return location string for operation
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Sets the location string identifying the location the operation was executed (e.g. GPS locator, source file line,
	 * etc.).
	 *
	 * @param location
	 *            location string for operation
	 */
	public void setLocation(String location) {
		if (location != null && location.isEmpty()) {
			location = null;
		}
		this.location = location;
	}

	/**
	 * Gets the location string identifying the location the operation was executed (e.g. GPS locator, source file line,
	 * etc.).
	 *
	 * @param source
	 *            location source
	 */
	public void setLocation(Source source) {
		if (source != null) {
			Source geo = source.getSource(SourceType.GEOADDR);
			if (geo != null) {
				location = geo.getName();
			}
		}
	}

	/**
	 * Gets the list of correlators, which are a user-defined values to relate two separate operations as belonging to
	 * the same activity.
	 *
	 * @return user-defined set of correlators
	 */
	public Set<String> getCorrelator() {
		return correlators;
	}

	/**
	 * Sets correlators, which are a user-defined values to relate two separate operations as belonging to the same
	 * activity.
	 *
	 * @param clist
	 *            user-defined operation correlator
	 */
	public void setCorrelator(String... clist) {
		for (int i = 0; (clist != null) && (i < clist.length); i++) {
			if (clist[i] != null) {
				this.correlators.add(clist[i]);
			}
		}
	}

	/**
	 * Sets correlators, which are a user-defined values to relate two separate operations as belonging to the same
	 * activity.
	 *
	 * @param clist
	 *            user-defined correlators
	 */
	public void setCorrelator(Collection<String> clist) {
		if (clist != null) {
			this.correlators.addAll(clist);
		}
	}

	/**
	 * Remove all correlators
	 *
	 */
	public void clearCorrelators() {
		this.correlators.clear();
	}

	/**
	 * Remove all properties
	 *
	 */
	public void clearProperties() {
		this.properties.clear();
	}

	/**
	 * Gets the time the operation started.
	 *
	 * @return operation start time
	 */
	public UsecTimestamp getStartTime() {
		return new UsecTimestamp(startTimeUs);
	}

	/**
	 * Indicates that the operation has started at the specified start time.
	 *
	 * @param startTimeUsec
	 *            start time, in microseconds
	 * @throws IllegalArgumentException
	 *             if startTime or startTimeUsec is negative
	 */
	public void start(long startTimeUsec) {
		long start = System.nanoTime();
		this.startTimeNano = System.nanoTime();
		this.startTimeUs = startTimeUsec;
		_start(start);
	}

	/**
	 * Indicates that the operation has started at the specified start time.
	 *
	 * @param startTimestamp
	 *            start time
	 * @throws NullPointerException
	 *             if startTimestamp is {@code null}
	 * @throws IllegalArgumentException
	 *             if startTimestamp is invalid
	 */
	public void start(UsecTimestamp startTimestamp) {
		if (startTimestamp == null) {
			throw new NullPointerException("startTimestamp must be non-null");
		}
		start(startTimestamp.getTimeUsec());
	}

	/**
	 * Indicates that the operation has started immediately.
	 */
	public void start() {
		enableTiming = true;
		start(Useconds.CURRENT.get());
	}

	/**
	 * Gets the time the operation ended.
	 *
	 * @return operation end time
	 */
	public UsecTimestamp getEndTime() {
		return new UsecTimestamp(endTimeUs);
	}

	/**
	 * Indicates that the operation has stopped at the specified stop time.
	 *
	 * @param stopTimeUsec
	 *            stop time, in microseconds
	 * @param elapsedUsec
	 *            elapsed time in microseconds
	 * @throws IllegalArgumentException
	 *             if stopTime or stopTimeUsec is negative, or if the stop time is less than the previously specified
	 *             start time
	 */
	public void stop(long stopTimeUsec, long elapsedUsec) {
		long start = System.nanoTime();
		endTimeUs = stopTimeUsec;

		if (startTimeUs <= 0 || startTimeUs > (stopTimeUsec - elapsedUsec)) {
			long startUsec = stopTimeUsec - elapsedUsec;
			startTimeUs = startUsec;
		}

		if (startTimeNano > 0) {
			stopTimeNano = System.nanoTime();
			elapsedTimeNano = stopTimeNano - startTimeNano;
		}

		if (endTimeUs < startTimeUs) {
			if (startTimeNano > 0) {
				startTimeUs = endTimeUs - (elapsedTimeNano / 1000);
			} else {
				throw new IllegalArgumentException("end.time=" + endTimeUs + " is less than start.time=" + startTimeUs
						+ ", delta.usec=" + (endTimeUs - startTimeUs));
			}
		}

		elapsedTimeUsec = elapsedUsec > 0 ? elapsedUsec : endTimeUs - startTimeUs;
		_stop(start);
	}

	/**
	 * Indicates that the operation has stopped at the specified stop time.
	 *
	 * @param stopTimeUsec
	 *            stop time, in microseconds
	 * @throws IllegalArgumentException
	 *             if stopTime is negative, or if the stop time is less than the previously specified start time
	 */
	public void stop(long stopTimeUsec) {
		stop(stopTimeUsec, 0);
	}

	/**
	 * Indicates that the operation has stopped at the specified stop time.
	 *
	 * @param stopTimestamp
	 *            stop time
	 * @param elapsedUsec
	 *            elapsed time in microseconds
	 * @throws NullPointerException
	 *             if stopTimestamp is {@code null}
	 * @throws IllegalArgumentException
	 *             if stopTimestamp is invalid
	 */
	public void stop(UsecTimestamp stopTimestamp, long elapsedUsec) {
		if (stopTimestamp == null) {
			throw new NullPointerException("stopTimestamp must be non-null");
		}
		stop(stopTimestamp.getTimeUsec(), elapsedUsec);
	}

	/**
	 * Indicates that the operation has stopped immediately.
	 */
	public void stop() {
		stop(Useconds.CURRENT.get(), 0);
	}

	/**
	 * Return thread handle that owns this activity. Owner is the tread that started this activity when
	 * {@link com.jkoolcloud.tnt4j.tracker.TrackingActivity#start()} is called. There can only be one thread that owns
	 * an activity. All thread/activity metrics are computed based on the owner thread. It is possible, but not
	 * recommended to use the same {@link com.jkoolcloud.tnt4j.tracker.TrackingActivity} instance across multiple
	 * threads, where start/stop are run across thread boundaries.
	 *
	 * @return thread owner info
	 */
	public ThreadInfo getThreadInfo() {
		return ownerThread;
	}

	private void _start(long start) {
		if (startStopCount == 0) {
			startStopCount++;
			if (enableTiming) {
				ownerThread = tmbean.getThreadInfo(Thread.currentThread().getId());
				startCPUTime = cpuTimingSupported ? tmbean.getThreadCpuTime(ownerThread.getThreadId()) : 0;
				if (contTimingSupported) {
					startBlockTime = ownerThread.getBlockedTime();
					startWaitTime = ownerThread.getWaitedTime();
				}
			}
			onStart(start);
		}
	}

	private void _stop(long start) {
		if (startStopCount == 1) {
			startStopCount++;
			if (startCPUTime > 0) {
				if (contTimingSupported) {
					stopBlockTime = ownerThread == null ? 0 : ownerThread.getBlockedTime();
					stopWaitTime = ownerThread == null ? 0 : ownerThread.getWaitedTime();
					setWaitTimeUsec(((stopWaitTime - startWaitTime) + (stopBlockTime - startBlockTime)) * 1000);
				}
				stopCPUTime = getCurrentCpuTimeNano();
			}
			onStop(start);
		}
	}

	/**
	 * Override this method to implement logic once operation started.
	 *
	 * @param timer
	 *            operation start timestamp
	 */
	protected void onStart(long timer) {
	}

	/**
	 * Override this method to implement logic once operation stopped.
	 *
	 * @param timer
	 *            operation stop timestamp
	 */
	protected void onStop(long timer) {
	}

	/**
	 * This method returns total CPU time in nanoseconds currently used by the thread that owns this operation. Owner
	 * thread is the one that started this operation. Owner thread can be obtained by calling {@link #getThreadInfo()}
	 *
	 * @return total currently used CPU time in nanoseconds
	 */
	public long getCurrentCpuTimeNano() {
		return (cpuTimingSupported && (ownerThread != null) ? tmbean.getThreadCpuTime(ownerThread.getThreadId()) : -1);
	}

	/**
	 * This method returns total CPU time in nanoseconds used since the start. If the operation has stopped the value
	 * returned is an elapsed CPU time since between start/stop calls. If the operation has not stopped yet, the value
	 * is the current used CPU time since the start until now.
	 *
	 * @return total used CPU time in nanoseconds
	 */
	public long getUsedCpuTimeNano() {
		if (stopCPUTime > 0) {
			return (stopCPUTime - startCPUTime);
		} else if (startCPUTime > 0) {
			return (getCurrentCpuTimeNano() - startCPUTime);
		} else {
			return -1;
		}
	}

	/**
	 * This method returns total wall time computed between start/stop/current-time. wall-time is computed as total used
	 * cpu + blocked time + wait time.
	 *
	 * @return total wall time in microseconds
	 */
	public long getWallTimeUsec() {
		long wallTime;
		if (stopCPUTime > 0) {
			long cpuUsed = getUsedCpuTimeNano();
			double cpuUsec = (cpuUsed / 1000.0d);
			wallTime = (long) (cpuUsec + getWaitTimeUsec());
		} else {
			long cpuUsed = getUsedCpuTimeNano();
			double cpuUsec = (cpuUsed / 1000.0d);
			long blockTime = ownerThread == null ? 0 : ownerThread.getBlockedTime();
			long waitTime = ownerThread == null ? 0 : ownerThread.getWaitedTime();
			wallTime = (long) (cpuUsec + ((waitTime - startWaitTime) * 1000) + ((blockTime - startBlockTime) * 1000));
		}
		return wallTime;
	}

	/**
	 * This method returns total block time computed after activity has stopped.
	 * 
	 * @return total blocked time in microseconds, -1 if not stopped yet
	 */
	public long getOnlyBlockedTimeUsec() {
		return stopBlockTime > 0 ? ((stopBlockTime - startBlockTime) * 1000) : -1;
	}

	/**
	 * This method returns total wait time computed after activity has stopped.
	 * 
	 * @return total waited time in microseconds, -1 if not stopped yet
	 */
	public long getOnlyWaitTimeUsec() {
		return stopWaitTime > 0 ? ((stopWaitTime - startWaitTime) * 1000) : -1;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		UsecTimestamp ts = getStartTime();

		result = prime * result + ((opName == null) ? 0 : opName.hashCode());

		if (ts != null) {
			result = prime * result + ts.hashCode();
		}

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Operation)) {
			return false;
		}

		Operation other = (Operation) obj;

		if (opName == null) {
			if (other.opName != null) {
				return false;
			}
		} else if (!opName.equals(other.opName)) {
			return false;
		}

		if (startTimeUs != other.startTimeUs) {
			return false;
		}

		if (endTimeUs != other.endTimeUs) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		OpType type = getType();
		String res = getResource();
		UsecTimestamp sTime = getStartTime();
		UsecTimestamp eTime = getEndTime();
		StringBuilder str = new StringBuilder();

		str.append(getClass().getSimpleName()).append("{") //
				.append("Name:").append(getName()).append(",") //
				.append("Guid:").append(getGUID()).append(",") //
				.append("Type:").append(type == null ? "null" : type.toString()).append(",") //
				.append("Correlator:").append(getCorrelator()).append(",") //
				.append("Location:").append(getLocation()).append(",") //
				.append("Resource:").append(res == null ? "null" : res).append(",") //
				.append("User:").append(getUser()).append(",") //
				.append("SnapCount=").append(getSnapshotCount()).append(",") //
				.append("PropCount=").append(getPropertyCount()).append(",") //
				.append("CompCode:").append(getCompCode()).append(",") //
				.append("ReasonCode:").append(getReasonCode()).append(",") //
				.append("PID:").append(getPID()).append(",") //
				.append("TID:").append(getTID()).append(",") //
				.append("ElapsedUsec:").append(getElapsedTimeUsec()).append(",") //
				.append("WaitUsec:").append(getWaitTimeUsec()).append(",") //
				.append("WallUsec:").append(getWallTimeUsec()).append(",") //
				.append("StartTime:[").append(sTime.toString()).append("],") //
				.append("EndTime:[").append(eTime.toString()).append("],") //
				.append("Exception:").append(getExceptionString()).append("}");

		return str.toString();
	}

	/**
	 * Gets all available property keys associated with this operation
	 *
	 * @return a set of all available property keys
	 */
	public Set<String> getPropertyKeys() {
		return properties.keySet();
	}

	/**
	 * Add a user defined property
	 *
	 * @param prop
	 *            property to be added
	 * @see Property
	 */
	public void addProperty(Property prop) {
		properties.put(prop.getKey(), prop);
	}

	/**
	 * Gets a property associated with a specific key/id
	 *
	 * @param key
	 *            property id
	 * @return property associated with a given key
	 * @see Snapshot
	 */
	public Property getProperty(Object key) {
		return properties.get(key);
	}

	/**
	 * Gets the list of available properties
	 *
	 * @return list of available properties
	 * @see Property
	 */
	public Collection<Property> getProperties() {
		return properties.values();
	}

	/**
	 * Gets the number of available properties.
	 *
	 * @return number of available properties
	 */
	public int getPropertyCount() {
		return properties != null ? properties.size() : 0;
	}

	/**
	 * Gets all available snapshot keys associated with this operation
	 *
	 * @return a set of all available snapshot keys
	 */
	public Set<String> getSnapshotKeys() {
		return snapshots.keySet();
	}

	/**
	 * Gets a snapshot associated with a specific key/id
	 *
	 * @param snapId
	 *            snapshot id
	 * @return snapshot associated with a given id
	 * @see Snapshot
	 */
	public Snapshot getSnapshot(Object snapId) {
		return snapshots.get(snapId);
	}

	/**
	 * Associate a snapshot with this operation
	 *
	 * @param snapshot
	 *            with a list of properties
	 * @see Snapshot
	 */
	public void addSnapshot(Snapshot snapshot) {
		snapshots.put(snapshot.getSnapKey(), snapshot);
		snapshot.setTTL(getTTL());
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

	/**
	 * Returns value of {@code fieldName} defined field/property for this operation.
	 * <p>
	 * List of supported field names:
	 * <ul>
	 * <li>ElapsedTimeUsec</li>
	 * <li>ElapsedTimeNano</li>
	 * <li>StartTimeUsec</li>
	 * <li>StartTimeNano</li>
	 * <li>StopTimeNano</li>
	 * <li>EndTimeUsec</li>
	 * <li>StartTime</li>
	 * <li>EndTime</li>
	 * <li>WaitTimeUsec</li>
	 * <li>PID</li>
	 * <li>TID</li>
	 * <li>ReasonCode</li>
	 * <li>Guid</li>
	 * <li>Name</li>
	 * <li>Resource</li>
	 * <li>User</li>
	 * <li>Exception</li>
	 * <li>Location</li>
	 * <li>Type</li>
	 * <li>CompCode</li>
	 * <li>Severity</li>
	 * <li>TTL</li>
	 * <li>Throwable</li>
	 * <li>Correlator</li>
	 * <li>Snapshots</li>
	 * <li>SnapshotsCount</li>
	 * <li>Properties</li>
	 * <li>PropertiesCount</li>
	 * <li>Custom property name from contained properties set</li>
	 * </ul>
	 *
	 * @param fieldName
	 *            operation field or property name
	 * @return field/property contained value
	 */
	public Object getFieldValue(String fieldName) {
		if ("ElapsedTimeUsec".equalsIgnoreCase(fieldName)) {
			return elapsedTimeUsec;
		}
		if ("ElapsedTimeNano".equalsIgnoreCase(fieldName)) {
			return elapsedTimeNano;
		}
		if ("StartTimeUsec".equalsIgnoreCase(fieldName)) {
			return startTimeUs;
		}
		if ("StartTimeNano".equalsIgnoreCase(fieldName)) {
			return startTimeNano;
		}
		if ("StopTimeNano".equalsIgnoreCase(fieldName)) {
			return stopTimeNano;
		}
		if ("EndTimeUsec".equalsIgnoreCase(fieldName)) {
			return endTimeUs;
		}
		if ("StartTime".equalsIgnoreCase(fieldName)) {
			return getStartTime();
		}
		if ("EndTime".equalsIgnoreCase(fieldName)) {
			return getEndTime();
		}
		if ("WaitTimeUsec".equalsIgnoreCase(fieldName)) {
			return waitTimeUsec;
		}
		if ("PID".equalsIgnoreCase(fieldName)) {
			return pid;
		}
		if ("TID".equalsIgnoreCase(fieldName)) {
			return tid;
		}
		if ("ReasonCode".equalsIgnoreCase(fieldName)) {
			return opRC;
		}
		if ("Guid".equalsIgnoreCase(fieldName)) {
			return guid;
		}
		if ("Name".equalsIgnoreCase(fieldName)) {
			return opName;
		}
		if ("Resource".equalsIgnoreCase(fieldName)) {
			return resource;
		}
		if ("User".equalsIgnoreCase(fieldName)) {
			return user;
		}
		if ("Exception".equalsIgnoreCase(fieldName)) {
			return exceptionStr;
		}
		if ("Location".equalsIgnoreCase(fieldName)) {
			return location;
		}
		if ("Type".equalsIgnoreCase(fieldName)) {
			return opType;
		}
		if ("CompCode".equalsIgnoreCase(fieldName)) {
			return opCC;
		}
		if ("Severity".equalsIgnoreCase(fieldName)) {
			return opLevel;
		}
		if ("TTL".equalsIgnoreCase(fieldName)) {
			return ttlSec;
		}
		if ("Throwable".equalsIgnoreCase(fieldName)) {
			return exHandle;
		}
		if ("Correlator".equalsIgnoreCase(fieldName)) {
			return correlators;
		}
		if ("Snapshots".equalsIgnoreCase(fieldName)) {
			return snapshots;
		}
		if ("SnapshotsCount".equalsIgnoreCase(fieldName)) {
			return getSnapshotCount();
		}
		if ("Properties".equalsIgnoreCase(fieldName)) {
			return properties;
		}
		if ("PropertiesCount".equalsIgnoreCase(fieldName)) {
			return getPropertyCount();
		}

		Property property = getProperty(fieldName);
		if (property != null) {
			return property.getValue();
		}

		return null;
	}
}
