/*
 * Copyright 2014-2022 JKOOL, LLC.
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
 * This class allows developer to set and obtain actual {@link LimiterFactory} implementation instance. Developers may
 * create their own and set it globally using {@code setDefaultLimiterFactory}
 * 
 * @see LimiterFactory
 * @version $Revision: 1 $
 */
public class DefaultLimiterFactory {
	private static LimiterFactory factory = new LimiterFactoryImpl();

	private DefaultLimiterFactory() {
	}

	/**
	 * Set a global default throttle factory implementation
	 * 
	 * @param fac
	 *            limiter factory instance
	 * @return {@link LimiterFactory} instance
	 */
	public static LimiterFactory setDefaultLimiterFactory(LimiterFactory fac) {
		factory = fac;
		return factory;
	}

	/**
	 * Obtain a default limiter factory
	 * 
	 * @return {@link LimiterFactory} instance
	 */
	public static LimiterFactory getInstance() {
		return factory;
	}
}
