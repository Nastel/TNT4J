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

package com.jkoolcloud.tnt4j.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class defines a tags collection of type {@link java.util.Set}.
 *
 * @version $Revision: 1 $
 */
public class TagsSet implements Tagged {
	private Set<String> tags = new HashSet<>(89);

	/**
	 * Constructs an empty Tags set instance.
	 */
	public TagsSet() {
	}

	/**
	 * Constructs a Tags set instance with the user-defined array of tags.
	 * 
	 * @param tags
	 *            user-defined array of tags
	 * 
	 * @see #setTag(String...)
	 */
	public TagsSet(String... tags) {
		setTag(tags);
	}

	/**
	 * Constructs a Tags set instance with the user-defined list of tags.
	 * 
	 * @param tags
	 *            user-defined list of tags
	 * 
	 * @see #setTag(java.util.Collection)
	 */
	public TagsSet(Collection<String> tags) {
		setTag(tags);
	}

	@Override
	public Set<String> getTag() {
		return tags;
	}

	@Override
	public void setTag(String... tlist) {
		for (int i = 0; (tlist != null) && (i < tlist.length); i++) {
			if (tlist[i] != null) {
				this.tags.add(tlist[i]);
			}
		}
	}

	@Override
	public void setTag(Collection<String> tlist) {
		if (tlist != null) {
			this.tags.addAll(tlist);
		}
	}

	@Override
	public boolean isTagged(String... tlist) {
		if (tags != null) {
			for (String tag : tlist) {
				if (this.tags.contains(tag)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void clearTags() {
		this.tags.clear();
	}
}
