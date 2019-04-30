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

/**
 * TLog is sample utility which allows capture of input stream messages (such as STDIN) and pipe them into
 * a TNT4J event sink logger. This utility can be used to stream user entered messages from a console
 * or piped from other command line utilities.
 * 
 * @version $Revision: 1 $
 */

public class TLog implements Closeable {

	/*
	 * Static TLOG instance for capturing input stream
	 */
	private static TLog tlog;

	/*
	 * Tracking logger instance where all messages are logged
	 */
	private TrackingLogger logger;

	/*
	 * Input stream where messages are read from
	 */
	private InputStream input;

	/*
	 * Reader for parsing input stream into lines
	 */
	private Scanner scanner;

	/*
	 * Severity level for incoming messages
	 */	
	private OpLevel level = OpLevel.INFO;
	
	/**
	 * Create instance of TLogger
	 *
	 * @param source log source name
	 * @param sev severity level
	 * @param in input stream for reading messages
	 */
	public TLog(String source, OpLevel sev, InputStream in) {
		input = in;
		level = sev;
		logger = TrackingLogger.getInstance(source);		
	}

	/**
	 * Create instance of TLogger reading from System.in
	 *
	 * @param source log source name
	 * @param sev severity level
	 */
	public TLog(String source, OpLevel sev) {
		this(source, sev, System.in);
	}

	/**
	 * Create instance of TLogger with default severity level of INFO
	 *
	 * @param source log source name
	 * @param in input stream for reading messages
	 */
	public TLog(String source, InputStream in) {
		this(source, OpLevel.INFO, in);
	}

	/**
	 * Create instance of TLogger with default severity level of INFO
	 * and reading from System.in
	 *
	 * @param source log source name
	 */
	public TLog(String source) {
		this(source, OpLevel.INFO, System.in);
	}

	/**
	 * Open TLogger instance
	 *
	 * @throws IOException when IO error occurs
	 */
	public synchronized void open() throws IOException {
		scanner = new Scanner(input);
		logger.open();
	}

	@Override
	public synchronized void close() throws IOException {
		Utils.close(logger);
		Utils.close(scanner);
	}

	/**
	 * Read input stream line by line and log it. This is a blocking call
	 * and terminates when EOF is reached.
	 *
	 */
	public void tlog() {
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			logger.log(level, line);
		}		
	}
	
	/**
	 * Read input stream line by line and log it. This is a blocking call
	 * and terminates when EOF is reached.
	 *
	 * @param level severity level used for logging
	 */
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
				throw new IllegalArgumentException("Unknown option: " + args[i]);
			}
		}
		if (options.size() < 2) {
			throw new IllegalArgumentException("Missing options: --source|-s source --level|-l severity");
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
