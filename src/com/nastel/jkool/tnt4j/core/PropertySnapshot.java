/*
 * Copyright (c) 2014 Nastel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Nastel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with Nastel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 *
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
