/*
 * Copyright 2014-2022 JKOOL, LLC.
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
import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
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
 * {@code tnt4j.file.repository.refresh} property
 * </p>
 *
 * @see TokenRepository
 *
 * @version $Revision: 6 $
 *
 */

public class FileTokenRepository implements TokenRepository, Configurable {
	private static final EventSink logger = DefaultEventSinkFactory.defaultEventSink(FileTokenRepository.class);
	private static final ConcurrentHashMap<TokenRepositoryListener, EventListener<?>[]> LISTEN_MAP = new ConcurrentHashMap<>(49);
	private static long DEFAULT_REFRESH_DELAY = TimeUnit.SECONDS.toMillis(0);

	private String configName = null;
	private BasicConfigurationBuilder<PropertiesConfiguration> config = null;
	private PeriodicReloadingTrigger cfgReloadTrigger = null;
	protected Map<String, ?> settings = null;

	private long refDelay = DEFAULT_REFRESH_DELAY;

	/**
	 * Create file/property based token repository instance based on default file name or url specified by
	 * {@code tnt4j.token.repository} java property which should be found in specified properties.
	 *
	 */
	public FileTokenRepository() {
		this(System.getProperty("tnt4j.token.repository"),
				Long.getLong("tnt4j.file.repository.refresh", DEFAULT_REFRESH_DELAY));
	}

	/**
	 * Create file/property based token repository instance given a specific filename or url. File name is auto-loaded
	 * based on {@code tnt4j.file.repository.refresh} property which is set to 20000 (ms) by default.
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
		if (configName == null || !isOpen()) {
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
		if (configName == null || !isOpen()) {
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
			return isOpen() ? config.getConfiguration().getProperty(key) : null;
		} catch (ConfigurationException exc) {
			throw new ConfigurationRuntimeException("Failed to get configuration property", exc);
		}
	}

	@Override
	public Iterator<? extends Object> getKeys() {
		try {
			return isOpen() ? config.getConfiguration().getKeys() : null;
		} catch (ConfigurationException exc) {
			throw new ConfigurationRuntimeException("Failed to get configuration properties key set", exc);
		}
	}

	@Override
	public void remove(String key) {
		if (isOpen()) {
			try {
				config.getConfiguration().clearProperty(key);
			} catch (ConfigurationException exc) {
				throw new ConfigurationRuntimeException("Failed to remove configuration property", exc);
			}
		}
	}

	@Override
	public void set(String key, Object value) {
		if (isOpen()) {
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
		PropertiesConfiguration cfg = null;
		try {
			if (isOpen()) {
				cfg = config.getConfiguration();
			}
		} catch (ConfigurationException exc) {
		}
		return super.toString() + "{url: " + configName + ", delay: " + refDelay + ", config: " + cfg + "}";
	}

	@Override
	public boolean isOpen() {
		return config != null;
	}

	@Override
	public synchronized void open() throws IOException {
		if (isOpen() || (configName == null)) {
			return;
		}
		try {
			initConfig();
			if (cfgReloadTrigger != null && !cfgReloadTrigger.isRunning()) {
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
	protected synchronized void initConfig() throws MalformedURLException {
		int urlIndex = configName.indexOf("://");

		FileBasedBuilderParameters params = new Parameters().fileBased();

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

		close();

		if (refDelay > 0) {
			ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder = new ReloadingFileBasedConfigurationBuilder<>(
					PropertiesConfiguration.class).configure(params);
			cfgReloadTrigger = new PeriodicReloadingTrigger(builder.getReloadingController(), null, refDelay,
					TimeUnit.MILLISECONDS);
			config = builder;
		} else {
			config = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class).configure(params);
		}
	}

	@Override
	public synchronized void close() {
		if (cfgReloadTrigger != null) {
			cfgReloadTrigger.stop();
			cfgReloadTrigger = null;
		}
	}

	@Override
	public void reopen() throws IOException {
		close();
		removeListenersFromClosed();

		open();
		addListenersToOpened();
	}

	private void removeListenersFromClosed() {
		if (isOpen()) {
			for (Map.Entry<TokenRepositoryListener, EventListener<?>[]> le : LISTEN_MAP.entrySet()) {
				EventListener<?>[] pListeners = le.getValue();
				if (pListeners != null) {
					config.removeEventListener(ConfigurationEvent.ANY, (TokenConfigurationListener) pListeners[0]);
					config.removeEventListener(ConfigurationErrorEvent.ANY,
							(TokenConfigurationErrorListener) pListeners[1]);
				}
			}
		}
	}

	private void addListenersToOpened() {
		if (isOpen()) {
			for (Map.Entry<TokenRepositoryListener, EventListener<?>[]> le : LISTEN_MAP.entrySet()) {
				EventListener<?>[] pListeners = le.getValue();
				if (pListeners != null) {
					config.addEventListener(ConfigurationEvent.ANY, (TokenConfigurationListener) pListeners[0]);
					config.addEventListener(ConfigurationErrorEvent.ANY,
							(TokenConfigurationErrorListener) pListeners[1]);
				}
			}
		}
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
	final TokenRepositoryListener repListener;
	final EventSink logger;

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
	final TokenRepositoryListener repListener;
	final EventSink logger;

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
