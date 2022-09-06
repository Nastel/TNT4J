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

/**
 * <p>
 * A simple event listener interface for configuration observers. This interface can be implemented by classes that are
 * interested in "raw" events caused by repository objects. Each manipulation on a token repository object will generate
 * such an event. There is only a single method that is invoked when an event occurs..
 * </p>
 *
 * @see TokenRepositoryEvent
 *
 * @version $Revision: 1 $
 *
 */
public interface TokenRepositoryListener {
	/**
	 * Notifies this listener about a manipulation on a monitored repository object.
	 * 
	 * @param event
	 *            the event describing the manipulation
	 * 
	 * @see TokenRepositoryEvent
	 */
	void repositoryChanged(TokenRepositoryEvent event);

	/**
	 * Notifies this listener about an error on a monitored repository object.
	 * 
	 * @param event
	 *            the event describing the error
	 * 
	 * @see TokenRepositoryEvent
	 */
	void repositoryError(TokenRepositoryEvent event);

}
