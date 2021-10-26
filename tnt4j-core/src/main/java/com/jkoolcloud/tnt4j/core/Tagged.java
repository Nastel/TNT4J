/*
 * Copyright 2014-2021 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.core;

import java.util.Collection;

/**
 * This interface defines classes that can be tagged by user-defined string values.
 *
 * @version $Revision: 1 $
 */
public interface Tagged {

	/**
	 * Sets tags, which are user-defined values associated with this object.
	 *
	 * @param tags
	 *            user-defined array of tags
	 */
	void setTag(String... tags);

	/**
	 * Sets tags, which are user-defined values associated with this object.
	 *
	 * @param tags
	 *            user-defined list of tags
	 */
	void setTag(Collection<String> tags);

	/**
	 * Gets tags, which are user-defined values associated with this object.
	 *
	 * @return user-defined set of tags
	 */
	Collection<String> getTag();

	/**
	 * Checks if this object is tagged by any tag from user-defined tags list.
	 *
	 * @param tags
	 *            user-defined array of tags
	 *
	 * @return {@code true} if this object is tagged by any of user-defined tags, {@code false} - otherwise
	 */
	boolean isTagged(String... tags);

	/**
	 * Removes all tags associated with this object.
	 */
	void clearTags();
}
