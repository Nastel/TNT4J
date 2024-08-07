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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.core.ActivityListener;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.dump.DumpSinkFactory;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.locator.GeoLocator;
import com.jkoolcloud.tnt4j.selector.TrackingSelector;
import com.jkoolcloud.tnt4j.sink.*;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.source.SourceFactory;
import com.jkoolcloud.tnt4j.source.SourceType;
import com.jkoolcloud.tnt4j.tracker.TrackerFactory;
import com.jkoolcloud.tnt4j.utils.Utils;
import com.jkoolcloud.tnt4j.uuid.SignFactory;
import com.jkoolcloud.tnt4j.uuid.UUIDFactory;

/**
 * <p>
 * This class consolidates all configuration for {@link TrackerFactory} using a configuration file. Developers should
 * use this class and override default configuration with user defined elements. Configuration is loaded from a file or
 * string specified by {@code tnt4j.config} property which set to {@code tnt4j.properties} by default. Configuration
 * specifies factories, formatters, token repositories and other elements required by the framework using JSON like
 * convention.
 * </p>
 * <p>
 * Below is a example of the sample configuration file (tnt4j.properties):
 * </p>
 *
 * <pre>
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
 * @see com.jkoolcloud.tnt4j.repository.TokenRepository
 * @see TrackingSelector
 * @see EventFormatter
 * @see EventSinkFactory
 *
 * @version $Revision: 12 $
 */
public class TrackerConfigStore extends TrackerConfig {
	private static final EventSink logger = DefaultEventSinkFactory.defaultEventSink(TrackerConfigStore.class);

	public static final String CFG_LINE_PREFIX = "inline:";

	public static final String TNT4J_PROPERTIES_KEY = "tnt4j.config";
	public static final String TNT4J_PROPERTIES = "tnt4j.properties";
	public static final String TNT4J_PROPERTIES_PATH = "./config/";
	public static final String TNT4J_PROPERTIES_PATH_KEY = "tnt4j.config.path";

	private static final String DEFAULT_SOURCE = "*";
	private static final String SOURCE_KEY = "source";
	private static final String ENABLED_KEY = "enabled";
	private static final String LIKE_KEY = "like";
	private static final String IMPORT_KEY = "import";
	private static final String IMPORT_PATH = "import.path";

