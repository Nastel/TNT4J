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
package com.jkoolcloud.tnt4j.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.jkoolcloud.tnt4j.dump.DumpSinkFactory;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.source.SourceFactory;
import com.jkoolcloud.tnt4j.core.ActivityListener;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.repository.TokenRepository;
import com.jkoolcloud.tnt4j.selector.TrackingSelector;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.sink.SinkEventFilter;
import com.jkoolcloud.tnt4j.sink.SinkLogEventListener;
import com.jkoolcloud.tnt4j.source.SourceType;
import com.jkoolcloud.tnt4j.tracker.TrackerFactory;
import com.jkoolcloud.tnt4j.utils.Utils;
import com.jkoolcloud.tnt4j.uuid.SignFactory;
import com.jkoolcloud.tnt4j.uuid.UUIDFactory;

/**
 * <p>
 * This class consolidates all configuration for {@link TrackerFactory} using a configuration file.
 * Developers should use this class and override default configuration with user defined elements. Configuration is
 * loaded from a file specified by {@code tnt4j.config} property which set to {@code tnt4j.properties} by
 * default. Configuration specifies factories, formatters, token repositories and other elements required by the
 * framework using JSON like convention.
 * </p>
 * <p>
 * Below is a example of the sample configuration file (tnt4j.properties):
 * </p>
 *
 * <pre>
 * <code>
 * ; source: * designates all sources, which is used as default for non matching sources
 * {
 * source: *
 * event.sink.factory: com.jkoolcloud.tnt4j.sink.impl.slf4j.SLF4JEventSinkFactory
 * event.source.factory: com.jkoolcloud.tnt4j.core.DefaultSourceFactory
 * event.formatter: com.jkoolcloud.tnt4j.format.DefaultFormatter
 * token.repository: com.jkoolcloud.tnt4j.repository.FileTokenRepository
 * tracking.selector: com.jkoolcloud.tnt4j.selector.DefaultTrackingSelector
 * activity.listener: com.jkoolcloud.tnt4j.examples.MyActivityHandler
 * }
 * {
 * source: com
 * event.sink.factory: com.jkoolcloud.tnt4j.sink.impl.slf4j.SLF4JEventSinkFactory
 * event.formatter: com.jkoolcloud.tnt4j.format.DefaultFormatter
 * token.repository: com.jkoolcloud.tnt4j.repository.FileTokenRepository
 * tracking.selector: com.jkoolcloud.tnt4j.selector.DefaultTrackingSelector
 * activity.listener: com.jkoolcloud.tnt4j.examples.MyActivityHandler
 * }
 * ;Stanza used for sources that start with com.jkoolcloud
 * {
 * source: com.jkoolcloud
 * tracker.factory: com.jkoolcloud.tnt4j.tracker.DefaultTrackerFactory
 * dump.sink.factory: com.jkoolcloud.tnt4j.dump.DefaultDumpSinkFactory
 * event.sink.factory: com.jkoolcloud.tnt4j.sink.impl.SocketEventSinkFactory
 * event.sink.factory.Host: localhost
 * event.sink.factory.Port: 6408
 * event.formatter: com.jkoolcloud.tnt4j.format.JSONFormatter
 * tracking.selector: com.jkoolcloud.tnt4j.selector.DefaultTrackingSelector
 * tracking.selector.Repository: com.jkoolcloud.tnt4j.repository.FileTokenRepository
 * }
 * ; define source based on configuration from another source defined above
 * {
 * source: org
 * like: com.jkoolcloud
 * enabled: true
 * }
 * </code>
 * </pre>
 *
 * Below is an example of how to use {@link TrackerConfigStore} when registering with the framework.
 *
 * <pre>
 * {@code
 * TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(source);
 * TrackingLogger.register(config.build());
 * ...
 * }
 * </pre>
 *
 * @see TokenRepository
 * @see TrackingSelector
 * @see EventFormatter
 * @see EventSinkFactory
 *
 * @version $Revision: 11 $
 *
 */

public class TrackerConfigStore extends TrackerConfig {
	private static final EventSink logger = DefaultEventSinkFactory.defaultEventSink(TrackerConfigStore.class);

	public static final String TNT4J_PROPERTIES_KEY = "tnt4j.config";
	public static final String TNT4J_PROPERTIES = "tnt4j.properties";

	private static final String DEFAULT_SOURCE = "*";
	private static final String SOURCE_KEY = "source";
	private static final String ENABLED_KEY = "enabled";
	private static final String LIKE_KEY = "like";

