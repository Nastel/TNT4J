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

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Helper class that allows sharing of variables within ThreaLocal.
 * This class is useful when passing correlators, tags and other key value
 * pairs within same thread execution.
 * 
 * @version $Revision: 1 $
 *
 */
public class ContextTracker {
	public static String JK_CORR_ID = "JK_CORR_ID";

	private static ThreadLocal<ConcurrentMap<String, String>> CONTEXT = new ThreadLocal<ConcurrentMap<String, String>>() {
		@Override
		public ConcurrentMap<String, String> initialValue() {
			return new ConcurrentHashMap<String, String>();
		}
	};

	/**
	 * Associates the specified value with default key {@code JK_CORR_ID} with this map.
	 * 
	 * @param value
	 *            value to be associated with default key {@code JK_CORR_ID}
	 * @return null if no previous value exists, previous value
	 */
	public static String set(String value) {
		return set(JK_CORR_ID, value);
	}

	/**
	 * Get value associated with default key {@code JK_CORR_ID}
	 * 
	 * @return value associated with default key {@code JK_CORR_ID}
	 */
	public static String get() {
		return get(JK_CORR_ID);
	}

	/**
	 * Get value associated with a given key
	 * 
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
	 * Clear all keys and values associated with current context
	 * 
	 */
	public static void clear() {
		ConcurrentMap<String, String> map = CONTEXT.get();
		map.clear();
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
