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
 * 	source.factory: com.nastel.jkool.tnt4j.source.SourceFactoryImpl
 *	source.factory.GeoLocation: New York
 *	source.factory.Datacenter: MyDC
 *	source.factory.RootFQN: JVM=?#SERVER=?#NETADDR=?#DATACENTER=?#GEOADDR=?	
 * }
 * </pre>
 * 
 * @version $Revision: 1 $
 * 
 */

public class SourceFactoryImpl implements SourceFactory, Configurable {
	public final String DEFAULT_SOURCE_ROOT_FQN = System.getProperty("tnt4j.source.root.fqname", "JVM=?#SERVER=?#NETADDR=?#DATACENTER=?#GEOADDR=?");
	public final String DEFAULT_SOURCE_GEO_LOCATION = System.getProperty("tnt4j.current.geo.location", "unknown");
	public final String DEFAULT_SOURCE_DATACENTER = System.getProperty("tnt4j.current.geo.datacenter", "default");
	public final String DEFAULT_SOURCE_USER = System.getProperty("user.name");
	
	private Map<String, Object> config = null;
	private String rootFqn = DEFAULT_SOURCE_ROOT_FQN;
	private String geoAddr = DEFAULT_SOURCE_GEO_LOCATION;
	private String dcName = DEFAULT_SOURCE_DATACENTER;
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
	    return newSource(name, tp, parent, DEFAULT_SOURCE_USER);
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
		geoAddr = config.get("GeoLocation") != null? config.get("GeoLocation").toString(): geoAddr;
		dcName = config.get("Datacenter") != null? config.get("Datacenter").toString(): geoAddr;
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
	public String getCurrentGeoLocation() {
		return geoAddr;
	}
	
	/**
	 * <p>
	 * Returns current datacenter name, format is implementation specific.
	 * Developers should override this method for specific platforms.
	 * </p>
	 * 
	 * @return current geo location
	 */
	public String getCurrentDatacenter() {
		return dcName;
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
		if (name.equals("?") && type == SourceType.SERVER)
			name = Utils.getLocalHostName();
		else if (name.equals("?") && type == SourceType.NETADDR)
			name = Utils.getLocalHostAddress();
		else if (name.equals("?") && type == SourceType.JVM)
			name = Utils.getVMName();
		else if (name.equals("?") && type == SourceType.GEOADDR)
			name = getCurrentGeoLocation();
		else if (name.equals("?") && type == SourceType.DATACENTER)
			name = getCurrentDatacenter();
		else if (name.equals("?"))
			throw new RuntimeException("Unknown name for type=" + type);
		return name;
	}

	
	private Source createFromFQN(String fqn) {
		StringTokenizer tk = new StringTokenizer(fqn, "#");
		DefaultSource child = null, root = null;
		while (tk.hasMoreTokens()) {
			String sName = tk.nextToken();
			String[] pair = sName.split("=");
			SourceType type = SourceType.valueOf(pair[0]);
			DefaultSource source = new DefaultSource(getNameFromType(pair[1], type), type, null, DEFAULT_SOURCE_USER);
			if (child != null)
				child.setSource(source);
			if (root == null)
				root = source;
			child = source;
		}
		return root;		
	}
	
}
