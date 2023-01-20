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
package com.jkoolcloud.tnt4j.utils;

/**
 * This class generates microsecond precision current timestamp based on NTP.
 * <p>
 * Example: {@code Useconds.CURRENT.get();}
 */
public enum Useconds {
	CURRENT;

	private long startUsecs;
	private long startNanos;

	private Useconds() {
		sync();
	}

	/**
	 * Obtain NTP synchronized timestamp with microsecond precision and accuracy.
	 *
	 * @return synchronized microsecond timestamp
	 */
	public long get() {
		long microSeconds = (System.nanoTime() - this.startNanos) / 1000;
		return this.startUsecs + microSeconds;
	}

	/**
	 * Synchronized NTP millisecond clock and nanosecond/usec clocks to reduce clock drift and improve accuracy.
	 */
	public void sync() {
		this.startUsecs = TimeService.currentTimeUsecs();
		this.startNanos = System.nanoTime();
	}
}