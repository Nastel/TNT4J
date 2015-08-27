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
package com.nastel.jkool.tnt4j.format;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.nastel.jkool.tnt4j.config.Configurable;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.core.Property;
import com.nastel.jkool.tnt4j.core.Snapshot;
import com.nastel.jkool.tnt4j.core.ValueTypes;
import com.nastel.jkool.tnt4j.source.DefaultSourceFactory;
import com.nastel.jkool.tnt4j.source.Source;
import com.nastel.jkool.tnt4j.source.SourceType;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;
import com.nastel.jkool.tnt4j.utils.Useconds;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * JSON implementation of <code>Formatter</code> interface provides default formatting of <code>TrackingActvity</code>,
 * <code>TrackingEvent</code>, <code>Snapshot</code>, <code>Property</code> into JSON format.
 * </p>
 *
 *
 * @version $Revision: 22 $
 *
 * @see DefaultFormatter
 * @see TrackingActivity
 * @see TrackingEvent
 * @see Snapshot
 * @see Property
 */

public class JSONFormatter implements EventFormatter, Configurable, JSONLabels {
	private static final boolean NEWLINE_FORMAT = Boolean.getBoolean("tnt4j.formatter.json.newline");

	protected static final String START = "{";
	protected static final String START_LINE = "{\n";
	protected static final String END = "}";
	protected static final String END_LINE = "\n}";
	protected static final String ATTR_END = ",";
	protected static final String ATTR_END_LINE = ",\n";
	protected static final String ATTR_SEP = ": ";
	protected static final String ARRAY_END = "]";
	protected static final String ARRAY_START = "[";
	protected static final String ARRAY_START_LINE = "[\n";

	private Map<String, Object> config = null;
	private boolean newLineFormat = true;
	private String START_JSON = START_LINE;
	private String END_JSON = END_LINE;
	private String ATTR_JSON = ATTR_END_LINE;
	private String ARRAY_START_JSON = ARRAY_START_LINE;

	/**
	 * Create JSON formatter without newlines during formatting
	 *
	 */
	public JSONFormatter() {
		this(NEWLINE_FORMAT);
	}

	/**
	 * Create JSON formatter and conditionally format with newline
	 *
	 * @param newLine
	 *            apply newline formatting to JSON
	 */
	public JSONFormatter(boolean newLine) {
		newLineFormat = newLine;
		initTags();
	}

	private void initTags() {
		START_JSON = newLineFormat ? START_LINE : START;
		END_JSON = newLineFormat ? END_LINE : END;
		ATTR_JSON = newLineFormat ? ATTR_END_LINE : ATTR_END;
		ARRAY_START_JSON = newLineFormat ? ARRAY_START_LINE : ARRAY_START;
	}

	@Override
	public String format(Object obj, Object... args) {
		if (obj instanceof TrackingActivity) {
			return format((TrackingActivity) obj);
		} else if (obj instanceof TrackingEvent) {
			return format((TrackingEvent) obj);
		} else if (obj instanceof Snapshot) {
			return format((Snapshot) obj);
		} else if (obj instanceof Property) {
			return format((Property) obj);
		} else {
			StringBuilder jsonString = new StringBuilder(1024);
			jsonString.append(START_JSON);
			jsonString.append(JSON_TIME_USEC_LABEL).append(ATTR_SEP).append(Useconds.CURRENT.get()).append(ATTR_JSON);

			String msgText = Utils.format(obj.toString(), args);
			msgText = StringEscapeUtils.escapeJson(msgText); // escape double quote chars
			jsonString.append(JSON_MSG_TEXT_LABEL).append(ATTR_SEP).append(Utils.quote(msgText));
			jsonString.append(END_JSON);
			return jsonString.toString();
		}
	}

