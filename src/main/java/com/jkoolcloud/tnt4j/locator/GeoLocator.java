/*
 * Copyright 2014-2018 JKOOL, LLC.
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
 * This interface defines GEO location service, which is used to determine current geographical address and convert
 * regular address to GPS location. Developers should create specific geo location implementations.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 */

public interface GeoLocator {
	/**
	 * Convert "Latitude,Longitude" coordinates to readable location.
	 *
	 * @param coord
	 *            coordinates string
	 * @return readable location e.g "Brooklyn, New York"
	 */
	String coordsToLabel(String coord);

	/**
	 * Obtain current GEO coordinates of the current runtime as string "Latitude,Longitude"
	 * 
	 * @return coordinates as string "Latitude,Longitude"
	 */
	String getCurrentCoords();

	/**
	 * Obtain GEO based on a given IP address as string "Latitude,Longitude"
	 * 
	 * @param ipaddr
	 *            IP address
	 * @return location based on IP address as string "Latitude,Longitude"
	 */
	String getCoordsForIp(String ipaddr);

	/**
	 * Convert a given address to GEO coordinates
	 * 
	 * @param address
	 *            geo address
	 * @return geo coordinates as string "Latitude,Longitude"
	 */
	String toCoords(String address);
}
