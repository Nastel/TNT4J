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
package com.jkoolcloud.tnt4j.config;

import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.source.SourceType;

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
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig();

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source name
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(String source);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source name
	 * @param type
	 *            source type
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(String source, SourceType type);

	/**
	 * Create a default tracking configuration based on a given class
	 * 
	 * @param clazz
	 *            class for which to obtain configuration
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(Class<?> clazz);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(Source source);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source name
	 * @param type
	 *            source type
	 * @param configMap
	 *            configuration map containing source/properties configuration
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(String source, SourceType type, Map<String, Properties> configMap);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source name
	 * @param type
	 *            source type
	 * @param configName
	 *            configuration name where configuration elements are read from (e.g. filename)
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(String source, SourceType type, String configName);

	/**
	 * Create a default tracking configuration
	 *
	 * @param source
	 *            user defined source name
	 * @param type
	 *            source type
	 * @param configReader
	 *            configuration reader configuration elements to read from (e.g. String, byte[])
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(String source, SourceType type, Reader configReader);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param clazz
	 *            class for which to obtain configuration
	 * @param type
	 *            source type
	 * @param configName
	 *            configuration name where configuration elements are read from (e.g. filename)
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(Class<?> clazz, SourceType type, String configName);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param clazz
	 *            class for which to obtain configuration
	 * @param type
	 *            source type
	 * @param configMap
	 *            configuration map containing source/properties configuration
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(Class<?> clazz, SourceType type, Map<String, Properties> configMap);

	/**
	 * Create a default tracking configuration
	 *
	 * @param clazz
	 *            class for which to obtain configuration
	 * @param type
	 *            source type
	 * @param configReader
	 *            configuration reader configuration elements to read from (e.g. String, byte[])
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(Class<?> clazz, SourceType type, Reader configReader);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source
	 * @param configMap
	 *            configuration map containing source/properties configuration
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(Source source, Map<String, Properties> configMap);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source
	 * @param configName
	 *            configuration name where configuration elements are read from (e.g. filename)
	 * @see TrackerConfig
	 * @return new {@link TrackerConfig} instance with default values and factories
	 */
	TrackerConfig getConfig(Source source, String configName);
}
