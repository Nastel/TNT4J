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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.nastel.jkool.tnt4j.TrackingLogger;
import com.nastel.jkool.tnt4j.core.ActivityStatus;
import com.nastel.jkool.tnt4j.core.OpCompCode;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.core.Snapshot;
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
 * 
 * <li>This appender does not require a layout.</li>
 * <li>TNT4J hash tags can be passed using log4j messages (using <code>#tag=value</code> convention) as well as {@code MDC}.</li>
 * <li>All messages logged to this appender will be sent to all defined sinks as configured by tnt4j configuration.</li>
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
 * parameter values can be overridden by annotating the log event messages or passing them using {@code MDC}.
 *
 * <p>The following '#' hash tag annotations are supported for reporting activities:</p>
 * <table>
 * <tr><td><b>beg</b></td>				<td>Begin an activity (collection of related events/messages)</td></tr>
 * <tr><td><b>end</b></td>				<td>End an activity (collection of related events/messages)</td></tr>
 * <tr><td><b>app</b></td>				<td>Application/source name</td></tr>
 * </table>
 * 
 * <p>The following '#' hash tag annotations are supported for reporting events:</p>
 * <table>
 * <tr><td><b>app</b></td>				<td>Application/source name</td></tr>
 * <tr><td><b>usr</b></td>				<td>User name</td></tr>
 * <tr><td><b>cid</b></td>				<td>Correlator for relating events across threads, applications, servers</td></tr>
 * <tr><td><b>tag</b></td>				<td>User defined tag</td></tr>
 * <tr><td><b>loc</b></td>				<td>Location specifier</td></tr>
 * <tr><td><b>opn</b></td>			    <td>Event/Operation name</td></tr>
 * <tr><td><b>opt</b></td>			    <td>Event/Operation Type - Value must be either a member of {@link OpType} or the equivalent numeric value</td></tr>
 * <tr><td><b>rsn</b></td>				<td>Resource name on which operation/event took place</td></tr>
 * <tr><td><b>msg</b></td>				<td>Event message (user data) enclosed in single quotes e.g. <code>#msg='My error message'<code></td></tr>
 * <tr><td><b>sev</b></td>				<td>Event severity - Value can be either a member of {@link OpLevel} or any numeric value</td></tr>
 * <tr><td><b>ccd</b></td>				<td>Event completion code - Value must be either a member of {@link OpCompCode} or the equivalent numeric value</td></tr>
 * <tr><td><b>rcd</b></td>				<td>Reason code</td></tr>
 * <tr><td><b>elt</b></td>			    <td>Elapsed time of event, in microseconds</td></tr>
 * <tr><td><b>age</b></td>			    <td>Message/event age in microseconds (useful when receiving messages, designating message age on receipt)</td></tr>
 * <tr><td><b>stt</b></td>			    <td>Start time, as the number of microseconds since epoch</td></tr>
 * <tr><td><b>ent</b></td>				<td>End time, as the number of microseconds since epoch</td></tr>
 * <tr><td><b>%[s|i|l|f|d|n|b]/user-key</b></td><td>User defined key/value pair and [s|i|l|f|n|d|b] are type specifiers (i=Integer, l=Long, d=Double, f=Float, n=Number, s=String, b=Boolean) (e.g #%i/myfield=7634732)</td></tr>
 * </table>
 *
 * <p>An example of annotating (TNT4J) a single log message using log4j:</p>
 * <p><code>logger.error("Operation Failed #app=MyApp #opn=save #rsn=" + filename + "  #rcd="
 *  + errno + " #msg='My error message'");</code></p>
 *  
 *  
 * <p>An example of reporting a TNT4J activity using log4j (activity is a related collection of events):</p>
 * <p><code>logger.info("Starting order processing #app=MyApp #beg=" + activityName);</code></p>
 * <p><code></code></p>
 * <p><code>logger.debug("Operation processing #app=MyApp #opn=save #rsn=" + filename);</code></p>
 * <p><code>logger.error("Operation Failed #app=MyApp #opn=save #rsn=" + filename + "  #rcd=" + errno);</code></p>
 * <p><code>logger.info("Finished order processing #app=MyApp #end=" + activityName + " #%l/order=" + orderNo);</code></p>
 *  
 * @version $Revision: 1 $
 * 
 */
public class TNT4JAppender extends AppenderSkeleton {
	public static final String SNAPSHOT_CATEGORY		= "UserDefined";
	public static final String DEFAULT_OP_NAME			= "LoggingEvent";
		
	public static final String PARAM_BEGIN_LABEL		= "beg";
	public static final String PARAM_END_LABEL			= "end";
	
	public static final String PARAM_APPL_LABEL			= "app";
	public static final String PARAM_USER_LABEL			= "usr";
	public static final String PARAM_CORRELATOR_LABEL	= "cid";
	public static final String PARAM_TAG_LABEL			= "tag";
	public static final String PARAM_LOCATION_LABEL		= "loc";
	public static final String PARAM_OP_NAME_LABEL		= "opn";
	public static final String PARAM_OP_TYPE_LABEL		= "opt";
	public static final String PARAM_RESOURCE_LABEL		= "rsn";
	public static final String PARAM_MSG_DATA_LABEL     = "msg";
	public static final String PARAM_SEVERITY_LABEL		= "sev";
	public static final String PARAM_COMP_CODE_LABEL	= "ccd";
	public static final String PARAM_REASON_CODE_LABEL	= "rcd";
	public static final String PARAM_START_TIME_LABEL	= "stt";
	public static final String PARAM_END_TIME_LABEL		= "ent";
	public static final String PARAM_ELAPSED_TIME_LABEL	= "elt";
	public static final String PARAM_AGE_TIME_LABEL		= "age";
	
	public static final String TAG_TYPE_QUALIFIER		= "%";
	public static final String TAG_TYPE_INTEGER			= "%i/";
	public static final String TAG_TYPE_LONG		 	= "%l/";
	public static final String TAG_TYPE_DOUBLE		 	= "%d/";
	public static final String TAG_TYPE_FLOAT		 	= "%f/";
	public static final String TAG_TYPE_NUMBER		 	= "%n/";
	public static final String TAG_TYPE_BOOLEAN		 	= "%b/";
	public static final String TAG_TYPE_STRING		 	= "%s/";

	private static final ThreadLocal<Long> EVENT_TIMER = new ThreadLocal<Long>();
	
	private TrackingLogger logger;
	private String sourceName;
	private SourceType sourceType = SourceType.APPL;
	
	private boolean metricsOnException = true;
	private long metricsFrequency = 60, lastSnapshot = 0;

	/**
	 * Obtain source name associated with this appender.
	 * This name is used tnt4j source for loading tnt4j configuration.
	 *
	 * @return source name string that maps to tnt4j configuration
	 */
	public String getSourceName() {
		return sourceName;
	}
	
	/**
	 * Set source name associated with this appender.
	 * This name is used tnt4j source for loading tnt4j configuration.
	 *
	 */
	public void setSourceName(String name) {
		sourceName = name;
	}
	
	/**
	 * Obtain source type associated with this appender see {@code SourceType} 
	 *
	 * @return source type string representation
	 * @see SourceType
	 */
	public String getSourceType() {
		return sourceType.toString();
	}
	
	/**
	 * Assign default source type string see {@code SourceType} 
	 *
	 * @param type source type string representation, see {@code SourceType}
	 * @see SourceType
	 */
	public void setSourceType(String type) {
		sourceType = SourceType.valueOf(type);
	}
	
	@Override
	public void activateOptions() {
		try {
			if (sourceName == null) {
				setSourceName(getName());
			}
			logger = TrackingLogger.getInstance(sourceName, sourceType);
	        logger.open();
        } catch (IOException e) {
	        LogLog.error("Unable to create tnt4j tracker instance=" + getName(), e);
        }
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@SuppressWarnings("unchecked")
    @Override
	protected void append(LoggingEvent event) {
		long lastReport = System.currentTimeMillis();
		String eventMsg = event.getRenderedMessage();
				
		ThrowableInformation ti = event.getThrowableInformation();
		Throwable ex = ti != null ? ti.getThrowable() : null;

		HashMap<String, Object> attrs = new HashMap<String, Object>();
		Map<String, Object> l4jMdc = MDC.getContext();
		if (l4jMdc != null) attrs.putAll(l4jMdc);
		parseEventMessage(attrs, eventMsg);

		boolean activityMessage = isActivityInstruction(attrs);
		if (activityMessage) {
			processActivityAttrs(attrs, event, ex);
		} else {
			TrackingActivity activity = logger.getCurrentActivity();
			TrackingEvent tev = processEventMessage(attrs, activity, event, eventMsg, ex);

			boolean reportMetrics = activity.isNoop() 
					&& ((ex != null && metricsOnException)
			        || ((lastReport - lastSnapshot) > (metricsFrequency * 1000)));

			if (reportMetrics) {
				// report a single tracking event as part of an activity
				activity = logger.newActivity(tev.getSeverity(), getName());
				activity.start(tev.getOperation().getStartTime().getTimeUsec());
				activity.setSource(tev.getSource()); // use event's source name for this activity
				activity.setException(ex);
				activity.setStatus(ex != null ? ActivityStatus.EXCEPTION : ActivityStatus.END);
				activity.tnt(tev);
				activity.stop(tev.getOperation().getEndTime().getTimeUsec(), 0);
				logger.tnt(activity);
				lastSnapshot = lastReport;
			} else if (activity.isNoop()) {
				// report a single tracking event as datagram
				logger.tnt(tev);
			} else {
				activity.tnt(tev);
			}
		}
	}

	/**
	 * Determine if a given tag is an activity instruction that
	 * signifies activity start/end.
	 *
	 * @return true of activity instruction.
	 */
	protected boolean isActivityInstruction(Map<String, Object> attrs) {
		return attrs.get(PARAM_BEGIN_LABEL) != null || attrs.get(PARAM_END_LABEL) != null;		
	}
	
	/**
	 * Process a given log4j event into a TNT4J activity object {@link TrackingActivity}
	 * 
	 * @param attrs a set of name/value pairs
	 * @param jev log4j logging event object
	 * @param ex exception associated with this event
	 * 
	 * @return tnt4j tracking activity object
	 */
	private TrackingActivity processActivityAttrs(Map<String, Object> attrs, LoggingEvent jev, Throwable ex) {
		Snapshot snapshot = null;
		TrackingActivity activity = logger.getCurrentActivity();
		
		for (Map.Entry<String, Object> entry: attrs.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().toString();
			if (key.equalsIgnoreCase(PARAM_CORRELATOR_LABEL)) {
				activity.setCorrelator(value);
			} else if (key.equalsIgnoreCase(PARAM_LOCATION_LABEL)) {
				activity.setLocation(value);
			} else if (key.equalsIgnoreCase(PARAM_RESOURCE_LABEL)) {
				activity.setResource(value);
			}  else if (key.equalsIgnoreCase(PARAM_USER_LABEL)) {
				activity.setUser(value);
			} else if (key.equalsIgnoreCase(PARAM_SEVERITY_LABEL)) {
				activity.setSeverity(OpLevel.valueOf(value));
			} else if (key.equalsIgnoreCase(PARAM_BEGIN_LABEL)
					|| key.equals(PARAM_END_LABEL)
					|| key.equals(PARAM_APPL_LABEL)) {
				// skip and process later
			} else if (activity != null) {
				// add unknown attribute into snapshot
				if (snapshot == null) {
					snapshot = logger.newSnapshot(SNAPSHOT_CATEGORY, activity.getName());
					activity.addSnapshot(snapshot);
				}
				snapshot.add(stripKey(key), toType(key, value));
			}
		}
		
		Object activityName = attrs.get(PARAM_BEGIN_LABEL);
		if (attrs.get(PARAM_END_LABEL) != null && !activity.isNoop()) {
			activity.setStatus(ex != null? ActivityStatus.EXCEPTION: ActivityStatus.END);
			activity.stop(ex);
			logger.tnt(activity);
		} else if (activityName != null) {
			OpLevel level = getOpLevel(jev);
			activity = logger.newActivity(level, activityName.toString());
			activity.start();
			Object appl = attrs.get(PARAM_APPL_LABEL);
			if (appl != null) {
				activity.setSource(logger.getConfiguration().getSourceFactory().newSource(appl.toString()));				
			}
		}
		return activity;
	}

	/**
	 * Obtain elapsed nanoseconds since last log4j event
	 *
	 * @return elapsed nanoseconds since last log4j even
	 */
	protected long getElapsedNanosSinceLastEvent() {
		Long last = EVENT_TIMER.get();
		long now = System.nanoTime(), elapsedNanos = 0;
		
		elapsedNanos = last != null? now - last.longValue(): elapsedNanos;
		EVENT_TIMER.set(now);
		return elapsedNanos;
	}
	
	/**
	 * Parse a a given message into a map of key/value pairs.
	 * Tags are identified by '#key=value .. #keyN=value' sequence. 
	 * String values should be enclosed in single quotes.
	 * 
	 * @param tags a set of name/value pairs
	 * @param msg string message to be parsed
	 * @return a map of parsed name/value pairs from a given string message.
	 */
	private Map<String, Object> parseEventMessage(Map<String, Object> tags, String msg) {
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
				else if (c == '\'' && inValue) {
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
				String name = curTag[0].trim().replace("'", "");
				String value = (curTag.length > 1 ? curTag[1] : "");
				if (value.startsWith("'") && value.endsWith("'"))
					value = StringEscapeUtils.unescapeJava(value.substring(1, value.length()-1));
				tags.put(name, value);
			} 
		}
		return tags;
	}

	/**
	 * Process a given log4j event into a TNT4J event object {@link TrackingEvent}.
	 * 
	 * @param attrs a set of name/value pairs
	 * @param activity tnt4j activity associated with current message
	 * @param jev log4j logging event object
	 * @param eventMsg string message associated with this event
	 * @param ex exception associated with this event
	 * 
	 * @return tnt4j tracking event object
	 */
	private TrackingEvent processEventMessage(Map<String, Object> attrs, 
			TrackingActivity activity, LoggingEvent jev, String eventMsg, Throwable ex) {
		int rcode = 0;
		long elapsedTimeUsec = getElapsedNanosSinceLastEvent()/1000;
		long evTime = jev.getTimeStamp()*1000; // convert to usec
		long startTime = 0, endTime = 0;
		Snapshot snapshot = null;
	
		OpCompCode ccode = getOpCompCode(jev);
		OpLevel level = getOpLevel(jev);
		TrackingEvent event = logger.newEvent(level, DEFAULT_OP_NAME, null, eventMsg);
		event.setTag(jev.getThreadName());

		for (Map.Entry<String, Object> entry: attrs.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().toString();
			if (key.equalsIgnoreCase(PARAM_CORRELATOR_LABEL)) {
				event.setCorrelator(value);
			} else if (key.equalsIgnoreCase(PARAM_TAG_LABEL)) {
				event.setTag(value);
			} else if (key.equalsIgnoreCase(PARAM_LOCATION_LABEL)) {
				event.setLocation(value);
			} else if (key.equalsIgnoreCase(PARAM_RESOURCE_LABEL)) {
				event.getOperation().setResource(value);
			} else if (key.equalsIgnoreCase(PARAM_USER_LABEL)) {
				event.getOperation().setUser(value);
			} else if (key.equalsIgnoreCase(PARAM_ELAPSED_TIME_LABEL)) {
				elapsedTimeUsec = Long.parseLong(value);
			} else if (key.equalsIgnoreCase(PARAM_AGE_TIME_LABEL)) {
				event.setMessageAge(Long.parseLong(value));
			} else if (key.equalsIgnoreCase(PARAM_START_TIME_LABEL)) {
				startTime = Long.parseLong(value);
			} else if (key.equalsIgnoreCase(PARAM_END_TIME_LABEL)) {
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
				event.setSource(logger.getConfiguration().getSourceFactory().newSource(value));
			} else {
				// add unknown attribute into snapshot
				if (snapshot == null) {
					snapshot = logger.newSnapshot(SNAPSHOT_CATEGORY, event.getOperation().getName());
					event.getOperation().addSnapshot(snapshot);
				}
				snapshot.add(stripKey(key), toType(key, value));
			}
		}		
		startTime = startTime <= 0 ? (evTime - elapsedTimeUsec): evTime;
		endTime = endTime <= 0 ? (startTime + elapsedTimeUsec): endTime;
		
		event.start(startTime);
		event.stop(ccode, rcode, ex, endTime);
		return event;
	}
	
	/**
	 * Strip type qualifier from a given key
	 *
	 * @param key key with prefixed type qualifier
	 * @return stripped key
	 */
	private String stripKey(String key) {
		if (key.length() > 3 && key.charAt(0) == '%' && key.charAt(2) == '/') {
			return key.substring(3);
		} 
		return key;
	}

	/**
	 * Convert annotated value with key type qualifier such as %type/
	 * into the appropriate java object.
	 * <table>
	 * <tr><td><b>%[s|i|l|f|d|n|b]/user-key</b></td><td>User defined key/value pair and [s|i|l|f|n|d|b] are type specifiers (i=Integer, l=Long, d=Double, f=Float, n=Number, s=String, b=Boolean) (e.g #%i/myfield=7634732)</td></tr>
	 * </table>
	 *
	 * @param key key with prefixed type qualifier
	 * @param value to be converted based on type qualifier
	 * 
	 * @return converted value
	 */
	private Object toType(String key, String value) {
		try {
			if (!key.startsWith(TAG_TYPE_QUALIFIER)) {
				// if no type specified, assume a numeric field
				return Double.parseDouble(value);
			} else if (key.startsWith(TAG_TYPE_STRING)) {
				return value;
			} else if (key.startsWith(TAG_TYPE_NUMBER)) {
				return Double.parseDouble(value);
			} else if (key.startsWith(TAG_TYPE_INTEGER)) {
				return Integer.parseInt(value);
			} else if (key.startsWith(TAG_TYPE_LONG)) {
				return Long.parseLong(value);
			} else if (key.startsWith(TAG_TYPE_FLOAT)) {
				return Float.parseFloat(value);
			} else if (key.startsWith(TAG_TYPE_DOUBLE)) {
				return Double.parseDouble(value);
			} else if (key.startsWith(TAG_TYPE_BOOLEAN)) {
				return Boolean.parseBoolean(value);
			} else {
				return value;
			}
		} catch (NumberFormatException ne) {
			// return a string if exception occurs
			return value;
		}
	}

	/**
	 * Map log4j logging event level to TNT4J {@link OpLevel}. 
	 *
	 * @param event log4j logging event object
	 * @return TNT4J {@link OpLevel}. 
	 */
	private OpLevel getOpLevel(LoggingEvent event) {
		Level lvl = event.getLevel();
		if (lvl == Level.INFO) {
			return OpLevel.INFO;
		}
		else if (lvl == Level.FATAL) {
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

	
	/**
	 * Map log4j logging event level to TNT4J {@link OpCompCode}. 
	 *
	 * @param event log4j logging event object
	 * @return TNT4J {@link OpCompCode}. 
	 */
	private OpCompCode getOpCompCode(LoggingEvent event) {
		Level lvl = event.getLevel();
		if (lvl == Level.INFO) {
			return OpCompCode.SUCCESS;
		}
		else if (lvl == Level.ERROR) {
			return OpCompCode.ERROR;
		}
		else if (lvl == Level.WARN) {
			return OpCompCode.WARNING;
		}
		else if (lvl == Level.DEBUG) {
			return OpCompCode.SUCCESS;
		}
		else if (lvl == Level.TRACE) {
			return OpCompCode.SUCCESS;
		}
		else if (lvl == Level.OFF) {
			return OpCompCode.SUCCESS;
		}
		else {
			return OpCompCode.SUCCESS;
		}	
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
    public void close() {
		if (logger != null) {
			logger.close();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
    public boolean requiresLayout() {
	    return false;
    }
	
	/**
	 * Return whether appender generates metrics log entries with exception
	 *
	 * @return true to publish default jvm metrics when exception is logged
	 */
	public boolean getMetricsOnException() {
		return metricsOnException;
	}
	
	/**
	 * Direct appender to generate metrics log entries with exception when
	 * set to true, false otherwise.
	 *
	 * @param flag true to append metrics on exception, false otherwise
	 */
	public void setMetricsOnException(boolean flag) {
		metricsOnException = flag;
	}
	
	/**
	 * Appender generates metrics based on a given frequency in seconds.
	 *
	 */
	public long getMetricsFrequency() {
		return metricsFrequency;
	}
	
	/**
	 * Set metric collection frequency seconds.
	 * 
	 * @param freq number of seconds
	 */
	public void setMetricsFrequency(long freq) {
		metricsFrequency = freq;
	}
}
