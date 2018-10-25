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
package com.jkoolcloud.tnt4j.repository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;
import org.apache.commons.configuration2.reloading.ReloadingEvent;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements a file based token repository based on a property file following the key=value pairs defined
 * per line. File is auto-reloaded by default based on 20sec refresh time. The reload time can be changed by setting
 * {@code tnt4j.file.respository.refresh} property
 * </p>
 *
 * @see TokenRepository
 *
 * @version $Revision: 6 $
 *
 */

public class FileTokenRepository implements TokenRepository, Configurable {
	private static EventSink logger = DefaultEventSinkFactory.defaultEventSink(FileTokenRepository.class);
	private static ConcurrentHashMap<TokenRepositoryListener, EventListener<?>[]> LISTEN_MAP = new ConcurrentHashMap<>(
			49);

	private String configName = null;
	private BasicConfigurationBuilder<PropertiesConfiguration> config = null;
	private PeriodicReloadingTrigger cfgReloadTrigger = null;
	protected Map<String, ?> settings = null;
	private long refDelay = TimeUnit.SECONDS.toMillis(20);

	/**
	 * Create file/property based token repository instance based on default file name or url specified by
	 * {@code tnt4j.token.repository} java property which should be found in specified properties.
	 *
	 */
	public FileTokenRepository() {
		this(System.getProperty("tnt4j.token.repository"), Long.getLong("tnt4j.file.respository.refresh", 20000));
	}

	/**
	 * Create file/property based token repository instance given a specific filename or url. File name is auto-loaded
	 * based on {@code tnt4j.file.respository.refresh} property which is set to 20000 (ms) by default.
	 *
	 * @param url
	 *            file name or URL of the property file containing tokens
	 * @param refreshDelay
	 *            delay in milliseconds between refresh
	 */
	public FileTokenRepository(String url, long refreshDelay) {
		configName = url;
		refDelay = refreshDelay;
	}

	@Override
	public void addRepositoryListener(TokenRepositoryListener listener) {
		if (configName == null) {
			return;
		}
		TokenConfigurationListener cfListener = new TokenConfigurationListener(listener, logger);
		TokenConfigurationErrorListener cfErrListener = new TokenConfigurationErrorListener(listener, logger);
		EventListener<?>[] pListeners = new EventListener[2];
		pListeners[0] = cfListener;
		pListeners[1] = cfErrListener;
		LISTEN_MAP.put(listener, pListeners);
		config.addEventListener(ConfigurationEvent.ANY, cfListener);
		config.addEventListener(ConfigurationErrorEvent.ANY, cfErrListener);
	}

	@Override
	public void removeRepositoryListener(TokenRepositoryListener listener) {
		if (configName == null) {
			return;
		}
		EventListener<?>[] pListeners = LISTEN_MAP.get(listener);
		if (pListeners != null) {
			LISTEN_MAP.remove(listener);
			config.removeEventListener(ConfigurationEvent.ANY, (TokenConfigurationListener) pListeners[0]);
			config.removeEventListener(ConfigurationErrorEvent.ANY, (TokenConfigurationErrorListener) pListeners[1]);
		}
	}

	@Override
	public Object get(String key) {
		try {
			return config != null ? config.getConfiguration().getProperty(key) : null;
		} catch (ConfigurationException exc) {
			throw new ConfigurationRuntimeException("Failed to get configuration property", exc);
		}
	}

	@Override
	public Iterator<? extends Object> getKeys() {
		try {
			return config != null ? config.getConfiguration().getKeys() : null;
		} catch (ConfigurationException exc) {
			throw new ConfigurationRuntimeException("Failed to get configuration properties key set", exc);
		}
	}

	@Override
	public void remove(String key) {
		if (config != null) {
			try {
				config.getConfiguration().clearProperty(key);
			} catch (ConfigurationException exc) {
				throw new ConfigurationRuntimeException("Failed to remove configuration property", exc);
			}
		}
	}

	@Override
	public void set(String key, Object value) {
		if (config != null) {
			try {
				config.getConfiguration().setProperty(key, value);
			} catch (ConfigurationException exc) {
				throw new ConfigurationRuntimeException("Failed to set configuration property", exc);
			}
		}
	}

	@Override
	public String getName() {
		return configName;
	}

	@Override
	public String toString() {
		PropertiesConfiguration cfg;
		try {
			cfg = config.getConfiguration();
		} catch (ConfigurationException exc) {
			cfg = null;
		}
		return super.toString() + "{url: " + getName() + ", delay: " + refDelay + ", config: " + cfg + "}";
	}

	@Override
	public boolean isOpen() {
		return config != null;
	}

