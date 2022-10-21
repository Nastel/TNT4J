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

import java.util.*;
import java.util.Map.Entry;

import com.jkoolcloud.tnt4j.source.Source;

/**
 * This class defines a snapshot/collection of {@code Property} instances. A collection of name, value pairs with
 * associated user defined name and a time stamp when the snapshot was generated.
 *
 * @see Property
 * @see UsecTimestamp
 * @version $Revision: 8 $
 */
public class PropertySnapshot implements Snapshot {
	public static final String CATEGORY_DEFAULT = "Default";

	private String guid;
	private long ttl = TTL.TTL_DEFAULT;
	private OpLevel level;
	private OpType opType = OpType.SNAPSHOT;
	private String id;
	private String category;
	private String snapName;
	private String parent_id;
	private String sign;
	private UsecTimestamp timeStamp;
	private Source source;
	private Set<String> correlators = new HashSet<>(89);
	private Map<Object, Property> propSet = new LinkedHashMap<>();

	/**
	 * Constructs a Property snapshot with the specified name and current time stamp.
	 *
	 * @param name
	 *            snapshot name
	 */
	public PropertySnapshot(String name) {
		this(CATEGORY_DEFAULT, name);
	}

	/**
	 * Constructs a Property snapshot with the specified name and current time stamp.
	 *
	 * @param cat
	 *            snapshot category name
	 * @param name
	 *            snapshot name
	 */
	public PropertySnapshot(String cat, String name) {
		this(cat, name, OpLevel.INFO, OpType.SNAPSHOT, UsecTimestamp.now());
	}

	/**
	 * Constructs a Property snapshot with the specified name and current time stamp.
	 *
	 * @param cat
	 *            snapshot category name
	 * @param name
	 *            snapshot name
	 * @param lvl
	 *            severity level
	 * @see OpLevel
	 */
	public PropertySnapshot(String cat, String name, OpLevel lvl) {
		this(cat, name, lvl, OpType.SNAPSHOT, UsecTimestamp.now());
	}

	/**
	 * Constructs a Property snapshot with the specified name, given time stamp.
	 *
	 * @param cat
	 *            snapshot category name
	 * @param name
	 *            snapshot name
	 * @param lvl
	 *            severity level
	 * @param time
	 *            time stamp associated with the snapshot
	 * @see OpLevel
	 * @see UsecTimestamp
	 */
	public PropertySnapshot(String cat, String name, OpLevel lvl, UsecTimestamp time) {
		this(cat, name, lvl, OpType.SNAPSHOT, time);
	}

	/**
	 * Constructs a Property snapshot with the specified name, type, given time stamp.
	 *
	 * @param cat
	 *            snapshot category name
	 * @param name
	 *            snapshot name
	 * @param lvl
	 *            severity level
	 * @param type
	 *            operation associated with this snapshot
	 * @param time
	 *            time stamp associated with the snapshot
	 * @see OpLevel
	 * @see OpType
	 * @see UsecTimestamp
	 */
	protected PropertySnapshot(String cat, String name, OpLevel lvl, OpType type, UsecTimestamp time) {
		category = cat;
		snapName = name;
		timeStamp = time;
		level = lvl;
		opType = type;
		id = snapName + "@" + category;
	}

	@Override
	public String getGUID() {
		return guid;
	}

	@Override
	public void setGUID(String uid) {
		this.guid = uid;
	}

	/**
	 * Gets the current severity level to associated with snapshot.
	 *
	 * @return current severity level
	 */
	@Override
	public OpLevel getSeverity() {
		return level;
	}

	/**
	 * Sets the current severity level to associated with snapshot.
	 *
	 * @param lvl
	 *            operation severity level
	 */
	public void setSeverity(OpLevel lvl) {
		level = lvl;
	}

	/**
	 * Add a property with a given key and value.
	 *
	 * @param key
	 *            property key name
	 * @param value
	 *            value associated with the key
	 * @return reference to this snapshot
	 */
	public PropertySnapshot add(String key, Object value) {
		this.add(key, value, false);
		return this;
	}

	/**
	 * Add a property with a given key and value.
	 *
	 * @param key
	 *            property key name
	 * @param value
	 *            value associated with the key
	 * @param transient_
	 *            flag indicating whether property is transient
	 * @return reference to this snapshot
	 */
	public PropertySnapshot add(String key, Object value, boolean transient_) {
		this.add((Object) key, value, transient_);
		return this;
	}

	/**
	 * Add a property with a given key and value.
	 *
	 * @param key
	 *            property key name
	 * @param value
	 *            value associated with the key
	 * @return reference to this snapshot
	 */
	@Override
	public PropertySnapshot add(Object key, Object value) {
		this.add(key.toString(), value, false);
		return this;
	}

	/**
	 * Add a property with a given key and value.
	 *
	 * @param key
	 *            property key name
	 * @param value
	 *            value associated with the key
	 * @param transient_
	 *            flag indicating whether property is transient
	 * @return reference to this snapshot
	 */
	@Override
	public PropertySnapshot add(Object key, Object value, boolean transient_) {
		this.add(new Property(String.valueOf(key), value, transient_));
		return this;
	}

	/**
	 * Add a property with a given key and value.
	 *
	 * @param key
	 *            property key name
	 * @param value
	 *            value associated with the key
	 * @param valType
	 *            value type such as (currency, percent). See {@link ValueTypes}.
	 * @return reference to this snapshot
	 */
	@Override
	public PropertySnapshot add(Object key, Object value, String valType) {
		this.add(new Property(key.toString(), value, valType));
		return this;
	}