	private String configPath = System.getProperty(TNT4J_PROPERTIES_PATH_KEY);
	private String configFile = null;
	private Map<String, Properties> configMap = null;

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a file or string
	 * specified by {@code tnt4j.config} property.
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 */
	protected TrackerConfigStore(String source) {
		this(source, SourceType.APPL);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a file or string
	 * specified by {@code tnt4j.config} property.
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
	 * Create an default configuration with a specific source name. Configuration is loaded from a file or string
	 * specified by {@code tnt4j.config} property if fileName is {@code null}.
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
		initConfigExt(fileName);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a key/Properties map.
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 * @param type
	 *            type of the source instance
	 * @param cfgMap
	 *            configuration map containing source/properties configuration
	 */
	protected TrackerConfigStore(String source, SourceType type, Map<String, Properties> cfgMap) {
		super(source, type);
		loadConfigProps(cfgMap);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a key/Properties map.
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 * @param type
	 *            type of the source instance
	 * @param configReader
	 *            configuration reader to read source/properties configuration
	 */
	protected TrackerConfigStore(String source, SourceType type, Reader configReader) {
		super(source, type);
		initConfig(configReader);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a key/Properties map.
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 * @param cfgMap
	 *            configuration map containing source/properties configuration
	 */
	protected TrackerConfigStore(String source, Map<String, Properties> cfgMap) {
		super(source, SourceType.APPL);
		loadConfigProps(cfgMap);
	}

	/**
	 * Create an default configuration with a specific source name and a given file name.
	 *
	 * @param source
	 *            source instance associated with the configuration
	 * @param cfgMap
	 *            configuration map containing source/properties configuration
	 */
	protected TrackerConfigStore(Source source, Map<String, Properties> cfgMap) {
		super(source);
		loadConfigProps(cfgMap);
	}

	/**
	 * Create an default configuration with a specific source name. Configuration is loaded from a file or string
	 * specified by {@code tnt4j.config} property.
	 *
	 * @param source
	 *            source instance associated with the configuration
	 */
	protected TrackerConfigStore(Source source) {
		this(source, (String) null);
	}

	/**
	 * Create an default configuration with a specific source name and a given file name.
	 *
	 * @param source
	 *            source instance associated with the configuration
	 * @param fileName
	 *            configuration file name
	 */
	protected TrackerConfigStore(Source source, String fileName) {
		super(source);
		initConfigExt(fileName);
	}

	/**
	 * Get current configuration map for all sources.
	 *
	 * @return current configuration map
	 */
	public Map<String, Properties> getConfigMap() {
		return configMap;
	}

	/**
	 * Get current configuration for a specific source.
	 * 
	 * @param key
	 *            configuration key
	 * @return current configuration for a specific key (source)
	 */
	public Properties getConfig(Object key) {
		return configMap.get(key);
	}

	/**
	 * Get current configuration file based on a given path and file.
	 * 
	 * @param path
	 *            configuration path (folder)
	 * @param fileName
	 *            configuration file name
	 * @return configuration file name
	 *
	 * @throws IOException
	 *             if I/O error occurs while accessing configuration file
	 */
	protected String getConfigFromPath(String path, String fileName) throws IOException {
		String cfgPath = (path == null ? getConfigPath() : path);
		if (cfgPath == null) {
			return fileName;
		} else {
			return cfgPath.endsWith(File.separator) ? (cfgPath + fileName) : (cfgPath + File.separator + fileName);
		}
	}

	/**
	 * Get configuration files root path.
	 * <p>
	 * Configuration files root path is one of:
	 * <ul>
	 * <li>system property {@value #TNT4J_PROPERTIES_PATH_KEY} defined value</li>
	 * <li>if system property {@value #TNT4J_PROPERTIES_PATH_KEY} is not defined, then system property
	 * {@value #TNT4J_PROPERTIES_KEY} defined configuration file parent path is used</li>
	 * <li>if system property {@value #TNT4J_PROPERTIES_KEY} is not defined, {@value #TNT4J_PROPERTIES_PATH} path is
	 * used</li>
	 * </ul>
	 * 
	 * @return configuration files path
	 *
	 * @throws IOException
	 *             if I/O error occurs while resolving parent path
	 */
	protected String getConfigPath() throws IOException {
		if (configPath == null) {
			if (configFile != null) {
				configPath = Utils.getParentPath(configFile);
			} else {
				configPath = TNT4J_PROPERTIES_PATH;
			}
		}

		return configPath;
	}

	/**
	 * Initialize configuration based on a given file and extended attributes.
	 * 
	 * @param fileName
	 *            configuration file name
	 */
	private void initConfigExt(String fileName) {
		if (StringUtils.isEmpty(fileName)) {
			String cfgData = System.getProperty(TNT4J_PROPERTIES_KEY, TNT4J_PROPERTIES);
			if (cfgData.startsWith(CFG_LINE_PREFIX)) {
				// must be not a file path but configuration string itself
				initConfig(new StringReader(cfgData.substring(CFG_LINE_PREFIX.length())));
			} else {
				initConfig(cfgData);
			}
		} else {
			initConfig(fileName);
		}
	}

	/**
	 * Initialize configuration based on a given file.
	 * 
	 * @param fileName
	 *            configuration file name
	 */
	private void initConfig(String fileName) {
		configFile = fileName == null ? System.getProperty(TNT4J_PROPERTIES_KEY, TNT4J_PROPERTIES) : fileName;
		setProperty(TNT4J_PROPERTIES_KEY, configFile);
		configMap = loadConfiguration(configFile);
		loadConfigProps(configMap);
	}

	/**
	 * Reads configuration data from provided reader into and applies new configuration values.
	 *
	 * @param configReader
	 *            configuration data reader
	 */
	public void initConfig(Reader configReader) {
		configMap = loadConfiguration(configReader);
		loadConfigProps(configMap);
	}

	/**
	 * Create a configurable object based on class and prefix.
	 *
	 * @param classProp
	 *            class implementation key
	 * @param prefix
	 *            prefix for configuring class implementation
	 */
	private Object createConfigurableObject(String classProp, String prefix) {
		Properties props = getProperties();
		try {
			return Utils.createConfigurableObject(classProp, prefix, props);
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR,
					"Failed to create configurable instance class={}, property={}, prefix={}, props={}",
					props.get(classProp), classProp, prefix, props, e);
		}
		return null;
	}

	/**
	 * Load and apply configuration properties.
	 *
	 * @param map
	 *            map of keys and associated properties
	 */
	private void loadConfigProps(Map<String, Properties> map) {
		setProperties(loadProperties(map));
		applyProperties();
	}

	/**
	 * Applies properties defined configuration.
	 */
	public void applyProperties() {
		if (props != null) {
			logger.log(OpLevel.DEBUG, "Loading properties source={}, tid={}, properties.size={}", srcName,
					Thread.currentThread().getId(), props.size());
			setUUIDFactory((UUIDFactory) createConfigurableObject("uuid.factory", "uuid.factory."));
			setSignFactory((SignFactory) createConfigurableObject("sign.factory", "sign.factory."));
			setGeoLocator((GeoLocator) createConfigurableObject("geo.locator", "geo.locator."));
			setDefaultEventSinkFactory((EventSinkFactory) createConfigurableObject("default.event.sink.factory",
					"default.event.sink.factory."));
			setSourceFactory((SourceFactory) createConfigurableObject("source.factory", "source.factory."));
			setTrackerFactory((TrackerFactory) createConfigurableObject("tracker.factory", "tracker.factory."));
			setEventSinkFactory(
					(EventSinkFactory) createConfigurableObject("event.sink.factory", "event.sink.factory."));
			setEventFormatter((EventFormatter) createConfigurableObject("event.formatter", "event.formatter."));
			setTrackingSelector((TrackingSelector) createConfigurableObject("tracking.selector", "tracking.selector."));
			setDumpSinkFactory((DumpSinkFactory) createConfigurableObject("dump.sink.factory", "dump.sink.factory."));
			setActivityListener((ActivityListener) createConfigurableObject("activity.listener", "activity.listener."));
			setSinkLogEventListener(
					(SinkLogEventListener) createConfigurableObject("sink.log.listener", "sink.log.listener."));
			setSinkErrorListener(
					(SinkErrorListener) createConfigurableObject("sink.error.listener", "sink.error.listener."));
			setSinkEventFilter((SinkEventFilter) createConfigurableObject("sink.event.filter", "sink.event.filter."));
			logger.log(OpLevel.DEBUG, "Loaded properties source={}, tid={}, properties.size={}", srcName,
					Thread.currentThread().getId(), props.size());
		}
	}

	/**
	 * Load properties for its source.
	 *
	 * @param map
	 *            map of keys and associated properties
	 * @return properties associated with this configuration source
	 */
	private Properties loadProperties(Map<String, Properties> map) {
		int maxKeyLen = 0;
		Properties selectedSet = null;
		if (map == null) {
			return selectedSet;
		}
		for (Entry<String, Properties> entry : map.entrySet()) {
			if (entry.getKey().equals(DEFAULT_SOURCE)) {
				selectedSet = entry.getValue();
				continue;
			}
			// find the best match (longest string match)
			String configKey = entry.getKey();
			boolean match = this.srcName.contains(configKey);
			if (match && configKey.length() > maxKeyLen) {
				maxKeyLen = configKey.length();
				selectedSet = entry.getValue();
			}
		}
		return selectedSet;
	}

	/**
	 * Load configuration from a file.
	 *
	 * @param configFile
	 *            configuration file name or URL
	 * @return map of keys and associated properties
	 */
	private Map<String, Properties> loadConfiguration(String configFile) {
		Map<String, Properties> map = null;
		try {
			map = loadConfigResource(configFile);
			logger.log(OpLevel.DEBUG, "Loaded configuration source={}, file={}, config.size={}, tid={}", srcName,
					configFile, map.size(), Thread.currentThread().getId());
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, "Unable to load configuration: source={}, file={}", srcName, configFile, e);
			RuntimeException re = new RuntimeException(e);
			throw re;
		}
		return map;
	}

	/**
	 * Load configuration from a reader.
	 *
	 * @param reader
	 *            configuration reader
	 * @return map of keys and associated properties
	 */
	private Map<String, Properties> loadConfiguration(Reader reader) {
		Map<String, Properties> map = null;
		try {
			BufferedReader bfReader = (reader instanceof BufferedReader ? (BufferedReader) reader
					: new BufferedReader(reader));
			map = loadConfigResource(bfReader);
			logger.log(OpLevel.DEBUG, "Loaded configuration source={}, reader={}, config.size={}, tid={}", srcName,
					reader.getClass().getSimpleName(), map.size(), Thread.currentThread().getId());
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, "Failed to load configuration: source={}, reader={}", srcName,
					reader.getClass().getSimpleName(), e);
		}
		return map;
	}

