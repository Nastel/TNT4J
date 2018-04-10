/*
 * Copyright 2014-2018 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.jkoolcloud.tnt4j.source.Source;

/**
 * <p>
 * Implements a collection of related event and sub-activities.
 * </p>
 *
 * <p>
 * Represents a collection of trackable items {@link Trackable} that should be considered to be a single related unit.
 * These are generally delimited by BEGIN/END (or BEGIN/EXCEPTION)calls.
 * </p>
 *
 * <p>
 * A {@link Activity} is required to have its start time set, and to have it's end time set if its status is not
 * {@link ActivityStatus#BEGIN}.
 * </p>
 *
 * @see ActivityStatus
 * @see ActivityListener
 * @see Message
 * @see Operation
 * @see Property
 * @see Trackable
 *
 * @version $Revision: 11 $
 */
public class Activity extends Operation implements Trackable {
	private Source appl;
	private String tracking_id;
	private String parentId;
	private String sign;
	private ActivityStatus status = ActivityStatus.BEGIN;

	private HashSet<String> idset = new HashSet<String>(89);
	private ArrayList<ActivityListener> activityListeners = null;

	/**
	 * Creates a Activity object with the specified tracking id.
	 *
	 * @param id
	 *            Activity tracking id
	 * @throws NullPointerException
	 *             if the tracking id is {@code null}
	 * @throws IllegalArgumentException
	 *             if the tracking id is empty or is too long
	 * @see #setTrackingId(String)
	 */
	public Activity(String id) {
		super(Operation.NOOP, OpType.ACTIVITY);
		setTrackingId(id);
		setTID(Thread.currentThread().getId());
	}

	/**
	 * Creates a Activity object with the specified tracking id and name.
	 *
	 * @param id
	 *            Activity tracking id
	 * @param name
	 *            activity name
	 * @throws NullPointerException
	 *             if the tracking id is {@code null}
	 * @throws IllegalArgumentException
	 *             if the tracking id is empty or is too long
	 * @see #setTrackingId(String)
	 */
	public Activity(String id, String name) {
		super(name, OpType.ACTIVITY);
		setTrackingId(id);
		setTID(Thread.currentThread().getId());
	}

	/**
	 * Creates a Activity object with the specified tracking id.
	 *
	 * @param appl
	 *            application handle associated with this activity
	 * @param id
	 *            Activity tracking id
	 * @throws NullPointerException
	 *             if the tracking id is {@code null}
	 * @throws IllegalArgumentException
	 *             if the tracking id is empty or is too long
	 * @see #setTrackingId(String)
	 * @see #setSource(Source)
	 */
	public Activity(String id, Source appl) {
		super(appl.getName(), OpType.ACTIVITY);
		setTrackingId(id);
		setSource(appl);
		setTID(Thread.currentThread().getId());
	}

	/**
	 * Creates a Activity object with the specified tracking id.
	 *
	 * @param id
	 *            Activity tracking id
	 * @param name
	 *            assign activity name
	 * @param appl
	 *            application handle associated with this activity
	 * @throws NullPointerException
	 *             if the tracking id is {@code null}
	 * @throws IllegalArgumentException
	 *             if the tracking id is empty or is too long
	 * @see #setTrackingId(String)
	 * @see #setSource(Source)
	 */
	public Activity(String id, String name, Source appl) {
		super(name, OpType.ACTIVITY);
		setTrackingId(id);
		setSource(appl);
		setTID(Thread.currentThread().getId());
	}

	/**
	 * Register an activity listener for notifications when activity timing events occur.
	 *
	 * @param listener
	 *            activity listener to register
	 * @see ActivityListener
	 */
	public void addActivityListener(ActivityListener listener) {
		if (activityListeners == null) {
			activityListeners = new ArrayList<ActivityListener>(10);
		}
		activityListeners.add(listener);
	}

	/**
	 * Remove an activity listener for notifications when activity timing events occur.
	 *
	 * @param listener
	 *            activity listener to remove
	 * @see ActivityListener
	 */
	public void removeActivityListener(ActivityListener listener) {
		if (activityListeners != null) {
			activityListeners.remove(listener);
		}
	}

	/**
	 * Subclasses should use this helper class to trigger start notifications
	 *
	 * @see ActivityListener
	 */
	protected void notifyStarted() {
		if (activityListeners == null) {
			return;
		}
		for (ActivityListener listener : activityListeners) {
			listener.started(this);
		}
	}

	/**
	 * Subclasses should use this helper class to trigger stop notifications
	 *
	 * @see ActivityListener
	 */
	protected void notifyStopped() {
		if (activityListeners == null) {
			return;
		}
		for (ActivityListener listener : activityListeners) {
			listener.stopped(this);
		}
	}

	@Override
	protected void onStart(long start) {
		super.onStart(start);
		notifyStarted();
	}

	@Override
	protected void onStop(long start) {
		super.onStop(start);
		notifyStopped();
	}

	@Override
	public void setSource(Source source) {
		appl = source;
	}

	@Override
	public Source getSource() {
		return appl;
	}

	/**
	 * Gets the Activity tracking id, which is the unique identifier for the Activity.
	 *
	 * @return Activity tracking id
	 */
	@Override
	public String getTrackingId() {
		return tracking_id;
	}

	/**
	 * Sets the Activity tracking id, which is the unique identifier for the Activity. Could be any string that will
	 * uniquely identify this Activity.
	 *
	 * @param id
	 *            Activity tracking id
	 */
	@Override
	public void setTrackingId(String id) {
		this.tracking_id = id;
	}