	private String configFile = null;

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a file specified by
	 * {@code tnt4j.config} property.
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 */
	protected TrackerConfigStore(String source) {
		this(source, SourceType.APPL);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a file specified by
	 * {@code tnt4j.config} property.
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 * @param type
	 *            source type
	 */
	protected TrackerConfigStore(String source, SourceType type) {
		this(source, type, (String) null);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a file specified by
	 * {@code tnt4j.config} property if fileName is null.
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 * @param type
	 *            type of the source instance
	 * @param fileName
	 *            configuration file name
	 */
	protected TrackerConfigStore(String source, SourceType type, String fileName) {
		super(source, type);
		initConfig(fileName);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a
	 * key/Properties map.
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 * @param type
	 *            type of the source instance
	 * @param configMap
	 *            configuration map containing source/properties configuration
	 */
	protected TrackerConfigStore(String source, SourceType type, Map<String, Properties> configMap) {
		super(source, type);
		loadConfigProps(configMap);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a
	 * key/Properties map.
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 * @param configMap
	 *            configuration map containing source/properties configuration
	 */
	protected TrackerConfigStore(String source, Map<String, Properties> configMap) {
		super(source, SourceType.APPL);
		loadConfigProps(configMap);
	}

	/**
	 * Create an default configuration with a specific source name and a given file name;
	 *
	 * @param source
	 *            source instance associated with the configuration
	 * @param configMap
	 *            configuration map containing source/properties configuration
	 */
	protected TrackerConfigStore(Source source, Map<String, Properties> configMap) {
		super(source);
		loadConfigProps(configMap);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a file specified by
	 * {@code tnt4j.config} property.
	 *
	 * @param source
	 *            source instance associated with the configuration
	 */
	protected TrackerConfigStore(Source source) {
		this(source, (String) null);
	}

	/**
	 * Create an default configuration with a specific source name and a given file name;
	 *
	 * @param source
	 *            source instance associated with the configuration
	 * @param fileName
	 *            configuration file name
	 */
	protected TrackerConfigStore(Source source, String fileName) {
		super(source);
		initConfig(fileName);
	}

	private void initConfig(String fileName) {
		configFile = fileName == null ? System.getProperty(TNT4J_PROPERTIES_KEY, TNT4J_PROPERTIES) : fileName;
		setProperty(TNT4J_PROPERTIES_KEY, configFile);
		Map<String, Properties> configMap = loadConfiguration(configFile);
		loadConfigProps(configMap);
	}

	private Object createConfigurableObject(String classProp, String prefix) {
		Properties props = getProperties();
		try {
			return Utils.createConfigurableObject(classProp, prefix, props);
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, "Failed to create configurable instance class={0}, property={1}, prefix={2}", props.get(classProp), classProp, prefix, e);
		}
		return null;
	}

	private void loadConfigProps(Map<String, Properties> map) {
		setProperties(loadProperties(map));
		applyProperties();
	}

	/**
	 * Applies properties defined configuration.
	 */
	public void applyProperties() {
		if (props != null) {
			if (logger.isSet(OpLevel.DEBUG)) {
				logger.log(OpLevel.DEBUG, "Loaded properties source={0}, tid={1}, properties={2}", srcName, Thread.currentThread().getId(), props);
			}
			setUUIDFactory((UUIDFactory) createConfigurableObject("uuid.factory", "uuid.factory."));
			setSignFactory((SignFactory) createConfigurableObject("sign.factory", "sign.factory."));
			setDefaultEventSinkFactory((EventSinkFactory) createConfigurableObject("default.event.sink.factory", "default.event.sink.factory."));
			setSourceFactory((SourceFactory) createConfigurableObject("source.factory", "source.factory."));
			setTrackerFactory((TrackerFactory) createConfigurableObject("tracker.factory", "tracker.factory."));
			setEventSinkFactory((EventSinkFactory) createConfigurableObject("event.sink.factory", "event.sink.factory."));
			setEventFormatter((EventFormatter) createConfigurableObject("event.formatter", "event.formatter."));
			setTrackingSelector((TrackingSelector) createConfigurableObject("tracking.selector", "tracking.selector."));
			setDumpSinkFactory((DumpSinkFactory) createConfigurableObject("dump.sink.factory", "dump.sink.factory."));
			setActivityListener((ActivityListener) createConfigurableObject("activity.listener", "activity.listener."));
			setSinkLogEventListener((SinkLogEventListener) createConfigurableObject("sink.log.listener", "sink.log.listener."));
			setSinkEventFilter((SinkEventFilter) createConfigurableObject("sink.event.filter", "sink.event.filter."));
		}
	}

	private Properties loadProperties(Map<String, Properties> map) {
		int maxKeyLen = 0;
		Properties selectedSet = null;
		if (map == null) return selectedSet;
		for (Entry<String, Properties> entry : map.entrySet()) {
			if (entry.getKey().equals(DEFAULT_SOURCE)) {
				selectedSet = entry.getValue();
				continue;
			}
			// find the best match (longest string match)
			String configKey = entry.getKey();
			boolean match = this.srcName.indexOf(configKey) >= 0;
			if (match && configKey.length() > maxKeyLen) {
				maxKeyLen = configKey.length();
				selectedSet = entry.getValue();
			}
		}
		return selectedSet;
	}

	private Map<String, Properties> loadConfiguration(String configFile) {
		Map<String, Properties> map = null;
		try {
			map = loadConfigResource(configFile);
			logger.log(OpLevel.DEBUG, "Loaded configuration source={0}, file={1}, config.size={2}, tid={3}",
						srcName, configFile, map.size(), Thread.currentThread().getId());
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, "Unable to load configuration: source={0}, file={1}", srcName, configFile, e);
		}
		return map;
	}

	private Map<String, Properties> loadConfigResource(String fileName) throws IOException {
		LinkedHashMap<String, Properties> map = new LinkedHashMap<String, Properties>(111);
		BufferedReader reader = null;
		try {
			reader = getConfigReader(fileName);
			Properties config = null;
			do {
				config = readStanza(reader);
				String key = config.getProperty(SOURCE_KEY);
				String like = config.getProperty(LIKE_KEY);
				String enabled = config.getProperty(ENABLED_KEY);
				if (enabled != null && enabled.equalsIgnoreCase("true")) {
					logger.log(OpLevel.WARNING,
							"Disabling properties for source={0}, like={1}, enabled={2}", key, like, enabled);
					continue;
				}
				if (like != null) {
					config = mergeConfig(key, like, config, map);
				}
				if (key != null) {
					map.put(key, config);
				}
			} while (!config.isEmpty());
		} finally {
			Utils.close(reader);
		}
		return map;
	}

	private Properties mergeConfig(String key, String like, Properties config, Map<String, Properties> map) {
		Properties copyFrom = map.get(like);
		if (copyFrom == null) {
			copyFrom = map.get(DEFAULT_SOURCE);
			logger.log(OpLevel.WARNING, "Properties for source={0}, like={1} not found, assigning default set={2}",
					key, like, DEFAULT_SOURCE);
		}
		// merge properties from "like" model with original
		Properties merged = new Properties();
		merged.putAll(copyFrom);
		merged.putAll(config);
		return merged;
	}

	private BufferedReader getConfigReader(String fileName) throws IOException {
		BufferedReader reader = null;
		IOException exc = null;

		try {
			if (fileName != null) {
				reader = new BufferedReader(new FileReader(fileName));
				return reader;
			}
		} catch (IOException err) {
			exc = err;
		}

		String tnt4jResource = "/" + TNT4J_PROPERTIES;
		InputStream ins = getClass().getResourceAsStream(tnt4jResource);
		if (ins == null) {
			FileNotFoundException ioe = new FileNotFoundException("Resource '" + tnt4jResource + "' not found");
			ioe.initCause(exc);
			throw ioe;
		}
		reader = new BufferedReader(new InputStreamReader(ins));
		return reader;
	}

	private Properties readStanza(BufferedReader reader) throws IOException {
		String line;
		Properties props = new Properties();
		do {
			line = reader.readLine();
			if (line != null) {
				line = line.trim();
				if ((line.isEmpty())
						|| line.startsWith("{")
						|| line.startsWith(";")
						|| line.startsWith("#")
						|| line.startsWith("//")
						|| line.endsWith("}")) {
					continue;
				}
				int sepIndex = line.indexOf(":");
				if (sepIndex <= 0) {
					logger.log(OpLevel.WARNING, "Skipping invalid source={0}, file={1}, entry='{2}'", srcName, configFile, line);
					continue;
				}
				String key = line.substring(0, sepIndex).trim();
				String value = line.substring(sepIndex + 1).trim();
				props.setProperty(key, Utils.resolve(value, value));
			}
		} while (line != null && !line.endsWith("}"));
		return props;
	}
}
