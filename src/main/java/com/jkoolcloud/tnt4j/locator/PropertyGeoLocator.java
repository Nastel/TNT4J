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
package com.jkoolcloud.tnt4j.locator;

import java.util.Map;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Simple implementation of {@link GeoLocator} which uses
 * java property to lookup current location.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 */
public class PropertyGeoLocator implements GeoLocator, Configurable {
	public  static final String UNKNOWN_LOCATION = "0,0";
	
	private String GEO_LOCATION;
	private Map<String, Object> settings;
	
	/**
	 * Create default property geo locator
	 * This locator uses simple property lookup to determine
	 * GEO location.
	 * 
	 */
	public PropertyGeoLocator() {
	}

	/**
	 * Create default geo locator with a given
	 * java property used for geo lookup
	 * 
	 * @param prop property name for geo lookup
	 */
	public PropertyGeoLocator(String prop) {
		GEO_LOCATION = System.getProperty(prop, UNKNOWN_LOCATION);		
	}
	
	@Override
	public String getCurrentCoords() {
		return GEO_LOCATION;
	}

	@Override
	public String coordsToLabel(String coord) {
		return coord;
	}

	@Override
	public String getCoordsForIp(String ipaddr) {
		return ipaddr;
	}

	@Override
	public String toCoords(String address) {
		return address;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		return settings;
	}

	@Override
	public void setConfiguration(Map<String, Object> vars) throws ConfigException {
		this.settings = vars;
		GEO_LOCATION = Utils.getString("geo-addr", settings, UNKNOWN_LOCATION);
	}
}
