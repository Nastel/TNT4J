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
package com.jkoolcloud.tnt4j.dump;

/**
 * <p>
 * This interface defines a provider interface for generating dump collections. Each application should creates
 * implementations of this interface to generate application specific dumps. Providers instances are registered with
 * {@link com.jkoolcloud.tnt4j.TrackingLogger} class and triggered on {@code TrackingLogger.dump()} method call.
 * {@link com.jkoolcloud.tnt4j.dump.DumpProvider#getDump()} is called when application specific dump need to be obtained
 * by the underlying framework.
 * </p>
 *
 * @see DumpCollection
 *
 * @version $Revision: 1 $
 *
 */

public interface DumpProvider {
	/**
	 * DUMP_BEFORE generated before dump is written to one or more destination(s)
	 */
	int DUMP_BEFORE = 0;

	/**
	 * DUMP_AFTER generated after dump is written to one or more destination(s)
	 */
	int DUMP_AFTER = 1;

	/**
	 * DUMP_COMPLETE generated after all dumps are written to one or more destination(s)
	 */
	int DUMP_COMPLETE = 2;

	/**
	 * DUMP_ERROR generated when and error is encountered during dump generation
	 */
	int DUMP_ERROR = 3;

	/**
	 * Name of the dump provider
	 * 
	 * @return name of the dump provider
	 */
	String getProviderName();

	/**
	 * Name of the dump provider category
	 * 
	 * @return name of the dump provider category
	 * 
	 */
	String getCategoryName();

	/**
	 * Return the dump collection associated with this provider
	 * 
	 * @return dump collection associated with this dump provider
	 * 
	 */
	DumpCollection getDump();
}
