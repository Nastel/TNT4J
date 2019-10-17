/*
 * Copyright 2014-2019 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.sink.impl.jul;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSink;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * Concrete implementation of {@link EventSink} interface for java unified logging (JUL). The event sink uses file
 * handler and JUL XML formatter.
 * </p>
 *
 *
 * @see AbstractEventSink
 * @see JULEventSinkFactory
 *
 * @version $Revision: 1 $
 *
 */
public class JULEventSink extends AbstractEventSink {

	protected String pattern;
	protected int logCount = 3;
	protected int byteLimit = 10 * 1024 * 1024; // 10MB
	protected boolean append = true;
	protected Level level = Level.FINE;
	protected FileHandler fhandler;
	protected Logger logger;
	protected Formatter jFmt;

	/**
	 * Create logging event sink
	 * 
	 * @param name
	 *            event sink name
	 * @param pattern
	 *            log file pattern
	 */
	public JULEventSink(String name, String pattern) {
		super(name);
		this.pattern = pattern;
		logger = Logger.getLogger(name);
	}

	/**
	 * Create logging event sink
	 * 
	 * @param name
	 *            event sink name
	 * @param pattern
	 *            log file pattern
	 * @param byteLimit
	 *            log file size in bytes
	 * @param logCount
	 *            number of logs before rotation
	 * @param append
	 *            append to file or not
	 * @param frmt
	 *            message formatter
	 * @param jfmt
	 *            JUL message formatter
	 */
	public JULEventSink(String name, String pattern, int byteLimit, int logCount, boolean append, Level level,
			EventFormatter frmt, Formatter jfmt) {
		this(name, pattern);
		this.setEventFormatter(frmt);
		this.logCount = logCount;
		this.byteLimit = byteLimit;
		this.append = append;
		this.level = level;
		this.jFmt = jfmt;
	}

	@Override
	public Object getSinkHandle() {
		return logger;
	}

	@Override
	public boolean isOpen() {
		return fhandler != null;
	}

	@Override
	protected synchronized void _open() throws IOException {
		if (!isOpen()) {
			fhandler = new FileHandler(pattern, byteLimit, logCount, append);
			fhandler.setFormatter(jFmt);
			logger.setLevel(level);
			logger.addHandler(fhandler);
		}
	}

	@Override
	protected synchronized void _close() throws IOException {
		if (isOpen()) {
			logger.removeHandler(fhandler);
			fhandler.close();
			fhandler = null;
		}
	}

	@Override
	protected void _write(Object msg, Object... args) throws IOException, InterruptedException {
		_writeLog(Level.INFO, getEventFormatter().format(msg, args));
	}

	@Override
	protected void _log(TrackingEvent event) throws IOException {
		_writeLog(getLevel(event.getSeverity()), getEventFormatter().format(event));
	}

	@Override
	protected void _log(TrackingActivity activity) throws IOException {
		_writeLog(getLevel(activity.getSeverity()), getEventFormatter().format(activity));
	}

	@Override
	protected void _log(Snapshot snapshot) {
		_writeLog(getLevel(snapshot.getSeverity()), getEventFormatter().format(snapshot));
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) {
		_writeLog(getLevel(sev), getEventFormatter().format(msg, args));
	}

	@Override
	public void flush() {
		fhandler.flush();
	}

	@Override
	public String toString() {
		return super.toString() + "{" + this.getClass().getName() + ": " + fhandler + "}";
	}

	protected synchronized void _writeLog(Level level, String msg) {
		_checkState();
		incrementBytesSent(msg.length());
		logger.log(level, msg);
	}

	/**
	 * Convert {@link OpLevel} to Level
	 * 
	 * @param sev
	 *            severity
	 * @return logging level
	 */
	private Level getLevel(OpLevel sev) {
		switch (sev) {
		case INFO:
			return Level.INFO;
		case DEBUG:
			return Level.FINE;
		case TRACE:
			return Level.FINER;
		case NOTICE:
		case WARNING:
			return Level.WARNING;
		case ERROR:
		case CRITICAL:
		case FAILURE:
		case FATAL:
		case HALT:
			return Level.SEVERE;
		case NONE:
			return Level.OFF;
		default:
			return Level.INFO;
		}
	}
}
