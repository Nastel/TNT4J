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

import java.util.Collection;
import java.util.Map;

/**
 * This interface defines a snapshot construct, which has a name and a time stamp and collection of elements.
 *
 * @see UsecTimestamp
 *
 * @version $Revision: 5 $
 */
public interface Snapshot extends Trackable {

	/**
	 * Obtain snapshot id (fully qualified name), which is a combination of name and category {@code name@category}.
	 *
	 * @return snapshot id
	 */
	String getId();

	/**
	 * Gets the current severity level to associated with the snapshot
	 *
	 * @return current severity level
	 */
	OpLevel getSeverity();

	/**
	 * Obtain a snapshot category name
	 *
	 * @return name of the snapshot category
	 */
	String getCategory();

	/**
	 * Obtain the time stamp of the snapshot.
	 *
	 * @return time stamp in ms
	 */
	long getTime();

	/**
	 * Obtain the number of properties in the snapshot
	 *
	 * @return property count
	 */
	int size();

	/**
	 * Obtain a fully qualified time stamp object
	 *
	 * @return time stamp object
	 */
	UsecTimestamp getTimeStamp();

	/**
	 * Obtain a collection containing snapshot properties
	 *
	 * @return collection containing snapshot properties
	 */
	Collection<Property> getProperties();

	/**
	 * Adds a property to the snapshot
	 *
	 * @param property
	 *            instance to be added to the snapshot
	 * @return snapshot instance
	 */
	Snapshot add(Property property);

	/**
	 * Adds a property to the snapshot
	 *
	 * @param key
	 *            object to be removed
	 * @param value
	 *            property value
	 * @return snapshot instance
	 */
	Snapshot add(Object key, Object value);

	/**
	 * Adds a property to the snapshot
	 *
	 * @param key
	 *            object to be removed
	 * @param value
	 *            property value
	 * @param _transient
	 *            flag indicating whether property is transient
	 * @return snapshot instance
	 */
	Snapshot add(Object key, Object value, boolean _transient);

	/**
	 * Adds a property to the snapshot
	 *
	 * @param key
	 *            object to be removed
	 * @param value
	 *            property value
	 * @param valType
	 *            value type such as (currency, percent). {@link ValueTypes}.
	 * @return snapshot instance
	 */
	Snapshot add(Object key, Object value, String valType);

	/**
	 * Add all properties from a given map
	 *
	 * @param map
	 *            of key/value pairs to be added
	 * @return snapshot instance
	 */
	Snapshot addAll(Map<? extends Object, ? extends Object> map);

	/**
	 * Removed a property specified by the given key.
	 *
	 * @param key
	 *            object to be removed
	 * @return Property associated with the key, null if does not exist
	 */
	Property remove(Object key);

	/**
	 * Obtain a property associated with the given key
	 *
	 * @param key
	 *            identifying a property
	 * @return Property associated with the key, null if does not exist
	 */
	Property get(Object key);

	/**
	 * Obtain snapshot key, which is the GUID of this snapshot if present, otherwise - this snapshot id.
	 * 
	 * @return snapshot key
	 * 
	 * @see #getId()
	 * @see #getGUID()
	 */
	String getSnapKey();
}
