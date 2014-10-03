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
package com.nastel.jkool.tnt4j.repository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.event.ConfigurationErrorEvent;
import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.ConfigurationException;

import com.nastel.jkool.tnt4j.config.ConfigException;
import com.nastel.jkool.tnt4j.config.Configurable;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.sink.DefaultEventSinkFactory;
import com.nastel.jkool.tnt4j.sink.EventSink;


/**
 * <p>This class implements a file based token repository based on a property file following
 * the key=value pairs defined per line. File is auto-reloaded by default based on 20sec refresh
 * time. The reload time can be changed by setting <code>tnt4j.file.respository.refresh</code> property</p>
 *
 * @see TokenRepository
 *
 * @version $Revision: 6 $
 *
 */

public class FileTokenRepository implements TokenRepository, Configurable {
	private static EventSink logger = DefaultEventSinkFactory.defaultEventSink(FileTokenRepository.class);	
	private static ConcurrentHashMap<TokenRepositoryListener, TokenConfigurationListener> LISTEN_MAP = new ConcurrentHashMap<TokenRepositoryListener, TokenConfigurationListener>(49);
	
	private String configName = null;
	private PropertiesConfiguration config = null;
	protected Map<String, Object> settings = null;
	private long refDelay = 20000;
	
	/**
	 * Create file/property based token repository instance based on default 
	 * file name or url specified by <code>tnt4j.token.repository</code> java
	 * property which should be found in specified properties.
	 * 
	 */
	public FileTokenRepository() {
		this(System.getProperty("tnt4j.token.repository", "tnt4j-tokens.properties"), 
				Long.getLong("tnt4j.file.respository.refresh", 20000));
	}

	/**
	 * Create file/property based token repository instance given 
	 * a specific filename or url. File name is auto-loaded based on 
	 * <code>tnt4j.file.respository.refresh</code> property which is set to 20000 (ms) 
	 * by default.
	 * 
	 * @param url file name or URL of the property file containing tokens
	 */
	public FileTokenRepository(String url, long refreshDelay) {
    	configName = url;
    	refDelay = refreshDelay;
	}

	@Override
	public void addRepositoryListener(TokenRepositoryListener listener) {
		TokenConfigurationListener pListener = new TokenConfigurationListener(listener, logger);
		LISTEN_MAP.put(listener, pListener);
		config.addConfigurationListener(pListener);
		config.addErrorListener(pListener);
	}

	@Override
    public void removeRepositoryListener(TokenRepositoryListener listener) {
		TokenConfigurationListener pListener = LISTEN_MAP.get(listener);
		if (pListener != null) {
			LISTEN_MAP.remove(listener);
			config.removeConfigurationListener(pListener);
			config.removeErrorListener(pListener);
		}
	}
	
	@Override
    public Object get(String key) {
		return config.getProperty(key);
	}

	@Override
    public Iterator<? extends Object> getKeys() {
	    return config.getKeys();
    }

	@Override
    public void remove(String key) {
	    config.clearProperty(key);
    }

	@Override
    public void set(String key, Object value) {
	    config.setProperty(key, value);
    }

	@Override
    public String getName() {
	    return configName;
    }

	@Override
	public String toString() {
		return super.toString() + "{url: " + getName() + ", delay: " + refDelay + ", config: " + config + "}";
	}

	@Override
    public boolean isOpen() {
	    return config != null;
    }

	@Override
    public void open() throws IOException {
		if (isOpen()) return;
        try {
        	initConfig();
        	if (refDelay > 0) {
	        	FileChangedReloadingStrategy reloadConfig = new FileChangedReloadingStrategy();
	        	reloadConfig.setRefreshDelay(refDelay);
	        	config.setReloadingStrategy(reloadConfig);	
	        }
        } catch (Throwable e) {
        	IOException ioe = new IOException(e.toString());
        	ioe.initCause(e);
        	throw ioe;
        }	
    }

	private void initConfig() throws ConfigurationException, MalformedURLException {
   		int urlIndex = configName.indexOf("://");
   		if (urlIndex > 0) {
   			config = new PropertiesConfiguration(new URL(configName)); new PropertiesConfiguration(configName);
   		} else {
			URL configResource = getClass().getResource("/" + configName);
			if (configResource != null) {
				config = new PropertiesConfiguration(configResource); 
			} else {
				config = new PropertiesConfiguration(configName);
			}
   		}		
	}
	
	@Override
    public void close() throws IOException {
	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		return settings;
	}

	@Override
	public void setConfiguration(Map<String, Object> props) throws ConfigException {
		settings = props;
		Object fileUrl = props.get("Url");
		configName = fileUrl != null? fileUrl.toString(): configName;
		
		Object delay = props.get("RefreshTime");
		refDelay = delay != null? Long.parseLong(delay.toString()): refDelay;
	}
	
}

class ReloadFileRepository implements Runnable {
	PropertiesConfiguration fileConfiguration = null;
	
	ReloadFileRepository(PropertiesConfiguration config) {
		fileConfiguration = config;
	}
	
	@Override
    public void run() {
		fileConfiguration.getProperty("test.property");	    
    }
	
}

class TokenConfigurationListener implements ConfigurationListener, ConfigurationErrorListener{
	TokenRepositoryListener repListener = null;
	EventSink logger = null;
	
	public TokenConfigurationListener(TokenRepositoryListener listener, EventSink log) {
		repListener = listener;
		logger = log;
	}
	
	@Override
    public void configurationChanged(ConfigurationEvent event) {	
		if (event.isBeforeUpdate()) return;
		logger.log(OpLevel.DEBUG, "configurationChanged: type={0}, {1}:{2}", 
				event.getType(), event.getPropertyName(), event.getPropertyValue());
		switch (event.getType()) {
			case AbstractConfiguration.EVENT_ADD_PROPERTY:
				repListener.repositoryChanged(new TokenRepositoryEvent(event.getSource(), 
						TokenRepository.EVENT_ADD_KEY, event.getPropertyName(), event.getPropertyValue(), null));
				break;
			case AbstractConfiguration.EVENT_SET_PROPERTY:
				repListener.repositoryChanged(new TokenRepositoryEvent(event.getSource(), 
						TokenRepository.EVENT_SET_KEY, event.getPropertyName(), event.getPropertyValue(), null));
				break;
			case AbstractConfiguration.EVENT_CLEAR_PROPERTY:
				repListener.repositoryChanged(new TokenRepositoryEvent(event.getSource(), 
						TokenRepository.EVENT_CLEAR_KEY, event.getPropertyName(), event.getPropertyValue(), null));
				break;
			case AbstractConfiguration.EVENT_CLEAR:
				repListener.repositoryChanged(new TokenRepositoryEvent(event.getSource(), 
						TokenRepository.EVENT_CLEAR, event.getPropertyName(), event.getPropertyValue(), null));
				break;
			case AbstractFileConfiguration.EVENT_RELOAD:
				repListener.repositoryChanged(new TokenRepositoryEvent(event.getSource(), 
						TokenRepository.EVENT_RELOAD, event.getPropertyName(), event.getPropertyValue(), null));
				break;
		}
    }

	@Override
    public void configurationError(ConfigurationErrorEvent event) {
		logger.log(OpLevel.ERROR, "Configuration error detected, event={0}", event, event.getCause());
		repListener.repositoryError(new TokenRepositoryEvent(event.getSource(), 
				TokenRepository.EVENT_EXCEPTION, event.getPropertyName(), event.getPropertyValue(), event.getCause()));
    }
	
}
