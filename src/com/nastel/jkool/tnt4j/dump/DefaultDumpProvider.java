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
package com.nastel.jkool.tnt4j.dump;

/**
 * <p>
 * This class implements a default dump provider. This is an abstract class and
 * designed to be used for creating extension sub classes.
 * </p>
 *
 * @see DumpProvider
 *
 * @version $Revision: 1 $
 *
 */

abstract public class DefaultDumpProvider implements DumpProvider {
	String pname, category;

	/**
	 * Create a new default dump provider with given name and category
	 *
	 *@param name provider name
	 *@param cat provider category
	 */
	public DefaultDumpProvider(String name, String cat) {
		pname = name;
		category = cat;
	}

	@Override
    public String getCategoryName() {
	    return category;
    }

	@Override
    public String getProviderName() {
	    return pname;
    }
}
