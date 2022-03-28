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

package com.jkoolcloud.tnt4j.sink.impl;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSink;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * Abstract {@link com.jkoolcloud.tnt4j.sink.EventSink} implementation that routes log messages to underlying logger,
 * e.g. JUL, LOG4J, SLF4J.
 * 
 * @version $Revision: 1 $
 */
public abstract class LoggerEventSink extends AbstractEventSink {

	/**
	 * Create a logger event sink with a given name
	 *
	 * @param nm
	 *            event sink name
	 */
	public LoggerEventSink(String nm) {
		super(nm);
	}

	/**
	 * Create a logger event sink with a given name and event formatter and default
	 * {@link com.jkoolcloud.tnt4j.core.TTL}
	 *
	 * @param nm
	 *            event sink name
	 * @param fmt
	 *            event formatter instance
	 */
	public LoggerEventSink(String nm, EventFormatter fmt) {
		super(nm, fmt);
	}

	/**
	 * Create a logger event sink with a given name and event formatter
	 *
	 * @param nm
	 *            event sink name
	 * @param ttl
	 *            time to live for events written to this sink
	 * @param fmt
	 *            event formatter instance
	 *
	 * @see com.jkoolcloud.tnt4j.core.TTL
	 */
	public LoggerEventSink(String nm, long ttl, EventFormatter fmt) {
		super(nm, ttl, fmt);
	}

	@Override
	protected void _log(TrackingEvent event) {
		writeLine(event.getSeverity(), new LogEntry() {
			@Override
			public String getString() {
				return getEventFormatter().format(event);
			}
		}, event.getOperation().getThrowable());
	}

	@Override
	protected void _log(TrackingActivity activity) {
		writeLine(activity.getSeverity(), new LogEntry() {
			@Override
			public String getString() {
				return getEventFormatter().format(activity);
			}
		}, activity.getThrowable());
	}

	@Override
	protected void _log(Snapshot snapshot) {
		writeLine(snapshot.getSeverity(), new LogEntry() {
			@Override
			public String getString() {
				return getEventFormatter().format(snapshot);
			}
		}, null);
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) {
		writeLine(sev, new LogEntry() {
			@Override
			public String getString() {
				return getEventFormatter().format(ttl, src, sev, msg, args);
			}
		}, Utils.getThrowable(args));
	}

	@Override
	protected void _write(Object msg, Object... args) {
		writeLine(OpLevel.INFO, new LogEntry() {
			@Override
			public String getString() {
				return getEventFormatter().format(msg, args);
			}
		}, Utils.getThrowable(args));
	}

	/**
	 * Writes log entry line.
	 * 
	 * @param sev
	 *            severity of log entry
	 * @param entry
	 *            entry to log
	 * @param t
	 *            throwable bound to entry
	 */
	protected abstract void writeLine(OpLevel sev, LogEntry entry, Throwable t);

	/**
	 * Interface for log entry.
	 */
	protected interface LogEntry {
		/**
		 * Returns log entry as string.
		 * 
		 * @return log entry string
		 */
		String getString();
	}
}
