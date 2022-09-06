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
package com.jkoolcloud.tnt4j.core;

/**
 * This interface defines a list of user defined value types, which qualify the intended use of properties values see
 * {@link Property} and {@link Snapshot}.
 * 
 * @version $Revision: 1 $
 */
public interface ValueTypes {
	String VALUE_TYPE_NONE = "none";

	// temperature types
	String VALUE_TYPE_TEMP_F = "temp.f";
	String VALUE_TYPE_TEMP_C = "temp.c";

	// Rates of change
	String VALUE_TYPE_RATE_SEC = "rate.sec";

	// currency types
	String VALUE_TYPE_CURRENCY = "currency";
	String VALUE_TYPE_CURRENCY_USD = "currency.usd";
	String VALUE_TYPE_CURRENCY_EUR = "currency.eur";
	String VALUE_TYPE_CURRENCY_UK = "currency.uk";

	String VALUE_TYPE_CRYPTO = "crypto";
	String VALUE_TYPE_CRYPTO_BTC = "crypto.btc";
	String VALUE_TYPE_CRYPTO_ETH = "crypto.eth";
	String VALUE_TYPE_CRYPTO_WEI = "crypto.eth.wei";
	String VALUE_TYPE_CRYPTO_EOS = "crypto.eos";
	String VALUE_TYPE_CRYPTO_LTC = "crypto.ltc";

	// size/length value types
	String VALUE_TYPE_SIZE = "size";
	String VALUE_TYPE_SIZE_BYTE = "size.byte";
	String VALUE_TYPE_SIZE_KBYTE = "size.kb";
	String VALUE_TYPE_SIZE_MBYTE = "size.mb";
	String VALUE_TYPE_SIZE_GBYTE = "size.gb";
	String VALUE_TYPE_SIZE_TBYTE = "size.tb";
	String VALUE_TYPE_SIZE_METER = "size.meter";
	String VALUE_TYPE_SIZE_KM = "size.km";
	String VALUE_TYPE_SIZE_FOOT = "size.foot";
	String VALUE_TYPE_SIZE_MILE = "size.mile";

	// time/age related value types
	String VALUE_TYPE_AGE = "age";
	String VALUE_TYPE_AGE_NSEC = "age.nsec";
	String VALUE_TYPE_AGE_USEC = "age.usec";
	String VALUE_TYPE_AGE_MSEC = "age.msec";
	String VALUE_TYPE_AGE_SEC = "age.sec";
	String VALUE_TYPE_AGE_MIN = "age.min";
	String VALUE_TYPE_AGE_HOUR = "age.hour";
	String VALUE_TYPE_AGE_DAY = "age.day";
	String VALUE_TYPE_AGE_WEEK = "age.week";
	String VALUE_TYPE_AGE_MONTH = "age.month";
	String VALUE_TYPE_AGE_YEAR = "age.year";

	// time related value types
	String VALUE_TYPE_TIMESTAMP = "timestamp";

	// speed/velocity
	String VALUE_TYPE_SPEED_KMH = "speed.kmh";
	String VALUE_TYPE_SPEED_MPH = "speed.mph";

	// mass value types
	String VALUE_TYPE_MASS_LB = "mass.lb";
	String VALUE_TYPE_MASS_OZ = "mass.oz";
	String VALUE_TYPE_MASS_GRAM = "mass.g";
	String VALUE_TYPE_MASS_KG = "mass.kg";

	// address value types
	String VALUE_TYPE_ADDR = "addr";
	String VALUE_TYPE_IPADDR = "addr.ip";
	String VALUE_TYPE_IPADDR_V4 = "addr.ip.v4";
	String VALUE_TYPE_IPADDR_V6 = "addr.ip.v6";

	// generic number based value types
	String VALUE_TYPE_COUNTER = "counter";
	String VALUE_TYPE_COUNTER_TIMETICKS = "counter.msec";
	String VALUE_TYPE_GAUGE = "gauge";
	String VALUE_TYPE_DERIVE = "derive";
	String VALUE_TYPE_ABSOLUTE = "absolute";

	String VALUE_TYPE_FLAG = "flag";
	String VALUE_TYPE_PERCENT = "percent";
	String VALUE_TYPE_ID = "id";
	String VALUE_TYPE_GUID = "guid";
}
