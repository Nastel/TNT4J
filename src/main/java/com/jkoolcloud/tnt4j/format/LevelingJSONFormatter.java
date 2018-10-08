/*
 * Copyright 2014-2018 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.format;

import java.util.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.core.Operation;
import com.jkoolcloud.tnt4j.core.Property;
import com.jkoolcloud.tnt4j.core.PropertySnapshot;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * This class provides JSON formatting for tnt4j activities, events and snapshots.
 * <p>
 * Difference from {@link com.jkoolcloud.tnt4j.format.JSONFormatter} is that TNT4J metadata is added to produced JSON
 * depending on configured level: {@code 0} - only payload is serialized, {@code 9} - produces all TNT4J metadata (same
 * as {@link com.jkoolcloud.tnt4j.format.JSONFormatter}).
 * <p>
 * This formatter supports the following configuration properties (in addition to those supported by
 * {@link com.jkoolcloud.tnt4j.format.JSONFormatter}):
 * <ul>
 * <li>Level - level of TNT4J metadata in produced JSON: {@code 0} - only payload is serialized, {@code 9} - produces
 * all TNT4J metadata (same as {@link com.jkoolcloud.tnt4j.format.JSONFormatter}). Default value - {@code 0}.</li>
 * <li>KeyReplacements - property key string replacement fragments having format: "original1"-&gt;"replacement1"
 * "original2"-&gt;"replacement2" ... "originalX"-&gt;"replacementX". Default value - {@code ""}.</li>
 * <li>ValueReplacements - property value string replacement fragments having format: "original1"-&gt;"replacement1"
 * "original2"-&gt;"replacement2" ... "originalX"-&gt;"replacementX". Default value - {@code ""}.</li>
 * </ul>
 *
 * @version $Revision: 1 $
 *
 * @see TrackingActivity
 * @see TrackingEvent
 * @see Snapshot
 * @see com.jkoolcloud.tnt4j.core.Property
 */
public class LevelingJSONFormatter extends JSONFormatter {

	private int level = 0;

	private Comparator<Snapshot> snapshotComparator;
	private Comparator<Property> propertyComparator;

	protected Map<String, String> keyReplacements = new HashMap<String, String>();
	protected Map<String, String> valueReplacements = new HashMap<String, String>();

	/**
	 * Create leveling JSON formatter without newlines during formatting
	 */
	public LevelingJSONFormatter() {
		super();
	}

	/**
	 * Create leveling JSON formatter and conditionally format with newline
	 *
	 * @param newLine
	 *            apply newline formatting to JSON
	 */
	public LevelingJSONFormatter(boolean newLine) {
		super(newLine);
	}