	/**
	 * Load configuration resources from a file.
	 *
	 * @param fileName
	 *            configuration file name
	 * @return map of keys and associated properties
	 *
	 * @throws IOException
	 *             if I/O error occurs accessing configuration resource
	 */
	private Map<String, Properties> loadConfigResource(String fileName) throws IOException {
		return loadConfigResource(getConfigReader(fileName));
	}

	/**
	 * Load configuration resources from a reader.
	 *
	 * @param reader
	 *            configuration reader
	 * @return map of keys and associated properties
	 *
	 * @throws IOException
	 *             if I/O error occurs while reading configuration
	 */
	private Map<String, Properties> loadConfigResource(BufferedReader reader) throws IOException {
		Map<String, Properties> map = new LinkedHashMap<>(111);
		Properties config = null;
		do {
			config = readStanza(reader);
			String key = config.getProperty(SOURCE_KEY);
			String like = config.getProperty(LIKE_KEY);
			String include = config.getProperty(IMPORT_KEY);
			String includePath = config.getProperty(IMPORT_PATH);
			String enabled = config.getProperty(ENABLED_KEY);
			if (enabled != null && enabled.equalsIgnoreCase("true")) {
				logger.log(OpLevel.WARNING, "Disabling properties for source={}, like={}, enabled={}", key, like,
						enabled);
				continue;
			}
			if (include != null) {
				// parse and process a comma separated list of "import" elements
				String[] incList = include.split(",");
				for (String includeFile : incList) {
					includeFile = getConfigFromPath(includePath, includeFile);
					map.putAll(loadConfiguration(includeFile));
					logger.log(OpLevel.DEBUG,
							"Import configuration source={}, config.file={}, import.file={}, map.size={}, tid={}", key,
							configFile, includeFile, map.size(), Thread.currentThread().getId());
				}
			}
			if (like != null) {
				// parse and process a comma separated list of "like" elements
				String[] likeList = like.split(",");
				Properties mergedConfig = new Properties();
				for (String copyFromKey : likeList) {
					mergedConfig = copyConfig(key, copyFromKey, mergedConfig, map);
					logger.log(OpLevel.DEBUG,
							"Merge configuration source={}, config.file={}, like.source={}, config.size={}, tid={}",
							key, configFile, copyFromKey, mergedConfig.size(), Thread.currentThread().getId());
				}
				config = mergeConfig(key, config, mergedConfig);
			}
			if (key != null) {
				map.put(key, config);
			}
		} while (!config.isEmpty());
		return map;
	}

