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
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

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

	private Map<String, ?> config = null;
	protected boolean newLineFormat = true;
	protected String defOpName = DEF_OP_NAME;
	protected SpecNumbersHandling specialNumbersHandling = SpecNumbersHandling.SUPPRESS;

	protected String START_JSON = START_LINE;
	protected String END_JSON = END_LINE;
	protected String ATTR_JSON = ATTR_END_LINE;
	protected String ARRAY_START_JSON = ARRAY_START_LINE;

	/**
	 * Create JSON formatter without newlines during formatting
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

	/**
	 * Adds JSON string entry to provided JSON string builder.
	 * 
	 * @param jsonString
	 *            builder building JSON string
	 * @param label
	 *            entry label
	 * @param value
	 *            entry value
	 * 
	 * @see #addJsonEntry(StringBuilder, String, String, boolean)
	 */
	protected void addJsonEntry(StringBuilder jsonString, String label, String value) {
		addJsonEntry(jsonString, label, value, false);
	}

	/**
	 * Adds JSON string entry to provided JSON string builder.
	 * 
	 * @param jsonString
	 *            builder building JSON string
	 * @param label
	 *            entry label
	 * @param value
	 *            entry value
	 * @param escape
	 *            flag indicating to escape value to be compliant with JSON standard
	 */
	protected void addJsonEntry(StringBuilder jsonString, String label, String value, boolean escape) {
		if (Utils.isEmpty(value)) {
			return;
		}

		Utils.quote(escape ? StringEscapeUtils.escapeJson(value) : value, addJsonEntryLabel(jsonString, label));
	}

	/**
	 * Adds JSON numeric entry to provided JSON string builder.
	 * 
	 * @param jsonString
	 *            builder building JSON string
	 * @param label
	 *            entry label
	 * @param value
	 *            entry value
	 */
	protected void addJsonEntry(StringBuilder jsonString, String label, long value) {
		addJsonEntryLabel(jsonString, label).append(value);
	}

	/**
	 * Adds JSON enum entry to provided JSON string builder.
	 * 
	 * @param jsonString
	 *            builder building JSON string
	 * @param label
	 *            entry label
	 * @param value
	 *            entry value
	 */
	protected void addJsonEntry(StringBuilder jsonString, String label, Enum<?> value) {
		Utils.quote(value, addJsonEntryLabel(jsonString, label));
	}

	/**
	 * Adds JSON collection entry to provided JSON string builder.
	 * 
	 * @param jsonString
	 *            builder building JSON string
	 * @param label
	 *            entry label
	 * @param value
	 *            entry value
	 */
	protected void addJsonEntry(StringBuilder jsonString, String label, Collection<?> value) {
		if (Utils.isEmpty(value)) {
			return;
		}

		addJsonEntryLabel(jsonString, label).append(ARRAY_START_JSON).append(itemsToJSON(value)).append(ARRAY_END);
	}

	/**
	 * Adds JSON object entry to provided JSON string builder.
	 * 
	 * @param jsonString
	 *            builder building JSON string
	 * @param label
	 *            entry label
	 * @param value
	 *            entry value
	 */
	protected void addJsonEntry(StringBuilder jsonString, String label, Object value) {
		String pValue = propValueToString(value);
		if (isNoNeedToQuote(value)) {
			addJsonEntryLabel(jsonString, label).append(pValue);
		} else {
			Utils.quote(StringEscapeUtils.escapeJson(pValue), addJsonEntryLabel(jsonString, label));
		}
	}

	/**
	 * Adds JSON delimiter token to provided JSON string builder.
	 * 
	 * @param jsonString
	 *            builder building JSON string
	 * @return JSON string builder instance
	 */
	protected StringBuilder addJsonDelimToken(StringBuilder jsonString) {
		if (jsonString.length() == 0) {
			jsonString.append(START_JSON);
		} else {
			jsonString.append(ATTR_JSON);
		}

		return jsonString;
	}

	/**
	 * Adds JSON entry label to provided JSON string builder.
	 * 
	 * @param jsonString
	 *            builder building JSON string
	 * @param label
	 *            entry label
	 * @return JSON string builder instance
	 */
	protected StringBuilder addJsonEntryLabel(StringBuilder jsonString, String label) {
		addJsonDelimToken(jsonString).append(label).append(ATTR_SEP);

		return jsonString;
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
			addJsonEntry(jsonString, JSON_TIME_USEC_LABEL, Useconds.CURRENT.get());
			addJsonEntry(jsonString, JSON_MSG_TEXT_LABEL, Utils.format(Utils.toString(obj), args), true);

			return jsonString.append(END_JSON).toString();
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

		addJsonEntry(jsonString, JSON_GUID_LABEL, event.getGUID());
		addJsonEntry(jsonString, JSON_TRACK_ID_LABEL, event.getTrackingId());
		addJsonEntry(jsonString, JSON_TRACK_SIGN_LABEL, event.getSignature());
		addJsonEntry(jsonString, JSON_PARENT_TRACK_ID_LABEL, event.getParentId());
		addJsonEntry(jsonString, JSON_SOURCE_LABEL, event.getSource().getName(), true);
		addJsonEntry(jsonString, JSON_SOURCE_SSN_LABEL, getSSN(event.getSource()), true);
		addJsonEntry(jsonString, JSON_SOURCE_FQN_LABEL, event.getSource().getFQName(), true);
		addJsonEntry(jsonString, JSON_SOURCE_URL_LABEL, event.getSource().getUrl(), true);

		if (event.get2(TrackingEvent.OBJ_ONE) != null) {
			// we have a relation
			addJsonEntry(jsonString, JSON_RELATE_TYPE_LABEL, event.get2Type());
			addJsonEntry(jsonString, JSON_RELATE_FQN_A_LABEL, event.get2(TrackingEvent.OBJ_ONE).getFQName());
			addJsonEntry(jsonString, JSON_RELATE_FQN_B_LABEL, event.get2(TrackingEvent.OBJ_TWO).getFQName());
		}

		addJsonEntry(jsonString, JSON_SEVERITY_LABEL, event.getSeverity());
		addJsonEntry(jsonString, JSON_SEVERITY_NO_LABEL, event.getSeverity().ordinal());
		addJsonEntry(jsonString, JSON_TYPE_LABEL, event.getOperation().getType());
		addJsonEntry(jsonString, JSON_TYPE_NO_LABEL, event.getOperation().getType().ordinal());
		addJsonEntry(jsonString, JSON_PID_LABEL, event.getOperation().getPID());
		addJsonEntry(jsonString, JSON_TID_LABEL, event.getOperation().getTID());
		addJsonEntry(jsonString, JSON_COMP_CODE_LABEL, event.getOperation().getCompCode());
		addJsonEntry(jsonString, JSON_COMP_CODE_NO_LABEL, event.getOperation().getCompCode().ordinal());
		addJsonEntry(jsonString, JSON_REASON_CODE_LABEL, event.getOperation().getReasonCode());
		addJsonEntry(jsonString, JSON_TTL_SEC_LABEL, event.getTTL());
		addJsonEntry(jsonString, JSON_LOCATION_LABEL, event.getLocation(), true);
		addJsonEntry(jsonString, JSON_OPERATION_LABEL, event.getOperation().getResolvedName(), true);
		addJsonEntry(jsonString, JSON_RESOURCE_LABEL, event.getOperation().getResource(), true);
		addJsonEntry(jsonString, JSON_USER_LABEL, event.getOperation().getUser(), true);
		addJsonEntry(jsonString, JSON_TIME_USEC_LABEL, Useconds.CURRENT.get());
		if (event.getOperation().getStartTime() != null) {
			addJsonEntry(jsonString, JSON_START_TIME_USEC_LABEL, event.getOperation().getStartTime().getTimeUsec());
		}
		if (event.getOperation().getEndTime() != null) {
			addJsonEntry(jsonString, JSON_END_TIME_USEC_LABEL, event.getOperation().getEndTime().getTimeUsec());
			addJsonEntry(jsonString, JSON_ELAPSED_TIME_USEC_LABEL, event.getOperation().getElapsedTimeUsec());
			if (event.getOperation().getWaitTimeUsec() > 0) {
				addJsonEntry(jsonString, JSON_WAIT_TIME_USEC_LABEL, event.getOperation().getWaitTimeUsec());
			}
			if (event.getMessageAge() > 0) {
				addJsonEntry(jsonString, JSON_MSG_AGE_USEC_LABEL, event.getMessageAge());
			}
		}
		addJsonEntry(jsonString, JSON_SNAPSHOT_COUNT_LABEL, event.getOperation().getSnapshotCount());
		addJsonEntry(jsonString, JSON_PROPERTY_COUNT_LABEL, event.getOperation().getPropertyCount());
		addJsonEntry(jsonString, JSON_MSG_SIZE_LABEL, event.getSize());
		addJsonEntry(jsonString, JSON_MSG_MIME_LABEL, event.getMimeType());
		addJsonEntry(jsonString, JSON_MSG_ENC_LABEL, event.getEncoding());
		addJsonEntry(jsonString, JSON_MSG_CHARSET_LABEL, event.getCharset());
		addJsonEntry(jsonString, JSON_MSG_TEXT_LABEL, event.getMessage(), true);
		addJsonEntry(jsonString, JSON_EXCEPTION_LABEL, event.getOperation().getExceptionString(), true);
		addJsonEntry(jsonString, JSON_CORR_ID_LABEL, event.getCorrelator());
		addJsonEntry(jsonString, JSON_MSG_TAG_LABEL, event.getTag());
		addJsonEntry(jsonString, JSON_PROPERTIES_LABEL, event.getOperation().getProperties());
		addJsonEntry(jsonString, JSON_SNAPSHOTS_LABEL, event.getOperation().getSnapshots());

		return jsonString.append(END_JSON).toString();
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

		addJsonEntry(jsonString, JSON_GUID_LABEL, activity.getGUID());
		addJsonEntry(jsonString, JSON_TRACK_ID_LABEL, activity.getTrackingId());
		addJsonEntry(jsonString, JSON_TRACK_SIGN_LABEL, activity.getSignature());
		addJsonEntry(jsonString, JSON_PARENT_TRACK_ID_LABEL, activity.getParentId());
		addJsonEntry(jsonString, JSON_SOURCE_LABEL, activity.getSource().getName(), true);
		addJsonEntry(jsonString, JSON_SOURCE_SSN_LABEL, getSSN(activity.getSource()), true);
		addJsonEntry(jsonString, JSON_SOURCE_FQN_LABEL, activity.getSource().getFQName(), true);
		addJsonEntry(jsonString, JSON_SOURCE_URL_LABEL, activity.getSource().getUrl(), true);
		addJsonEntry(jsonString, JSON_STATUS_LABEL, activity.getStatus());
		addJsonEntry(jsonString, JSON_SEVERITY_LABEL, activity.getSeverity());
		addJsonEntry(jsonString, JSON_SEVERITY_NO_LABEL, activity.getSeverity().ordinal());
		addJsonEntry(jsonString, JSON_TYPE_LABEL, activity.getType());
		addJsonEntry(jsonString, JSON_TYPE_NO_LABEL, activity.getType().ordinal());
		addJsonEntry(jsonString, JSON_PID_LABEL, activity.getPID());
		addJsonEntry(jsonString, JSON_TID_LABEL, activity.getTID());
		addJsonEntry(jsonString, JSON_COMP_CODE_LABEL, activity.getCompCode());
		addJsonEntry(jsonString, JSON_COMP_CODE_NO_LABEL, activity.getCompCode().ordinal());
		addJsonEntry(jsonString, JSON_REASON_CODE_LABEL, activity.getReasonCode());
		addJsonEntry(jsonString, JSON_TTL_SEC_LABEL, activity.getTTL());
		addJsonEntry(jsonString, JSON_LOCATION_LABEL, activity.getLocation(), true);
		addJsonEntry(jsonString, JSON_OPERATION_LABEL, activity.getResolvedName(), true);
		addJsonEntry(jsonString, JSON_RESOURCE_LABEL, activity.getResource(), true);
		addJsonEntry(jsonString, JSON_USER_LABEL, activity.getSource().getUser(), true);
		addJsonEntry(jsonString, JSON_TIME_USEC_LABEL, Useconds.CURRENT.get());
		if (activity.getStartTime() != null) {
			addJsonEntry(jsonString, JSON_START_TIME_USEC_LABEL, activity.getStartTime().getTimeUsec());
		}
		if (activity.getEndTime() != null) {
			addJsonEntry(jsonString, JSON_END_TIME_USEC_LABEL, activity.getEndTime().getTimeUsec());
			addJsonEntry(jsonString, JSON_ELAPSED_TIME_USEC_LABEL, activity.getElapsedTimeUsec());
			if (activity.getWaitTimeUsec() > 0) {
				addJsonEntry(jsonString, JSON_WAIT_TIME_USEC_LABEL, activity.getWaitTimeUsec());
			}
		}
		addJsonEntry(jsonString, JSON_ID_COUNT_LABEL, activity.getIdCount());
		addJsonEntry(jsonString, JSON_SNAPSHOT_COUNT_LABEL, activity.getSnapshotCount());
		addJsonEntry(jsonString, JSON_PROPERTY_COUNT_LABEL, activity.getPropertyCount());

		addJsonEntry(jsonString, JSON_EXCEPTION_LABEL, activity.getExceptionString(), true);
		addJsonEntry(jsonString, JSON_CORR_ID_LABEL, activity.getCorrelator());
		addJsonEntry(jsonString, JSON_ID_SET_LABEL, activity.getIds());
		addJsonEntry(jsonString, JSON_PROPERTIES_LABEL, activity.getProperties());
		addJsonEntry(jsonString, JSON_SNAPSHOTS_LABEL, activity.getSnapshots());

		return jsonString.append(END_JSON).toString();
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

		addJsonEntry(jsonString, JSON_GUID_LABEL, snap.getGUID());
		addJsonEntry(jsonString, JSON_TRACK_ID_LABEL, snap.getTrackingId());
		addJsonEntry(jsonString, JSON_TRACK_SIGN_LABEL, snap.getSignature());
		addJsonEntry(jsonString, JSON_PARENT_TRACK_ID_LABEL, snap.getParentId());
		addJsonEntry(jsonString, JSON_FQN_LABEL, snap.getId(), true);
		addJsonEntry(jsonString, JSON_CATEGORY_LABEL, snap.getCategory());
		addJsonEntry(jsonString, JSON_NAME_LABEL, snap.getName(), true);
		addJsonEntry(jsonString, JSON_COUNT_LABEL, snap.size());
		addJsonEntry(jsonString, JSON_TIME_USEC_LABEL, snap.getTimeStamp().getTimeUsec());
		addJsonEntry(jsonString, JSON_TTL_SEC_LABEL, snap.getTTL());

		Source source = snap.getSource();
		if (source != null) {
			addJsonEntry(jsonString, JSON_SOURCE_LABEL, source.getName(), true);
			addJsonEntry(jsonString, JSON_SOURCE_SSN_LABEL, getSSN(source), true);
			addJsonEntry(jsonString, JSON_SOURCE_FQN_LABEL, source.getFQName(), true);
			addJsonEntry(jsonString, JSON_SOURCE_URL_LABEL, source.getUrl(), true);
		}
		if (snap.getSeverity().ordinal() > OpLevel.NONE.ordinal()) {
			addJsonEntry(jsonString, JSON_SEVERITY_LABEL, snap.getSeverity());
			addJsonEntry(jsonString, JSON_SEVERITY_NO_LABEL, snap.getSeverity().ordinal());
		}
		addJsonEntry(jsonString, JSON_TYPE_LABEL, snap.getType());
		addJsonEntry(jsonString, JSON_TYPE_NO_LABEL, snap.getType().ordinal());
		addJsonEntry(jsonString, JSON_PROPERTIES_LABEL, snap.getProperties());

		return jsonString.append(END_JSON).toString();
	}

	/**
	 * Format a given {@link Property} into JSON format.
	 * <p>
	 * If property is transient {@link com.jkoolcloud.tnt4j.core.Property#isTransient()}, empty string
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

		StringBuilder jsonString = new StringBuilder(256);
		addJsonEntry(jsonString, JSON_NAME_LABEL, prop.getKey(), true);
		addJsonEntry(jsonString, JSON_TYPE_LABEL, prop.getDataType());
		if (prop.getValueType() != null && !prop.getValueType().equalsIgnoreCase(ValueTypes.VALUE_TYPE_NONE)) {
			addJsonEntry(jsonString, JSON_VALUE_TYPE_LABEL, prop.getValueType());
		}
		addJsonEntry(jsonString, JSON_VALUE_LABEL, value);

		return jsonString.append(END_JSON).toString();
	}

	/**
	 * Converts property value to string representation specific for JKool.
	 * <ul>
	 * <li>{@link Date} value is serialized as number using {@link java.util.Date#getTime()}</li>
	 * <li>{@link com.jkoolcloud.tnt4j.core.UsecTimestamp} value is serialized as number using
	 * {@link com.jkoolcloud.tnt4j.core.UsecTimestamp#getTimeUsec()}</li>
	 * <li>all other cases are serialized using {@link Utils#toString(Object)}</li>
	 * </ul>
	 *
	 * @param propValue
	 *            property value to convert
	 * @return string representation of property value
	 *
	 * @see java.util.Date#getTime()
	 * @see com.jkoolcloud.tnt4j.core.UsecTimestamp#getTimeUsec()
	 * @see com.jkoolcloud.tnt4j.utils.Utils#toString(Object)
	 */
	protected static String propValueToString(Object propValue) {
		if (propValue instanceof Date) {
			return Long.toString(((Date) propValue).getTime());
		} else if (propValue instanceof UsecTimestamp) {
			return Long.toString(((UsecTimestamp) propValue).getTimeUsec());
		} else {
			return Utils.toString(propValue);
		}
	}

	/**
	 * Checks whether provided {@code value} can be un-quoted in produced JSON.
	 *
	 * @param value
	 *            value to check
	 * @return {@code true} if value is one of: {@code null}, boolean, number, date, timestamp, {@code false} -
	 *         otherwise
	 *
	 * @see #isSpecialSuppress(Object)
	 */
	protected boolean isNoNeedToQuote(Object value) {
		return value == null || value instanceof Boolean || value instanceof Date || value instanceof UsecTimestamp
				|| (value instanceof Number && !isSpecialEnquote(value));
	}

	/**
	 * Checks whether provided {@code value} is special numeric value and if formatter is configured to suppress these
	 * values.
	 *
	 * @param value
	 *            value to check
	 * @return {@code true} if value is special and should be suppressed, {@code false} - otherwise
	 *
	 * @see #isSpecial(Object)
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

		addJsonEntry(jsonString, JSON_SEVERITY_LABEL, level);
		addJsonEntry(jsonString, JSON_SEVERITY_NO_LABEL, level.ordinal());
		addJsonEntry(jsonString, JSON_TYPE_LABEL, OpType.LOG);
		addJsonEntry(jsonString, JSON_TYPE_NO_LABEL, OpType.LOG.ordinal());

		addJsonEntry(jsonString, JSON_PID_LABEL, Utils.getVMPID());
		addJsonEntry(jsonString, JSON_TID_LABEL, Thread.currentThread().getId());

		String usrName = source == null ? DefaultSourceFactory.getInstance().getRootSource().getUser()
				: source.getUser();
		addJsonEntry(jsonString, JSON_USER_LABEL, usrName, true);
		addJsonEntry(jsonString, JSON_TTL_SEC_LABEL, ttl);
		addJsonEntry(jsonString, JSON_TIME_USEC_LABEL, Useconds.CURRENT.get());
		addJsonEntry(jsonString, JSON_OPERATION_LABEL, defOpName);

		if (source != null) {
			addJsonEntry(jsonString, JSON_SOURCE_LABEL, source.getName(), true);
			addJsonEntry(jsonString, JSON_SOURCE_SSN_LABEL, getSSN(source), true);
			addJsonEntry(jsonString, JSON_SOURCE_FQN_LABEL, source.getFQName(), true);
			addJsonEntry(jsonString, JSON_SOURCE_URL_LABEL, source.getUrl(), true);
			Source geoloc = source.getSource(SourceType.GEOADDR);
			if (geoloc != null) {
				addJsonEntry(jsonString, JSON_LOCATION_LABEL, geoloc.getName());
			}
		}
		if (!Utils.isEmpty(msg)) {
			addJsonEntry(jsonString, JSON_MSG_TEXT_LABEL, Utils.format(msg, args), true);
		}
		Throwable ex = Utils.getThrowable(args);
		if (ex != null) {
			addJsonEntry(jsonString, JSON_EXCEPTION_LABEL, ex.toString(), true);
		}

		return jsonString.append(END_JSON).toString();
	}

	/**
	 * Builds string reforestation of provided activity entity {@code items} collection.
	 *
	 * @param items
	 *            collection of activity entity items
	 * @return string representation of activity enmity items collection
	 */
	protected String itemsToJSON(Collection<?> items) {
		if (Utils.isEmpty(items)) {
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
	public Map<String, ?> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, ?> settings) {
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
