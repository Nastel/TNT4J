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

/**
 * Classes that implement this interface support of parent/child relationship
 * of related items.
 * 
 * @version $Revision: 2 $
 */
public interface LinkedItem {
		
	/**
	 * Gets item tracking signature
	 *
	 * @return item tracking signature
	 */
	public String getTrackingId();
	
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
	public void setParentItem(LinkedItem parentObject);
	
	/**
	 * Gets the parent object for this object.
	 * 
	 * @return parent object
	 */
	public LinkedItem getParentItem();
	
	/**
	 * Removes all children objects.
	 */
	public void clearChildren();
}
