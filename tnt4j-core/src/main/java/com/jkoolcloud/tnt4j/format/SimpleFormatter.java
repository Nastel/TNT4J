/*
 * Copyright 2014-2019 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.format;

import java.util.Collection;
import java.util.Set;
import java.util.TimeZone;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Property;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.core.UsecTimestamp;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Simple implementation of {@link Formatter} interface provides simple/minimal formatting of {@link TrackingActivity}
 * and {@code TrackingEvent} as well as any object passed to {@code format()} method call. Event entries are formatted
 * as follows: {@code event-text-msg {event-tracking-info}} where {@code event-tracking-info} consists of
 * {@code "name: value"} pairs.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see DefaultFormatter
 * @see TrackingActivity
 * @see TrackingEvent
 */

public class SimpleFormatter extends DefaultFormatter {

	/**
	 * Create a simple event formatter with default setting. Format: "{2} | {1} | {0}", TimeZone: UTC.
	 *
	 */
	public SimpleFormatter() {
	}

	/**
	 * Create a simple event formatter instance with a given format
	 *
	 * @param format
	 *            string (e.g. "{2} | {1} | {0}")
	 */
	public SimpleFormatter(String format) {
		super(format);
	}

	/**
	 * Create a simple event formatter instance with a given format, timezone
	 *
	 * @param format
	 *            string (e.g. "{2} | {1} | {0}")
	 * @param tz
	 *            time zone
	 */
	public SimpleFormatter(String format, TimeZone tz) {
		super(format, tz);
	}

	@Override
	public String format(TrackingEvent event) {
		StringBuilder msg = new StringBuilder(1024);
		msg.append(event.getMessage()).append(" ");
		msg.append("{name: '").append(event.getOperation().getResolvedName()).append("'");
		if (event.getOperation().getPropertyCount() > 0) {
			msg.append(separator);
			msg.append("prop-count: '").append(event.getOperation().getPropertyCount()).append("'");
		}
		if (event.getOperation().getSnapshotCount() > 0) {
			msg.append(separator);
			msg.append("snap-count: '").append(event.getOperation().getSnapshotCount()).append("'");
		}
		if (event.getOperation().getResource() != null) {
			msg.append(separator);
			msg.append("resource: '").append(event.getOperation().getResource()).append("'");
		}
		if (event.getOperation().getReasonCode() != 0) {
			msg.append(separator);
			msg.append("rcode: '").append(event.getOperation().getReasonCode()).append("'");
		}
		if (event.getOperation().getElapsedTimeUsec() != 0) {
			msg.append(separator);
			msg.append("usec: '").append(event.getOperation().getElapsedTimeUsec()).append("'");
		}
		if (event.getMessageAge() != 0) {
			msg.append(separator);
			msg.append("age.usec: '").append(event.getMessageAge()).append("'");
		}
		if (event.getOperation().getWaitTimeUsec() != 0) {
			msg.append(separator);
			msg.append("wait.usec: '").append(event.getOperation().getWaitTimeUsec()).append("'");
		}
		if (!event.getTag().isEmpty()) {
			msg.append(separator);
			msg.append("tag: '").append(event.getTag()).append("'");
		}
		if (!event.getOperation().getCorrelator().isEmpty()) {
			msg.append(separator);
			msg.append("corr-id: '").append(event.getOperation().getCorrelator()).append("'");
		}
		if (event.getOperation().getLocation() != null) {
			msg.append(separator);
			msg.append("location: '").append(event.getOperation().getLocation()).append("'");
		}
		if (event.getSource() != null) {
			msg.append(separator);
			msg.append("source: '").append(event.getSource().getName()).append("'");
		}
		if (event.getParentId() != null) {
			msg.append(separator);
			msg.append("parent-id: '").append(event.getParentId()).append("'");
		}
		if (event.getTrackingId() != null) {
			msg.append(separator);
			msg.append("track-id: '").append(event.getTrackingId()).append("'");
		}
		if (!Utils.isEmpty(event.getGUID())) {
			msg.append(separator);
			msg.append("guid: '").append(event.getGUID()).append("'");
		}
		if (event.getOperation().getPropertyCount() > 0) {
			msg.append("\n\t").append("Properties {");
			formatProperties(msg, event.getOperation().getProperties());
			msg.append("\n\t}");
		}
		if (event.getOperation().getSnapshotCount() > 0) {
			Collection<Snapshot> snapshots = event.getOperation().getSnapshots();
			for (Snapshot snap : snapshots) {
				msg.append("\n\t");
				format(msg, snap);
			}
		}
		if (event.getOperation().getThrowable() != null) {
			msg.append("\nThrowable {\n").append(Utils.printThrowable(event.getOperation().getThrowable())).append("}");
		}
		msg.append("}");
		return msg.toString();
	}