	/**
	 * Merge configurations.
	 *
	 * @param key
	 *            configuration source key
	 * @param toConfig
	 *            target configuration for merge
	 * @param fromConfig
	 *            source configuration for merge
	 * @return merged configuration
	 */
	private static Properties mergeConfig(String key, Properties toConfig, Properties fromConfig) {
		Properties merged = new Properties();
		merged.putAll(fromConfig);
		merged.putAll(toConfig);
		return merged;
	}

	/**
	 * Copy configurations.
	 *
	 * @param key
	 *            configuration source key
	 * @param like
	 *            source of configuration to copy from
	 * @param toConfig
	 *            target configuration for merge
	 * @param fromMap
	 *            source configuration for merge
	 * @return merged configuration
	 */
	private static Properties copyConfig(String key, String like, Properties toConfig,
			Map<String, Properties> fromMap) {
		Properties copyFrom = fromMap.get(like);
		if (copyFrom == null) {
			copyFrom = fromMap.get(DEFAULT_SOURCE);
			logger.log(OpLevel.WARNING, "Properties for source={}, like={} not found, assigning default set={}", key,
					like, DEFAULT_SOURCE);
			if (copyFrom == null) {
				throw new RuntimeException("Missing properties for source=" + key + ", like=" + like);
			}
		}
		// copy properties from "like" model with original
		toConfig.putAll(copyFrom);
		return toConfig;
	}

