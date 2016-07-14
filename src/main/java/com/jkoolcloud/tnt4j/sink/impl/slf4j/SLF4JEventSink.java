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
package com.jkoolcloud.tnt4j.sink.impl.slf4j;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSink;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * {@code EventSink} implementation that routes log messages to SLF4J. This implementation is designed to log
 * messages over SLF4J framework.
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
		open();
	}

	@Override
	protected void _log(TrackingEvent event) {
		switch (event.getSeverity()) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			logger.error(getEventFormatter().format(event), event.getOperation().getThrowable());
			break;
		case DEBUG:
			logger.debug(getEventFormatter().format(event), event.getOperation().getThrowable());
			break;
		case INFO:
		case NONE:
			logger.info(getEventFormatter().format(event), event.getOperation().getThrowable());
			break;
		case TRACE:
			logger.trace(getEventFormatter().format(event), event.getOperation().getThrowable());
			break;
		case NOTICE:
		case WARNING:
			logger.warn(getEventFormatter().format(event), event.getOperation().getThrowable());
			break;
		default:
			logger.info(getEventFormatter().format(event), event.getOperation().getThrowable());
			break;
		}
	}

	@Override
	protected void _log(TrackingActivity activity) {
		Throwable ex = activity.getThrowable();
		switch (activity.getSeverity()) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			logger.error(getEventFormatter().format(activity), ex);
			break;
		case DEBUG:
			logger.debug(getEventFormatter().format(activity), ex);
			break;
		case INFO:
		case NONE:
			logger.info(getEventFormatter().format(activity), ex);
			break;
		case TRACE:
			logger.trace(getEventFormatter().format(activity), ex);
			break;
		case NOTICE:
		case WARNING:
			logger.warn(getEventFormatter().format(activity), ex);
			break;
		default:
			logger.info(getEventFormatter().format(activity), ex);
			break;
		}
	}

	@Override
    protected void _log(Snapshot snapshot) {
		switch (snapshot.getSeverity()) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			logger.error(getEventFormatter().format(snapshot));	
			break;
		case DEBUG:
			logger.debug(getEventFormatter().format(snapshot));	
			break;
		case INFO:
		case NONE:
			logger.info(getEventFormatter().format(snapshot));	
			break;
		case TRACE:
			logger.trace(getEventFormatter().format(snapshot));	
			break;
		case NOTICE:
		case WARNING:
			logger.warn(getEventFormatter().format(snapshot));	
			break;
		default:
			logger.info(getEventFormatter().format(snapshot));	
			break;
		}
	}
	
	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) {
		switch (sev) {
		case HALT:
		case FATAL:
		case CRITICAL:
		case FAILURE:
		case ERROR:
			logger.error(getEventFormatter().format(ttl, src, sev, msg, args), Utils.getThrowable(args));
			break;
		case DEBUG:
			logger.debug(getEventFormatter().format(ttl, src, sev, msg, args), Utils.getThrowable(args));
			break;
		case INFO:
		case NONE:
			logger.info(getEventFormatter().format(ttl, src, sev, msg, args), Utils.getThrowable(args));
			break;
		case TRACE:
			logger.trace(getEventFormatter().format(ttl, src, sev, msg, args), Utils.getThrowable(args));
			break;
		case NOTICE:
		case WARNING:
			logger.warn(getEventFormatter().format(ttl, src, sev, msg, args), Utils.getThrowable(args));
			break;
		default:
			logger.info(getEventFormatter().format(ttl, src, sev, msg, args), Utils.getThrowable(args));
			break;
		}
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
		case INFO:
		case NONE:
			return logger.isInfoEnabled();
		case TRACE:
			return logger.isTraceEnabled();
		case NOTICE:
		case WARNING:
			return logger.isWarnEnabled();
		default:
			return logger.isInfoEnabled();
		}
	}
	
	@Override
	protected void _write(Object msg, Object... args) throws IOException {
		logger.info(getEventFormatter().format(msg, args));
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
	public synchronized void open() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(getName());
		}
	}

	@Override
	public void close() throws IOException {
	}	
}
