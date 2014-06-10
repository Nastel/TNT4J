/*
 * Copyright 2014 Nastel Technologies, Inc.
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

import com.nastel.jkool.tnt4j.source.Source;

/**
 * <p>
 * This interfaces defines configuration factory that allows creation of underlying configuration objects used to
 * configure the framework.
 * </p>
 * 
 * 
 * @see TrackerConfig
 * 
 * @version $Revision: 5 $
 * 
 */
public interface ConfigFactory {
	/**
	 * Create a default tracking configuration
	 * 
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig();

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source name
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(String source);

	/**
	 * Create a default tracking configuration based on a given class
	 * 
	 * @param clazz
	 *            class for which to obtain configuration
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(Class<?> clazz);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(Source source);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source name
	 * @param configName
	 *            configuration name where configuration elements are read from (e.g. filename)
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(String source, String configName);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param clazz
	 *            class for which to obtain configuration
	 * @param configName
	 *            configuration name where configuration elements are read from (e.g. filename)
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(Class<?> clazz, String configName);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source
	 * @param configName
	 *            configuration name where configuration elements are read from (e.g. filename)
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(Source source, String configName);
}
