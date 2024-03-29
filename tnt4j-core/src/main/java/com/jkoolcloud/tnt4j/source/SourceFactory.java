/*
 * Copyright 2014-2023 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.source;

import com.jkoolcloud.tnt4j.locator.GeoLocator;

/**
 * This interface defines a source factory which creates instances of sources. Source may contain other sources.
 * 
 * @version $Revision: 1 $
 * 
 */

public interface SourceFactory {
	/**
	 * Obtain geo locator instance
	 * 
	 * @return geo locator instance
	 */
	GeoLocator getGeoLocator();

	/**
	 * Gets root source.
	 * 
	 * @return root source
	 */
	Source getRootSource();

	/**
	 * Create a new source
	 * 
	 * @param name
	 *            source name
	 * @return source handle
	 */
	Source newSource(String name);

	/**
	 * Create a new source given name and type
	 * 
	 * @param name
	 *            source name
	 * @param type
	 *            source type
	 * @return source handle
	 */
	Source newSource(String name, SourceType type);

	/**
	 * Create a new source given name and type
	 * 
	 * @param name
	 *            source name
	 * @param type
	 *            source type
	 * @param parent
	 *            source
	 * @return source handle
	 */
	Source newSource(String name, SourceType type, Source parent);

	/**
	 * Create a new source given name and type
	 * 
	 * @param name
	 *            source name
	 * @param type
	 *            source type
	 * @param parent
	 *            source
	 * @param user
	 *            user name associated with the source
	 * @return source handle
	 */
	Source newSource(String name, SourceType type, Source parent, String user);

	/**
	 * Create a new source based on a given fully qualified path. Format: type=name|?#type=name... Example:
	 * RUNTIME=?#SERVER=?#NETADDR=?#DATACENTER=?#GEOADDR=?
	 * 
	 * @param fqn
	 *            fully qualified path for the source
	 * @return source handle representing the path.
	 */
	Source newFromFQN(String fqn);

	/**
	 * Create a derived source based on a given fully qualified path and a a default parent source. Same as
	 * {@code fromFQN(fqn, getRootSource())} Format: type=name|?#type=name... Example:
	 * RUNTIME=?#SERVER=?#NETADDR=?#DATACENTER=?#GEOADDR=?
	 * 
	 * @param fqn
	 *            fully qualified path for the source
	 * @return source handle representing the path.
	 */
	Source fromFQN(String fqn);

	/**
	 * Create a derived source based on a given fully qualified path and a given parent source. Format:
	 * type=name|?#type=name... Example: RUNTIME=?#SERVER=?#NETADDR=?#DATACENTER=?#GEOADDR=?
	 * 
	 * @param fqn
	 *            fully qualified path for the source
	 * @param parent
	 *            source
	 * @return source handle representing the path.
	 */
	Source fromFQN(String fqn, Source parent);

	/**
	 * Gets streaming source name (sender name)
	 * 
	 * @return streaming source name (sender name)
	 */
	String getSSN();
}
