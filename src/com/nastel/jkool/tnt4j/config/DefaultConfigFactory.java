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
package com.nastel.jkool.tnt4j.config;


/**
 * <p>
 * This class provides a static way to get default configuration factory.
 * </p>
 *
 * <pre>
 * {@code
 * TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(source);
 * TrackingLogger.register(config.build());
 * ...
 * }
 * </pre>
 *
 * @see TrackerConfigStore
 * @see TrackerConfig
 *
 * @version $Revision: 5 $
 *
 */
public class DefaultConfigFactory  {
	private static ConfigFactory factory = new ConfigFactoryStoreImpl();

	private DefaultConfigFactory() {
	}

	/**
	 * Set a default configuration factory implementation
	 * @param fac configuration factory
	 *
	 * @return <code>ConfigFactory</code> instance
	 */
	public static ConfigFactory setDefaultConfigFactory(ConfigFactory fac) {
		factory = fac;
		return factory;
	}

	/**
	 * Obtain a default tracking configuration factory
	 *
	 * @return default <code>ConfigFactory</code> instance
	 */
	public static ConfigFactory getInstance() {
		return factory;
	}
}