	/**
	 * Gets the Activity status.
	 *
	 * @return Activity status
	 */
	public ActivityStatus getStatus() {
		return status;
	}

	/**
	 * Sets the Activity status.
	 *
	 * @param status
	 *            Activity status
	 * @throws NullPointerException
	 *             if status is {@code null}y
	 */
	public void setStatus(ActivityStatus status) {
		if (status == null) {
			throw new NullPointerException("status must be a non-null");
		}
		this.status = status;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParentId(Trackable parentObject) {
		this.parentId = parentObject != null ? parentObject.getTrackingId() : parentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParentId() {
		return parentId;
	}

	/**
	 * Adds the specified linked item to the list of items referenced in this Activity. This method does NOT check for
	 * duplicates.
	 *
	 * @param item
	 *            linked item referenced in Activity
	 * @throws NullPointerException
	 *             if item is {@code null}
	 * @see #containsId(String)
	 */
	public void add(Trackable item) {
		if (item == null) {
			throw new NullPointerException("trackable item must be non-null");
		}

		String tid = item.getTrackingId();
		if (tid != null) {
			idset.add(tid);
		}
		Set<String> cid = item.getCorrelator();
		if (cid != null) {
			idset.addAll(cid);
		}
		if (item instanceof Snapshot) {
			addSnapshot((Snapshot) item);
		}
		item.setTTL(getTTL());
		item.setParentId(this);
	}

	/**
	 * Checks whether the specified tracking id has been added to the list of items referenced in this Activity.
	 *
	 * @param id
	 *            linked item to test for
	 * @return {@code true} if the Activity contains specified item, {@code false} otherwise
	 */
	public boolean containsId(String id) {
		if (id == null) {
			return false;
		}
		return idset.contains(id);
	}

	/**
	 * Gets the list of tracking ids referenced in this Activity.
	 *
	 * @return list of tracking ids
	 */
	public Set<String> getIds() {
		return idset;
	}

	/**
	 * Gets the number of linked items referenced in this Activity.
	 *
	 * @return number of linked items
	 */
	public int getIdCount() {
		return idset != null ? idset.size() : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 31 + ((tracking_id == null) ? 0 : tracking_id.hashCode());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Activity)) {
			return false;
		}

		Activity other = (Activity) obj;

		if (tracking_id == null) {
			if (other.tracking_id != null) {
				return false;
			}
		} else if (!tracking_id.equals(other.tracking_id)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		OpType sType = getType();
		ActivityStatus Status = getStatus();
		UsecTimestamp sTime = getStartTime();
		UsecTimestamp eTime = getEndTime();
		StringBuilder str = new StringBuilder();

		str.append(getClass().getSimpleName()).append("{")
			.append("Name:").append(getName()).append(",")
			.append("ParentId:").append(parentId != null? parentId: "root").append(",")
			.append("TrackId:").append(getTrackingId()).append(",")
			.append("Status:").append(Status == null ? "null" : Status.toString()).append(",")
			.append("Type:").append(sType == null ? "null" : sType.toString()).append(",")
			.append("PID:").append(getPID()).append(",")
			.append("TID:").append(getTID()).append(",")
			.append("ElapsedUsec:").append(getElapsedTimeUsec()).append(",")
			.append("WallUsec:").append(getWallTimeUsec()).append(",")
			.append("FQName:").append(getSource().getFQName()).append(",")
			.append("IdCount=").append(getIdCount()).append(",")
			.append("SnapCount=").append(getSnapshotCount()).append(",")
			.append("StartTime:[").append(sTime.toString()).append("],")
			.append("EndTime:[").append(eTime.toString()).append("]}");

		return str.toString();
	}

	@Override
	public Object getFieldValue(String fieldName) {
		if ("Name".equalsIgnoreCase(fieldName)) {
			return getName();
		}
		if ("ParentId".equalsIgnoreCase(fieldName)) {
			return getParentId();
		}
		if ("TrackId".equalsIgnoreCase(fieldName)) {
			return getTrackingId();
		}
		if ("Status".equalsIgnoreCase(fieldName)) {
			return getStatus();
		}
		if ("Type".equalsIgnoreCase(fieldName)) {
			return getType();
		}
		if ("PID".equalsIgnoreCase(fieldName)) {
			return getPID();
		}
		if ("TID".equalsIgnoreCase(fieldName)) {
			return getTID();
		}
		if ("ElapsedUsec".equalsIgnoreCase(fieldName)) {
			return getElapsedTimeUsec();
		}
		if ("WallUsec".equalsIgnoreCase(fieldName)) {
			return getWallTimeUsec();
		}
		if ("FQName".equalsIgnoreCase(fieldName)) {
			return getSource().getFQName();
		}
		if ("IdCount".equalsIgnoreCase(fieldName)) {
			return getIdCount();
		}
		if ("SnapCount".equalsIgnoreCase(fieldName)) {
			return getSnapshotCount();
		}
		if ("StartTime".equalsIgnoreCase(fieldName)) {
			return getStartTime();
		}
		if ("EndTime".equalsIgnoreCase(fieldName)) {
			return getEndTime();
		}
		if ("Severity".equalsIgnoreCase(fieldName)) {
			return getSeverity();
		}
		if ("Resource".equalsIgnoreCase(fieldName)) {
			return getResource();
		}
		if ("Source".equalsIgnoreCase(fieldName)) {
			return getSource();
		}
		Property property = getProperty(fieldName);
		if (property != null) {
			return property.getValue();
		}

		return super.getFieldValue(fieldName);
	}

	@Override
	public String getSignature() {
		return sign;
	}

	@Override
	public void setSignature(String sign) {
		this.sign = sign;
	}
}
