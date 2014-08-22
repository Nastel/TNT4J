/*
 * Copyright 2014 Nastel Technologies, Inc.
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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.nastel.jkool.tnt4j.TrackingLogger;
import com.nastel.jkool.tnt4j.core.ActivityStatus;
import com.nastel.jkool.tnt4j.core.OpCompCode;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.source.DefaultSourceFactory;
import com.nastel.jkool.tnt4j.source.SourceType;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * <p>Log4j appender for sending log4j events to TNT4j logging framework.</p>
 *
 * <p>This appender will extract information from the log4j {@code LoggingEvent} and construct the
 * appropriate message for sending to TNT4j.</p>
 *
 * <p>This appender has the following behavior:</p>
 * <ul>
 * <li>This appender does use a layout.</li>
 *
 * <li>All messages send to this appender will be send to all defined sinks as configured by tnt4j configuration.</li>
 *
 * </ul>
 *
 * <p>This appender supports the following properties:</p>
 * <table cellspacing=10>
 * <tr><td valign=top><b>metricsOnException</b></td><td valign=top>report jvm metrics on exception (true|false)</td></tr>
 * <tr><td valign=top><b>metricsFrequency</b></td><td valign=top>report jvm metrics on every specified number of seconds (only on logging activity)</td></tr>
 * </table>
 *
 * <p>This appender by default sets the following TNT4j Activity and Event parameters based on the information
 * in the log4j event, as follows:</p>
 * <table cellspacing=10>
 * <tr><td valign=top><b>TNT4j Parameter</b></td>	<td valign=top><b>Log4j Event field</b></td></tr>
 * <tr><td valign=top>Tag</td>						<td valign=top>Thread name</td></tr>
 * <tr><td valign=top>Severity</td>					<td valign=top>Level</td></tr>
 * <tr><td valign=top>Completion Code</td>			<td valign=top>Level</td></tr>
 * <tr><td valign=top>Message Data</td>				<td valign=top>Message</td></tr>
 * <tr><td valign=top>Start Time</td>				<td valign=top>Timestamp</td></tr>
 * <tr><td valign=top>End Time</td>					<td valign=top>Timestamp</td></tr>
 * </table>
 *
 * <p>In addition, it will set other TNT4j Activity and Event parameters based on the local environment.  These default
 * parameter values can be overridden by annotating the log event messages.
 *
  * <p>The following annotations are supported:</p>
 * <table>
 * <tr><td><b>app</b></td>				<td>Application name</td></tr>
 * <tr><td><b>usr</b></td>				<td>User name</td></tr>
 * <tr><td><b>cid</b></td>				<td>Correlator for relating events</td></tr>
 * <tr><td><b>tag</b></td>				<td>User defined tag</td></tr>
 * <tr><td><b>loc</b></td>				<td>Location specifier</td></tr>
 * <tr><td><b>opn</b></td>			    <td>Event/Operation name</td></tr>
 * <tr><td><b>opt</b></td>			    <td>Event/Operation Type - Value must be either a member of {@link OpType} or the equivalent numeric value</td></tr>
 * <tr><td><b>msg</b></td>				<td>Event message (user data)</td></tr>
 * <tr><td><b>sev</b></td>				<td>Event Severity - Value can be either a member of {@link OpLevel} or any numeric value</td></tr>
 * <tr><td><b>ccd</b></td>				<td>Event Completion Code - Value must be either a member of {@link OpCompCode} or the equivalent numeric value</td></tr>
 * <tr><td><b>rcd</b></td>				<td>Reason Code</td></tr>
 * <tr><td><b>elt</b></td>			    <td>Elapsed Time of event, in milliseconds</td></tr>
 * <tr><td><b>stt</b></td>			    <td>Start Time, as the number of milliseconds since epoch</td></tr>
 * <tr><td><b>ent</b></td>				<td>End Time, as the number of milliseconds since epoch</td></tr>
 * <tr><td><b>rsn</b></td>				<td>Resource name on which operation/event took place</td></tr>
 * </table>
 *
 * <p>An example of annotating (TNT4J) a log message using log4j:</p>
 * <p><code>logger.error("Operation Failed #app=MyApp #opn=save #rsn=" + filename + "  #rcd="
 *  + errno);</code></p>
 *  
 *  
 * @version $Revision: 1 $
 * 
 */

