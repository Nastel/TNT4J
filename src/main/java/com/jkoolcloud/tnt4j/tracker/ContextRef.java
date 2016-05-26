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
package com.jkoolcloud.tnt4j.tracker;

import com.jkoolcloud.tnt4j.uuid.DefaultUUIDFactory;
import com.jkoolcloud.tnt4j.uuid.UUIDFactory;

/**
 * A wrapper class that represents a context reference for a given object.
 * 
 * @version $Revision: 1 $
 *
 */
public class ContextRef {
	/**
	 * object id that should be unique within the JVM
	 * 
	 */
	String objectId;

	/**
	 * correlation id associated with objectId
	 * 
	 */
	String corrid;

	/**
	 * timer in nanoseconds when context was created
	 * 
	 */
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
	 * @param cid correlation to be associated with this context reference
	 */
	public ContextRef(Object obj, String cid) {
		this(getObjectRef(obj), cid);
	}
	
	/**
	 * Create a context reference for a given object with
	 * a given unique id.
	 * 
	 * @param oid for which to create a reference
	 * @param cid correlation to be associated with this context reference
	 */
	public ContextRef(String oid, String cid) {
		objectId = oid;
		corrid = cid;
		createdTimeNanos = System.nanoTime();
	}
	
	/**
	 * Obtain object id (not its correlation id). Use {@link #cid()}
	 * to obtain correlation id associated with this object.
	 * 
	 * @return object id associated with this context
	 */
	public String oid() {
		return objectId;
	}
	
	/**
	 * Obtain object unique id (not its id). Use {@link #cid()}
	 * to obtain correlation id associated with this object.
	 * 
	 * @return object unique id
	 */
	public String cid() {
		return corrid;
	}
	
	/**
	 * Calculate elapsed nanoseconds from now to when
	 * this context reference was created.
	 * 
	 * @return elapsed nanoseconds since context was created
	 */
	public long elapsedNanos() {
		return System.nanoTime() - createdTimeNanos;
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

	@Override
	public int hashCode() {
		return objectId.hashCode();
	}
}
