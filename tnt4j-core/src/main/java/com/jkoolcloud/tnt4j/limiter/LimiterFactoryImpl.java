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
package com.jkoolcloud.tnt4j.limiter;

/**
 * Default rate limiter factory implementation based on Google Guava Library
 * {@code https://code.google.com/p/guava-libraries/}
 *
 * @version $Revision: 1 $
 */
public class LimiterFactoryImpl implements LimiterFactory {

	@Override
	public Limiter newLimiter(double maxMps, double maxBps) {
		return new LimiterImpl(maxMps, maxBps, true);
	}

	@Override
	public Limiter newLimiter(double maxMps, double maxBps, boolean enabled) {
		return new LimiterImpl(maxMps, maxBps, enabled);
	}
}