	/**
	 * Get reader for a specific URL.
	 *
	 * @param url
	 *            configuration URL
	 * @return reader associated with given URL
	 *
	 * @throws IOException
	 *             if I/O error occurs accessing configuration file
	 */
	private static BufferedReader getReaderFromURL(String url) throws IOException {
		Reader rdr = null;
		InputStream ins = null;
		try {
			URL cfgResource = new URL(url);
			try {
				ins = cfgResource.openStream();
			} catch (IOException ioe) {
				ins = getResourceAsStream(getName(cfgResource.getPath()));
			}
		} catch (MalformedURLException mue) {
			try {
				rdr = new FileReader(url);
			} catch (FileNotFoundException fnfe) {
				ins = getResourceAsStream(getName(url));
			}
		}

		if (rdr == null) {
			if (ins == null) {
				throw new FileNotFoundException("Configuration resource '" + url + "' not found");
			}
			rdr = new InputStreamReader(ins);
		}
		return new BufferedReader(rdr);
	}

	private static String getName(String pStr) {
		Path p = Paths.get(pStr);
		return p.getFileName().toString();
	}

	/**
	 * Returns an {@link java.io.InputStream} for reading the specified configuration resource.
	 *
	 * @param resName
	 *            configuration resource name
	 * @return input stream to read the configuration resource, or {@code null} if the configuration resource could not
	 *         be found
	 */
	private static InputStream getResourceAsStream(String resName) {
		InputStream ins = Utils.getResourceAsStream(resName);
		if (ins == null) {
			ins = Utils.getResourceAsStream(TrackerConfigStore.class, resName);
		}
		return ins;
	}

	/**
	 * Get reader for a specific resource.
	 *
	 * @param resName
	 *            configuration resource name
	 * @return reader associated with given URL
	 *
	 * @throws IOException
	 *             if resource not found or can't be accessed
	 */
	private static BufferedReader getReaderFromResource(String resName) throws IOException {
		InputStream ins = getResourceAsStream(resName);
		if (ins == null) {
			throw new FileNotFoundException("Configuration resource '" + resName + "' not found");
		}
		return new BufferedReader(new InputStreamReader(ins));
	}

	/**
	 * Get reader for a specific resource or URL.
	 *
	 * @param resName
	 *            configuration resource name
	 * @return reader associated with given URL
	 *
	 * @throws IOException
	 *             if I/O error occurs accessing configuration resource
	 */
	private static BufferedReader getConfigReader(String resName) throws IOException {
		IOException urle = null;
		if (resName != null) {
			try {
				return getReaderFromURL(resName);
			} catch (IOException e) {
				urle = e;
			}
		}
		try {
			return getReaderFromResource(TNT4J_PROPERTIES);
		} catch (IOException rese) {
			throw urle == null ? rese : urle;
		}
	}

	/**
	 * Read configuration stanza.
	 *
	 * @param reader
	 *            configuration reader
	 * @return properties associated with configuration
	 *
	 * @throws IOException
	 *             if I/O error occurs reading configuration stanza lines
	 */
	private Properties readStanza(BufferedReader reader) throws IOException {
		String line;
		Properties props = new Properties();
		do {
			line = reader.readLine();
			if (line != null) {
				line = line.trim();
				if (line.isEmpty() || StringUtils.startsWithAny(line, ";", "#", "//", "{", "}")) {
					continue;
				}
				int sepIndex = line.indexOf(':');
				if (sepIndex <= 0) {
					logger.log(OpLevel.WARNING, "Skipping invalid source={}, file={}, entry='{}'", srcName, configFile,
							line);
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
