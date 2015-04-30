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
package com.nastel.jkool.tnt4j.core;

/**
 * This interface defines a list of user defined value types, which qualify the
 * intended use of properties values see {@link Property} and {@link Snapshot}.
 * @version $Revision: 1 $
 */
public interface ValueTypes {
	public static final String VALUE_TYPE_CURRENCY = "currency";
	public static final String VALUE_TYPE_CURRENCY_USD = "currency.usd";
	public static final String VALUE_TYPE_CURRENCY_EUR = "currency.eur";
	public static final String VALUE_TYPE_CURRENCY_UK = "currency.uk";

	public static final String VALUE_TYPE_BYTES = "bytes";
	public static final String VALUE_TYPE_KBYTES = "kbyes";
	public static final String VALUE_TYPE_MBYTES = "mbytes";
	public static final String VALUE_TYPE_GBYTES = "gbytes";
	public static final String VALUE_TYPE_TBYTES = "tbytes";
	
	public static final String VALUE_TYPE_AGE = "age";
	public static final String VALUE_TYPE_AGE_NSECS = "age.nsecs";
	public static final String VALUE_TYPE_AGE_USECS = "age.usecs";
	public static final String VALUE_TYPE_AGE_MSEC = "age.msecs";
	public static final String VALUE_TYPE_AGE_SECS = "age.secs";
	public static final String VALUE_TYPE_AGE_MINS= "age.mins";
	public static final String VALUE_TYPE_AGE_HOURS= "age.hours";
	public static final String VALUE_TYPE_AGE_DAYS = "age.days";
	public static final String VALUE_TYPE_AGE_WEEKS = "age.weeks";
	public static final String VALUE_TYPE_AGE_MONTHS = "age.months";
	public static final String VALUE_TYPE_AGE_YEARS = "age.years";

	public static final String VALUE_TYPE_SPEED_KMH = "speed.kmh";
	public static final String VALUE_TYPE_SPEED_MPH = "speed.mph";
	
	public static final String VALUE_TYPE_WEIGHT_LB = "weight.lb";
	public static final String VALUE_TYPE_WEIGHT_OZ = "weight.oz";
	public static final String VALUE_TYPE_WEIGHT_GRAM = "weight.g";
	public static final String VALUE_TYPE_WEIGHT_KG = "weight.kg";
	
	public static final String VALUE_TYPE_PERCENT = "percent";
	public static final String VALUE_TYPE_ADDRESS = "address";
	public static final String VALUE_TYPE_IPADDRESS = "ip.addr";
	public static final String VALUE_TYPE_IPADDRESS_V4 = "ip.addr.v4";
	public static final String VALUE_TYPE_IPADDRESS_V6 = "ip.addr.v6";
	public static final String VALUE_TYPE_TIMETICKS = "timeticks";
	public static final String VALUE_TYPE_TIMESTAMP = "timestamp";
	public static final String VALUE_TYPE_COUNTER = "counter";
	public static final String VALUE_TYPE_GUAGE = "guage";
	public static final String VALUE_TYPE_GUID = "guid";
}
