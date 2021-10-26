/*
 * Copyright 2014-2021 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.sink.impl;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * This class implements a delayed element which a specific expiration time.
 *
 *
 * @version $Revision: 1 $
 */
public class DelayedElement<T> implements Delayed {
	private T elm;
	private long expiryTime;

	/**
	 * Create a delayed element wrapper
	 * 
	 * @param element
	 *            object instance
	 * @param delay
	 *            time in milliseconds
	 */
	public DelayedElement(T element, long delay) {
		this(element, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * Create a delayed element wrapper
	 * 
	 * @param element
	 *            object instance
	 * @param delay
	 *            time in milliseconds
	 * @param unit
	 *            time unit for delay
	 */
	public DelayedElement(T element, long delay, TimeUnit unit) {
		this.elm = element;
		this.expiryTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay, unit);
	}

	/**
	 * Obtain element instance
	 * 
	 * @return element instance
	 */
	public T getElement() {
		return elm;
	}

	@Override
	public long getDelay(TimeUnit timeUnit) {
		long diff = expiryTime - System.currentTimeMillis();
		return timeUnit.convert(diff, TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed o) {
		return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
	}

	@Override
	public String toString() {
		return elm + ":" + expiryTime;
	}
}