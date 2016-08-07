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
 * This interface defines GEO location service, which is used to determine
 * current geographical address and convert regular address to GPS location.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 */

public interface GeoLocator {
	/**
	 * Obtain current GEO location of the current runtime 
	 * 
	 * @return address, or geo location (GPS coordinates)
	 */
	String getCurrentLocation();

	/**
	 * Convert a given address to GEO coordinates 
	 * 
	 * @param address geo address
	 * @return geo coordinates
	 */
	String toCoordinates(String address);
}
