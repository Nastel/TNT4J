/*
 * Copyright 2014-2021 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.uuid;

/**
 * Implementations of this interface provide implementation for 
 * generating globally unique IDs.
 *
 * @version $Revision: 1 $
 */
public interface UUIDFactory {
	/**
	 * Return a new UUID
	 *
	 * @return string value of UUID
	 */
	String newUUID();
	
	/**
	 * Return a new UUID based on a given object
	 * 
	 * @param obj handle for which to generate UUID
	 * @return string value of UUID
	 */
	String newUUID(Object obj);
}
