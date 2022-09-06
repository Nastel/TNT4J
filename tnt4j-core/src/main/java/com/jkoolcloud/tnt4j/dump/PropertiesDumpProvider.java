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

import java.util.Map.Entry;
import java.util.Properties;

/**
 * <p>
 * This class is a dump provider for dumping java properties using System.getProperties();
 * 
 * </p>
 * 
 * @see DumpCollection
 * 
 * @version $Revision: 2 $
 * 
 */
public class PropertiesDumpProvider extends DefaultDumpProvider {
	Properties props;

	/**
	 * Create a new java properties dump provider with a given name and System.getProperties().
	 * 
	 * @param name
	 *            provider name
	 */
	public PropertiesDumpProvider(String name) {
		this(name, null);
	}

	/**
	 * Create a new java properties dump provider with a given name and user specified properties.
	 * 
	 * @param name
	 *            provider name
	 * @param pr
	 *            properties
	 */
	public PropertiesDumpProvider(String name, Properties pr) {
		super(name, "System");
		props = pr;
	}

	@Override
	public DumpCollection getDump() {
		Dump dump = new Dump("Properties", this);
		Properties p = props != null ? props : System.getProperties();
		for (Entry<Object, Object> entry : p.entrySet()) {
			dump.add(entry.getKey().toString(), entry.getValue());
		}
		return dump;
	}
}
