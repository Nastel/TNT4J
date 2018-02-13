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
package com.jkoolcloud.tnt4j.sink;

import java.util.concurrent.TimeUnit;

import com.jkoolcloud.tnt4j.limiter.Limiter;

/**
 * Event rate limiter wrapper around {@link Limiter} implementations.
 *
 * @version $Revision: 1 $
 */
public class EventLimiter {
	public static final long BLOCK_NONE = 0;
	public static final long BLOCK_UNTIL_GRANTED = -1;

	Limiter limiter;
	long timeout;
	TimeUnit unit;

	/**
	 * Create an event rate limiter with specified limiter implementation and timeout.
	 * 
	 * @param limiter
	 *            rate limiter implementation
	 * @param timeout
	 *            where &lt; 0 to block until granted, 0 no block, &gt; 0 block for max timeout
	 * @param unit
	 *            time unit for timeout value
	 * @see Limiter
	 */
	public EventLimiter(Limiter limiter, long timeout, TimeUnit unit) {
		this.limiter = limiter;
		this.timeout = timeout;
		this.unit = unit;
	}

	/**
	 * Get rate limiter implementation
	 *
	 * @return rate limiter
	 * @see Limiter
	 */
	public Limiter getLimiter() {
		return limiter;
	}

	/**
	 * Obtain permit for messages/bytes chunk. This call may block to satisfy max limits.
	 * 
	 * @param msgs
	 *            message count
	 * @param bytes
	 *            byte count
	 * @return true if permit obtained, false otherwise
	 */
	public boolean obtain(int msgs, int bytes) {
		if (timeout < BLOCK_NONE) {
			limiter.obtain(msgs, bytes);
			return true;
		} else {
			return limiter.tryObtain(msgs, bytes, timeout, unit);
		}
	}
}
