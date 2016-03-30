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
package com.nastel.jkool.tnt4j.utils;

import java.util.Iterator;
import java.util.List;


public class MathUtils {
	
	public static double getBollingerLow(List<Number> harray, int dcount, int interval) {
		double low = 0;
		if (harray != null && harray.size() >= interval) {
			double mean = getHMean(harray);
			double dev = Math.sqrt(getHVariance(harray, mean));
			low = mean - (dcount * dev);
		}
		return low;
	}

	public static double getBollingerHigh(List<Number> harray, int dcount, int interval) {
		double high = 0;
		if (harray != null && harray.size() >= interval) {
			double mean = getHMean(harray);
			double dev = Math.sqrt(getHVariance(harray, mean));
			high = mean + (dcount * dev);
		}
		return high;
	}

	public static double getHVariance(List<Number> harray, double mean) {
		double vsum = 0, variance = 0;
		if (harray != null && harray.size() > 1) {
			Iterator<Number> it = harray.iterator();
			while (it.hasNext()) {
				Number vl = it.next();
				double val = vl.doubleValue() - mean;
				vsum += (val * val);
			}
			if (harray.size() > 1) {
				variance = vsum / (harray.size() - 1);
			}
		}
		return variance;
	}

	public static double getHMean(List<Number> list) {
		Iterator<Number> it = list.iterator();
		double vsum = 0, mean = 0;
		int count = 0;
		while (it.hasNext()) {
			Number vl = it.next();
			double val = vl.doubleValue();
			vsum += val;
			count++;
		}
		mean = count > 0 ? vsum / count: mean;
		return mean;
	}
}