public class TNT4JAppender extends AppenderSkeleton {
	public static final String DEFAULT_OP_NAME 			 = "LoggingEvent";
	
	public static final String PARAM_APPL_LABEL          = "app";
	public static final String PARAM_USER_LABEL          = "usr";
	public static final String PARAM_CORRELATOR_LABEL    = "cid";
	public static final String PARAM_TAG_LABEL           = "tag";
	public static final String PARAM_LOCATION_LABEL      = "loc";
	public static final String PARAM_OP_NAME_LABEL       = "opn";
	public static final String PARAM_OP_TYPE_LABEL       = "opt";
	public static final String PARAM_RESOURCE_LABEL      = "rsn";
	public static final String PARAM_MSG_DATA_LABEL      = "msg";
	public static final String PARAM_SEVERITY_LABEL      = "sev";
	public static final String PARAM_COMP_CODE_LABEL     = "ccd";
	public static final String PARAM_REASON_CODE_LABEL   = "rcd";
	public static final String PARAM_START_TIME_LABEL    = "stt";
	public static final String PARAM_END_TIME_LABEL      = "ent";
	public static final String PARAM_ELAPSED_TIME_LABEL  = "elt";
	
	private TrackingLogger logger;
	private boolean metricsOnException = true;
	private long metricsFrequency = 60, lastSnapshot = 0;

	@Override
	public void activateOptions() {
		try {
			logger = TrackingLogger.getInstance(getName(), SourceType.VIRTUAL);
	        logger.open();
        } catch (IOException e) {
	        LogLog.error("Unable to create tnt4j tracker instance=" + getName(), e);
        }
	}

	@Override
	protected void append(LoggingEvent event) {
		long lastReport = System.currentTimeMillis();

		String eventMsg = event.getRenderedMessage();
		ThrowableInformation ti = event.getThrowableInformation();
		Throwable ex = ti != null? ti.getThrowable(): null;

		OpLevel level = getOpLevel(event);
		TrackingEvent tev = logger.newEvent(level, DEFAULT_OP_NAME, null, eventMsg);
		tev.setTag(event.getThreadName());
		processEventMessage(tev, event, eventMsg, ex);
		
		boolean reportMetrics = (ex != null && metricsOnException) || ((lastReport - lastSnapshot) > (metricsFrequency * 1000)); 
		
		if (reportMetrics) {
			// report a single tracking event as part of an activity
			TrackingActivity activity = logger.newActivity(level, getName());
			activity.start(tev.getOperation().getStartTime().getTimeMillis());
			activity.setException(ex);
			activity.setStatus(ex != null? ActivityStatus.EXCEPTION: ActivityStatus.END);
			activity.stop(tev.getOperation().getEndTime().getTimeMillis());
			activity.tnt(tev);
			logger.tnt(activity);
			lastSnapshot = lastReport;
		} else {
			// report a single tracking event as datagram
			logger.tnt(tev);
		}
	}

