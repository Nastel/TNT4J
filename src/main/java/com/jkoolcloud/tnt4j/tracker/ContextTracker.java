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
package com.jkoolcloud.tnt4j.tracker;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.jkoolcloud.tnt4j.uuid.DefaultUUIDFactory;

/**
 * Helper class that allows sharing of variables within ThreadLocal and across threads within the same JVM. This class
 * is useful when passing correlators, tags and other key value pairs within and across threads. Use
 * {@link #getRef(Object, String)} and {@link #clearRef(Object)} to track object references across thread boundaries
 * within the same JVM.
 * 
 * @version $Revision: 1 $
 *
 */
public class ContextTracker {
	public static final String JK_CORR_SESSION_ID = "JK_CORR_SID";
	public static final String JK_CORR_REQUEST_ID = "JK_CORR_RID";

	private static final ConcurrentMap<String, ContextRef> REF_MAP = new ConcurrentHashMap<String, ContextRef>();

	private static ThreadLocal<ConcurrentMap<String, String>> CONTEXT = new ThreadLocal<ConcurrentMap<String, String>>() {
		@Override
		public ConcurrentMap<String, String> initialValue() {
			return new ConcurrentHashMap<String, String>();
		}
	};

	/**
	 * Obtain a context reference {@link ContextRef} for a specific object. Tracking reference is cached until
	 * {@link #clearRef(Object)} is called. Use this method to track object references across threads within the same
	 * JVM.
	 * 
	 * @param obj
	 *            object for which context reference is obtained
	 * @return context reference associated with the specified object
	 */
	public ContextRef getRef(Object obj) {
		return getRef(obj, DefaultUUIDFactory.getInstance().newUUID());
	}

	/**
	 * Obtain a context reference {@link ContextRef} for a specific object and associate it with a specified correlation
	 * id. Context reference is cached until {@link #clearRef(Object)} is called. Use this method to track object
	 * references across threads within the same JVM. The thread passing object to another thread should call
	 * {@link #getRef(Object)} and the thread that receives object should call {@link #clearRef(Object)}.
	 * 
	 * @param obj
	 *            object for which context reference is obtained
	 * @param cid
	 *            correlation id to be associated with this object
	 * @return context reference associated with the specified object
	 */
	public ContextRef getRef(Object obj, String cid) {
		String refKey = ContextRef.getObjectRef(obj);
		ContextRef ref = REF_MAP.get(refKey);
		if (ref == null) {
			ref = new ContextRef(obj, cid);
			ContextRef prev = REF_MAP.putIfAbsent(ref.oid(), ref);
			ref = prev != null ? prev : ref;
		}
		return ref;
	}

	/**
	 * Discard a context reference {@link ContextRef} associated with a given object. Context references are created and
	 * cached using {@link #getRef(Object)} and discarded using {@link #clearRef(Object)}. Use this method to track
	 * object references across threads within the same JVM.
	 * 
	 * @param obj
	 *            object whose reference is discarded
	 * @return context reference associated with the specified object
	 */
	public ContextRef clearRef(Object obj) {
		String refKey = ContextRef.getObjectRef(obj);
		return REF_MAP.remove(refKey);
	}

	/**
	 * Associates the specified value with default key {@code JK_CORR_SESSION_ID} with this map.
	 * 
	 * @param value
	 *            value to be associated with default key {@code JK_CORR_SESSION_ID}
	 * @return null if no previous value exists, previous value
	 */
	public static String set(String value) {
		return set(JK_CORR_SESSION_ID, value);
	}

	/**
	 * Get value associated with default key {@code JK_CORR_SESSION_ID}
	 * 
	 * @return value associated with default key {@code JK_CORR_SESSION_ID}
	 */
	public static String get() {
		return get(JK_CORR_SESSION_ID);
	}

	/**
	 * Associates the specified value with default key {@code JK_CORR_REQUEST_ID} with this map.
	 * 
	 * @param value
	 *            value to be associated with default key {@code JK_CORR_REQUEST_ID}
	 * @return null if no previous value exists, previous value
	 */
	public static String setRequestId(String value) {
		return set(JK_CORR_REQUEST_ID, value);
	}

	/**
	 * Get value associated with default key {@code JK_CORR_REQUEST_ID}
	 * 
	 * @return value associated with default key {@code JK_CORR_REQUEST_ID}
	 */
	public static String getRequestId() {
		return get(JK_CORR_REQUEST_ID);
	}

	/**
	 * Get value associated with a given key.
	 *
	 * @param key
	 *            key of associated value to get
	 * @return value associated with a given key
	 */
	public static String get(String key) {
		ConcurrentMap<String, String> map = CONTEXT.get();
		return map.get(key);
	}

	/**
	 * Associates the specified value with default key {@code JK_CORR_ID} with this map.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 * @return null if no previous value exists, previous value
	 */
	public static String set(String key, String value) {
		ConcurrentMap<String, String> map = CONTEXT.get();
		return map.put(key, value);
	}

	/**
	 * If the specified key is not already associated with a value, associate it with the given value.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 * @return null if no previous value exists, previous value
	 */
	public static String setIfAbsent(String key, String value) {
		ConcurrentMap<String, String> map = CONTEXT.get();
		return map.putIfAbsent(key, value);
	}

	/**
	 * Clear all context keys and values associated with current context
	 * 
	 */
	public static void clearContext() {
		ConcurrentMap<String, String> map = CONTEXT.get();
		map.clear();
	}

	/**
	 * Clear all context reference key/value associated with current context
	 * 
	 */
	public static void clearRefs() {
		REF_MAP.clear();
	}

	/**
	 * Get all value associated with current context
	 * 
	 * @return all value associated with current context
	 */
	public Collection<String> getValues() {
		ConcurrentMap<String, String> map = CONTEXT.get();
		return map.values();
	}

	/**
	 * Get all keys associated with current context
	 * 
	 * @return all keys associated with current context
	 */
	public Set<String> getKeys() {
		ConcurrentMap<String, String> map = CONTEXT.get();
		return map.keySet();
	}

	/**
	 * Get a set of all key/value pairs
	 * 
	 * @return a set of all key/value pairs
	 */
	public Set<Entry<String, String>> entrySet() {
		ConcurrentMap<String, String> map = CONTEXT.get();
		return map.entrySet();
	}
}