	/**
	 * Format a given <code>TrackingEvent</code> into JSON format
	 *
	 * @param event
	 *            tracking event instance to be formatted
	 * @see TrackingEvent
	 */
	@Override
	public String format(TrackingEvent event) {
		StringBuilder jsonString = new StringBuilder(1024);

		jsonString.append(START_JSON).append(JSON_TRACK_ID_LABEL).append(ATTR_SEP).append(
		        Utils.quote(event.getTrackingId())).append(ATTR_JSON);
		if (!Utils.isEmpty(event.getParentId())) {
			jsonString.append(JSON_PARENT_TRACK_ID_LABEL).append(ATTR_SEP).append(
			        Utils.quote(event.getParentId())).append(ATTR_JSON);
		}
		jsonString.append(JSON_SOURCE_LABEL).append(ATTR_SEP).append(
		        Utils.quote(event.getSource().getName())).append(ATTR_JSON);
		jsonString.append(JSON_SOURCE_FQN_LABEL).append(ATTR_SEP).append(
		        Utils.quote(event.getSource().getFQName())).append(ATTR_JSON);
		String ssn = event.getSource().getSourceFactory().getSSN();
		if (!Utils.isEmpty(ssn)) {
			String escaped = StringEscapeUtils.escapeJson(ssn); // escape double quote chars
			jsonString.append(JSON_SOURCE_SSN_LABEL).append(ATTR_SEP).append(Utils.quote(escaped)).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(event.getSource().getUrl())) {
			String escaped = StringEscapeUtils.escapeJson(event.getSource().getUrl()); // escape double quote chars
			jsonString.append(JSON_SOURCE_URL_LABEL).append(ATTR_SEP).append(Utils.quote(escaped)).append(ATTR_JSON);
		}
		jsonString.append(JSON_SEVERITY_LABEL).append(ATTR_SEP).append(Utils.quote(event.getSeverity()))
		        .append(ATTR_JSON);
		jsonString.append(JSON_SEVERITY_NO_LABEL).append(ATTR_SEP).append(event.getSeverity().ordinal())
		        .append(ATTR_JSON);
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP).append(
		        Utils.quote(event.getOperation().getType())).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_NO_LABEL).append(ATTR_SEP).append(
		        event.getOperation().getType().ordinal()).append(ATTR_JSON);
		jsonString.append(JSON_PID_LABEL).append(ATTR_SEP).append(event.getOperation().getPID()).append(ATTR_JSON);
		jsonString.append(JSON_TID_LABEL).append(ATTR_SEP).append(event.getOperation().getTID()).append(ATTR_JSON);
		jsonString.append(JSON_COMP_CODE_LABEL).append(ATTR_SEP).append(
		        Utils.quote(event.getOperation().getCompCode())).append(ATTR_JSON);
		jsonString.append(JSON_COMP_CODE_NO_LABEL).append(ATTR_SEP).append(event.getOperation().getCompCode().ordinal()).append(ATTR_JSON);
		jsonString.append(JSON_REASON_CODE_LABEL).append(ATTR_SEP).append(event.getOperation().getReasonCode()).append(ATTR_JSON);
		jsonString.append(JSON_TTL_SEC_LABEL).append(ATTR_SEP).append(event.getTTL()).append(ATTR_JSON);

		if (!Utils.isEmpty(event.getLocation())) {
			String escaped = StringEscapeUtils.escapeJson(event.getLocation()); // escape double quote chars
			jsonString.append(JSON_LOCATION_LABEL).append(ATTR_SEP).append(
			        Utils.quote(escaped)).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(event.getOperation().getResolvedName())) {
			String escaped = StringEscapeUtils.escapeJson(event.getOperation().getResolvedName()); // escape double quote chars
			jsonString.append(JSON_OPERATION_LABEL).append(ATTR_SEP).append(
					Utils.quote(escaped)).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(event.getOperation().getResource())) {
			String escaped = StringEscapeUtils.escapeJson(event.getOperation().getResource()); // escape double quote chars
			jsonString.append(JSON_RESOURCE_LABEL).append(ATTR_SEP).append(
			        Utils.quote(escaped)).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(event.getOperation().getUser())) {
			String escaped = StringEscapeUtils.escapeJson(event.getOperation().getUser()); // escape double quote chars
			jsonString.append(JSON_USER_LABEL).append(ATTR_SEP).append(
			        Utils.quote(escaped)).append(ATTR_JSON);
		}
		jsonString.append(JSON_TIME_USEC_LABEL).append(ATTR_SEP).append(Useconds.CURRENT.get()).append(ATTR_JSON);
		if (event.getOperation().getStartTime() != null) {
			jsonString.append(JSON_START_TIME_USEC_LABEL).append(ATTR_SEP).append(
			        event.getOperation().getStartTime().getTimeUsec()).append(ATTR_JSON);
		}
		if (event.getOperation().getEndTime() != null) {
			jsonString.append(JSON_END_TIME_USEC_LABEL).append(ATTR_SEP).append(
			        event.getOperation().getEndTime().getTimeUsec()).append(ATTR_JSON);
			jsonString.append(JSON_ELAPSED_TIME_USEC_LABEL).append(ATTR_SEP).append(
			        event.getOperation().getElapsedTimeUsec()).append(ATTR_JSON);
			if (event.getOperation().getWaitTimeUsec() > 0) {
				jsonString.append(JSON_WAIT_TIME_USEC_LABEL).append(ATTR_SEP).append(
				        event.getOperation().getWaitTimeUsec()).append(ATTR_JSON);
			}
			if (event.getMessageAge() > 0) {
				jsonString.append(JSON_MSG_AGE_USEC_LABEL).append(ATTR_SEP).append(event.getMessageAge())
				        .append(ATTR_JSON);
			}
		}
		int snapCount = event.getOperation().getSnapshotCount();
		int propCount = event.getOperation().getPropertyCount();
		jsonString.append(JSON_SNAPSHOT_COUNT_LABEL).append(ATTR_SEP).append(snapCount).append(ATTR_JSON);
		jsonString.append(JSON_PROPERTY_COUNT_LABEL).append(ATTR_SEP).append(propCount).append(ATTR_JSON);
		jsonString.append(JSON_MSG_SIZE_LABEL).append(ATTR_SEP).append(event.getSize()).append(ATTR_JSON);
		jsonString.append(JSON_MSG_MIME_LABEL).append(ATTR_SEP).append(Utils.quote(event.getMimeType())).append(ATTR_JSON);
		jsonString.append(JSON_MSG_ENC_LABEL).append(ATTR_SEP).append(Utils.quote(event.getEncoding())).append(ATTR_JSON);
		jsonString.append(JSON_MSG_CHARSET_LABEL).append(ATTR_SEP).append(Utils.quote(event.getCharset()));

		String msgText = event.getMessage();
		if (!Utils.isEmpty(msgText)) {
			jsonString.append(ATTR_JSON);
			msgText = StringEscapeUtils.escapeJson(msgText); // escape double quote chars
			jsonString.append(JSON_MSG_TEXT_LABEL).append(ATTR_SEP).append(Utils.quote(msgText));
		}

		String exStr = event.getOperation().getExceptionString();
		if (!Utils.isEmpty(exStr)) {
			jsonString.append(ATTR_JSON);
			String excText = StringEscapeUtils.escapeJson(exStr); // escape double quote chars
			jsonString.append(JSON_EXCEPTION_LABEL).append(ATTR_SEP).append(Utils.quote(excText));
		}
		if (!Utils.isEmpty(event.getCorrelator())) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_CORR_ID_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON).append(itemsToJSON(event.getCorrelator()))
	        	.append(ARRAY_END);
		}
		if (!Utils.isEmpty(event.getTag())) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_MSG_TAG_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON).append(itemsToJSON(event.getTag()))
			        .append(ARRAY_END);
		}
		if (propCount > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_PROPERTIES_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON).append(
			        itemsToJSON(event.getOperation().getProperties())).append(ARRAY_END);
		}
		if (snapCount > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_SNAPSHOTS_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON).append(
			        itemsToJSON(event.getOperation().getSnapshots())).append(ARRAY_END);
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	/**
	 * Format a given <code>TrackingActivity</code> into JSON format
	 *
	 * @param activity
	 *            tracking activity instance to be formatted
	 * @see TrackingActivity
	 */
	@Override
	public String format(TrackingActivity activity) {
		StringBuilder jsonString = new StringBuilder(1024);
		String START_JSON = newLineFormat ? START_LINE : START;
		String END_JSON = newLineFormat ? END_LINE : END;
		String ATTR_JSON = newLineFormat ? ATTR_END_LINE : ATTR_END;

		jsonString.append(START_JSON).append(JSON_TRACK_ID_LABEL).append(ATTR_SEP).append(
		        Utils.quote(activity.getTrackingId())).append(ATTR_JSON);
		if (activity.getParentId() != null) {
			jsonString.append(JSON_PARENT_TRACK_ID_LABEL).append(ATTR_SEP).append(
			        Utils.quote(activity.getParentId())).append(ATTR_JSON);
		}
		jsonString.append(JSON_SOURCE_LABEL).append(ATTR_SEP).append(
		        Utils.quote(activity.getSource().getName())).append(ATTR_JSON);
		jsonString.append(JSON_SOURCE_FQN_LABEL).append(ATTR_SEP).append(
		        Utils.quote(activity.getSource().getFQName())).append(ATTR_JSON);

		String info = activity.getSource().getSourceFactory().getSSN();
		if (!Utils.isEmpty(info)) {
			String escaped = StringEscapeUtils.escapeJson(info); // escape double quote chars
			jsonString.append(JSON_SOURCE_SSN_LABEL).append(ATTR_SEP).append(Utils.quote(escaped)).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(activity.getSource().getUrl())) {
			String escaped = StringEscapeUtils.escapeJson(activity.getSource().getUrl()); // escape double quote chars
			jsonString.append(JSON_SOURCE_URL_LABEL).append(ATTR_SEP).append(Utils.quote(escaped)).append(ATTR_JSON);
		}
		jsonString.append(JSON_STATUS_LABEL).append(ATTR_SEP).append(Utils.quote(activity.getStatus()))
		        .append(ATTR_JSON);
		jsonString.append(JSON_SEVERITY_LABEL).append(ATTR_SEP)
		        .append(Utils.quote(activity.getSeverity())).append(ATTR_JSON);
		jsonString.append(JSON_SEVERITY_NO_LABEL).append(ATTR_SEP)
		        .append(activity.getSeverity().ordinal()).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP).append(Utils.quote(activity.getType()))
		        .append(ATTR_JSON);
		jsonString.append(JSON_TYPE_NO_LABEL).append(ATTR_SEP).append(activity.getType().ordinal())
		        .append(ATTR_JSON);
		jsonString.append(JSON_PID_LABEL).append(ATTR_SEP).append(activity.getPID()).append(ATTR_JSON);
		jsonString.append(JSON_TID_LABEL).append(ATTR_SEP).append(activity.getTID()).append(ATTR_JSON);
		jsonString.append(JSON_COMP_CODE_LABEL).append(ATTR_SEP).append(
		        Utils.quote(activity.getCompCode())).append(ATTR_JSON);
		jsonString.append(JSON_COMP_CODE_NO_LABEL).append(ATTR_SEP).append(activity.getCompCode().ordinal()).append(ATTR_JSON);
		jsonString.append(JSON_REASON_CODE_LABEL).append(ATTR_SEP).append(activity.getReasonCode()).append(ATTR_JSON);
		jsonString.append(JSON_TTL_SEC_LABEL).append(ATTR_SEP).append(activity.getTTL()).append(ATTR_JSON);
		if (!Utils.isEmpty(activity.getLocation())) {
			String escaped = StringEscapeUtils.escapeJson(activity.getLocation()); // escape double quote chars
			jsonString.append(JSON_LOCATION_LABEL).append(ATTR_SEP).append(Utils.quote(escaped)).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(activity.getResolvedName())) {
			String escaped = StringEscapeUtils.escapeJson(activity.getResolvedName()); // escape double quote chars
			jsonString.append(JSON_OPERATION_LABEL).append(ATTR_SEP).append(Utils.quote(escaped)).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(activity.getResource())) {
			String escaped = StringEscapeUtils.escapeJson(activity.getResource()); // escape double quote chars
			jsonString.append(JSON_RESOURCE_LABEL).append(ATTR_SEP)
			        .append(Utils.quote(escaped)).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(activity.getSource().getUser())) {
			String escaped = StringEscapeUtils.escapeJson(activity.getSource().getUser()); // escape double quote chars
			jsonString.append(JSON_USER_LABEL).append(ATTR_SEP).append(Utils.quote(escaped)).append(ATTR_JSON);
		}

		jsonString.append(JSON_TIME_USEC_LABEL).append(ATTR_SEP).append(Useconds.CURRENT.get()).append(ATTR_JSON);
		if (activity.getStartTime() != null) {
			jsonString.append(JSON_START_TIME_USEC_LABEL).append(ATTR_SEP).append(
			        activity.getStartTime().getTimeUsec()).append(ATTR_JSON);
		}
		if (activity.getEndTime() != null) {
			jsonString.append(JSON_END_TIME_USEC_LABEL).append(ATTR_SEP).append(
			        activity.getEndTime().getTimeUsec()).append(ATTR_JSON);
			jsonString.append(JSON_ELAPSED_TIME_USEC_LABEL).append(ATTR_SEP).append(
			        activity.getElapsedTimeUsec()).append(ATTR_JSON);
			if (activity.getWaitTimeUsec() > 0) {
				jsonString.append(JSON_WAIT_TIME_USEC_LABEL).append(ATTR_SEP).append(
				        activity.getWaitTimeUsec()).append(ATTR_JSON);
			}
		}
		jsonString.append(JSON_ID_COUNT_LABEL).append(ATTR_SEP).append(activity.getIdCount()).append(ATTR_JSON);
		jsonString.append(JSON_SNAPSHOT_COUNT_LABEL).append(ATTR_SEP).append(activity.getSnapshotCount()).append(ATTR_JSON);
		jsonString.append(JSON_PROPERTY_COUNT_LABEL).append(ATTR_SEP).append(activity.getPropertyCount());

		String exStr = activity.getExceptionString();
		if (!Utils.isEmpty(exStr)) {
			jsonString.append(ATTR_JSON);
			String excText = StringEscapeUtils.escapeJson(exStr); // escape double quote chars
			jsonString.append(JSON_EXCEPTION_LABEL).append(ATTR_SEP).append(Utils.quote(excText));
		}
		if (!Utils.isEmpty(activity.getCorrelator())) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_CORR_ID_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON).append(
					itemsToJSON(activity.getCorrelator())).append(ARRAY_END);
		}
		if (activity.getIdCount() > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_ID_SET_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON).append(
			        itemsToJSON(activity.getIds())).append(ARRAY_END);
		}
		if (activity.getPropertyCount() > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_PROPERTIES_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON).append(
			        itemsToJSON(activity.getProperties())).append(ARRAY_END);
		}
		if (activity.getSnapshotCount() > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_SNAPSHOTS_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON).append(
			        itemsToJSON(activity.getSnapshots())).append(ARRAY_END);
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	/**
	 * Format a given <code>Snapshot</code> into JSON format
	 *
	 * @param snap
	 *            snapshot object to be formatted into JSON
	 * @see Snapshot
	 */
	@Override
	public String format(Snapshot snap) {
		StringBuilder jsonString = new StringBuilder(1024);
		jsonString.append(START_JSON);

		if (!Utils.isEmpty(snap.getTrackingId())) {
			jsonString.append(START_JSON).append(JSON_TRACK_ID_LABEL).append(ATTR_SEP).append(
			        Utils.quote(snap.getTrackingId())).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(snap.getParentId())) {
			jsonString.append(JSON_PARENT_TRACK_ID_LABEL).append(ATTR_SEP).append(
			        Utils.quote(snap.getParentId())).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(snap.getId())) {
			jsonString.append(JSON_FQN_LABEL).append(ATTR_SEP)
			        .append(Utils.quote(snap.getId())).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(snap.getCategory())) {
			jsonString.append(JSON_CATEGORY_LABEL).append(ATTR_SEP)
			        .append(Utils.quote(snap.getCategory())).append(ATTR_JSON);
		}
		jsonString.append(JSON_NAME_LABEL).append(ATTR_SEP).append(Utils.quote(snap.getName())).append(ATTR_JSON);
		jsonString.append(JSON_COUNT_LABEL).append(ATTR_SEP).append(snap.size()).append(ATTR_JSON);
		jsonString.append(JSON_TIME_USEC_LABEL).append(ATTR_SEP).append(snap.getTimeStamp().getTimeUsec()).append(ATTR_JSON);
		jsonString.append(JSON_TTL_SEC_LABEL).append(ATTR_SEP).append(snap.getTTL()).append(ATTR_JSON);

		Source source = snap.getSource();
		if (source != null) {
			jsonString.append(JSON_SOURCE_LABEL).append(ATTR_SEP).append(Utils.quote(source.getName())).append(ATTR_JSON);
			jsonString.append(JSON_SOURCE_FQN_LABEL).append(ATTR_SEP).append(Utils.quote(source.getFQName())).append(ATTR_JSON);
			String ssn = source.getSourceFactory().getSSN();
			if (!Utils.isEmpty(ssn)) {
				String escaped = StringEscapeUtils.escapeJson(ssn); // escape double quote chars
				jsonString.append(JSON_SOURCE_SSN_LABEL).append(ATTR_SEP).append(Utils.quote(escaped)).append(ATTR_JSON);
			}
			if (!Utils.isEmpty(source.getUrl())) {
				String escaped = StringEscapeUtils.escapeJson(source.getUrl()); // escape double quote chars
				jsonString.append(JSON_SOURCE_URL_LABEL).append(ATTR_SEP).append(Utils.quote(escaped)).append(ATTR_JSON);
			}
		}
		jsonString.append(JSON_SEVERITY_LABEL).append(ATTR_SEP).append(Utils.quote(snap.getSeverity())).append(ATTR_JSON);
		jsonString.append(JSON_SEVERITY_NO_LABEL).append(ATTR_SEP).append(snap.getSeverity().ordinal()).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP).append(Utils.quote(snap.getType())).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_NO_LABEL).append(ATTR_SEP).append(snap.getType().ordinal());
		if (snap.size() > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_PROPERTIES_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON).append(
			        itemsToJSON(snap.getSnapshot())).append(ARRAY_END);
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	/**
	 * Format a given <code>Property</code> into JSON format
	 *
	 * @param prop property object to be formatted into JSON
	 * @return formatted property as a JSON string
	 * @see Property
	 */
	public String format(Property prop) {
		StringBuilder jsonString = new StringBuilder(1024);
		jsonString.append(START_JSON);
		Object value = prop.getValue();
		jsonString.append(JSON_NAME_LABEL).append(ATTR_SEP).append(Utils.quote(prop.getKey()))
		        .append(ATTR_JSON);
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP).append(Utils.quote(prop.getDataType()))
		        .append(ATTR_JSON);
		if (prop.getValueType() != null && !prop.getValueType().equalsIgnoreCase(ValueTypes.VALUE_TYPE_NONE)) {
			jsonString.append(JSON_VALUE_TYPE_LABEL).append(ATTR_SEP).append(Utils.quote(prop.getValueType())).append(ATTR_JSON);
		}
		if (value instanceof Number) {
			jsonString.append(JSON_VALUE_LABEL).append(ATTR_SEP).append(value);
		} else {
			String valueText = StringEscapeUtils.escapeJson(String.valueOf(value));
			jsonString.append(JSON_VALUE_LABEL).append(ATTR_SEP).append(Utils.quote(valueText));
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	@Override
	public String format(long ttl, Source source, OpLevel level, String msg, Object... args) {
		StringBuilder jsonString = new StringBuilder(1024);
		jsonString.append(START_JSON);
		jsonString.append(JSON_SEVERITY_LABEL).append(ATTR_SEP).append(Utils.quote(level)).append(ATTR_JSON);
		jsonString.append(JSON_SEVERITY_NO_LABEL).append(ATTR_SEP).append(level.ordinal()).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP).append(Utils.quote(OpType.EVENT)).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_NO_LABEL).append(ATTR_SEP).append(OpType.EVENT.ordinal()).append(ATTR_JSON);

		jsonString.append(JSON_PID_LABEL).append(ATTR_SEP).append(Utils.getVMPID()).append(ATTR_JSON);
		jsonString.append(JSON_TID_LABEL).append(ATTR_SEP).append(Thread.currentThread().getId()).append(ATTR_JSON);

		String usrName = StringEscapeUtils.escapeJson(source == null? DefaultSourceFactory.getInstance().getRootSource().getUser(): source.getUser());
		jsonString.append(JSON_USER_LABEL).append(ATTR_SEP).append(Utils.quote(usrName)).append(ATTR_JSON);
		jsonString.append(JSON_TTL_SEC_LABEL).append(ATTR_SEP).append(ttl).append(ATTR_JSON);
		jsonString.append(JSON_TIME_USEC_LABEL).append(ATTR_SEP).append(Useconds.CURRENT.get()).append(ATTR_JSON);
		if (source != null) {
			jsonString.append(JSON_SOURCE_LABEL).append(ATTR_SEP).append(Utils.quote(source.getName())).append(ATTR_JSON);
			jsonString.append(JSON_SOURCE_FQN_LABEL).append(ATTR_SEP).append(Utils.quote(source.getFQName()));
			String ssn = source.getSourceFactory().getSSN();
			if (!Utils.isEmpty(ssn)) {
				jsonString.append(ATTR_JSON);
				String escaped = StringEscapeUtils.escapeJson(ssn); // escape double quote chars
				jsonString.append(JSON_SOURCE_SSN_LABEL).append(ATTR_SEP).append(Utils.quote(escaped));
			}
			if (!Utils.isEmpty(source.getUrl())) {
				jsonString.append(ATTR_JSON);
				String escaped = StringEscapeUtils.escapeJson(source.getUrl()); // escape double quote chars
				jsonString.append(JSON_SOURCE_URL_LABEL).append(ATTR_SEP).append(Utils.quote(escaped));
			}
			Source location = source.getSource(SourceType.GEOADDR);
			if (location != null) {
				jsonString.append(ATTR_JSON);
				jsonString.append(JSON_LOCATION_LABEL).append(ATTR_SEP).append(Utils.quote(location.getName()));
			}
		}
		if (!Utils.isEmpty(msg)) {
			String msgText = Utils.format(msg, args);
			msgText = StringEscapeUtils.escapeJson(msgText); // escape double quote chars
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_MSG_TEXT_LABEL).append(ATTR_SEP).append(Utils.quote(msgText));
		}
		Throwable ex = Utils.getThrowable(args);
		if (ex != null) {
			jsonString.append(ATTR_JSON);
			String excText = StringEscapeUtils.escapeJson(ex.toString()); // escape double quote chars
			jsonString.append(JSON_EXCEPTION_LABEL).append(ATTR_SEP).append(Utils.quote(excText));
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	private String itemsToJSON(Collection<?> items) {
		if (items == null)
			return "";
		StringBuilder json = new StringBuilder(2048);
		for (Object item : items) {
			if (json.length() > 0)
				json.append(ATTR_JSON);
			if (item instanceof TrackingEvent) {
				json.append(format((TrackingEvent) item));
			} else if (item instanceof TrackingActivity) {
				json.append(format((TrackingActivity) item));
			} else if (item instanceof Snapshot) {
				json.append(format((Snapshot) item));
			} else if (item instanceof Property) {
				json.append(format((Property) item));
			} else {
				String vText = StringEscapeUtils.escapeJson(String.valueOf(item)); // escape double quote chars
				json.append(Utils.quote(vText));
			}
		}
		return json.toString();
	}

	@Override
	public Map<String, Object> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> settings) {
		config = settings;
		newLineFormat = config.get("Newline") != null ? Boolean.valueOf(config.get("Newline").toString())
		        : newLineFormat;
		initTags();
	}
}
