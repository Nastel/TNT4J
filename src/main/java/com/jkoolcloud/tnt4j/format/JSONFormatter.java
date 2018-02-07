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
package com.jkoolcloud.tnt4j.format;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.core.*;
import com.jkoolcloud.tnt4j.source.DefaultSourceFactory;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.source.SourceType;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Useconds;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * JSON implementation of {@link Formatter} interface provides default formatting of {@link TrackingActivity},
 * {@link TrackingEvent}, {@link Snapshot}, {@link Property} into JSON format.
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
	protected static final String EMPTY_STR = "";
	protected static final String EMPTY_PROP = "{}";
	private static final String DEF_OP_NAME = "log";

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
	protected boolean newLineFormat = true;
	protected String defOpName = DEF_OP_NAME;
	protected SpecNumbersHandling specialNumbersHandling = SpecNumbersHandling.SUPPRESS;

	protected String START_JSON = START_LINE;
	protected String END_JSON = END_LINE;
	protected String ATTR_JSON = ATTR_END_LINE;
	protected String ARRAY_START_JSON = ARRAY_START_LINE;

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

			String msgText = StringEscapeUtils.escapeJson(Utils.format(Utils.toString(obj), args)); // escape double quote chars
			jsonString.append(JSON_MSG_TEXT_LABEL).append(ATTR_SEP);
			Utils.quote(msgText, jsonString);
			jsonString.append(END_JSON);
			return jsonString.toString();
		}
	}

	/**
	 * Format a given {@link TrackingEvent} into JSON format
	 *
	 * @param event
	 *            tracking event instance to be formatted
	 * @see TrackingEvent
	 */
	@Override
	public String format(TrackingEvent event) {
		StringBuilder jsonString = new StringBuilder(1024);

		jsonString.append(START_JSON);
		if (!Utils.isEmpty(event.getTrackingId())) {
			jsonString.append(JSON_TRACK_ID_LABEL).append(ATTR_SEP);
			Utils.quote(event.getTrackingId(), jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(event.getSignature())) {
			jsonString.append(JSON_TRACK_SIGN_LABEL).append(ATTR_SEP);
			Utils.quote(event.getSignature(), jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(event.getParentId())) {
			jsonString.append(JSON_PARENT_TRACK_ID_LABEL).append(ATTR_SEP);
			Utils.quote(event.getParentId(), jsonString).append(ATTR_JSON);
		}
		jsonString.append(JSON_SOURCE_LABEL).append(ATTR_SEP);
		Utils.quote(StringEscapeUtils.escapeJson(event.getSource().getName()), jsonString).append(ATTR_JSON);
		String ssn = getSSN(event.getSource());
		if (!Utils.isEmpty(ssn)) {
			String escaped = StringEscapeUtils.escapeJson(ssn); // escape double quote chars
			jsonString.append(JSON_SOURCE_SSN_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		jsonString.append(JSON_SOURCE_FQN_LABEL).append(ATTR_SEP);
		Utils.quote(StringEscapeUtils.escapeJson(event.getSource().getFQName()), jsonString).append(ATTR_JSON);
		if (!Utils.isEmpty(event.getSource().getUrl())) {
			String escaped = StringEscapeUtils.escapeJson(event.getSource().getUrl()); // escape double quote chars
			jsonString.append(JSON_SOURCE_URL_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		if (event.get2(TrackingEvent.OBJ_ONE) != null) {
			// we have a relation
			jsonString.append(JSON_RELATE_TYPE_LABEL).append(ATTR_SEP);
			Utils.quote(event.get2Type(), jsonString).append(ATTR_JSON);
			jsonString.append(JSON_RELATE_FQN_A_LABEL).append(ATTR_SEP);
			Utils.quote(event.get2(TrackingEvent.OBJ_ONE).getFQName(), jsonString).append(ATTR_JSON);
			jsonString.append(JSON_RELATE_FQN_B_LABEL).append(ATTR_SEP);
			Utils.quote(event.get2(TrackingEvent.OBJ_TWO).getFQName(), jsonString).append(ATTR_JSON);
		}
		jsonString.append(JSON_SEVERITY_LABEL).append(ATTR_SEP);
		Utils.quote(event.getSeverity(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_SEVERITY_NO_LABEL).append(ATTR_SEP).append(event.getSeverity().ordinal())
				.append(ATTR_JSON);
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP);
		Utils.quote(event.getOperation().getType(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_NO_LABEL).append(ATTR_SEP).append(event.getOperation().getType().ordinal())
				.append(ATTR_JSON);
		jsonString.append(JSON_PID_LABEL).append(ATTR_SEP).append(event.getOperation().getPID()).append(ATTR_JSON);
		jsonString.append(JSON_TID_LABEL).append(ATTR_SEP).append(event.getOperation().getTID()).append(ATTR_JSON);
		jsonString.append(JSON_COMP_CODE_LABEL).append(ATTR_SEP);
		Utils.quote(event.getOperation().getCompCode(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_COMP_CODE_NO_LABEL).append(ATTR_SEP).append(event.getOperation().getCompCode().ordinal())
				.append(ATTR_JSON);
		jsonString.append(JSON_REASON_CODE_LABEL).append(ATTR_SEP).append(event.getOperation().getReasonCode())
				.append(ATTR_JSON);
		jsonString.append(JSON_TTL_SEC_LABEL).append(ATTR_SEP).append(event.getTTL()).append(ATTR_JSON);

		if (!Utils.isEmpty(event.getLocation())) {
			String escaped = StringEscapeUtils.escapeJson(event.getLocation()); // escape double quote chars
			jsonString.append(JSON_LOCATION_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(event.getOperation().getResolvedName())) {
			String escaped = StringEscapeUtils.escapeJson(event.getOperation().getResolvedName()); // escape double
																									// quote chars
			jsonString.append(JSON_OPERATION_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(event.getOperation().getResource())) {
			String escaped = StringEscapeUtils.escapeJson(event.getOperation().getResource()); // escape double quote
																								// chars
			jsonString.append(JSON_RESOURCE_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(event.getOperation().getUser())) {
			String escaped = StringEscapeUtils.escapeJson(event.getOperation().getUser()); // escape double quote chars
			jsonString.append(JSON_USER_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		jsonString.append(JSON_TIME_USEC_LABEL).append(ATTR_SEP).append(Useconds.CURRENT.get()).append(ATTR_JSON);
		if (event.getOperation().getStartTime() != null) {
			jsonString.append(JSON_START_TIME_USEC_LABEL).append(ATTR_SEP)
					.append(event.getOperation().getStartTime().getTimeUsec()).append(ATTR_JSON);
		}
		if (event.getOperation().getEndTime() != null) {
			jsonString.append(JSON_END_TIME_USEC_LABEL).append(ATTR_SEP)
					.append(event.getOperation().getEndTime().getTimeUsec()).append(ATTR_JSON);
			jsonString.append(JSON_ELAPSED_TIME_USEC_LABEL).append(ATTR_SEP)
					.append(event.getOperation().getElapsedTimeUsec()).append(ATTR_JSON);
			if (event.getOperation().getWaitTimeUsec() > 0) {
				jsonString.append(JSON_WAIT_TIME_USEC_LABEL).append(ATTR_SEP)
						.append(event.getOperation().getWaitTimeUsec()).append(ATTR_JSON);
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
		jsonString.append(JSON_MSG_MIME_LABEL).append(ATTR_SEP);
		Utils.quote(event.getMimeType(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_MSG_ENC_LABEL).append(ATTR_SEP);
		Utils.quote(event.getEncoding(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_MSG_CHARSET_LABEL).append(ATTR_SEP);
		Utils.quote(event.getCharset(), jsonString);

		String msgText = event.getMessage();
		if (!Utils.isEmpty(msgText)) {
			jsonString.append(ATTR_JSON);
			msgText = StringEscapeUtils.escapeJson(msgText); // escape double quote chars
			jsonString.append(JSON_MSG_TEXT_LABEL).append(ATTR_SEP);
			Utils.quote(msgText, jsonString);
		}

		String exStr = event.getOperation().getExceptionString();
		if (!Utils.isEmpty(exStr)) {
			jsonString.append(ATTR_JSON);
			String excText = StringEscapeUtils.escapeJson(exStr); // escape double quote chars
			jsonString.append(JSON_EXCEPTION_LABEL).append(ATTR_SEP);
			Utils.quote(excText, jsonString);
		}
		if (!Utils.isEmpty(event.getCorrelator())) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_CORR_ID_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(event.getCorrelator())).append(ARRAY_END);
		}
		if (!Utils.isEmpty(event.getTag())) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_MSG_TAG_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(event.getTag())).append(ARRAY_END);
		}
		if (propCount > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_PROPERTIES_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(event.getOperation().getProperties())).append(ARRAY_END);
		}
		if (snapCount > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_SNAPSHOTS_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(event.getOperation().getSnapshots())).append(ARRAY_END);
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	/**
	 * Format a given {@link TrackingActivity} into JSON format
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

		jsonString.append(START_JSON);
		if (!Utils.isEmpty(activity.getTrackingId())) {
			jsonString.append(JSON_TRACK_ID_LABEL).append(ATTR_SEP);
			Utils.quote(activity.getTrackingId(), jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(activity.getSignature())) {
			jsonString.append(JSON_TRACK_SIGN_LABEL).append(ATTR_SEP);
			Utils.quote(activity.getSignature(), jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(activity.getParentId())) {
			jsonString.append(JSON_PARENT_TRACK_ID_LABEL).append(ATTR_SEP);
			Utils.quote(activity.getParentId(), jsonString).append(ATTR_JSON);
		}
		jsonString.append(JSON_SOURCE_LABEL).append(ATTR_SEP);
		Utils.quote(StringEscapeUtils.escapeJson(activity.getSource().getName()), jsonString).append(ATTR_JSON);
		String ssn = getSSN(activity.getSource());
		if (!Utils.isEmpty(ssn)) {
			String escaped = StringEscapeUtils.escapeJson(ssn); // escape double quote chars
			jsonString.append(JSON_SOURCE_SSN_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		jsonString.append(JSON_SOURCE_FQN_LABEL).append(ATTR_SEP);
		Utils.quote(StringEscapeUtils.escapeJson(activity.getSource().getFQName()), jsonString).append(ATTR_JSON);

		if (!Utils.isEmpty(activity.getSource().getUrl())) {
			String escaped = StringEscapeUtils.escapeJson(activity.getSource().getUrl()); // escape double quote chars
			jsonString.append(JSON_SOURCE_URL_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		jsonString.append(JSON_STATUS_LABEL).append(ATTR_SEP);
		Utils.quote(activity.getStatus(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_SEVERITY_LABEL).append(ATTR_SEP);
		Utils.quote(activity.getSeverity(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_SEVERITY_NO_LABEL).append(ATTR_SEP).append(activity.getSeverity().ordinal())
				.append(ATTR_JSON);
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP);
		Utils.quote(activity.getType(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_NO_LABEL).append(ATTR_SEP).append(activity.getType().ordinal()).append(ATTR_JSON);
		jsonString.append(JSON_PID_LABEL).append(ATTR_SEP).append(activity.getPID()).append(ATTR_JSON);
		jsonString.append(JSON_TID_LABEL).append(ATTR_SEP).append(activity.getTID()).append(ATTR_JSON);
		jsonString.append(JSON_COMP_CODE_LABEL).append(ATTR_SEP);
		Utils.quote(activity.getCompCode(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_COMP_CODE_NO_LABEL).append(ATTR_SEP).append(activity.getCompCode().ordinal())
				.append(ATTR_JSON);
		jsonString.append(JSON_REASON_CODE_LABEL).append(ATTR_SEP).append(activity.getReasonCode()).append(ATTR_JSON);
		jsonString.append(JSON_TTL_SEC_LABEL).append(ATTR_SEP).append(activity.getTTL()).append(ATTR_JSON);
		if (!Utils.isEmpty(activity.getLocation())) {
			String escaped = StringEscapeUtils.escapeJson(activity.getLocation()); // escape double quote chars
			jsonString.append(JSON_LOCATION_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(activity.getResolvedName())) {
			String escaped = StringEscapeUtils.escapeJson(activity.getResolvedName()); // escape double quote chars
			jsonString.append(JSON_OPERATION_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(activity.getResource())) {
			String escaped = StringEscapeUtils.escapeJson(activity.getResource()); // escape double quote chars
			jsonString.append(JSON_RESOURCE_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(activity.getSource().getUser())) {
			String escaped = StringEscapeUtils.escapeJson(activity.getSource().getUser()); // escape double quote chars
			jsonString.append(JSON_USER_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}

		jsonString.append(JSON_TIME_USEC_LABEL).append(ATTR_SEP).append(Useconds.CURRENT.get()).append(ATTR_JSON);
		if (activity.getStartTime() != null) {
			jsonString.append(JSON_START_TIME_USEC_LABEL).append(ATTR_SEP).append(activity.getStartTime().getTimeUsec())
					.append(ATTR_JSON);
		}
		if (activity.getEndTime() != null) {
			jsonString.append(JSON_END_TIME_USEC_LABEL).append(ATTR_SEP).append(activity.getEndTime().getTimeUsec())
					.append(ATTR_JSON);
			jsonString.append(JSON_ELAPSED_TIME_USEC_LABEL).append(ATTR_SEP).append(activity.getElapsedTimeUsec())
					.append(ATTR_JSON);
			if (activity.getWaitTimeUsec() > 0) {
				jsonString.append(JSON_WAIT_TIME_USEC_LABEL).append(ATTR_SEP).append(activity.getWaitTimeUsec())
						.append(ATTR_JSON);
			}
		}
		jsonString.append(JSON_ID_COUNT_LABEL).append(ATTR_SEP).append(activity.getIdCount()).append(ATTR_JSON);
		jsonString.append(JSON_SNAPSHOT_COUNT_LABEL).append(ATTR_SEP).append(activity.getSnapshotCount())
				.append(ATTR_JSON);
		jsonString.append(JSON_PROPERTY_COUNT_LABEL).append(ATTR_SEP).append(activity.getPropertyCount());

		String exStr = activity.getExceptionString();
		if (!Utils.isEmpty(exStr)) {
			jsonString.append(ATTR_JSON);
			String excText = StringEscapeUtils.escapeJson(exStr); // escape double quote chars
			jsonString.append(JSON_EXCEPTION_LABEL).append(ATTR_SEP);
			Utils.quote(excText, jsonString);
		}
		if (!Utils.isEmpty(activity.getCorrelator())) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_CORR_ID_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(activity.getCorrelator())).append(ARRAY_END);
		}
		if (activity.getIdCount() > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_ID_SET_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(activity.getIds())).append(ARRAY_END);
		}
		if (activity.getPropertyCount() > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_PROPERTIES_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(activity.getProperties())).append(ARRAY_END);
		}
		if (activity.getSnapshotCount() > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_SNAPSHOTS_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(activity.getSnapshots())).append(ARRAY_END);
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	/**
	 * Format a given {@link Snapshot} into JSON format
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
			jsonString.append(JSON_TRACK_ID_LABEL).append(ATTR_SEP);
			Utils.quote(snap.getTrackingId(), jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(snap.getSignature())) {
			jsonString.append(JSON_TRACK_SIGN_LABEL).append(ATTR_SEP);
			Utils.quote(snap.getSignature(), jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(snap.getParentId())) {
			jsonString.append(JSON_PARENT_TRACK_ID_LABEL).append(ATTR_SEP);
			Utils.quote(snap.getParentId(), jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(snap.getId())) {
			String escaped = StringEscapeUtils.escapeJson(snap.getId()); // escape double quote chars
			jsonString.append(JSON_FQN_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(snap.getCategory())) {
			jsonString.append(JSON_CATEGORY_LABEL).append(ATTR_SEP);
			Utils.quote(snap.getCategory(), jsonString).append(ATTR_JSON);
		}
		if (!Utils.isEmpty(snap.getName())) {
			String escaped = StringEscapeUtils.escapeJson(snap.getName()); // escape double quote chars
			jsonString.append(JSON_NAME_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		jsonString.append(JSON_COUNT_LABEL).append(ATTR_SEP).append(snap.size()).append(ATTR_JSON);
		jsonString.append(JSON_TIME_USEC_LABEL).append(ATTR_SEP).append(snap.getTimeStamp().getTimeUsec())
				.append(ATTR_JSON);
		jsonString.append(JSON_TTL_SEC_LABEL).append(ATTR_SEP).append(snap.getTTL()).append(ATTR_JSON);

		Source source = snap.getSource();
		if (source != null) {
			jsonString.append(JSON_SOURCE_LABEL).append(ATTR_SEP);
			Utils.quote(StringEscapeUtils.escapeJson(source.getName()), jsonString).append(ATTR_JSON);
			String ssn = getSSN(source);
			if (!Utils.isEmpty(ssn)) {
				String escaped = StringEscapeUtils.escapeJson(ssn); // escape double quote chars
				jsonString.append(JSON_SOURCE_SSN_LABEL).append(ATTR_SEP);
				Utils.quote(escaped, jsonString).append(ATTR_JSON);
			}
			jsonString.append(JSON_SOURCE_FQN_LABEL).append(ATTR_SEP);
			Utils.quote(StringEscapeUtils.escapeJson(source.getFQName()), jsonString).append(ATTR_JSON);
			if (!Utils.isEmpty(source.getUrl())) {
				String escaped = StringEscapeUtils.escapeJson(source.getUrl()); // escape double quote chars
				jsonString.append(JSON_SOURCE_URL_LABEL).append(ATTR_SEP);
				Utils.quote(escaped, jsonString).append(ATTR_JSON);
			}
		}
		jsonString.append(JSON_SEVERITY_LABEL).append(ATTR_SEP);
		Utils.quote(snap.getSeverity(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_SEVERITY_NO_LABEL).append(ATTR_SEP).append(snap.getSeverity().ordinal())
				.append(ATTR_JSON);
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP);
		Utils.quote(snap.getType(), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_NO_LABEL).append(ATTR_SEP).append(snap.getType().ordinal());
		if (snap.size() > 0) {
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_PROPERTIES_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(snap.getSnapshot())).append(ARRAY_END);
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	/**
	 * Format a given {@link Property} into JSON format.
	 * <p>
	 * If property is transient (@link {@link com.jkoolcloud.tnt4j.core.Property#isTransient()}, empty string
	 * {@value #EMPTY_STR} is returned.
	 * <p>
	 * Empty string {@value #EMPTY_STR} is returned when {@code specialNumbersHandling} is set to
	 * {@link SpecNumbersHandling#SUPPRESS} and property value is {@link Double} or {@link Float} containing
	 * {@code 'Infinity'} or {@code 'NaN'} value
	 *
	 * @param prop
	 *            property object to be formatted into JSON
	 * @return formatted property as a JSON string, or empty string {@value #EMPTY_STR} if property is {@code null},
	 *         transient or having special numeric value
	 * @see Property
	 */
	public String format(Property prop) {
		if (prop == null || prop.isTransient()) {
			return EMPTY_STR;
		}

		Object value = prop.getValue();

		if (isSpecialSuppress(value)) {
			return EMPTY_STR;
		}

		StringBuilder jsonString = new StringBuilder(1024);
		jsonString.append(START_JSON);
		jsonString.append(JSON_NAME_LABEL).append(ATTR_SEP);
		Utils.quote(StringEscapeUtils.escapeJson(prop.getKey()), jsonString).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP);
		Utils.quote(prop.getDataType(), jsonString).append(ATTR_JSON);
		if (prop.getValueType() != null && !prop.getValueType().equalsIgnoreCase(ValueTypes.VALUE_TYPE_NONE)) {
			jsonString.append(JSON_VALUE_TYPE_LABEL).append(ATTR_SEP);
			Utils.quote(prop.getValueType(), jsonString).append(ATTR_JSON);
		}

		jsonString.append(JSON_VALUE_LABEL).append(ATTR_SEP);

		boolean valueAdded = false;
		if (value == null) {
			jsonString.append(value);
			valueAdded = true;
		} else if (value instanceof Number) {
			if (!isSpecialEnquote(value)) {
				jsonString.append(value);
				valueAdded = true;
			}
		} else if (value instanceof Boolean) {
			jsonString.append(value);
			valueAdded = true;
		}

		if (!valueAdded) {
			Utils.quote(StringEscapeUtils.escapeJson(Utils.toString(value)), jsonString);
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	/**
	 * Checks whether provided {@code value} is special numeric value and if formatter is configured to suppress these
	 * values.
	 *
	 * @param value
	 *            value to check
	 * @return {@code true} if value is special and should be suppressed, {@code false} - otherwise
	 */
	protected boolean isSpecialSuppress(Object value) {
		return specialNumbersHandling == SpecNumbersHandling.SUPPRESS && isSpecial(value);
	}

	/**
	 * Checks whether provided {@code value} is special numeric value and if formatter is configured to enquote these
	 * values.
	 *
	 * @param value
	 *            value to check
	 * @return {@code true} if value is special and should be enquoted, {@code false} - otherwise
	 */
	protected boolean isSpecialEnquote(Object value) {
		return specialNumbersHandling == SpecNumbersHandling.ENQUOTE && isSpecial(value);
	}

	private static boolean isSpecial(Object value) {
		if (value instanceof Number) {
			return Utils.isSpecialNumberValue((Number) value);
		}

		return false;
	}

	@Override
	public String format(long ttl, Source source, OpLevel level, String msg, Object... args) {
		StringBuilder jsonString = new StringBuilder(1024);
		jsonString.append(START_JSON);
		jsonString.append(JSON_SEVERITY_LABEL).append(ATTR_SEP);
		Utils.quote(level, jsonString).append(ATTR_JSON);
		jsonString.append(JSON_SEVERITY_NO_LABEL).append(ATTR_SEP).append(level.ordinal()).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP);
		Utils.quote(OpType.DATAGRAM, jsonString).append(ATTR_JSON);
		jsonString.append(JSON_TYPE_NO_LABEL).append(ATTR_SEP).append(OpType.DATAGRAM.ordinal()).append(ATTR_JSON);

		jsonString.append(JSON_PID_LABEL).append(ATTR_SEP).append(Utils.getVMPID()).append(ATTR_JSON);
		jsonString.append(JSON_TID_LABEL).append(ATTR_SEP).append(Thread.currentThread().getId()).append(ATTR_JSON);

		String usrName = StringEscapeUtils.escapeJson(
				source == null ? DefaultSourceFactory.getInstance().getRootSource().getUser() : source.getUser());
		jsonString.append(JSON_USER_LABEL).append(ATTR_SEP);
		Utils.quote(usrName, jsonString).append(ATTR_JSON);
		jsonString.append(JSON_TTL_SEC_LABEL).append(ATTR_SEP).append(ttl).append(ATTR_JSON);
		jsonString.append(JSON_TIME_USEC_LABEL).append(ATTR_SEP).append(Useconds.CURRENT.get()).append(ATTR_JSON);
		jsonString.append(JSON_OPERATION_LABEL).append(ATTR_SEP);
		Utils.quote(defOpName, jsonString).append(ATTR_JSON);

		if (source != null) {
			jsonString.append(JSON_SOURCE_LABEL).append(ATTR_SEP);
			Utils.quote(StringEscapeUtils.escapeJson(source.getName()), jsonString).append(ATTR_JSON);
			String ssn = getSSN(source);
			if (!Utils.isEmpty(ssn)) {
				String escaped = StringEscapeUtils.escapeJson(ssn); // escape double quote chars
				jsonString.append(JSON_SOURCE_SSN_LABEL).append(ATTR_SEP);
				Utils.quote(escaped, jsonString).append(ATTR_JSON);
			}
			jsonString.append(JSON_SOURCE_FQN_LABEL).append(ATTR_SEP);
			Utils.quote(StringEscapeUtils.escapeJson(source.getFQName()), jsonString);
			if (!Utils.isEmpty(source.getUrl())) {
				jsonString.append(ATTR_JSON);
				String escaped = StringEscapeUtils.escapeJson(source.getUrl()); // escape double quote chars
				jsonString.append(JSON_SOURCE_URL_LABEL).append(ATTR_SEP);
				Utils.quote(escaped, jsonString);
			}
			Source geoloc = source.getSource(SourceType.GEOADDR);
			if (geoloc != null) {
				jsonString.append(ATTR_JSON);
				jsonString.append(JSON_LOCATION_LABEL).append(ATTR_SEP);
				Utils.quote(geoloc.getName(), jsonString);
			}
		}
		if (!Utils.isEmpty(msg)) {
			String msgText = Utils.format(msg, args);
			msgText = StringEscapeUtils.escapeJson(msgText); // escape double quote chars
			jsonString.append(ATTR_JSON);
			jsonString.append(JSON_MSG_TEXT_LABEL).append(ATTR_SEP);
			Utils.quote(msgText, jsonString);
		}
		Throwable ex = Utils.getThrowable(args);
		if (ex != null) {
			jsonString.append(ATTR_JSON);
			String excText = StringEscapeUtils.escapeJson(ex.toString()); // escape double quote chars
			jsonString.append(JSON_EXCEPTION_LABEL).append(ATTR_SEP);
			Utils.quote(excText, jsonString);
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	/**
	 * Builds string reforestation of provided activity entity {@code items} collection.
	 *
	 * @param items
	 *            collection of activity entity items
	 * @return string representation of activity enmity items collection
	 */
	protected String itemsToJSON(Collection<?> items) {
		if (items == null) {
			return EMPTY_STR;
		}
		StringBuilder jsonString = new StringBuilder(2048);
		for (Object item : items) {
			String itemJSON;
			if (item instanceof TrackingEvent) {
				itemJSON = format((TrackingEvent) item);
			} else if (item instanceof TrackingActivity) {
				itemJSON = format((TrackingActivity) item);
			} else if (item instanceof Snapshot) {
				itemJSON = format((Snapshot) item);
			} else if (item instanceof Property) {
				itemJSON = format((Property) item);
			} else {
				itemJSON = Utils.quote(StringEscapeUtils.escapeJson(Utils.toString(item))); // escape double quote chars
			}

			if (StringUtils.isNotEmpty(itemJSON)) {
				addDelimiterOnDemand(jsonString, ATTR_JSON);
				jsonString.append(itemJSON);
			}
		}
		return jsonString.toString();
	}

	private static void addDelimiterOnDemand(StringBuilder json, String delimiter) {
		if (StringUtils.isEmpty(json) || StringUtils.isEmpty(delimiter)) {
			return;
		}

		if (!StringUtils.endsWith(json, delimiter)) {
			json.append(delimiter);
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> settings) {
		config = settings;
		newLineFormat = Utils.getBoolean("Newline", settings, newLineFormat);
		defOpName = Utils.getString("OpName", settings, defOpName);
		String specNumbers = Utils.getString("SpecNumbersHandling", settings, SpecNumbersHandling.SUPPRESS.name());
		try {
			specialNumbersHandling = SpecNumbersHandling.valueOf(specNumbers.toUpperCase());
		} catch (IllegalArgumentException exc) {
			specialNumbersHandling = SpecNumbersHandling.SUPPRESS;
		}
		initTags();
	}

	/**
	 * Builds string representation of provided {@code source}.
	 *
	 * @param source
	 *            source instance
	 * @return string representation of source
	 */
	protected static String getSSN(Source source) {
		String ssn = source.getSSN();
		return Utils.isEmpty(ssn) ? source.getSourceFactory().getSSN() : ssn;
	}

	/**
	 * Enumeration of special numbers values handling techniques used by this formatter.
	 */
	public enum SpecNumbersHandling {
		/**
		 * Suppress properties having special numeric value.
		 */
		SUPPRESS,
		/**
		 * Enquote special numeric value.
		 */
		ENQUOTE,
		/**
		 * Maintain value as is.
		 */
		MAINTAIN,
	}

}
