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
 * Classes that implement this interface support of parent/child relationship
 * of related items.
 * 
 * @version $Revision: 2 $
 */
public interface Trackable {
		
	/**
	 * Obtains current/active <code>Source</code> handle associated
	 * with the current trackable objects
	 *
	 * @return current active application handle
	 * @see Source
	 */
	public Source getSource();

	/**
	 * Gets the correlator, which is a user-defined value to relate two separate
	 * trackable objects.
	 *
	 * @return user-defined correlator
	 */
	public String getCorrelator();
	
	/**
	 * Gets the type of operation.
	 *
	 * @return operation type
	 */
	public OpType getType();
	
	/**
	 * Gets item tracking signature
	 *
	 * @return item tracking signature
	 */
	public String getTrackingId();
	
	/**
	 * Gets parent's tracking signature
	 *
	 * @return parent's tracking signature
	 */
	public String getParentId();
	
	/**
	 * Sets item tracking signature
	 *
	 * @param signature tracking signature
	 */
	public void setTrackingId(String signature);
	
	/**
	 * Sets the parent object for this object.
	 * 
	 * @param parentObject parent object
	 * @throws IllegalArgumentException if parentObject is not a valid type of parent
	 */
	public void setParentId(Trackable parentObject);
}
