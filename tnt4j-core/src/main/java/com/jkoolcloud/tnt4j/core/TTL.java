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
package com.jkoolcloud.tnt4j.core;

/**
 * This interface defines classes that can have time to live attributes.
 * 
 * @version $Revision: 2 $
 */
public interface TTL {

	/**
	 * Time to live CONTEXT, taken from underlying TTL object
	 */
	long TTL_CONTEXT = -100;

	/**
	 * Time to live NONE, disable persistence
	 */
	long TTL_NONE = -1;

	/**
	 * Time to live is default -- as per underlying sink implementation
	 */
	long TTL_DEFAULT = 0;

	/**
	 * Gets time to live in seconds
	 *
	 * @return time to live in seconds
	 */
	long getTTL();

	/**
	 * Sets time to live in seconds. Negative number implies no TTL and instructs underlying sinks to disable
	 * persistence.
	 *
	 * @param ttl
	 *            time to live in seconds
	 */
	void setTTL(long ttl);
}