	/**
	 * Set current/active {@code Source} with the current activity.
	 *
	 * @see Source
	 */
	@Override
	public void setSource(Source src) {
		source = src;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return snapName;
	}

	@Override
	public UsecTimestamp getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets snapshot timestamp value.
	 *
	 * @param timeStamp
	 *            new timestamp value
	 */
	public void setTimeStamp(UsecTimestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public long getTime() {
		return timeStamp.getTimeMillis();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(512);
		str.append(this.getClass().getSimpleName()) //
				.append("{Category: ").append(category) //
				.append(", Name: ").append(snapName) //
				.append(", Guid: ").append(guid) //
				.append(", TimeStamp: ").append(timeStamp) //
				.append(", Count: ").append(this.size()) //
				.append(", List: [");
		for (Property item : propSet.values()) {
			str.append(item);
		}
		str.append("]}");
		return str.toString();
	}

	@Override
	public Collection<Property> getProperties() {
		return propSet.values();
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public Set<String> getCorrelator() {
		return correlators;
	}

	@Override
	public void setCorrelator(String... clist) {
		for (int i = 0; (clist != null) && (i < clist.length); i++) {
			if (clist[i] != null) {
				this.correlators.add(clist[i]);
			}
		}
	}

	@Override
	public void setCorrelator(Collection<String> clist) {
		if (clist != null) {
			this.correlators.addAll(clist);
		}
	}

	@Override
	public String getParentId() {
		return parent_id;
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public String getTrackingId() {
		return getGUID();
	}

	@Override
	public OpType getType() {
		return opType;
	}

	@Override
	public void setParentId(Trackable parentObject) {
		parent_id = parentObject != null ? parentObject.getTrackingId() : parent_id;
	}

	@Override
	public void setParentId(String parentId) {
		parent_id = parentId;
	}

	@Override
	public void setTrackingId(String signature) {
		setGUID(signature);
	}

	@Override
	public Snapshot add(Property property) {
		propSet.put(property.getKey(), property);
		return this;
	}

	@Override
	public int size() {
		return propSet.size();
	}

	@Override
	public Property get(Object key) {
		return propSet.get(key);
	}

	@Override
	public Property remove(Object key) {
		return propSet.remove(key);
	}

	@Override
	public Snapshot addAll(Map<? extends Object, ? extends Object> m) {
		for (Entry<? extends Object, ? extends Object> entry : m.entrySet()) {
			this.add(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public int hashCode() {
		return getSnapKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Snapshot) {
			return ((Snapshot) obj).getSnapKey().equals(getSnapKey());
		}
		return false;
	}

	@Override
	public String getSnapKey() {
		return guid == null ? id : guid;
	}

	@Override
	public long getTTL() {
		return ttl;
	}

	@Override
	public void setTTL(long ttl) {
		this.ttl = ttl;
	}

	@Override
	public String getSignature() {
		return sign;
	}

	@Override
	public void setSignature(String sign) {
		this.sign = sign;
	}

	/**
	 * Returns value of {@code fieldName} defined field/property for this snapshot.
	 * <p>
	 * List of supported field names (in common with {@link com.jkoolcloud.tnt4j.source.Source#getFieldValue(String)}):
	 * <ul>
	 * <li>Name</li>
	 * <li>ParentId</li>
	 * <li>TrackingId</li>
	 * <li>Type</li>
	 * <li>Category</li>
	 * <li>Id</li>
	 * <li>Signature</li>
	 * <li>Guid</li>
	 * <li>Source</li>
	 * <li>Timestamp</li>
	 * <li>TTL</li>
	 * <li>Severity</li>
	 * <li>Correlators</li>
	 * <li>Properties</li>
	 * <li>PropertiesCount</li>
	 * <li>Custom property name from contained properties set</li>
	 * </ul>
	 *
	 * @param fieldName
	 *            snapshot field or property name
	 * @return field/property contained value
	 */
	@Override
	public Object getFieldValue(String fieldName) {
		if ("Name".equalsIgnoreCase(fieldName)) {
			return snapName;
		}
		if ("ParentId".equalsIgnoreCase(fieldName)) {
			return parent_id;
		}
		if ("TrackingId".equalsIgnoreCase(fieldName)) {
			return getTrackingId();
		}
		if ("Type".equalsIgnoreCase(fieldName)) {
			return opType;
		}
		if ("Category".equalsIgnoreCase(fieldName)) {
			return category;
		}
		if ("Id".equalsIgnoreCase(fieldName)) {
			return id;
		}
		if ("Signature".equalsIgnoreCase(fieldName)) {
			return sign;
		}
		if ("Guid".equalsIgnoreCase(fieldName)) {
			return guid;
		}
		if ("Source".equalsIgnoreCase(fieldName)) {
			return source;
		}
		if ("Timestamp".equalsIgnoreCase(fieldName)) {
			return getTime();
		}
		if ("TTL".equalsIgnoreCase(fieldName)) {
			return ttl;
		}
		if ("Severity".equalsIgnoreCase(fieldName)) {
			return level;
		}
		if ("Correlators".equalsIgnoreCase(fieldName)) {
			return correlators;
		}
		if ("Properties".equalsIgnoreCase(fieldName)) {
			return propSet;
		}
		if ("PropertiesCount".equalsIgnoreCase(fieldName)) {
			return propSet == null ? 0 : propSet.size();
		}

		if (source != null) {
			Object sfValue = source.getFieldValue(fieldName);
			if (sfValue != null) {
				return sfValue;
			}
		}

		Property property = get(fieldName);
		if (property != null) {
			return property.getValue();
		}

		return null;
	}
}
