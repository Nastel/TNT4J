/*
 * Copyright 2014-2024 JKOOL, LLC.
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

import java.util.Collection;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.OpType;
import com.jkoolcloud.tnt4j.core.Trackable;
import com.jkoolcloud.tnt4j.source.Source;

/**
 * Log entry defines a named list of properties.
 * 
 * @version $Revision: 1 $
 */
public class LogEntry extends TrackingEvent {

	private LogType logType = LogType.GENERAL;

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, String opName, String msg, Object... args) {
		super(tr, src, severity, opName, (String) null, msg, args);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, String opName, byte[] msg, Object... args) {
		super(tr, src, severity, opName, (String) null, msg, args);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName, String tag, String msg,
			Object... args) {
		super(tr, src, severity, opType, opName, null, tag, msg, args);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName, String tag, byte[] msg,
			Object... args) {
		super(tr, src, severity, opType, opName, null, tag, msg, args);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName, Collection<String> tags,
			String msg, Object... args) {
		super(tr, src, severity, opType, opName, null, tags, msg, args);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName, Collection<String> tags,
			byte[] msg, Object... args) {
		super(tr, src, severity, opType, opName, null, tags, msg, args);
	}

	@Override
	public void setParentId(Trackable parentObject) {
		throw new UnsupportedOperationException("Logs can't have parent relation");
	}

	@Override
	public void setParentId(String parentId) {
		throw new UnsupportedOperationException("Logs can't have parent relation");
	}

	@Override
	public void setCorrelator(String... cid) {
		throw new UnsupportedOperationException("Logs can't have correlators");
	}

	@Override
	public void setCorrelator(Collection<String> cids) {
		throw new UnsupportedOperationException("Logs can't have correlators");
	}

	/**
	 * Gets log type.
	 * 
	 * @return message type
	 */
	public LogType getLogType() {
		return logType;
	}

	/**
	 * Sets log type.
	 * 
	 * @param logType
	 *            log type
	 */
	public void setLogType(LogType logType) {
		this.logType = logType;
	}

	/**
	 * Returns value of {@code fieldName} defined field/property for this tracking event.
	 * <p>
	 * List of supported field names (in common with
	 * {@link com.jkoolcloud.tnt4j.tracker.TrackingEvent#getFieldValue(String)}):
	 * <ul>
	 * <li>LogType</li>
	 * </ul>
	 *
	 * @param fieldName
	 *            event field or property name
	 * @return field/property contained value
	 */
	@Override
	public Object getFieldValue(String fieldName) {
		if ("LogType".equalsIgnoreCase(fieldName)) {
			return logType;
		}

		return super.getFieldValue(fieldName);
	}
}
