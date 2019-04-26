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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.jkoolcloud.tnt4j.TrackingLogger;
import com.jkoolcloud.tnt4j.core.OpLevel;

public class TLog implements Closeable {

	/*
	 * Static TLOG instance for capturing input stream
	 */
	private static TLog tlog;
	/*
	 * Tracking logger instance where all messages are logged.
	 */
	private TrackingLogger logger;
	private InputStream input;
	private Scanner scanner;
	private OpLevel level = OpLevel.INFO;
	
	public TLog(String source, OpLevel sev, InputStream in) {
		input = in;
		level = sev;
		logger = TrackingLogger.getInstance(source);		
	}

	public TLog(String source, OpLevel sev) {
		this(source, sev, System.in);
	}

	public TLog(String source, InputStream in) {
		this(source, OpLevel.INFO, in);
	}

	public TLog(String source) {
		this(source, OpLevel.INFO, System.in);
	}

	public void open () throws IOException {
		scanner = new Scanner(input);
		logger.open();
	}

	@Override
	public void close() throws IOException {
		Utils.close(logger);
		scanner.close();
	}

	public void tlog() {
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			logger.log(level, line);
		}		
	}
	
	public void tlog(OpLevel level) {
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			logger.log(level, line);
		}		
	}
		
	private static Map<String, String> parseOptions(String[] args) {
		HashMap<String, String> options = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--source") || (args[i].equals("-s"))) {
				options.put("source", args[++i]);
			} else if (args[i].equals("--level") || (args[i].equals("-l"))) {
				options.put("level", args[++i]);
			} else {
				throw new IllegalArgumentException("Uknown option: " + args[i]);
			}
		}
		if (options.size() < 2) {
			throw new IllegalArgumentException("Missing options: --source|-l source --level|-l severity");
		}
		return options;
	}

	public static void main(String[] args) throws IOException {
		try {
			Map<String, String> options = parseOptions(args);
			System.out.println("Options: " + options);
			String source = options.get("source");
			OpLevel level = OpLevel.valueOf(options.get("level"));

			tlog = new TLog(source, level);
			tlog.open();
			tlog.tlog();			
		} catch (IllegalArgumentException la) {
			System.err.println(la.getMessage());
		} finally {
			Utils.close(tlog);
		}
	}
}
