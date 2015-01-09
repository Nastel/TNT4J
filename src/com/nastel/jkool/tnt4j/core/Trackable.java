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

import com.nastel.jkool.tnt4j.source.Source;

/**
 * Classes that implement this interface support define trackable objects.
 * Trackable are objects which can be tracked and related to one another using
 * tracking id and correlation id. Each trackable objects has a tracking id
 * which uniquely identifies this instance, parent id -- tracking id of the parent
 * trackable instance and correlator which is a user defined token which could be
 * another tracking id from another trackable source.
 * 
 * @version $Revision: 2 $
 */
public interface Trackable {
		
	/**
	 * Set current/active <code>Source</code> with the trackable entity
	 *
	 * @param src application source handle
	 * @see Source
	 */
	void setSource(Source src);
	
	/**
	 * Obtains current/active <code>Source</code> handle associated
	 * with the current trackable objects
	 *
	 * @return current active application handle
	 * @see Source
	 */
	Source getSource();

	/**
	 * Returns user defined correlator, which are used to relate any
	 * pair of trackable instances.
	 *
	 * @return user-defined correlator
	 */
	String getCorrelator();
	
	/**
	 * Sets tracking correlator
	 *
	 * @param cid tracking correlator
	 */
	void setCorrelator(String cid);
	
	/**
	 * Gets operation type associated with the trackable instance
	 *
	 * @return operation type
	 */
	OpType getType();
	
	/**
	 * Gets tracking signature associated with this trackable instance.
	 *
	 * @return item tracking signature
	 */
	String getTrackingId();
	
	/**
	 * Gets parent's tracking id.
	 *
	 * @return parent's tracking id
	 */
	String getParentId();
	
	/**
	 * Sets tracking signature
	 *
	 * @param signature tracking signature
	 */
	void setTrackingId(String signature);
	
	/**
	 * Sets the parent object for this object.
	 * 
	 * @param parentObject parent object
	 * @throws IllegalArgumentException if parentObject is not a valid type of parent
	 */
	void setParentId(Trackable parentObject);
}