	@Override
	public String format(TrackingEvent event) {
		if (level == 9) {
			return super.format(event);
		}

		StringBuilder jsonString = new StringBuilder(1024);

		jsonString.append(START_JSON);
		jsonString.append(JSON_SOURCE_LABEL).append(ATTR_SEP);
		Utils.quote(StringEscapeUtils.escapeJson(event.getSource().getName()), jsonString).append(ATTR_JSON);
		String ssn = getSSN(event.getSource());
		if (!Utils.isEmpty(ssn)) {
			String escaped = StringEscapeUtils.escapeJson(ssn); // escape double quote chars
			jsonString.append(JSON_SOURCE_SSN_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP);
		Utils.quote(event.getOperation().getType(), jsonString).append(ATTR_JSON);

		if (level > 0) {
			jsonString.append(JSON_SOURCE_FQN_LABEL).append(ATTR_SEP);
			Utils.quote(StringEscapeUtils.escapeJson(event.getSource().getFQName()), jsonString).append(ATTR_JSON);
			if (!Utils.isEmpty(event.getOperation().getResolvedName())) {
				String escaped = StringEscapeUtils.escapeJson(event.getOperation().getResolvedName()); // escape double
																										// quote chars
				jsonString.append(JSON_OPERATION_LABEL).append(ATTR_SEP);
				Utils.quote(escaped, jsonString).append(ATTR_JSON);
			}
		}

		Snapshot selfSnapshot = getSelfSnapshot(event.getOperation());
		if (event.getTag() != null) {
			Set<String> tags = event.getTag();
			if (!tags.isEmpty()) {
				selfSnapshot.add(JSON_MSG_TAG_FIELD, tags);
			}
		}

		event.getOperation().addSnapshot(selfSnapshot);

		int snapCount = event.getOperation().getSnapshotCount();
		int propCount = event.getOperation().getPropertyCount();

		if (propCount > 0) {
			jsonString.append(JSON_PROPERTIES_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(getProperties(event.getOperation()))).append(ARRAY_END);
		}
		if (snapCount > 0) {
			jsonString.append(JSON_SNAPSHOTS_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(getSnapshots(event.getOperation()))).append(ARRAY_END);
		}

		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	@Override
	public String format(TrackingActivity activity) {
		if (level == 9) {
			return super.format(activity);
		}

		StringBuilder jsonString = new StringBuilder(1024);

		jsonString.append(START_JSON);
		jsonString.append(JSON_SOURCE_LABEL).append(ATTR_SEP);
		Utils.quote(StringEscapeUtils.escapeJson(activity.getSource().getName()), jsonString).append(ATTR_JSON);
		String ssn = getSSN(activity.getSource());
		if (!Utils.isEmpty(ssn)) {
			String escaped = StringEscapeUtils.escapeJson(ssn); // escape double quote chars
			jsonString.append(JSON_SOURCE_SSN_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		if (level > 0) {
			jsonString.append(JSON_SOURCE_FQN_LABEL).append(ATTR_SEP);
			Utils.quote(StringEscapeUtils.escapeJson(activity.getSource().getFQName()), jsonString).append(ATTR_JSON);
			if (!Utils.isEmpty(activity.getResolvedName())) {
				String escaped = StringEscapeUtils.escapeJson(activity.getResolvedName()); // escape double quote chars
				jsonString.append(JSON_OPERATION_LABEL).append(ATTR_SEP);
				Utils.quote(escaped, jsonString).append(ATTR_JSON);
			}
		}
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP);
		Utils.quote(activity.getType(), jsonString).append(ATTR_JSON);

		Snapshot selfSnapshot = getSelfSnapshot(activity);
		selfSnapshot.add(JSON_ID_COUNT_FIELD, activity.getIdCount());

		activity.addSnapshot(selfSnapshot);

		if (activity.getPropertyCount() > 0) {
			jsonString.append(JSON_PROPERTIES_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(getProperties(activity))).append(ARRAY_END);
		}
		if (activity.getSnapshotCount() > 0) {
			jsonString.append(JSON_SNAPSHOTS_LABEL).append(ATTR_SEP).append(ARRAY_START_JSON)
					.append(itemsToJSON(getSnapshots(activity))).append(ARRAY_END);
		}

		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	private Snapshot getSelfSnapshot(Operation op) {
		Snapshot selfSnapshot = new PropertySnapshot("Self");

		if (op.getCorrelator() != null) {
			Set<String> cids = op.getCorrelator();
			if (!cids.isEmpty()) {
				selfSnapshot.add(JSON_CORR_ID_FIELD, cids);
			}
		}
		if (op.getUser() != null) {
			selfSnapshot.add(JSON_USER_FIELD, op.getUser());
		}
		if (op.getLocation() != null) {
			selfSnapshot.add(JSON_LOCATION_FIELD, op.getLocation());
		}
		selfSnapshot.add(JSON_SEVERITY_FIELD, op.getSeverity());
		selfSnapshot.add(JSON_PID_FIELD, op.getPID());
		selfSnapshot.add(JSON_TID_FIELD, op.getTID());
		selfSnapshot.add(JSON_SNAPSHOT_COUNT_FIELD, op.getSnapshotCount());
		selfSnapshot.add(JSON_ELAPSED_TIME_USEC_FIELD, op.getElapsedTimeUsec());

		return selfSnapshot;
	}

	@Override
	public String format(Snapshot snapshot) {
		if (level == 9) {
			return super.format(snapshot);
		}

		StringBuilder jsonString = new StringBuilder(1024);
		jsonString.append(START_JSON);
		Source source = snapshot.getSource();
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
		jsonString.append(JSON_TYPE_LABEL).append(ATTR_SEP);
		Utils.quote(snapshot.getType(), jsonString).append(ATTR_JSON);
		String snapName = getSnapName(snapshot);
		if (!Utils.isEmpty(snapName)) {
			String escaped = StringEscapeUtils.escapeJson(snapName); // escape double quote chars
			jsonString.append(JSON_NAME_LABEL).append(ATTR_SEP);
			Utils.quote(escaped, jsonString).append(ATTR_JSON);
		}
		if (snapshot.size() > 0) {
			jsonString.append(JSON_PROPERTIES_LABEL).append(ATTR_SEP).append(START_JSON)
					.append(itemsToJSON(getProperties(snapshot))).append(END_JSON);
		}
		jsonString.append(END_JSON);
		return jsonString.toString();
	}

	@Override
	public String format(Property prop) {
		if (level == 9) {
			return super.format(prop);
		}

		if (prop == null || prop.isTransient()) {
			return EMPTY_STR;
		}

		Object value = prop.getValue();

		if (isSpecialSuppress(value)) {
			return EMPTY_STR;
		}

		StringBuilder jsonString = new StringBuilder(256);
		jsonString.append(Utils.quote(StringEscapeUtils.escapeJson(getKeyStr(prop.getKey())))).append(ATTR_SEP);

		if (isNoNeedToQuote(value)) {
			jsonString.append(propValueToString(value));
		} else {
			Utils.quote(StringEscapeUtils.escapeJson(propValueToString(value)), jsonString);
		}

		return jsonString.toString();
	}

	protected String getKeyStr(String key) {
		return Utils.replace(key, keyReplacements);
	}

	protected String getValueStr(Object value) {
		return Utils.replace(Utils.toString(value), valueReplacements);
	}

	protected String getSnapName(Snapshot snapshot) {
		return snapshot.getName();
	}

	protected Collection<Snapshot> getSnapshots(Operation op) {
		Collection<Snapshot> sList = op.getSnapshots();

		return getSortedCollection(sList, getSnapshotComparator());
	}

	private Comparator<Snapshot> getSnapshotComparator() {
		if (snapshotComparator == null) {
			snapshotComparator = new Comparator<Snapshot>() {
				@Override
				public int compare(Snapshot s1, Snapshot s2) {
					String s1Path = getSnapName(s1);
					String s2Path = getSnapName(s2);

					return s1Path.compareTo(s2Path);
				}
			};
		}

		return snapshotComparator;
	}

	protected Collection<Property> getProperties(Operation op) {
		Collection<Property> pList = op.getProperties();

		return getSortedCollection(pList, getPropertyComparator());
	}

	protected Collection<Property> getProperties(Snapshot snap) {
		Collection<Property> pList = snap.getSnapshot();

		return getSortedCollection(pList, getPropertyComparator());
	}

	private static <T> Collection<T> getSortedCollection(Collection<T> col, Comparator<T> comp) {
		List<T> cList;
		if (col instanceof List<?>) {
			cList = (List<T>) col;
		} else {
			cList = Collections.list(Collections.enumeration(col));
		}
		Collections.sort(cList, comp);

		return cList;
	}

	private Comparator<Property> getPropertyComparator() {
		if (propertyComparator == null) {
			propertyComparator = new Comparator<Property>() {
				@Override
				public int compare(Property p1, Property p2) {
					return p1.getKey().compareTo(p2.getKey());
				}
			};
		}

		return propertyComparator;
	}

	@Override
	public void setConfiguration(Map<String, ?> settings) {
		super.setConfiguration(settings);

		level = Utils.getInt("Level", settings, level);

		String pValue = Utils.getString("KeyReplacements", settings, "");
		if (StringUtils.isEmpty(pValue)) {
			initDefaultKeyReplacements();
		} else {
			Utils.parseReplacements(pValue, keyReplacements);
		}

		pValue = Utils.getString("ValueReplacements", settings, "");
		if (StringUtils.isEmpty(pValue)) {
			initDefaultValueReplacements();
		} else {
			Utils.parseReplacements(pValue, valueReplacements);
		}
	}

	/**
	 * Initializes default set symbol replacements for a attribute keys.
	 */
	protected void initDefaultKeyReplacements() {
	}

	/**
	 * Initializes default set symbol replacements for a attribute values.
	 */
	protected void initDefaultValueReplacements() {
	}
}
