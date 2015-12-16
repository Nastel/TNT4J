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
package com.nastel.jkool.tnt4j.tracker;

import com.nastel.jkool.tnt4j.uuid.DefaultUUIDFactory;
import com.nastel.jkool.tnt4j.uuid.UUIDFactory;

/**
 * A wrapper class that represents a context reference for a given object.
 * 
 * @version $Revision: 1 $
 *
 */
public class ContextRef {
	String objectId;
	String corrid;
	long createdTimeNanos;
	
	/**
	 * Create a context reference for a given object
	 * 
	 * @param obj for which to create a reference
	 */
	public ContextRef(Object obj) {
		this(obj, DefaultUUIDFactory.getInstance());
	}
	
	/**
	 * Create a context reference for a given object and
	 * a specific {@link UUIDFactory} which is used to generate
	 * correlation id for this object.
	 * 
	 * @param obj for which to create a reference
	 * @param uuidFactory used to generate correlation ids
	 */
	public ContextRef(Object obj, UUIDFactory uuidFactory) {
		this(obj, uuidFactory.newUUID());
	}
	
	/**
	 * Create a context reference for a given object with
	 * a given unique id.
	 * 
	 * @param obj for which to create a reference
	 * @param cid to be associated with this context reference
	 */
	public ContextRef(Object obj, String cid) {
		objectId = getObjectRef(obj);
		corrid = cid;
		createdTimeNanos = System.nanoTime();
	}
	
	/**
	 * Obtain object id (not its uuid). Use {{@link #cid()}
	 * to obtain  correlation id associated with this object.
	 * 
	 * @return object id
	 */
	public String id() {
		return objectId;
	}
	
	/**
	 * Obtain object unique id (not its id). Use {{@link #id()}
	 * to obtain correlation id associated with this object.
	 * 
	 * @return object unique id
	 */
	public String cid() {
		return corrid;
	}
	
	/**
	 * Calculate elapsed nanoseconds from now to when
	 * this context was created.
	 * 
	 * @return elapsed nanoseconds since context was created
	 */
	public long elapsedNanos() {
		return System.nanoTime() - createdTimeNanos;
	}
	
	@Override
	public int hashCode() {
		return objectId.hashCode();
	}
	
	/**
	 * Object object reference id
	 * 
	 * @param obj for which to generate a reference id
	 * @return Object object reference id
	 */
	public static String getObjectRef(Object obj) {
		return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));		
	}
}
