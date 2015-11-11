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

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * This class implements time tracker for a set of keys.
 * The class maintains a cache of timestamp hits and measures time
 * since the last hit on a set of keys.
 *
 * @version $Revision: 1$
 */
public class TimeTracker {

	/*
	 * Timing map maintains the number of nanoseconds since last event for a specific server/application combo.
	 */
	final ConcurrentMap<String, TimeStats> EVENT_MAP;
	final Cache<String, TimeStats> EVENT_CACHE;

	/**
	 * Create a default time tracker with specified capacity and life span
	 * 
	 * @param capacity
	 *            maximum capacity
	 * @param lifeSpan life span in milliseconds
	 */
	public static TimeTracker newTracker(int capacity, long lifeSpan) {
		return new TimeTracker(capacity, lifeSpan);
	}
	
	/**
	 * Create a time tracker with specified capacity and life span
	 * 
	 * @param capacity
	 *            maximum capacity
	 * @param lifeSpan life span in milliseconds
	 */
	private TimeTracker(int capacity, long lifeSpan) {
		EVENT_CACHE = CacheBuilder.newBuilder().concurrencyLevel(Runtime.getRuntime().availableProcessors()).recordStats()
				.maximumSize(capacity).expireAfterWrite(lifeSpan, TimeUnit.MILLISECONDS).build();	
		EVENT_MAP = EVENT_CACHE.asMap();
	}
	
	/**
	 * Hit and obtain elapsed nanoseconds since last event
	 * 
	 * @param key
	 *            timer key
	 * @return elapsed nanoseconds since last event
	 */
	public long hitAndGet(String key) {
		TimeStats last = EVENT_MAP.get(key);
		if (last == null) {
			last = EVENT_MAP.putIfAbsent(key, new TimeStats());
			last = last == null? EVENT_MAP.get(key): last;
		}
		return last.hit();
	}
	
	/**
	 * obtain hit count for a specific key
	 * 
	 * @param key
	 *            timer key
	 * @return hit count for a specific key
	 */
	public long getHitCount(String key) {
		TimeStats last = EVENT_MAP.get(key);		
		return last != null? last.getHitCount(): 0;
	}
	
	/**
	 * obtain elapsed nanoseconds for a specific key
	 * 
	 * @param key
	 *            timer key
	 * @return hit count for a specific key
	 */
	public long getElapsedNanos(String key) {
		TimeStats last = EVENT_MAP.get(key);		
		return last != null? last.getAgeNanos(): 0;
	}	
		
	/**
	 * Get map of all time statistics maintained by this tracker
	 * 
	 * @return map of all time statistics maintained by this tracker
	 */
	public Map<String, TimeStats> getTimeStats() {
		return EVENT_MAP;
	}
}
