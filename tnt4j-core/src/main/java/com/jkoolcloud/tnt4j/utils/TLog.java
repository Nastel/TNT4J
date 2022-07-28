/*
 * Copyright 2014-2021 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.TrackingLogger;
import com.jkoolcloud.tnt4j.core.OpLevel;

/**
 * TLog is a utility which allows capture standard input stream messages (such as STDIN) and pipe them into a TNT4J
 * event sink logger. TLog utility can be used to stream user entered messages from a console or piped from other
 * command line utilities.
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
	private OpLevel logLevel = OpLevel.INFO;

	/*
	 * Stop key to be used to terminate stream reading
	 */
	private String stopKey = null;

	/**
	 * Create instance of TLogger
	 *
	 * @param source
	 *            log source name
	 * @param level
	 *            severity level
	 * @param in
	 *            input stream for reading messages
	 */
	public TLog(String source, OpLevel level, InputStream in) {
		input = in;
		logLevel = level;
		logger = TrackingLogger.getInstance(source);
	}

	/**
	 * Create instance of TLogger reading from System.in
	 *
	 * @param source
	 *            log source name
	 * @param level
	 *            severity level
	 */
	public TLog(String source, OpLevel level) {
		this(source, level, System.in);
	}

	/**
	 * Create instance of TLogger with default severity level of INFO
	 *
	 * @param source
	 *            log source name
	 * @param in
	 *            input stream for reading messages
	 */
	public TLog(String source, InputStream in) {
		this(source, OpLevel.INFO, in);
	}

	/**
	 * Create instance of TLogger with default severity level of INFO and reading from System.in
	 *
	 * @param source
	 *            log source name
	 */
	public TLog(String source) {
		this(source, OpLevel.INFO, System.in);
	}

	/**
	 * Open TLogger instance
	 *
	 * @throws IOException
	 *             when IO error occurs
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
	 * Define a stop key used to detect end of the stream and stop reading the input stream. Line must start with the
	 * stop key to terminate reading.
	 *
	 * @param key
	 *            stop key
	 */
	public void setStopKey(String key) {
		stopKey = key;
	}

	/**
	 * Return stop key word associated with the logger
	 *
	 * @return stop key associated with the logger, null if none.
	 */
	public String getStopKey() {
		return stopKey;
	}

	/**
	 * Read input stream line by line and log it. This is a blocking call and terminates when EOF is reached or stop key
	 * word detected.
	 * 
	 */
	public void tlog() {
		tlog(logLevel);
	}

	/**
	 * Read input stream line by line and log it. This is a blocking call and terminates when EOF is reached or stop key
	 * word (if defined) is detected.
	 *
	 * @param level
	 *            severity level used for logging
	 */
	public void tlog(OpLevel level) {
		if (scanner == null) {
			throw new IllegalStateException("logger not opened. open() must be called first.");
		}
		synchronized (scanner) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (stopKey != null && line.startsWith(stopKey)) {
					break;
				}
				logger.log(level, line);
			}
		}
	}

	private static Map<String, String> parseOptions(String[] args) {
		HashMap<String, String> options = new HashMap<>();
		options.put("stopkey", "end");
		for (int i = 0; i < args.length; i++) {
			if (StringUtils.equalsAny(args[i], "--source", "-s")) {
				options.put("source", args[++i]);
			} else if (StringUtils.equalsAny(args[i], "--level", "-l")) {
				options.put("level", args[++i]);
			} else if (StringUtils.equalsAny(args[i], "--stopkey", "-k")) {
				options.put("stopkey", args[++i]);
			} else {
				throw new IllegalArgumentException("Unknown option: " + args[i]);
			}
		}
		if (options.size() < 3) {
			throw new IllegalArgumentException(
					"Missing options: --source|-s source --level|-l severity --stopkey|-k stop-keyword");
		}
		return options;
	}

	public static void main(String[] args) throws IOException {
		try {
			Map<String, String> options = parseOptions(args);
			System.out.println("Options: " + options);
			String source = options.get("source");
			String stopKey = options.get("stopkey");
			OpLevel level = OpLevel.valueOf(options.get("level"));

			tlog = new TLog(source, level);
			tlog.setStopKey(stopKey);
			tlog.open();
			tlog.tlog();
		} catch (IllegalArgumentException la) {
			System.err.println(la.getMessage());
		} finally {
			Utils.close(tlog);
		}
	}
}
