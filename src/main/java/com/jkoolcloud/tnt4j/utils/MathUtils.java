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
package com.jkoolcloud.tnt4j.utils;

import java.util.Iterator;
import java.util.List;



/**
 * Math utility methods.
 *
 * @version $Revision: 1 $
 */
public class MathUtils {
	
    private MathUtils() {
    }
    
	/**
	 * Compute low Bollinger band value. Same as {@code getBBLow(List, 2, 20)}
	 *
	 * @param harray of numbers (measurements) sorted by time
	 * @return low Bolligner band value
	 */
	public static double getBBLow(List<Number> harray) {
		return getBBLow(harray, 2, 20);
	}

	/**
	 * Compute high Bollinger band value. Same as {@code getBoHigh(List, 2, 20)}
	 *
	 * @param harray of numbers (measurements) sorted by time
	 * @return high Bolligner band value
	 */
	public static double getBBHigh(List<Number> harray) {
		return getBBHigh(harray, 2, 20);
	}

	/**
	 * Compute low Bollinger band value. Typical values for N and K are 20 and 2, respectively.
	 *
	 * @param harray of numbers (measurements) sorted by time
	 * @param kTimes K times an N-period standard deviation above the EMA(nPeriod)
	 * @param nPeriod an N-period moving average (EMA)
	 * @return low Bolligner band value
	 */
	public static double getBBLow(List<Number> harray, int kTimes, int nPeriod) {
		double low = 0;
		if (harray != null && harray.size() >= nPeriod) {
			double mean = getEMA(harray, nPeriod);
			double dev = Math.sqrt(getHVariance(harray, mean));
			low = mean - (kTimes * dev);
		}
		return low;
	}

	/**
	 * Compute high Bollinger band value. Typical values for N and K are 20 and 2, respectively.
	 *
	 * @param harray of numbers (measurements) sorted by time
	 * @param kTimes K times an N-period standard deviation above the EMA(nPeriod)
	 * @param nPeriod an N-period exponentially moving average (EMA)
	 * @return high Bolligner band value
	 */
	public static double getBBHigh(List<Number> harray, int kTimes, int nPeriod) {
		double high = 0;
		if (harray != null && harray.size() >= nPeriod) {
			double mean = getEMA(harray, nPeriod);
			double dev = Math.sqrt(getHVariance(harray, mean));
			high = mean + (kTimes * dev);
		}
		return high;
	}

	/**
	 * Compute historical variance for a given set of measurements
	 *
	 * @param harray of numbers (measurements)
	 * @return variance
	 */
	public static double getHVariance(List<Number> harray) {
		return getHVariance(harray, getHMean(harray));
	}

	/**
	 * Compute historical variance given a mean
	 *
	 * @param harray of numbers (measurements)
	 * @param mean computed for the given array of numbers
	 * @return variance
	 */
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

	/**
	 * Compute mean for a given set of numbers
	 *
	 * @param harray of numbers (measurements)
	 * @return mean
	 */
	public static double getHMean(List<Number> harray) {
		Iterator<Number> it = harray.iterator();
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
	
	/**
	 * Compute mean for a given set of numbers and number of elements
	 *
	 * @param harray of numbers (measurements)
	 * @param nPeriod an N-period for which to compute the mean
	 * @return mean
	 */
	public static double getHMean(List<Number> harray, int nPeriod) {
		double vsum = 0, mean = 0;
		int count = 0;
		Iterator<Number> it = harray.iterator();
		while (it.hasNext() && (count < nPeriod)) {
			Number vl = it.next();
			double val = vl.doubleValue();
			vsum += val;
			count++;
		}
		mean = count > 0 ? vsum / count: mean;
		return mean;
	}

	/**
	 * Compute EMA for a given set of numbers 
	 * and 20 N period moving average. 
	 *
	 * @param harray of numbers (measurements)
	 * @return N-period exponentially moving average
	 */
	public static double getEMA(List<Number> harray) {
		return getEMA(harray, 20);
	}

	/**
	 * Compute EMA for a given set of numbers 
	 * and a given N-period moving average
	 *
	 * @param harray of numbers (measurements)
	 * @param nPeriod an N-period for which to compute the EMA
	 * @return N-period exponentially moving average
	 */
	public static double getEMA(List<Number> harray, int nPeriod) {
		if (harray != null && harray.size() > 1) {
			// calculate based EMA(nSamples) which is same as SMA(nPeriod)
			double nEma = getHMean(harray, nPeriod);
			float k = (float) 2 / (float) (nPeriod + 1);
			int size = harray.size();
			for (int i = nPeriod; i < size; i++) {
				Number cVal = harray.get(i);
				nEma = getKoEMA(cVal.doubleValue(), k, nEma);
			}
			return nEma;
		}
		return 0.0;
	}

	/**
	 * Compute EMA for a given number 
	 * and a previous N-period moving average
	 *
	 * @param cVal measured number
	 * @param nPeriod an N-period for which to compute the EMA
	 * @param pEMA previous computed EMA
	 * @return N-period exponentially moving average
	 */
	public static double getEMA(double cVal, int nPeriod, double pEMA) {
		float k = (float) 2 / (float) (nPeriod + 1);
		return getKoEMA(cVal, k, pEMA);
	}

	/**
	 * Compute EMA for a given number, k 
	 * and a given N-period moving average
	 *
	 * @param cVal measured number
	 * @param k weighting coefficient 
	 * @param pEMA previous computed EMA
	 * @return N-period exponentially moving average
	 */
	public static double getKoEMA(double cVal, float k, double pEMA) {
		return (k * (cVal - pEMA)) + pEMA;
	}
}
