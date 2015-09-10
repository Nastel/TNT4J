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
 * This class allows developer to set and obtain actual {@link ThrottleFactory} implementation
 * instance. Developers may create their own and set it globally using {@code setDefaultThrottleFactory}
 * 
 * @see ThrottleFactory
 * @version $Revision: 1 $
 */
public class DefaultThrottleFactory {
	private static ThrottleFactory factory = new ThrottleFactoryImpl();
	
	private DefaultThrottleFactory() {
	}
	
	/**
	 * Set a global default throttle factory implementation
	 * 
	 * @param tfac throttle factory instance
	 * @return {@link ThrottleFactory} instance
	 */
	public static ThrottleFactory setDefaultThrottleFactory(ThrottleFactory tfac) {
		factory = tfac;
		return factory;
	}
	
	/**
	 * Obtain a default throttle factory
	 * 
	 * @return {@link ThrottleFactory} instance
	 */
	public static ThrottleFactory getInstance() {
		return factory;
	}	
}
