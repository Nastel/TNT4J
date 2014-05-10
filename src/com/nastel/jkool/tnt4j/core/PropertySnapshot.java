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

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class defines a snapshot/collection of <code>Property</code> instances. A collection of name, value pairs with
 * associated user defined name and a time stamp when the snapshot was generated.
 * 
 * @see Property
 * @see UsecTimestamp
 * @version $Revision: 8 $
 */
public class PropertySnapshot extends ArrayList<Property> implements Snapshot<Property> {
	private static final long serialVersionUID = 1L;

	private String category = null, snapName = null;
	private UsecTimestamp timeStamp = null;

	/**
	 * Constructs a Property snapshot with the specified name and current time stamp.
	 * 
	 * @param name
	 *            snapshot name
	 */
	public PropertySnapshot(String name) {
		this(null, name, 16);
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
		this(cat, name, 16);
	}

	/**
	 * Constructs a Property snapshot with the specified name, current time stamp and a given capacity.
	 * 
	 * @param cat
	 *            snapshot category name
	 * @param name
	 *            snapshot name
	 * @param capacity
	 *            initial capacity
	 */
	public PropertySnapshot(String cat, String name, int capacity) {
		this(cat, name, new UsecTimestamp(), 16);
	}

	/**
	 * Constructs a Property snapshot with the specified name, given time stamp and a given capacity.
	 * 
	 * @param cat
	 *            snapshot category name
	 * @param name
	 *            snapshot name
	 * @param capacity
	 *            initial capacity
	 * @param time
	 *            time stamp associated with the snapshot
	 */
	public PropertySnapshot(String cat, String name, UsecTimestamp time, int capacity) {
		super(capacity);
		category = cat;
		snapName = name;
		timeStamp = time;
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
	public PropertySnapshot add(Object key, Object value) {
		this.add(new Property(key.toString(), value));
		return this;
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
		str.append("PropertySnapshot{Category: " + category
				+ ", Name: " + snapName).append(", TimeStamp: ").append(timeStamp).append(
		        ", Count: " + this.size()).append(", List: [");
		for (Property item : this) {
			str.append(item);
		}
		str.append("]}");
		return str.toString();
	}

	@Override
	public Collection<Property> getSnapshot() {
		return this;
	}

	@Override
    public String getCategory() {
	    return category;
    }
}
