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

package com.jkoolcloud.tnt4j.uuid;

/**
 * This class allows developer to set and obtain actual {@link SignFactory} implementation instance. Developers may
 * create their own factory implementation and set it globally using {@link #setDefaultSignFactory(SignFactory)}}
 * 
 * @see SignFactory
 * @version $Revision: 1 $
 */
public class DefaultSignFactory {
	private static SignFactory factory = new NullSignFactoryImpl();

	private DefaultSignFactory() {
	}

	/**
	 * Set a global default signature factory implementation
	 * 
	 * @param sf
	 *            signature factory instance
	 * @return {@link SignFactory} instance
	 */
	public static SignFactory setDefaultSignFactory(SignFactory sf) {
		factory = sf;
		return factory;
	}

	/**
	 * Obtain a default signature factory
	 * 
	 * @return {@link SignFactory} instance
	 */
	public static SignFactory getInstance() {
		return factory;
	}
}
