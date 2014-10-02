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
package com.nastel.jkool.tnt4j.source;

import java.util.Map;
import java.util.StringTokenizer;

import com.nastel.jkool.tnt4j.config.Configurable;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * This class provides default implementation of <code>SourceFactory</code> interface.
 * New sources are created off the root source which is defined by <code>RootFQN</code>
 * configuration property. This factory provides the following configuration attributes:
 * 
 * <pre>
 * {@code
 *  source.factory: com.nastel.jkool.tnt4j.source.SourceFactoryImpl
 *  source.factory.GEOADDR: New York
 *  source.factory.DATACENTER: MyDC
 *  source.factory.DEVICE: HPPRO
 *  source.factory.RootFQN: RUNTIME=?#SERVER=?#NETADDR=?#DATACENTER=?#GEOADDR=?	
 * }
 * </pre>
 * 
 * @version $Revision: 1 $
 * 
 */

public class SourceFactoryImpl implements SourceFactory, Configurable {
	public final static String UNKNOWN_SOURCE = "UNKNOWN";
	public final static String DEFAULT_SOURCE_ROOT_FQN = System.getProperty("tnt4j.source.root.fqname", "RUNTIME=?#SERVER=?#NETADDR=?#DATACENTER=?#GEOADDR=?");
	
	private final static String [] DEFAULT_SOURCES;
	
	static {
		int i = 0;
		DEFAULT_SOURCES = new String[SourceType.length()];
		for (SourceType type: SourceType.values()) {
			String typeValue = "unknown";
			String typeString = type.toString().toLowerCase();
			typeValue = System.getProperty("tnt4j.source." + typeString);
			
			if (typeValue == null) {
				if (typeString.equalsIgnoreCase(SourceType.SERVER.name())) {
					typeValue = Utils.getLocalHostName();
				} else if (typeString.equalsIgnoreCase(SourceType.RUNTIME.name())) {
					typeValue = Utils.getVMName();				
				} else if (typeString.equalsIgnoreCase(SourceType.NETADDR.name())) {
					typeValue = Utils.getLocalHostAddress();				
				} else if (typeString.equalsIgnoreCase(SourceType.USER.name())) {
					typeValue = System.getProperty("user.name");				
				} else {
					typeValue = UNKNOWN_SOURCE;
				}
			} 
			if (typeValue.startsWith("$")) {
				// points to another environment variable
				typeValue = System.getProperty(typeValue.substring(1), UNKNOWN_SOURCE);
			}
			DEFAULT_SOURCES[i++] = typeValue;
		}
	}

	private Map<String, Object> config = null;
	private String rootFqn = DEFAULT_SOURCE_ROOT_FQN;
	private String [] defaultSources = DEFAULT_SOURCES.clone();
	private Source rootSource = null;

	public SourceFactoryImpl() {
		rootSource = newFromFQN(rootFqn);
	}
	
	@Override
    public Source newFromFQN(String fqn) {
		return createFromFQN(fqn);
    }

	@Override
    public Source newSource(String name) {
	    return newSource(name, SourceType.APPL, getRootSource());
    }

	@Override
    public Source newSource(String name, SourceType tp) {
	    return newSource(name, tp, getRootSource());
    }

	@Override
    public Source newSource(String name, SourceType tp, Source parent) {
	    return newSource(name, tp, parent, getNameFromType("?", SourceType.USER));
    }

	@Override
    public Source newSource(String name, SourceType tp, Source parent, String user) {
	    return new DefaultSource(getNameFromType(name, tp), tp, parent, user);
    }

	@Override
	public Source getRootSource() {
		return rootSource;
	}

	@Override
    public Map<String, Object> getConfiguration() {
	    return config;
    }

	@Override
    public void setConfiguration(Map<String, Object> settings) {
		config = settings;
		
		// initialize source types for this factory
		for (SourceType type: SourceType.values()) {
			String typeString = type.toString().toUpperCase();
			Object typeValue = config.get(typeString);
			if (typeValue != null) {
				defaultSources[type.ordinal()] = String.valueOf(typeValue);
			}
		}
		if (config.get("RootFQN") != null) {
			rootFqn = config.get("RootFQN") != null? config.get("RootFQN").toString(): DEFAULT_SOURCE_ROOT_FQN;
			rootSource = newFromFQN(rootFqn);			
		}
   }
	
	/**
	 * <p>
	 * Returns current GEO location, format is implementation specific.
	 * Developers should override this method for specific platforms and GEO
	 * implementations.
	 * </p>
	 * 
	 * @return current geo location
	 */
	public String getCurrentGeoAddr() {
		return defaultSources[SourceType.GEOADDR.ordinal()];
	}
	
	/**
	 * <p>
	 * Returns current datacenter name, format is implementation specific.
	 * Developers should override this method for specific platforms.
	 * </p>
	 * 
	 * @return current datacenter name
	 */
	public String getCurrentDatacenter() {
		return defaultSources[SourceType.DATACENTER.ordinal()];
	}
	
	/**
	 * <p>
	 * Obtains default name based on a given name/type pair ? name is converted into a runtime binding. Example: ?,
	 * SERVER will return localhost name of the location server.
	 * </p>
	 * 
	 * @return source name based on given name and type
	 */
	protected String getNameFromType(String name, SourceType type) {
		if (name.equals("?")) return defaultSources[type.ordinal()];
		if (name.equals("$")) return System.getProperty(name.substring(1), UNKNOWN_SOURCE);
		return name;
	}

	
	private Source createFromFQN(String fqn) {
		StringTokenizer tk = new StringTokenizer(fqn, "#");
		DefaultSource child = null, root = null;
		while (tk.hasMoreTokens()) {
			String sName = tk.nextToken();
			String[] pair = sName.split("=");
			SourceType type = SourceType.valueOf(pair[0]);
			DefaultSource source = new DefaultSource(getNameFromType(pair[1], type), type, null,  getNameFromType("?", SourceType.USER));
			if (child != null)
				child.setSource(source);
			if (root == null)
				root = source;
			child = source;
		}
		return root;		
	}
	
}