	@Override
	public String format(TrackingActivity activity) {
		StringBuilder msg = new StringBuilder(1024);
		msg.append("{status: '").append(activity.getStatus()).append("'").append(separator);
		msg.append("time: '").append(UsecTimestamp.getTimeStamp(timeZone)).append("'").append(separator);
		msg.append("sev: '").append(activity.getSeverity()).append("'").append(separator);
		msg.append("type: '").append(activity.getType()).append("'").append(separator);

		if (!Utils.isEmpty(activity.getResolvedName())) {
			msg.append("name: '").append(activity.getResolvedName()).append("'");
		}
		if (!Utils.isEmpty(activity.getResource())) {
			msg.append(separator);
			msg.append("resource: '").append(activity.getResource()).append("'");
		}
		if (activity.getElapsedTimeUsec() != 0) {
			msg.append(separator);
			msg.append("usec: '").append(activity.getElapsedTimeUsec()).append("'");
		}
		if (activity.getWaitTimeUsec() != 0) {
			msg.append(separator);
			msg.append("wait.usec: '").append(activity.getWaitTimeUsec()).append("'");
		}
		if (activity.getStartTime() != null) {
			msg.append(separator);
			msg.append("start.time: '").append(activity.getStartTime()).append("'");
		}
		if (activity.getEndTime() != null) {
			msg.append(separator);
			msg.append("end.time: '").append(activity.getEndTime()).append("'");
		}
		if (!Utils.isEmpty(activity.getLocation())) {
			msg.append(separator);
			msg.append("location: '").append(activity.getLocation()).append("'");
		}
		if (activity.getIdCount() > 0) {
			msg.append(separator);
			msg.append("id-count: '").append(activity.getIdCount()).append("'");
		}
		if (activity.getSnapshotCount() > 0) {
			msg.append(separator);
			msg.append("snap-count: '").append(activity.getSnapshotCount()).append("'");
		}
		if (activity.getSource() != null) {
			msg.append(separator);
			msg.append("source: '").append(activity.getSource().getName()).append("'");
		}
		if (!Utils.isEmpty(activity.getParentId())) {
			msg.append(separator);
			msg.append("parent-id: '").append(activity.getParentId()).append("'");
		}
		if (!Utils.isEmpty(activity.getTrackingId())) {
			msg.append(separator);
			msg.append("track-id: '").append(activity.getTrackingId()).append("'");
		}
		if (!Utils.isEmpty(activity.getGUID())) {
			msg.append(separator);
			msg.append("guid: '").append(activity.getGUID()).append("'");
		}
		if (activity.getSnapshotCount() > 0) {
			msg.append(separator);
			Collection<Snapshot> snapshots = activity.getSnapshots();
			for (Snapshot snap : snapshots) {
				msg.append("\n\t");
				format(msg, snap);
			}
		}
		if (activity.getThrowable() != null) {
			msg.append(separator);
			msg.append("\nThrowable {\n").append(Utils.printThrowable(activity.getThrowable())).append("}");
		}
		msg.append("}");
		return msg.toString();
	}

	@Override
	public String format(Snapshot snap) {
		StringBuilder msg = new StringBuilder(1024);
		return format(msg, snap).toString();
	}

	@Override
	public String format(long ttl, Source src, OpLevel level, String msg, Object... args) {
		String formatted = super.format(ttl, src, level, msg, args);
		Throwable error = Utils.getThrowable(args);
		if (error != null) {
			formatted += "\nThrowable {\n" + Utils.printThrowable(error) + "}";
		}
		return formatted;
	}

	protected StringBuilder format(StringBuilder msg, Snapshot snap) {
		msg.append("Snapshot(fqn: '").append(snap.getId()).append("'");
		String pid = snap.getParentId();
		String tid = snap.getTrackingId();
		Set<String> cid = snap.getCorrelator();
		if (pid != null) {
			msg.append(separator);
			msg.append("parent-id: '").append(pid).append("'");
		}
		if (tid != null) {
			msg.append(separator);
			msg.append("track-id: '").append(tid).append("'");
		}
		if (!Utils.isEmpty(snap.getGUID())) {
			msg.append(separator);
			msg.append("guid: '").append(snap.getGUID()).append("'");
		}
		if (!cid.isEmpty()) {
			msg.append(separator);
			msg.append("corr-id: '").append(cid).append("'");
		}
		msg.append(") {");
		formatProperties(msg, snap.getProperties());
		msg.append("\n\t}");
		return msg;
	}

	protected void formatProperties(StringBuilder msg, Collection<Property> properties) {
		for (Property prop : properties) {
			if (prop.isTransient()) {
				continue;
			}
			msg.append("\n\t\t").append(prop.getKey()).append(": '").append(prop.getValue()).append(":")
					.append(prop.getDataType()).append(":").append(prop.getValueType()).append("'");
		}
	}
}
