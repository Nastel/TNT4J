/*
 * Copyright 2014-2019 JKOOL, LLC.
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class maintains timing and performance statistics for a
 * specific user object;
 *
 * @version $Revision: 1$
 */
public class TimeStats {
	AtomicLong hitStamp = new AtomicLong(0);
	AtomicLong hitCount = new AtomicLong(0);
	
	AtomicLong missCount = new AtomicLong(0);
	AtomicLong missStamp = new AtomicLong(0);
	
	/**
	 * Create a time hit object with specified timer starting now.
	 * 
	 */
	public TimeStats() {
		this(System.nanoTime());
	}
	
	/**
	 * Create a time hit object with specified timer.
	 * 
	 * @param nanotime
	 *            timer in nanoseconds
	 */
	public TimeStats(long nanotime) {
		hitStamp.set(nanotime);
	}

	/**
	 * Obtain hit age in nanoseconds since the last hit
	 * 
	 * @return hit age in nanoseconds since the last hit
	 */
	public long getAgeNanos() {
		return (System.nanoTime() - hitStamp.get());
	}
	
	/**
	 * Obtain age in nanoseconds since the last hit
	 * 
	 * @param tunit
	 *            target time unit
	 * @return age in specified time units
	 */
	public long getHitAge(TimeUnit tunit) {
		return tunit.convert(System.nanoTime() - hitStamp.get(), TimeUnit.NANOSECONDS);
	}
	
	/**
	 * Obtain age in nanoseconds since the last miss
	 * 
	 * @param tunit
	 *            target time unit
	 * @return age in specified time units
	 */
	public long getMissAge(TimeUnit tunit) {
		return missStamp.get() > 0? tunit.convert(System.nanoTime() - missStamp.get(), TimeUnit.NANOSECONDS): 0;
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
		return (nanos - hitStamp.get());
	}
	
	/**
	 * Obtain timer in nanoseconds based on last hit
	 * 
	 * @return timer in nanoseconds based on last hit
	 */
	public long getLastTimeNanos() {
		return hitStamp.get();
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
	 * Miss this entry, time stamp it and count the number of miss
	 * 
	 * @return elapsed nanoseconds since the last miss
	 */
	public long miss() {
		return miss(+1);
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
		long lastStamp = hitStamp.get();
		long now = System.nanoTime();
		hitStamp.compareAndSet(lastStamp, now);
		long age = now - lastStamp;
		hitCount.addAndGet(delta);
		return age < 0 ? 0 : age;
	}
	
	/**
	 * Hit miss entry, time stamp it and count the number of misses
	 * 
	 * @param delta
	 *            increment miss count by specified delta
	 * 
	 * @return elapsed nanoseconds since the last miss
	 */
	public long miss(long delta) {
		long lastStamp = missStamp.get();
		long now = System.nanoTime();
		missStamp.compareAndSet(lastStamp, now);
		long age = now - lastStamp;
		missCount.addAndGet(delta);
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
	 * Obtain current miss count
	 * 
	 * @return current miss count
	 */
	public long getMissCount() {
		return missCount.get();
	}
}