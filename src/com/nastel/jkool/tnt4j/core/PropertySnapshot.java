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

import com.nastel.jkool.tnt4j.source.Source;

/**
 * This class defines a snapshot/collection of <code>Property</code> instances. A collection of name, value pairs with
 * associated user defined name and a time stamp when the snapshot was generated.
 * 
 * @see Property
 * @see UsecTimestamp
 * @version $Revision: 8 $
 */
public class PropertySnapshot  implements Snapshot {
	
	private OpLevel level;
	private OpType opType = OpType.INQUIRE;
	private String id = null;
	private String category = null;
	private String snapName = null;
	private String correlator;
	private String tracking_id;
	private String parent_id;
	private UsecTimestamp timeStamp = null;
	private Source source;
	private HashMap<Object, Property> propSet = new HashMap<Object, Property>();

	/**
	 * Constructs a Property snapshot with the specified name and current time stamp.
	 * 
	 * @param name
	 *            snapshot name
	 */
	public PropertySnapshot(String name) {
		this(null, name);
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
		this(cat, name, new UsecTimestamp(), OpLevel.INFO, OpType.INQUIRE);
	}

	/**
	 * Constructs a Property snapshot with the specified name and current time stamp.
	 * 
	 * @param cat
	 *            snapshot category name
	 * @param name
	 *            snapshot name
	 * @param lvl severity level
	 */
	public PropertySnapshot(String cat, String name, OpLevel lvl) {
		this(cat, name, lvl, OpType.INQUIRE);
	}

	/**
	 * Constructs a Property snapshot with the specified name and current time stamp.
	 * 
	 * @param cat
	 *            snapshot category name
	 * @param name
	 *            snapshot name
	 * @param lvl severity level
	 * @param type operation associated with this snapshot
	 */
	public PropertySnapshot(String cat, String name, OpLevel lvl, OpType type) {
		this(cat, name, new UsecTimestamp(), lvl, type);
	}

	/**
	 * Constructs a Property snapshot with the specified name, given time stamp and a given capacity.
	 * 
	 * @param cat
	 *            snapshot category name
	 * @param name
	 *            snapshot name
	 * @param time
	 *            time stamp associated with the snapshot
	 * @param lvl severity level
	 * @param type operation associated with this snapshot
	 */
	public PropertySnapshot(String cat, String name, UsecTimestamp time, OpLevel lvl, OpType type) {
		category = cat;
		snapName = name;
		timeStamp = time;
		level = lvl;
		opType = type;
		id = snapName + "@" + category;
	}

	/**
	 * Gets the current severity level to associated with snapshot.
	 *
	 * @return current severity level
	 */
	public OpLevel getSeverity() {
		return level;
	}

	/**
	 * Sets the current severity level to associated with snapshot
	 *
	 * @param lvl operation severity level
	 */
	public void setSeverity(OpLevel lvl) {
		level = lvl;
	}

	/**
	 * Add a property with a given key and value
	 * 
	 * @param key
	 *            property key name
	 * @param value
	 *            value associated with the key
	 * @return reference to this snapshot
	 */
	public PropertySnapshot add(String key, Object value) {
		this.add(new Property(key, value));
		return this;
	}

	/**
	 * Add a property with a given key and value
	 * 
	 * @param key
	 *            property key name
	 * @param value
	 *            value associated with the key
	 * @return reference to this snapshot
	 */
	@Override
	public PropertySnapshot add(Object key, Object value) {
		this.add(new Property(key.toString(), value));
		return this;
	}

	/**
	 * Set current/active <code>Source</code> with the current activity
	 *
	 * @see Source
	 */
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

	@Override
	public long getTime() {
		return timeStamp.getTimeMillis();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(512);
		str.append(this.getClass().getSimpleName()).append("{Category: " + category
				+ ", Name: " + snapName).append(", TimeStamp: ").append(timeStamp).append(
		        ", Count: " + this.size()).append(", List: [");
		for (Property item : propSet.values()) {
			str.append(item);
		}
		str.append("]}");
		return str.toString();
	}

	@Override
	public Collection<Property> getSnapshot() {
		return propSet.values();
	}

	@Override
    public String getCategory() {
	    return category;
    }

	@Override
    public String getCorrelator() {
	    return correlator;
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
	    return tracking_id;
    }

	@Override
    public OpType getType() {
	    return opType;
    }

	@Override
    public void setCorrelator(String cid) {
		correlator = cid;
	}

	@Override
    public void setParentId(Trackable parentObject) {
		parent_id = parentObject != null? parentObject.getTrackingId(): parent_id;
	}

	@Override
    public void setTrackingId(String signature) {
		tracking_id = signature;
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
}
