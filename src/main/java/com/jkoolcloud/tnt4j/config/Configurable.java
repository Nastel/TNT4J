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
package com.jkoolcloud.tnt4j.config;

import java.util.Map;

/**
 * <p>
 * This interface defines classes that can be configured using properties.
 * </p>
 * 
 * @version $Revision: 1 $
 *
 */
public interface Configurable {
	/**
	 * Obtain current configuration settings
	 *
	 * @return current configuration settings
	 */
	Map<String, ?> getConfiguration();

	/**
	 * Apply given configuration settings
	 *
	 * @param settings
	 *            apply given settings as configuration (name, value pairs)
	 * @throws ConfigException
	 *             if error applying configuration settings
	 */
	void setConfiguration(Map<String, ?> settings) throws ConfigException;
}
