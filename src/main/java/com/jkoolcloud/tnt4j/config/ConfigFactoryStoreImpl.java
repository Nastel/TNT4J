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

import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.source.SourceType;

/**
 * <p>
 * This class implements a default configuration factory backed by {@link TrackerConfigStore} class.
 * </p>
 * 
 * <pre>
 * {@code
 * TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(source);
 * TrackingLogger.getInstance(config.build());
 * ...
 * }
 * </pre>
 * 
 * @see TrackerConfig
 * @see TrackerConfigStore
 * 
 * @version $Revision: 3 $
 * 
 */
public class ConfigFactoryStoreImpl implements ConfigFactory {

	protected ConfigFactoryStoreImpl() {
	}

	@Override
	public TrackerConfig getConfig() {
		return new TrackerConfigStore(System.getProperty("tn4j.source.name", "com.default.App"));
	}

	@Override
	public TrackerConfig getConfig(String source) {
		return new TrackerConfigStore(source);
	}

	@Override
	public TrackerConfig getConfig(String source, SourceType type) {
		return new TrackerConfigStore(source, type);
	}

	@Override
	public TrackerConfig getConfig(Source source) {
		return new TrackerConfigStore(source);
	}

	@Override
	public TrackerConfig getConfig(String source, SourceType type, String configName) {
		return new TrackerConfigStore(source, type, configName);
	}

	@Override
	public TrackerConfig getConfig(String source, SourceType type, Reader configReader) {
		return new TrackerConfigStore(source, type, configReader);
	}

	@Override
	public TrackerConfig getConfig(Source source, String configName) {
		return new TrackerConfigStore(source, configName);
	}

	@Override
	public TrackerConfig getConfig(Class<?> clazz) {
		return getConfig(clazz.getName());
	}

	@Override
	public TrackerConfig getConfig(Class<?> clazz, SourceType type, String configName) {
		return getConfig(clazz.getName(), type, configName);
	}

	@Override
	public TrackerConfig getConfig(String source, SourceType type, Map<String, Properties> configMap) {
		return new TrackerConfigStore(source, type, configMap);
	}

	@Override
	public TrackerConfig getConfig(Class<?> clazz, SourceType type, Map<String, Properties> configMap) {
		return getConfig(clazz.getName(), type, configMap);
	}

	@Override
	public TrackerConfig getConfig(Class<?> clazz, SourceType type, Reader configReader) {
		return getConfig(clazz.getName(), type, configReader);
	}

	@Override
	public TrackerConfig getConfig(Source source, Map<String, Properties> configMap) {
		return new TrackerConfigStore(source, configMap);
	}
}