	@Override
	public void open() throws IOException {
		if (isOpen() || (configName == null)) {
			return;
		}
		try {
			initConfig();
			if (cfgReloadTrigger != null) {
				cfgReloadTrigger.start();
			}
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}

	/**
	 * Initialize property configuration based on a configured configuration file name. The method attempts to load it
	 * from URL if given config is URL, then load it from class path and then from file system.
	 *
	 * @throws MalformedURLException
	 *             if malformed configuration file name
	 */
	protected void initConfig() throws MalformedURLException {
		int urlIndex = configName.indexOf("://");

		PropertiesBuilderParameters params = new Parameters().properties();

		if (urlIndex > 0) {
			params.setURL(new URL(configName));
		} else {
			URL configResource = getClass().getResource("/" + configName);
			if (configResource != null) {
				params.setURL(configResource);
			} else {
				params.setFileName(configName);
			}
		}

		if (refDelay > 0) {
			params.setReloadingRefreshDelay(refDelay);

			ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder = new ReloadingFileBasedConfigurationBuilder<>(
					PropertiesConfiguration.class);
			builder.configure(params);

			cfgReloadTrigger = new PeriodicReloadingTrigger(builder.getReloadingController(), null, refDelay,
					TimeUnit.MILLISECONDS);

			config = builder;
		} else {
			config = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
			config.configure(params);
		}

	}

	@Override
	public void close() throws IOException {
		if (cfgReloadTrigger != null) {
			cfgReloadTrigger.shutdown();
		}
	}

	@Override
	public void reopen() throws IOException {
		close();
		open();
	}

	@Override
	public Map<String, ?> getConfiguration() {
		return settings;
	}

	@Override
	public void setConfiguration(Map<String, ?> props) throws ConfigException {
		settings = props;
		configName = Utils.getString("Url", props, configName);
		refDelay = Utils.getLong("RefreshTime", props, refDelay);
	}

	@Override
	public boolean isDefined() {
		return configName != null;
	}
}

class TokenConfigurationListener implements EventListener<ConfigurationEvent> {
	TokenRepositoryListener repListener = null;
	EventSink logger = null;

	public TokenConfigurationListener(TokenRepositoryListener listener, EventSink log) {
		repListener = listener;
		logger = log;
	}

	@Override
	public void onEvent(ConfigurationEvent event) {
		if (event.isBeforeUpdate()) {
			return;
		}
		logger.log(OpLevel.DEBUG, "configurationChanged: type={0}, {1}:{2}", event.getEventType(),
				event.getPropertyName(), event.getPropertyValue());
		if (event.getEventType() == ConfigurationEvent.ADD_PROPERTY) {
			repListener.repositoryChanged(new TokenRepositoryEvent(event.getSource(), TokenRepository.EVENT_ADD_KEY,
					event.getPropertyName(), event.getPropertyValue(), null));
		} else if (event.getEventType() == ConfigurationEvent.SET_PROPERTY) {
			repListener.repositoryChanged(new TokenRepositoryEvent(event.getSource(), TokenRepository.EVENT_SET_KEY,
					event.getPropertyName(), event.getPropertyValue(), null));
		} else if (event.getEventType() == ConfigurationEvent.CLEAR_PROPERTY) {
			repListener.repositoryChanged(new TokenRepositoryEvent(event.getSource(), TokenRepository.EVENT_CLEAR_KEY,
					event.getPropertyName(), event.getPropertyValue(), null));
		} else if (event.getEventType() == ConfigurationEvent.CLEAR) {
			repListener.repositoryChanged(new TokenRepositoryEvent(event.getSource(), TokenRepository.EVENT_CLEAR,
					event.getPropertyName(), event.getPropertyValue(), null));
		} else if (event.getEventType() == ReloadingEvent.ANY) {
			repListener.repositoryChanged(new TokenRepositoryEvent(event.getSource(), TokenRepository.EVENT_RELOAD,
					event.getPropertyName(), event.getPropertyValue(), null));
		}
	}
}

class TokenConfigurationErrorListener implements EventListener<ConfigurationErrorEvent> {
	TokenRepositoryListener repListener = null;
	EventSink logger = null;

	public TokenConfigurationErrorListener(TokenRepositoryListener listener, EventSink log) {
		repListener = listener;
		logger = log;
	}

	@Override
	public void onEvent(ConfigurationErrorEvent event) {
		logger.log(OpLevel.ERROR, "Configuration error detected, event={0}", event, event.getCause());
		repListener.repositoryError(new TokenRepositoryEvent(event.getSource(), TokenRepository.EVENT_EXCEPTION,
				event.getPropertyName(), event.getPropertyValue(), event.getCause()));
	}

}
