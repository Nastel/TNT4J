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


/**
 * This interface defines a snapshot construct, which
 * has a name and a time stamp and collection of elements.
 *
 * @see UsecTimestamp
 * @version $Revision: 5 $
 */
public interface Snapshot extends Trackable {
	/**
	 * Obtain a name of the snapshot
	 * 
	 * @return name of the snapshot.
	 */
	public String getName();

	/**
	 * Gets the current severity level to associated with the snapshot
	 *
	 * @return current severity level
	 */
	public OpLevel getSeverity();

	/**
	 * Obtain a snapshot category name
	 * 
	 * @return name of the snapshot category
	 */
	public String getCategory();

	/**
	 * Obtain the time stamp of the snapshot.
	 * 
	 * @return time stamp in ms
	 */
	public long getTime();

	/**
	 * Obtain the number of properties in the snapshot
	 * 
	 * @return property count
	 */
	public int size();

	/**
	 * Obtain a fully qualified time stamp object
	 * 
	 * @return time stamp object
	 */
	public UsecTimestamp getTimeStamp();
	
	/**
	 * Obtain a collection containing snapshot elements
	 * 
	 * @return collection containing snapshot elements
	 */
	public Collection<Property> getSnapshot();

	/**
	 * Adds a property to the snapshot
	 * 
	 * @param property instance to be added to the snapshot
	 */
	public Snapshot add(Property property);
	
	/**
	 * Adds a property to the snapshot
	 * 
	 * @param key object to be removed
	 * @param value property value
	 */
	public Snapshot add(Object key, Object value);
	
	/**
	 * Removed a property specified by the given key.
	 * 
	 * @param key object to be removed
	 * @return Property associated with the key, null if does not exist
	 */
	public Property remove(Object key);
	
	/**
	 * Obtain a property associated with the given key
	 * 
	 * @param key identifying a property
	 * @return Property associated with the key, null if does not exist
	 */
	public Property get(Object key);
}
