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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.nastel.jkool.tnt4j.core.ActivityListener;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.Source;
import com.nastel.jkool.tnt4j.dump.DumpSinkFactory;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.repository.TokenRepository;
import com.nastel.jkool.tnt4j.selector.TrackingSelector;
import com.nastel.jkool.tnt4j.sink.DefaultEventSinkFactory;
import com.nastel.jkool.tnt4j.sink.EventSink;
import com.nastel.jkool.tnt4j.sink.EventSinkFactory;
import com.nastel.jkool.tnt4j.sink.SinkEventFilter;
import com.nastel.jkool.tnt4j.sink.SinkLogEventListener;
import com.nastel.jkool.tnt4j.tracker.TrackerFactory;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * This class consolidates all configuration for creating <code>Tracker</code> instances using a configuration file.
 * Developers should use this class and override default configuration with user defined elements. Configuration is
 * loaded from a file specified by <code>tnt4j.config</code> property which set to <code>tnt4j.properties</code> by
 * default. Configuration specifies factories, formatters, token repositories and other elements required by the
 * framework using JSON like convention.
 * </p>
 * <p>
 * Below is a example of the sample configuration file (tnt4j.properties):
 * </p>
 * 
 * <pre>
 * {@code
 * ; source: * designates all sources, which is used as default for non matching sources
 * {
 * source: *
 * event.sink.factory: com.nastel.jkool.tnt4j.logger.Log4JEventSinkFactory
 * event.formatter: com.nastel.jkool.tnt4j.format.DefaultFormatter
 * token.repository: com.nastel.jkool.tnt4j.repository.FileTokenRepository
 * tracking.selector: com.nastel.jkool.tnt4j.selector.DefaultTrackingSelector
 * activity.listener: com.nastel.jkool.tnt4j.examples.MyActivityHandler
 * }
 * {
 * source: com
 * event.sink.factory: com.nastel.jkool.tnt4j.logger.Log4JEventSinkFactory
 * event.formatter: com.nastel.jkool.tnt4j.format.DefaultFormatter
 * token.repository: com.nastel.jkool.tnt4j.repository.FileTokenRepository
 * tracking.selector: com.nastel.jkool.tnt4j.selector.DefaultTrackingSelector
 * activity.listener: com.nastel.jkool.tnt4j.examples.MyActivityHandler
 * }
 * ;Stanza used for sources that start with com.nastel
 * {
 * source: com.nastel
 * tracker.factory: com.nastel.jkool.tnt4j.tracker.DefaultTrackerFactory
 * dump.sink.factory: com.nastel.jkool.tnt4j.dump.DefaultDumpSinkFactory
 * event.sink.factory: com.nastel.jkool.tnt4j.sink.SocketEventSinkFactory
 * event.sink.factory.Host: localhost
 * event.sink.factory.Port: 6408
 * event.formatter: com.nastel.jkool.tnt4j.format.JSONFormatter
 * tracking.selector: com.nastel.jkool.tnt4j.selector.DefaultTrackingSelector
 * tracking.selector.Repository: com.nastel.jkool.tnt4j.repository.FileTokenRepository
 * }
 * {
 * source: org
 * event.sink.factory: com.nastel.jkool.tnt4j.sink.SocketEventSinkFactory
 * event.formatter: com.nastel.jkool.tnt4j.format.JSONFormatter
 * token.repository: com.nastel.jkool.tnt4j.repository.FileTokenRepository
 * tracking.selector: com.nastel.jkool.tnt4j.selector.DefaultTrackingSelector
 * }
 * }
 * </pre>
 * 
 * Below is an example of how to use <code>TrackerConfigStore</code> when registering with the framework.
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
	private static final EventSink logger = DefaultEventSinkFactory.defaultEventSink(TrackerConfigStore.class.getName());
	private static final String SOURCE_KEY = "source";
	private String configFile = null;

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a file specified by
	 * <code>tnt4j.config</code> property.
	 * 
	 * @param source
	 *            name of the source instance associated with the configuration
	 */
	protected TrackerConfigStore(String source) {
		this(new Source(source));
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a file specified by
	 * <code>tnt4j.config</code> property if fileName is null.
	 * 
	 * @param source
	 *            name of the source instance associated with the configuration
	 * @param fileName
	 *            configuration file name
	 */
	protected TrackerConfigStore(String source, String fileName) {
		this(new Source(source), fileName);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a file specified by
	 * <code>tnt4j.config</code> property.
	 * 
	 * @param source
	 *            source instance associated with the configuration
	 */
	protected TrackerConfigStore(Source source) {
		this(source, null);
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
		configFile = fileName == null ? System.getProperty("tnt4j.config", "tnt4j.properties") : fileName;
		Map<String, Properties> configMap = loadConfiguration(configFile);
		loadConfigProps(configMap);
	}

	private Object createConfigurableObject(String classProp, String prefix) {
		try {
			return Utils.createConfigurableObject(classProp, prefix, getProperties());
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, "Failed to create configurable instance {class: " 
					+ classProp + ", prefix: " + prefix + "}", e);
		}
		return null;
	}

	private void loadConfigProps(Map<String, Properties> map) {
		setProperties(loadProperties(map));
		if (props != null) {
			if (logger.isSet(OpLevel.DEBUG)) {
				logger.log(OpLevel.DEBUG, "Loading configuration {source: " + this.getSource().getName() + ", properties: " + props + "}");
			}
			setDefaultEventSinkFactory((EventSinkFactory) createConfigurableObject("default.event.sink.factory", "default.event.sink.factory."));
			setTrackerFactory((TrackerFactory) createConfigurableObject("tracker.factory", "tracker.factory."));
			setEventSinkFactory((EventSinkFactory) createConfigurableObject("event.sink.factory", "event.sink.factory."));
			setEventFormatter((EventFormatter) createConfigurableObject("event.formatter", "event.formatter."));
			setTokenRepository((TokenRepository) createConfigurableObject("tracking.selector.Repository", "tracking.selector.Repository."));
			setTrackingSelector((TrackingSelector) createConfigurableObject("tracking.selector", "tracking.selector."));
			setDumpSinkFactory((DumpSinkFactory) createConfigurableObject("dump.sink.factory", "dump.sink.factory."));
			setActivityListener((ActivityListener) createConfigurableObject("activity.listener", "activity.listener."));
			setSinkLogEventListener((SinkLogEventListener) createConfigurableObject("sink.log.listener", "sink.log.listener."));
			setSinkEventFilter((SinkEventFilter) createConfigurableObject("sink.event.filter", "sink.event.filter."));
		}
	}

	private Properties loadProperties(Map<String, Properties> map) {
		Properties defaultSet = null;
		for (Entry<String, Properties> entry : map.entrySet()) {
			if (entry.getKey().equals("*")) {
				defaultSet = entry.getValue();
				continue;
			}
			int idx = this.getSource().getName().indexOf(entry.getKey());
			if (idx >= 0) {
				return entry.getValue();
			}
		}
		return defaultSet;
	}

	private Map<String, Properties> loadConfiguration(String configFile) {
		Map<String, Properties> map = null;
		try {
			map = loadConfigFile(configFile);
			logger.log(OpLevel.DEBUG, "Loaded configuration {file: " + configFile + ", config.size: " + map.size() + "}");
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, "Unable to load configuration: file=" + configFile, e);
		}
		return map;
	}

	private static Map<String, Properties> loadConfigFile(String fileName) throws IOException {
		LinkedHashMap<String, Properties> map = new LinkedHashMap<String, Properties>(111);
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			Properties props = null;
			do {
				props = readStanza(reader);
				String key = props.getProperty(SOURCE_KEY);
				if (key != null) {
					map.put(key, props);
				}
			} while (props.size() > 0);
		} finally {
			Utils.close(reader);
		}
		return map;
	}

	private static Properties readStanza(BufferedReader reader) throws IOException {
		String line = null;
		Properties props = new Properties();
		do {
			line = reader.readLine();
			if (line != null) {
				line = line.trim();
				if ((line.length() == 0) 
						|| line.startsWith("{") 
						|| line.startsWith(";") 
						|| line.startsWith("#") 
						|| line.startsWith("//") 
						|| line.endsWith("}")) {
					continue;
				}
				int sepIndex = line.indexOf(":");
				if (sepIndex <= 0) {
					logger.log(OpLevel.WARNING, "Skipping invalid entry=" + Utils.quote(line));
					continue;
				}
				String key = line.substring(0, sepIndex).trim();
				String value = line.substring(sepIndex+1).trim();
				props.setProperty(key, value);
			}
		} while (line != null && !line.endsWith("}"));
		return props;
	}
}
