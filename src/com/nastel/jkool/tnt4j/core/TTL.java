/*
 * Copyright 2014-2015 JKOOL, LLC.
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
 * This interface defines classes that can have time to live attributes.
 * 
 * @version $Revision: 2 $
 */
public interface TTL {

	/**
	 * Time to live CONTEXT, taken from underlying TTL object
	 */
	static final long TTL_CONTEXT = -100;

	/**
	 * Time to live NONE, don't store
	 */
	static final long TTL_NONE = -1;

	/**
	 * Time to live default -- as per underlying storage
	 */
	static final long TTL_DEFAULT = 0;

	/**
	 * Gets time to live in seconds
	 *
	 * @return time to live in seconds
	 */
	long getTTL();

	/**
	 * Sets time to live in seconds. Negative number implies no time to live and instructs underlying tracker not to
	 * store this operation.
	 *
	 * @param ttl
	 *            time to live in seconds
	 */
	void setTTL(long ttl);
}
