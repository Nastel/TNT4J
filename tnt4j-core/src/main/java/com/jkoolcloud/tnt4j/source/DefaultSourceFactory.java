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
package com.jkoolcloud.tnt4j.source;

/**
 * This class allows developer to set and obtain actual {@link SourceFactory} implementation
 * instance. Developers may create their own and set it globally using {@link #setDefaultConfigFactory(SourceFactory)}
 * 
 * @see SourceFactory
 * @version $Revision: 1 $
 */

public class DefaultSourceFactory {
	private static SourceFactory factory = new SourceFactoryImpl();
	
	private DefaultSourceFactory() {
	}
	
	/**
	 * Set a global default source factory implementation
	 * 
	 * @param fac source factory instance
	 * @return {@link SourceFactory} instance
	 */
	public static SourceFactory setDefaultConfigFactory(SourceFactory fac) {
		factory = fac;
		return factory;
	}
	
	/**
	 * Obtain a default source factory
	 * 
	 * @return default {@link SourceFactory} instance
	 */
	public static SourceFactory getInstance() {
		return factory;
	}	
}
