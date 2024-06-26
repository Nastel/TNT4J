/*
 * Copyright 2014-2023 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.format;

import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.core.UsecTimestamp;
import com.jkoolcloud.tnt4j.source.DefaultSourceFactory;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Default implementation of {@link Formatter} interface provides default formatting of {@link TrackingActivity} and
 * {@link TrackingEvent} as well as any object passed to {@code format()} method call.
 * <p>
 * List of formatting tokens:
 * <ul>
 * <li>0 - event timestamp</li>
 * <li>1 - event severity level</li>
 * <li>2 - event message</li>
 * <li>3 - event source name</li>
 * </ul>
 *
 * @version $Revision: 5 $
 * 
 * @see Formatter
 * @see TrackingActivity
 * @see TrackingEvent
 */
public class DefaultFormatter implements EventFormatter, Configurable {
	public static final String SEPARATOR = System.getProperty("tnt4j.formatter.default.separator", " | ");
	public static final String DEFAULT_FORMAT_PATTERN = "{2} | {1} | {0} | {3}";

	protected String separator = SEPARATOR;
	protected TimeZone timeZone = TimeZone.getDefault();
	protected String formatString = DEFAULT_FORMAT_PATTERN;

	private Map<String, ?> config = null;

	/**
	 * Create a default event formatter
	 *
	 */
	public DefaultFormatter() {
	}

	/**
	 * Create a default event formatter
	 *
	 * @param format
	 *            string (e.g. {@value #DEFAULT_FORMAT_PATTERN})
	 */
	public DefaultFormatter(String format) {
		formatString = format;
	}

	/**
	 * Create a default event formatter
	 *
	 * @param format
	 *            string (e.g. {@value #DEFAULT_FORMAT_PATTERN})
	 * @param tz
	 *            time zone
	 */
	public DefaultFormatter(String format, TimeZone tz) {
		formatString = format;
		timeZone = tz;
	}

	/**
	 * Create a default event formatter
	 *
	 * @param format
	 *            string (e.g. {@value #DEFAULT_FORMAT_PATTERN})
	 * @param tzId
	 *            time zone id
	 */
	public DefaultFormatter(String format, String tzId) {
		formatString = format;
		timeZone = TimeZone.getTimeZone(tzId);
	}

	@Override
	public String format(Object obj, Object... args) {
		if (obj instanceof TrackingActivity) {
			return format((TrackingActivity) obj);
		} else if (obj instanceof TrackingEvent) {
			return format((TrackingEvent) obj);
		} else if (obj instanceof Snapshot) {
			return format((Snapshot) obj);
		} else {
			return Utils.format(Utils.toString(obj), args);
		}
	}

	@Override
	public String format(TrackingEvent event) {
		return event.toString();
	}

	@Override
	public String format(TrackingActivity activity) {
		return activity.getStatus() + separator + activity + separator + activity.getSource();
	}

	@Override
	public String format(Snapshot snapshot) {
		return format(snapshot.getSeverity(), snapshot.toString());
	}

	@Override
	public String format(long ttl, Source src, OpLevel level, String msg, Object... args) {
		String srcName = "";
		if (StringUtils.contains(formatString, "{3}")) {
			srcName = src != null ? src.getFQName() : DefaultSourceFactory.getInstance().getRootSource().getFQName();
		}
		String timeStr = "";
		if (StringUtils.contains(formatString, "{0}")) {
			timeStr = UsecTimestamp.getTimeStamp(timeZone);
		}
		String msgStr = "";
		if (StringUtils.contains(formatString, "{2}")) {
			msgStr = Utils.format(msg, args);
		}

		return Utils.format(formatString, timeStr, level, msgStr, srcName);
	}

	@Override
	public Map<String, ?> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, ?> settings) {
		config = settings;

		separator = Utils.getString("Separator", settings, SEPARATOR);
		formatString = Utils.getString("Format", settings, formatString);
		String tz = Utils.getString("TimeZone", settings, null);
		timeZone = Utils.isEmpty(tz) ? TimeZone.getDefault() : TimeZone.getTimeZone(tz);
	}
}
