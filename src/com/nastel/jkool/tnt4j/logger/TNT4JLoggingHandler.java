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
package com.nastel.jkool.tnt4j.logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.nastel.jkool.tnt4j.TrackingLogger;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.source.SourceType;
import com.nastel.jkool.tnt4j.tracker.TimeTracker;

/**
 * This class implements {@code java.util.logging.Handler} implementation that routes
 * java logging messages over TNT4J.
 */
public class TNT4JLoggingHandler extends Handler {

	TrackingLogger logger;
	
	/**
	 * Create a new logging handler
	 * 
	 * @param name logger/source name
	 */
	public TNT4JLoggingHandler(String name) {
		activate(name, SourceType.APPL);
	}
	
	/**
	 * Create a new logging handler
	 * 
	 * @param name logger/source name
	 * @param type source type
	 */
	public TNT4JLoggingHandler(String name, SourceType type) {
		activate(name, type);
	}
	
	/**
	 * Activate & initialize logging handler
	 *
	 */
	protected void activate(String sourceName, SourceType type) {
		try {
			logger = TrackingLogger.getInstance(sourceName, type);
	        logger.open();
        } catch (IOException e) {
        	this.getErrorManager().error("Unable to create tracker instance=" + sourceName, e, ErrorManager.OPEN_FAILURE);
        }
	}

	/**
	 * Obtain elapsed nanoseconds since last logging event
	 *
	 * @return elapsed nanoseconds since last logging event
	 */
	protected long getUsecsSinceLastEvent() {
		return TimeUnit.NANOSECONDS.toMicros(TimeTracker.hitAndGet());
	}

	@Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            reportError("Failed to format record=" + record, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }

        try {
        	String opName = record.getSourceMethodName() != null? record.getSourceClassName() + "." + record.getSourceMethodName(): record.getLoggerName();
        	logger.tnt(sevToLevel(record.getLevel()), OpType.EVENT, opName, null, getUsecsSinceLastEvent(), msg, record.getThrown());
        } catch (Exception ex) {
            reportError("Failed to write record=" + record, ex, ErrorManager.WRITE_FAILURE);
        }
    }

	private OpLevel sevToLevel(Level level) {
		switch (level.intValue()) {
		case 1000:
			return OpLevel.CRITICAL;
			
		case 900:
			return OpLevel.WARNING;
			
		case 800:
			return OpLevel.INFO;
			
		case 700:
			return OpLevel.INFO;
			
		case 500:
			return OpLevel.DEBUG;
			
		case 400:
		case 300:
			return OpLevel.TRACE;
		
		default:
			return OpLevel.INFO;
			
		}
    }

	@Override
    public void flush() {
		try {
	        logger.getEventSink().flush();
        } catch (IOException e) {
        	this.getErrorManager().error("Unable to flush logger=" + logger, e, ErrorManager.FLUSH_FAILURE);
        }
	}

	@Override
    public void close() throws SecurityException {
		logger.close();
	}

}
