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
package com.jkoolcloud.tnt4j.limiter;

/**
 * Factory interface to create instances of rate limiter implementations.
 *
 * @version $Revision: 1 $
 */
public interface LimiterFactory {
	/**
	 * Create a new limiter. 
	 * (equivalent to {@code newLimiter(maxMps, maxBps, true)})
	 * 
	 * @param maxMps maximum message/second rate (0 means unlimited)
	 * @param maxBps maximum bytes/second rate (0 means unlimited)
	 * @return new limiter instance
	 */
	Limiter newLimiter(double maxMps, double maxBps);

	/**
	 * Create a new limiter
	 * 
	 * @param maxMps maximum message/second rate (0 means unlimited)
	 * @param maxBps maximum bytes/second rate (0 means unlimited)
	 * @param enabled true to enable limiter, false otherwise
	 * @return new limiter instance
	 */
	Limiter newLimiter(double maxMps, double maxBps, boolean enabled);
}
