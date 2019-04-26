/*
 * Copyright 2014-2019 JKOOL, LLC.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.jkoolcloud.tnt4j.TrackingLogger;
import com.jkoolcloud.tnt4j.core.OpLevel;

public class Tlog {

	/*
	 * Tracking logger instance where all messages are logged.
	 */
	private static TrackingLogger logger;
	private static Scanner scanner;

	public static void main(String[] args) throws IOException {
		try {
			scanner = new Scanner(System.in);
			Map<String, String> options = parseOptions(args);
			System.out.println("Options: " + options);

			OpLevel level = OpLevel.valueOf(options.get("level"));
			logger = TrackingLogger.getInstance(options.get("source"));
			logger.open();
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				logger.log(level, line);
			}
		} finally {
			Utils.close(logger);
			scanner.close();
		}
	}

	private static Map<String, String> parseOptions(String[] args) {
		HashMap<String, String> options = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-source")) {
				options.put("source", args[++i]);
			} else if (args[i].equals("-level")) {
				options.put("level", args[++i]);
			} else {
				throw new IllegalArgumentException("Uknown option: " + args[i]);
			}
		}
		if (options.size() < 2) {
			throw new IllegalArgumentException("Missing options: -source source -level severity");
		}
		return options;
	}
}
