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
package com.jkoolcloud.tnt4j.sink.impl.slf4j;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.impl.LoggerEventSink;

/**
 * <p>
 * {@link com.jkoolcloud.tnt4j.sink.EventSink} implementation that routes log messages to SLF4J. This implementation is
 * designed to log messages over SLF4J framework.
 * </p>
 * 
 * @version $Revision: 12 $
 *
 * @see com.jkoolcloud.tnt4j.tracker.TrackingEvent
 * @see com.jkoolcloud.tnt4j.format.EventFormatter
 * @see com.jkoolcloud.tnt4j.core.OpLevel
 * @see com.jkoolcloud.tnt4j.sink.impl.slf4j.SLF4JEventSinkFactory
 */
public class SLF4JEventSink extends LoggerEventSink {

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
	}

	@Override
	public boolean isSet(OpLevel sev) {
		_checkState();

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

	@Override
	protected void writeLine(OpLevel sev, LogEntry entry, Throwable t) {
		if (!isSet(sev)) {
			return;
		}

		switch (sev) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			String msg = entry.getString();
			incrementBytesSent(msg.length());
			logger.error(msg, t);
			break;
		case DEBUG:
			msg = entry.getString();
			incrementBytesSent(msg.length());
			logger.debug(msg, t);
			break;
		case TRACE:
			msg = entry.getString();
			incrementBytesSent(msg.length());
			logger.trace(msg, t);
			break;
		case NOTICE:
		case WARNING:
			msg = entry.getString();
			incrementBytesSent(msg.length());
			logger.warn(msg, t);
			break;
		case NONE:
			break;
		case INFO:
		default:
			msg = entry.getString();
			incrementBytesSent(msg.length());
			logger.info(msg, t);
			break;
		}
	}
}
