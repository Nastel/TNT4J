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

import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * This class represents an exception that occurs when an error occurs
 * when TNT4J objects are configured. 
 * </p>
 * 
 * @version $Revision: 1 $
 */

public class ConfigException extends Exception {
    private static final long serialVersionUID = 3062997853952792045L;
    
    private Map<?, ?> config;
    
	/**
	 * Create a configuration exception with a given message and
	 * configuration settings.
	 *
	 * @param msg error message
	 * @param settings configuration settings
	 */
   public ConfigException(String msg, Map<String, Object> settings) {
    	super(msg);
    	config = settings;
    }

	/**
	 * Create a configuration exception with a given message and
	 * configuration settings.
	 *
	 * @param msg error message
	 * @param settings configuration settings
	 */
    public ConfigException(String msg, Properties settings) {
    	super(msg);
    	config = settings;
    }

	/**
	 * Return configuration settings related to the exception
	 *
	 * @return configuration settings
	 */
    public Map<?, ?> getConfiguration() {
    	return config;
    }
}