	private TrackingEvent processEventMessage(TrackingEvent event, LoggingEvent jev, String eventMsg, Throwable ex) {
		int rcode = 0;
		boolean sourced = false;
		OpCompCode ccode = ex == null? OpCompCode.SUCCESS: OpCompCode.ERROR;
		long evTime = jev.getTimeStamp(), startTime = 0, elapsedTime= 0, endTime = 0;
	
		Map<String, String> attrs = parseEventMessage(eventMsg);
		for (Map.Entry<String, String> entry: attrs.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.equalsIgnoreCase(PARAM_CORRELATOR_LABEL)) {
				event.setCorrelator(value);
			} else if (key.equalsIgnoreCase(PARAM_TAG_LABEL)) {
				event.setTag(value);
			} else if (key.equalsIgnoreCase(PARAM_LOCATION_LABEL)) {
				event.setLocation(value);
			} else if (key.equalsIgnoreCase(PARAM_RESOURCE_LABEL)) {
				event.getOperation().setResource(value);
			}  else if (key.equalsIgnoreCase(PARAM_USER_LABEL)) {
				event.getOperation().setUser(value);
			} else if (key.equalsIgnoreCase(PARAM_ELAPSED_TIME_LABEL)) {
				elapsedTime = Long.parseLong(value);
			} else if (key.equalsIgnoreCase(PARAM_START_TIME_LABEL)) {
				startTime = Long.parseLong(value);
			} else if (key.equalsIgnoreCase(PARAM_START_TIME_LABEL)) {
				endTime = Long.parseLong(value);
			} else if (key.equalsIgnoreCase(PARAM_REASON_CODE_LABEL)) {
				rcode = Integer.parseInt(value);
			} else if (key.equalsIgnoreCase(PARAM_COMP_CODE_LABEL)) {
				ccode = OpCompCode.valueOf(value);
			} else if (key.equalsIgnoreCase(PARAM_SEVERITY_LABEL)) {
				event.getOperation().setSeverity(OpLevel.valueOf(value));
			} else if (key.equalsIgnoreCase(PARAM_OP_TYPE_LABEL)) {
				event.getOperation().setType(OpType.valueOf(value));
			} else if (key.equalsIgnoreCase(PARAM_OP_NAME_LABEL)) {
				event.getOperation().setName(value);
			} else if (key.equalsIgnoreCase(PARAM_MSG_DATA_LABEL)) {
				event.setMessage(value);
			} else if (key.equalsIgnoreCase(PARAM_APPL_LABEL)) {
				event.setSource(DefaultSourceFactory.getInstance().newSource(value));
				sourced = true;
			}
		}		
		startTime = startTime == 0? (evTime - elapsedTime): evTime;
		endTime = endTime == 0? (startTime + elapsedTime): endTime;
		
		event.start(startTime);
		event.stop(ccode, rcode, ex, endTime);
		if (!sourced) {
			event.setSource(DefaultSourceFactory.getInstance().newSource(jev.getLogger().getName()));
		}
		return event;
	}
	
	private OpLevel getOpLevel(LoggingEvent event) {
		Level lvl = event.getLevel();
		if (lvl == Level.INFO) {
			return OpLevel.FATAL;
		}
		else if (lvl == Level.ERROR) {
			return OpLevel.ERROR;
		}
		else if (lvl == Level.WARN) {
			return OpLevel.WARNING;
		}
		else if (lvl == Level.DEBUG) {
			return OpLevel.DEBUG;
		}
		else if (lvl == Level.TRACE) {
			return OpLevel.TRACE;
		}
		else if (lvl == Level.OFF) {
			return OpLevel.NONE;
		}
		else {
			return OpLevel.INFO;
		}	
	}

	private Map<String, String> parseEventMessage(String msg) {
		HashMap<String, String> tags = new HashMap<String,String>();
		int curPos = 0;
		while (curPos < msg.length()) {
			if (msg.charAt(curPos) != '#') {
				curPos++;
				continue;
			}

			int start = ++curPos;
			boolean inValue = false;
			boolean quotedValue = false;
			while (curPos < msg.length()) {
				char c = msg.charAt(curPos);
				if (c == '=') {
					inValue = true;
				}
				else if (c == '"' && inValue && msg.charAt(curPos-1) != '\\') {
					// the double quote we just read was not escaped, so include it in value
					if (quotedValue) {
						// found closing quote
						curPos++;
						break;
					}
					else {
						quotedValue = true;
					}
				}
				else if (Character.isWhitespace(c) && !quotedValue) {
					break;
				}
				curPos++;
			}

			if (curPos > start) {
				String[] curTag = msg.substring(start, curPos).split("=");
				String name = curTag[0].trim().replace("\"", "");
				String value = (curTag.length > 1 ? curTag[1] : "");
				if (value.startsWith("\"") && value.endsWith("\""))
					value = StringEscapeUtils.unescapeJava(value.substring(1, value.length()-1));
				tags.put(name, value);
			}
		}
		return tags;
	}

	@Override
    public void close() {
		if (logger != null) {
			logger.close();
		}
	}

	@Override
    public boolean requiresLayout() {
	    return true;
    }
	
	public boolean getMetricsOnException() {
		return metricsOnException;
	}
	
	public void setMetricsOnException(boolean flag) {
		metricsOnException = flag;
	}
	
	public long getMetricsFrequency() {
		return metricsFrequency;
	}
	
	public void setMetricsFrequency(long freqSec) {
		metricsFrequency = freqSec;
	}
}
