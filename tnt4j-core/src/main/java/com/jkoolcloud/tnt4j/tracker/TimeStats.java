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
package com.jkoolcloud.tnt4j.tracker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class maintains timing and performance statistics for a specific user object;
 *
 * @version $Revision: 1 $
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
	public long getHitAgeNanos() {
		return getAgeNanos(hitStamp);
	}

	/**
	 * Obtain age in nanoseconds since the last hit
	 * 
	 * @param tUnit
	 *            target time unit
	 * @return age in specified time units
	 */
	public long getHitAge(TimeUnit tUnit) {
		return tUnit.convert(getAgeNanos(hitStamp), TimeUnit.NANOSECONDS);
	}

	/**
	 * Obtain age in nanoseconds since the last miss
	 * 
	 * @param tUnit
	 *            target time unit
	 * @return age in specified time units
	 */
	public long getMissAge(TimeUnit tUnit) {
		return missStamp.get() > 0 ? tUnit.convert(getAgeNanos(missStamp), TimeUnit.NANOSECONDS) : 0;
	}

	private static long getAgeNanos(AtomicLong stamp) {
		return System.nanoTime() - stamp.get();
	}

	/**
	 * Obtain age in nanoseconds since the last hit relative to a given timer in nanoseconds.
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

	private static long hitStamp(long delta, AtomicLong stamp, AtomicLong count) {
		long lastStamp = stamp.get();
		long now = System.nanoTime();
		stamp.set(now);
		count.addAndGet(delta);
		long age = now - lastStamp;
		return age < 0 ? 0 : age;
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
		return hitStamp(delta, hitStamp, hitCount);
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
		return hitStamp(delta, missStamp, missCount);
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