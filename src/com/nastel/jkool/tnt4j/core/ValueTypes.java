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

	public static final String VALUE_TYPE_BYTE = "byte";
	public static final String VALUE_TYPE_KBYTE = "kb";
	public static final String VALUE_TYPE_MBYTE = "mb";
	public static final String VALUE_TYPE_GBYTE = "gb";
	public static final String VALUE_TYPE_TBYTE = "tb";
	
	public static final String VALUE_TYPE_AGE = "age";
	public static final String VALUE_TYPE_AGE_NSEC = "age.nsec";
	public static final String VALUE_TYPE_AGE_USEC = "age.usec";
	public static final String VALUE_TYPE_AGE_MSEC = "age.msec";
	public static final String VALUE_TYPE_AGE_SEC = "age.sec";
	public static final String VALUE_TYPE_AGE_MIN = "age.min";
	public static final String VALUE_TYPE_AGE_HOUR = "age.hour";
	public static final String VALUE_TYPE_AGE_DAY  = "age.day";
	public static final String VALUE_TYPE_AGE_WEEK = "age.week";
	public static final String VALUE_TYPE_AGE_MONTH = "age.month";
	public static final String VALUE_TYPE_AGE_YEAR  = "age.year";

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
	public static final String VALUE_TYPE_GAUGE = "gauge";
	
	public static final String VALUE_TYPE_ID = "id";
	public static final String VALUE_TYPE_GUID = "guid";
}
