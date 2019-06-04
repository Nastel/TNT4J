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
package com.jkoolcloud.tnt4j.sink.impl.slf4j;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSink;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * {@code EventSink} implementation that routes log messages to SLF4J. This implementation is designed to log messages
 * over SLF4J framework.
 * </p>
 * 
 * 
 * @see TrackingEvent
 * @see EventFormatter
 * @see AbstractEventSink
 * @see OpLevel
 * 
 * @version $Revision: 11 $
 * 
 */
public class SLF4JEventSink extends AbstractEventSink {

	private Logger logger = null;

	/**
	 * Create a new slf4j backed event sink
	 * 
	 * @param name
	 *            slf4j event category/application name
	 * @param props
	 *            java properties used by the event sink
	 * @param frmt
	 *            event formatter used to format event entries
	 * 
	 */
	public SLF4JEventSink(String name, Properties props, EventFormatter frmt) {
		super(name, frmt);
		_open();
	}

	@Override
	protected void _log(TrackingEvent event) {
		writeLine(event.getSeverity(), getEventFormatter().format(event), event.getOperation().getThrowable());
	}

	@Override
	protected void _log(TrackingActivity activity) {
		writeLine(activity.getSeverity(), getEventFormatter().format(activity), activity.getThrowable());
	}

	@Override
	protected void _log(Snapshot snapshot) {
		writeLine(snapshot.getSeverity(), getEventFormatter().format(snapshot), null);
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) {
		writeLine(sev, getEventFormatter().format(ttl, src, sev, msg, args), Utils.getThrowable(args));
	}

	@Override
	public boolean isSet(OpLevel sev) {
		switch (sev) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			return logger.isErrorEnabled();
		case DEBUG:
			return logger.isDebugEnabled();
		case TRACE:
			return logger.isTraceEnabled();
		case NOTICE:
		case WARNING:
			return logger.isWarnEnabled();
		case NONE:
			return false;
		case INFO:
		default:
			return logger.isInfoEnabled();
		}
	}

	@Override
	protected void _write(Object msg, Object... args) throws IOException {
		writeLine(OpLevel.INFO, getEventFormatter().format(msg, args), Utils.getThrowable(args));
	}

	@Override
	public Object getSinkHandle() {
		return logger;
	}

	@Override
	public boolean isOpen() {
		return logger != null;
	}

	@Override
	protected synchronized void _open() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(getName());
		}
	}

	@Override
	protected void _close() throws IOException {
	}

	private void writeLine(OpLevel sev, String msg, Throwable t) {
		switch (sev) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			incrementBytesSent(msg.length());
			logger.error(msg, t);
			break;
		case DEBUG:
			incrementBytesSent(msg.length());
			logger.debug(msg, t);
			break;
		case TRACE:
			incrementBytesSent(msg.length());
			logger.trace(msg, t);
			break;
		case NOTICE:
		case WARNING:
			incrementBytesSent(msg.length());
			logger.warn(msg, t);
			break;
		case NONE:
			break;
		case INFO:
		default:
			incrementBytesSent(msg.length());
			logger.info(msg, t);
			break;
		}
	}
}
