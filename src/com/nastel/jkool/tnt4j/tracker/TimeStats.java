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

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class maintains timing and performance statistics for a
 * specific user object;
 *
 * @version $Revision: 1$
 */
public class TimeStats<T> {
	T userObject;
	AtomicLong nanoStamp = new AtomicLong(0);
	AtomicLong hitCount = new AtomicLong(0);
	
	/**
	 * Create a time hit object with specified timer starting now and
	 * null user defined object.
	 * 
	 */
	public TimeStats() {
		this(System.nanoTime(), null);
	}
	
	/**
	 * Create a time hit object with timer starting now and
	 * specified user defined object.
	 * 
	 * @param obj user defined object
	 */
	public TimeStats(T obj) {
		this(System.nanoTime(), obj);
	}
	
	/**
	 * Create a time hit object with specified timer and
	 * null user defined object.
	 * 
	 * @param nanotime
	 *            timer in nanoseconds
	 */
	public TimeStats(long nanotime) {
		this(nanotime, null);
	}

	/**
	 * Create a time hit object with specified timer and
	 * user defined object.
	 * 
	 * @param nanotime
	 *            timer in nanoseconds
	 * @param obj user defined object
	 */
	public TimeStats(long nanotime, T obj) {
		nanoStamp.set(nanotime);
		userObject = obj;
	}

	/**
	 * Obtain age in nanoseconds since the last hit
	 * 
	 * @return age in nanoseconds since the last hit
	 */
	public long getAgeNanos() {
		return (System.nanoTime() - nanoStamp.get());
	}
	
	/**
	 * Obtain age in nanoseconds since the last hit relative
	 * to a given timer in nanoseconds.
	 * 
	 * @param nanos
	 *            timer in nanoseconds
	 * @return age in nanoseconds since the relative timer
	 */
	public long getAgeNanos(long nanos) {
		return (nanos - nanoStamp.get());
	}
	
	/**
	 * Obtain timer in nanoseconds based on last hit
	 * 
	 * @return timer in nanoseconds based on last hit
	 */
	public long getLastTimeNanos() {
		return nanoStamp.get();
	}
		
	/**
	 * Hit this entry, time stamp it and count the number of hits
	 * 
	 * @return elapsed nanoseconds since the last hit
	 */
	public long hit() {
		return hit(+1);
	}
	
	/**
	 * Hit this entry, time stamp it and count the number of hits
	 * 
	 * @param delta
	 *            increment hit count by specified delta
	 * 
	 * @return elapsed nanoseconds since the last hit
	 */
	public long hit(long delta) {
		long lastStamp = nanoStamp.get();
		long now = System.nanoTime();
		nanoStamp.compareAndSet(lastStamp, now);
		long age = now - lastStamp;
		hitCount.addAndGet(delta);
		return age < 0 ? 0 : age;
	}
	
	/**
	 * Obtain current hit count
	 * 
	 * @return current hit count
	 */
	public long getHitCount() {
		return hitCount.get();
	}
	
	/**
	 * Obtain current user object associated with this entry
	 * 
	 * @return current user object associated with this entry, null if none.
	 */
	public T getUserObject() {
		return userObject;
	}
}