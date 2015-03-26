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
package com.nastel.jkool.tnt4j.examples;

/**
 * This class measures clock drift between
 * <code>System.currentTimeMillis()</code> and <code>System.nanoTime()>/code>.
 * 
 * @version $Revision: 1 $
 */
public class ClockDriftTest {
	private static final int ONEM = 1000000;

	public static void main(String[] args) {
		long start = System.nanoTime();
		long base = System.currentTimeMillis() - (start / ONEM);

		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
			long now = System.nanoTime();
			long drift = System.currentTimeMillis() - (now / ONEM) - base;
			long interval = (now - start) / ONEM;
			System.out.println("Clock drift.ms=" + drift + ", after interval.ms="
							+ interval + ", change.rate="
							+ ((double) (drift * 1000) / (double) interval)
							+ " ms/sec");
		}
	}
}