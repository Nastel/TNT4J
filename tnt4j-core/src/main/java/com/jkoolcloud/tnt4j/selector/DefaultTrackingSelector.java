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
 * {@link DefaultTrackingSelector} implements {@link TrackingSelector} interface and provides default file based
 * implementation for a tracking selector. Selector file should contain entries as follows:
 *
 * {@code key=SEV:value-regexp} Example (trace all severities, all orders):
 * {@code OrderApp.purchasing.order.id=DEBUG:.*}
 * 
 * @see OpLevel
 * 
 * @version $Revision: 8 $
 * 
 */
public class DefaultTrackingSelector implements TrackingSelector, Configurable {
	private static final EventSink logger = DefaultEventSinkFactory.defaultEventSink(DefaultTrackingSelector.class);
	private static final boolean DEFAULT_RETURN_UNDEFINED = Utils.getBoolean("tnt4j.selector.undefined.isset",
			System.getProperties(), true);

	private final HashMap<Object, PropertyToken> tokenMap = new HashMap<>(89);
	private Map<String, ?> config = null;
	private TokenRepository tokenRepository = null;
	private final PropertyListenerImpl listener;

	/**
	 * Create a default tracking selector. Each selector needs to be backed by a repository {@link TokenRepository} .
	 * 
	 */
	public DefaultTrackingSelector() {
		listener = new PropertyListenerImpl(this, logger);
	}

	/**
	 * Create a default tracking selector. Each selector needs to be backed by a repository {@link TokenRepository} .
	 * 
	 * @param repository
	 *            token repository implementation
	 */
	public DefaultTrackingSelector(TokenRepository repository) {
		this();

		setRepository(repository);
	}

	@Override
	public boolean isOpen() {
		return Utils.isOpen(tokenRepository);
	}

	@Override
	public synchronized void open() throws IOException {
		if (tokenRepository == null) {
			tokenRepository = new FileTokenRepository();
		}
		if (isDefined()) {
			tokenRepository.open();
			tokenRepository.addRepositoryListener(listener);
			reloadConfig();
		} else {
			logger.log(OpLevel.DEBUG, "Undefined token repository={}: default isSet()={}", tokenRepository,
					DEFAULT_RETURN_UNDEFINED);
		}
	}

	@Override
	public synchronized void close() throws IOException {
		clear();
		if (tokenRepository != null) {
			tokenRepository.removeRepositoryListener(listener);
			Utils.close(tokenRepository);
			tokenRepository = null;
		}
	}

	protected void reloadConfig() {
		clear();
		if (isOpen()) {
			Iterator<? extends Object> keys = tokenRepository.getKeys();
			if (keys == null) {
				return;
			}

			while (keys.hasNext()) {
				String key = String.valueOf(keys.next());
				putKey(key, tokenRepository.get(key).toString());
			}
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
				logger.log(OpLevel.DEBUG, "putKey: repository={}, token={}", tokenRepository, propertyToken);
				tokenMap.put(key, propertyToken);
			}
		} catch (Throwable ex) {
			logger.log(OpLevel.ERROR, "Failed to process key={}, value={}, repository={}", key, value, tokenRepository,
					ex);
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
		return token != null && token.isMatch(sev, key, value);
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
		return tokenRepository != null ? tokenRepository.getKeys() : null;
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
	public Map<String, ?> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, ?> props) throws ConfigException {
		config = props;
		TokenRepository tokenRepo = (TokenRepository) Utils.createConfigurableObject("Repository", "Repository.",
				config);
		setRepository(tokenRepo);
	}

	@Override
	public boolean exists(Object key) {
		return get(key) != null;
	}

	@Override
	public boolean isDefined() {
		return tokenRepository != null && tokenRepository.isDefined();
	}
}
