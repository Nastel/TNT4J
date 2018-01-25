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
package com.jkoolcloud.tnt4j.selector;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.repository.TokenRepository;
import com.jkoolcloud.tnt4j.repository.TokenRepositoryEvent;
import com.jkoolcloud.tnt4j.repository.TokenRepositoryListener;
import com.jkoolcloud.tnt4j.sink.EventSink;

/**
 * <p>Classes that implement this interface provide implementation for
 * the {@link TokenRepositoryListener} which handles token repository changes
 * </p>
 * 
 * @see OpLevel
 * @see TokenRepositoryListener
 *
 * @version $Revision: 3 $
 *
 */
class PropertyListenerImpl implements TokenRepositoryListener {
	DefaultTrackingSelector selector = null;
	EventSink logger = null;

	public PropertyListenerImpl(DefaultTrackingSelector instance, EventSink log) {
		selector = instance;
		logger = log;
	}

	@Override
	public void repositoryError(TokenRepositoryEvent event) {
		logger.log(OpLevel.ERROR, "Repository error detected, event={0}", event, event.getCause());
	}

	@Override
	public void repositoryChanged(TokenRepositoryEvent event) {
		logger.log(OpLevel.DEBUG, "repositoryChanged source={0}, type={1}, {2}={3}",
					event.getSource(), event.getType(), event.getKey(), event.getValue());
		switch (event.getType()) {
		case TokenRepository.EVENT_ADD_KEY:
		case TokenRepository.EVENT_SET_KEY:
			selector.putKey(event.getKey(), event.getValue());
			break;
		case TokenRepository.EVENT_CLEAR_KEY:
			selector.remove(event.getKey());
			break;
		case TokenRepository.EVENT_CLEAR:
			selector.clear();
			break;
		case TokenRepository.EVENT_RELOAD:
			selector.reloadConfig();
			break;
		case TokenRepository.EVENT_EXCEPTION:
			logger.log(OpLevel.ERROR, "Repository error detected, event={0}", event, event.getCause());
			break;
		}
	}
}