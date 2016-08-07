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

/**
 * <p>
 * Default geo locator class used for obtaining default/global GEO locator
 * instance.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 */
public class DefaultGeoLocator {
	private static GeoLocator geoLocator = new PropertyGeoLocator("tnt4j.geo.location");
	
	private DefaultGeoLocator() {
	}

	/**
	 * Set a global GEO location implementation
	 * 
	 * @param locator GEO locator implementation instance
	 * @return {@link GeoLocator} instance
	 */
	public static GeoLocator setDefaultGeoLocator(GeoLocator locator) {
		geoLocator = locator;
		return geoLocator;
	}
	
	/**
	 * Obtain a default GEO locator
	 * 
	 * @return {@link GeoLocator} instance
	 */
	public static GeoLocator getInstance() {
		return geoLocator;
	}
}
