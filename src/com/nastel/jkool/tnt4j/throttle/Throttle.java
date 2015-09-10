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
package com.nastel.jkool.tnt4j.throttle;

/**
 * Implementation if this interface provide throttle measurement and control
 * based on message/second (MPS) and/or bytes/second. 
 *
 * @version $Revision: 1 $
 */
public interface Throttle {
	static final int UNLIMITED = 0;

	/**
	 * Get total number of times throttling was invoked with delay (ms)
	 *
	 * @return total number of times throttling was invoked with delay (ms)
	 */
	long getDelayCount();
	
	/**
	 * Get last time in (seconds) blocked to achieve msgs/byte rates
	 *
	 * @return number of seconds blocked to achieve msgs/byte rates
	 */
	double getLastDelayTime();
	
	/**
	 * Get total time (seconds) blocked to achieve msgs/byte rates
	 *
	 * @return total time (seconds) blocked to achieve msgs/byte rates
	 */
	double getTotalDelayTime();
	
	/**
	 * Get accumulated byte count since start or last reset
	 *
	 * @return accumulated byte count
	 */
	long getTotalBytes();
	
	/**
	 * Get accumulated message count since start or last reset
	 *
	 * @return Get accumulated byte count
	 */
	long getTotalMsgs();
	
	/**
	 * Get maximum allowed message/second rate
	 *
	 * @return maximum allowed message rate, 0 means unlimited
	 */
	long getMaxMPS();
	
	/**
	 * Get maximum allowed bytes/second rate
	 *
	 * @return maximum allowed byte rate, 0 means unlimited
	 */
	long getMaxBPS();

	/**
	 * Sets maximum limits (0 means unlimited)
	 * 
	 * @param maxMps maximum message/second rate
	 * @param maxBps maximum bytes/second rate
	 * @return same throttled instance
	 */
	Throttle setLimits(int maxMps, int maxBps);

	/**
	 * Get current message/second rate
	 *
	 * @return maximum allowed message rate
	 */
	long getCurrentMPS();

	/**
	 * Get current bytes/second rate
	 *
	 * @return maximum allowed bytes rate
	 */
	long getCurrentBPS();

	/**
	 * Get timestamp in (ms) since start/reset of this throttle
	 *
	 * @return timestamp in (ms) since start/reset of this throttle
	 */
	long getStartTime();

	/**
	 * Reset all measured counts and start counting
	 * from reset time.
	 *
	 * @return same throttled instance
	 */
	Throttle reset();
	
	/**
	 * Enable/disable throttling. {@code throttle()} will never block
	 * if throttle is disabled.
	 *
	 * @param flag true to enable throttle, false otherwise
	 * @return same throttled instance
	 */
	Throttle setEnabled(boolean flag);

	/**
	 * Determine if throttle control is enabled
	 *
	 * @return true if enabled, false otherwise
	 */
	boolean isThrottled();
	
	/**
	 * Called on every message/byte chunk received.
	 * This call will block to satisfy max throttle limits.
	 *
	 * @return time spend to enforce the rates in seconds.
	 */
	double throttle(int msgCount, int byteCount);
}
