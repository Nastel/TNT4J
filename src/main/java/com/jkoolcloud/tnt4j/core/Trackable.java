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
package com.jkoolcloud.tnt4j.core;

import java.util.Collection;
import java.util.Set;

import com.jkoolcloud.tnt4j.source.Source;

/**
 * Classes that implement this interface support define trackable objects. Trackable are objects which can be tracked
 * and related to one another using tracking id and correlation id. Each trackable objects has a tracking id which
 * uniquely identifies this instance, parent id -- tracking id of the parent trackable instance and correlator which is
 * a user defined token which could be another tracking id from another trackable source.
 *
 * @version $Revision: 2 $
 */
public interface Trackable extends TTL, GlobalID {
	/**
	 * Set current/active {@link Source} with the trackable entity
	 *
	 * @param src
	 *            application source handle
	 * @see Source
	 */
	void setSource(Source src);

	/**
	 * Obtains current/active {@link Source} handle associated with the current trackable objects
	 *
	 * @return current active application handle
	 * @see Source
	 */
	Source getSource();

	/**
	 * Returns user defined correlators, which are used to relate any pair of trackable instances.
	 *
	 * @return user-defined correlator
	 */
	Set<String> getCorrelator();

	/**
	 * Sets tracking correlators
	 *
	 * @param cid
	 *            tracking correlator list
	 */
	void setCorrelator(String... cid);

	/**
	 * Sets tracking correlators
	 *
	 * @param cids
	 *            tracking correlator list
	 */
	void setCorrelator(Collection<String> cids);

	/**
	 * Gets operation type associated with the trackable instance
	 *
	 * @return operation type
	 */
	OpType getType();

	/**
	 * Gets parent's tracking id.
	 *
	 * @return parent's tracking id
	 */
	String getParentId();

	/**
	 * Get signature associated with this tracking instance. Use signature for point to point temper detection.
	 * Signatures should be computed based on contents of the trackable instance.
	 *
	 * @return item tracking identifier
	 */
	String getSignature();

	/**
	 * Sets signature for temper detection/protection. Use signature for point to point temper detection. Signatures
	 * should be computed based on contents of the trackable instance.
	 *
	 * @param sign
	 *            tracking identifier
	 */
	void setSignature(String sign);

	/**
	 * Gets tracking identifier associated with this trackable instance.
	 *
	 * @return item tracking identifier
	 */
	String getTrackingId();

	/**
	 * Sets tracking identifier
	 *
	 * @param id
	 *            tracking identifier
	 */
	void setTrackingId(String id);

	/**
	 * Sets the parent object for this object.
	 *
	 * @param parentObject
	 *            parent object
	 * @throws IllegalArgumentException
	 *             if parentObject is not a valid type of parent
	 */
	void setParentId(Trackable parentObject);

	/**
	 * Sets the parent ID for this object.
	 *
	 * @param parentId
	 *            parent ID
	 */
	void setParentId(String parentId);

	/**
	 * Obtain a name of this trackable instance.
	 *
	 * @return name of this trackable instance
	 */
	String getName();

	/**
	 * Returns value of {@code fieldName} defined field/property for this trackable.
	 *
	 * @param fieldName
	 *            trackable field or property name
	 * @return field/property contained value
	 */
	Object getFieldValue(String fieldName);
}
