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
package com.jkoolcloud.tnt4j.selector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.repository.FileTokenRepository;
import com.jkoolcloud.tnt4j.repository.TokenRepository;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * {@link DefaultTrackingSelector} implements {@link TrackingSelector} interface and provides default file
 * based implementation for a tracking selector. Selector file should contain entries as follows:
 * 
 * <code>key=SEV:value-regexp</code> Example (trace all severities, all orders):
 * <code>OrderApp.purchasing.order.id=DEBUG:.*</code>
 * 
 * @see OpLevel
 * 
 * @version $Revision: 7 $
 * 
 */
public class DefaultTrackingSelector implements TrackingSelector, Configurable {
	private static EventSink logger = DefaultEventSinkFactory.defaultEventSink(DefaultTrackingSelector.class);
	private static final boolean DEFAULT_RETURN_UNDEFINED = Boolean.valueOf(System.getProperty("tnt4j.selector.undefined.isset", "true"));
	private HashMap<Object, PropertyToken> tokenMap = new HashMap<Object, PropertyToken>(89);
	private Map<String, Object> config = null;
	private TokenRepository tokenRepository = null;
	private PropertyListenerImpl listener = null;

	/**
	 * Create a default tracking selector. Each selector needs to be backed by a repository {@link TokenRepository}
	 * .
	 * 
	 */
	public DefaultTrackingSelector() {
	}

	/**
	 * Create a default tracking selector. Each selector needs to be backed by a repository {@link TokenRepository}
	 * .
	 * 
	 * @param repository
	 *            token repository implementation
	 */
	public DefaultTrackingSelector(TokenRepository repository) {
		setRepository(repository);
	}

	@Override
	public boolean isOpen() {
		return tokenRepository != null && tokenRepository.isOpen();
	}

	@Override
	public synchronized void open() throws IOException {
		if (tokenRepository == null) {
			tokenRepository = new FileTokenRepository();
		}
		if (isDefined()) {
			tokenRepository.open();
			listener = new PropertyListenerImpl(this, logger);
			tokenRepository.addRepositoryListener(listener);
			reloadConfig();
		} else {
			logger.log(OpLevel.DEBUG, "Undefined token repository={0}: default isSet()={1}", tokenRepository, DEFAULT_RETURN_UNDEFINED);			
		}
	}

	@Override
	public synchronized void close() throws IOException {
		clear();
		if (tokenRepository != null) {
			tokenRepository.removeRepositoryListener(listener);
			Utils.close(tokenRepository);
		}
	}

	@Override
    public synchronized void reopen() throws IOException {
		close();
		open();
	}

	protected void reloadConfig() {
		clear();
		Iterator<? extends Object> keys = tokenRepository.getKeys();
		if (keys == null) return;
		
		while (keys.hasNext()) {
			String key = String.valueOf(keys.next());
			putKey(key, tokenRepository.get(key).toString());
		}
	}

	protected void putKey(Object key, Object val) {
		String value = String.valueOf(val);
		int index = value.indexOf(":");
		try {
			PropertyToken propertyToken = null;
			if (index > 0) {
				// token consists of sev:reg-exp pair
				String sevValue = value.substring(0, index);
				String valuePattern = value.substring(index + 1);
				OpLevel sevLimit = OpLevel.valueOf(sevValue.toUpperCase());
				propertyToken = new PropertyToken(sevLimit, key, value, valuePattern);
			} else {
				// token only has severity limit specified
				String sevValue = value.trim();
				if (!sevValue.isEmpty()) {
					OpLevel sevLimit = OpLevel.valueOf(sevValue.toUpperCase());
					propertyToken = new PropertyToken(sevLimit, key, value, null);
				}
			}
			if (propertyToken != null) {
				logger.log(OpLevel.DEBUG, 
							"putkey: repository={0}, token={1}", tokenRepository, propertyToken);
				tokenMap.put(key, propertyToken);
			}
		} catch (Throwable ex) {
			logger.log(OpLevel.ERROR, 
					"Failed to process key={0}, value={1}, repository={2}", key, value, tokenRepository, ex);
		}
	}

	@Override
    public boolean isSet(OpLevel sev, Object key) {
	    return isSet(sev, key, null);
    }

	@Override
	public boolean isSet(OpLevel sev, Object key, Object value) {
		if (!isDefined()) {
			return DEFAULT_RETURN_UNDEFINED;
		}
		PropertyToken token = tokenMap.get(key);
		return (token != null? token.isMatch(sev, key, value): false);
	}

	@Override
	public void remove(Object key) {
		tokenMap.remove(key);
	}

	@Override
	public Object get(Object key) {
		PropertyToken token = tokenMap.get(key);
		return token != null ? token.getValue() : null;
	}

	@Override
	public void set(OpLevel sev, Object key, Object value) {
		putKey(key, value != null ? sev.toString() + ":" + value : sev.toString());
	}

	@Override
	public void set(OpLevel sev, Object key) {
		set(sev, key, null);
	}

	@Override
    public Iterator<? extends Object> getKeys() {
	    return tokenRepository != null? tokenRepository.getKeys(): null;
    }

	@Override
	public TokenRepository getRepository() {
		return tokenRepository;
	}

	protected void clear() {
		tokenMap.clear();
	}

	@Override
	public void setRepository(TokenRepository repo) {
		Utils.close(this);
		tokenRepository = repo;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> props) throws ConfigException {
		config = props;
		TokenRepository tokenRepo = (TokenRepository) Utils.createConfigurableObject("Repository", "Repository.", config);
		setRepository(tokenRepo);
	}

	@Override
    public boolean exists(Object key) {
	    return get(key) != null;
    }

	@Override
    public boolean isDefined() {
		return (tokenRepository != null && tokenRepository.isDefined());
    }
}
