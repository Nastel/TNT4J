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

package com.jkoolcloud.tnt4j.uuid;

/**
 * This class allows developer to set and obtain actual {@link UUIDFactory} implementation
 * instance. Developers may create their own and set it globally using {@link #setDefaultUUIDFactory(UUIDFactory)}}
 * 
 * @see UUIDFactory
 * @version $Revision: 1 $
 */
public class DefaultUUIDFactory {
	private static UUIDFactory factory = new JUGFactoryImpl();
	
	private DefaultUUIDFactory() {
	}
	
	/**
	 * Set a global default UUID factory implementation
	 * 
	 * @param uuidf UUID factory instance
	 * @return {@link UUIDFactory} instance
	 */
	public static UUIDFactory setDefaultUUIDFactory(UUIDFactory uuidf) {
		factory = uuidf;
		return factory;
	}
	
	/**
	 * Obtain a default UUID factory
	 * 
	 * @return {@link UUIDFactory} instance
	 */
	public static UUIDFactory getInstance() {
		return factory;
	}	
}
