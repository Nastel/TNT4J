/*
 * Copyright 2014-2018 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.sink;

import java.io.IOException;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * This class implements a default abstract class for {@link EventSink} having logger sink branch to write events into.
 *
 * @version $Revision: 1 $
 *
 * @see com.jkoolcloud.tnt4j.sink.impl.SocketEventSink
 */
public abstract class LoggedEventSink extends AbstractEventSink {

	private EventSink logSink;

	/**
	 * Create an event sink with a given name
	 *
	 * @param nm
	 *            event sink name
	 */
	public LoggedEventSink(String nm) {
		super(nm);
	}

	/**
	 * Create an event sink with a given name and logger sink instance.
	 * 
	 * @param nm
	 *            event sink name
	 * @param logSink
	 *            logger sink instance
	 */
	public LoggedEventSink(String nm, EventSink logSink) {
		super(nm);
		this.logSink = logSink;
	}

	/**
	 * Create an event sink with a given name, event formatter and logger sink instance.
	 * 
	 * @param nm
	 *            event sink name
	 * @param fmt
	 *            event formatter instance
	 * @param logSink
	 *            logger sink instance
	 */
	public LoggedEventSink(String nm, EventFormatter fmt, EventSink logSink) {
		super(nm, fmt);
		this.logSink = logSink;
	}

	/**
	 * Create an event sink with a given name, ttl, event formatter and logger sink instance.
	 *
	 * @param nm
	 *            event sink name
	 * @param ttl
	 *            time-to-live for events written to this sink
	 * @param fmt
	 *            event formatter instance
	 * @param logSink
	 *            logger sink instance
	 */
	public LoggedEventSink(String nm, long ttl, EventFormatter fmt, EventSink logSink) {
		super(nm, ttl, fmt);
		this.logSink = logSink;
	}

	@Override
	protected void _write(Object msg, Object... args) throws IOException, InterruptedException {
		if (isOpen()) {
			writeLine(getEventFormatter().format(msg, args));
			if (logSink instanceof AbstractEventSink) {
				((AbstractEventSink) logSink)._write(msg, args);
			}
		}
	}

	@Override
	protected void _log(TrackingEvent event) throws IOException {
		writeLine(getEventFormatter().format(event));
		if (canForward(event.getSeverity())) {
			logSink.log(event);
		}
	}

	@Override
	protected void _log(TrackingActivity activity) throws IOException {
		writeLine(getEventFormatter().format(activity));
		if (canForward(activity.getSeverity())) {
			logSink.log(activity);
		}
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) throws IOException {
		writeLine(getEventFormatter().format(ttl, src, sev, msg, args));
		if (canForward(sev)) {
			logSink.log(ttl, src, sev, msg, args);
		}
	}

	@Override
	protected void _log(Snapshot snapshot) throws Exception {
		writeLine(getEventFormatter().format(snapshot));
		if (canForward(snapshot.getSeverity())) {
			logSink.log(snapshot);
		}
	}

	/**
	 * Check if logging should be forwarded to logger sink.
	 * 
	 * @param sev
	 *            severity level
	 * @return {@code true} if severity level is set for logger sink, {@code false} if not or logger sink is
	 *         {@code null}
	 */
	protected boolean canForward(OpLevel sev) {
		return logSink != null && logSink.isSet(sev);
	}

	@Override
	public boolean isSet(OpLevel sev) {
		// return logSink == null || logSink.isSet(sev);
		return super.isSet(sev);
	}

	/**
	 * Writes message string to sink.
	 * 
	 * @param msg
	 *            message string to write
	 * @throws IOException
	 *             if error occurs while writing message to sink
	 */
	protected abstract void writeLine(String msg) throws IOException;

	@Override
	public String toString() {
		return super.toString() + "{piped.sink: " + logSink + "}";
	}

	@Override
	public void open() throws IOException {
		if (logSink != null && !logSink.isOpen()) {
			logSink.open();
		}
	}

	@Override
	public void close() throws IOException {
		Utils.close(logSink);
	}
}
