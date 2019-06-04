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
package com.jkoolcloud.tnt4j.logger;

import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

import com.jkoolcloud.tnt4j.TrackingLogger;
import com.jkoolcloud.tnt4j.core.*;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Common methods used by all logging appender implementations to handle tags, qualifiers within log messages.
 * </p>
 *
 * @version $Revision: 1 $
 *
 */
public class AppenderTools implements AppenderConstants {

	private AppenderTools() {
	}

	/**
	 * Strip type qualifier from a given key
	 *
	 * @param key
	 *            key with prefixed type qualifier
	 * @return stripped key
	 */
	public static String getKey(String key) {
		int eIdx = key.indexOf("/");
		if (key.length() > 3 && key.charAt(0) == '%' && eIdx > 0) {
			return key.substring(eIdx + 1);
		}
		return key;
	}

	/**
	 * Extract value type from the key
	 * <table>
	 * <caption>Key/Value mapping</caption>
	 * <tr>
	 * <td><b>%[s|i|l|f|d|n|b][:valType]/user-key</b></td>
	 * <td>User defined key/value pair (e.g #%l:byte/memory=7634732)</td>
	 * </tr>
	 * </table>
	 *
	 * @param key
	 *            key with prefixed type qualifier
	 * @return value type extracted from the key - %[data-type][:value-type]/user-key
	 */
	public static String getValueType(String key) {
		if (key.startsWith(TAG_TYPE_QUALIFIER)) {
			int sIdx = key.indexOf(":");
			if (sIdx > 0) {
				int eIdx = key.indexOf("/");
				return ((eIdx - sIdx) > 1 ? key.substring(sIdx + 1, eIdx) : ValueTypes.VALUE_TYPE_NONE);
			}
		}
		return ValueTypes.VALUE_TYPE_NONE;
	}

	/**
	 * Convert annotated value with key type qualifier such as %type/ into key, value property.
	 * <table>
	 * <caption>Key/Value mapping</caption>
	 * <tr>
	 * <td><b>%[s|i|l|f|d|n|b][:value-type]/user-key</b></td>
	 * <td>User defined key/value pair and [s|i|l|f|n|d|b] are type specifiers (i=Integer, l=Long, d=Double, f=Float,
	 * n=Number, s=String, b=Boolean) (e.g #%i/myfield=7634732)</td>
	 * </tr>
	 * </table>
	 *
	 * @param key
	 *            key with prefixed type qualifier
	 * @param value
	 *            to be converted based on type qualifier
	 *
	 * @return property containing key and value
	 */
	public static Property toProperty(String key, String value) {
		Object pValue = value;

		try {
			if (!key.startsWith(TAG_TYPE_QUALIFIER)) {
				// if no type specified, assume a numeric field
				pValue = Long.parseLong(value);
			} else if (key.startsWith(TAG_TYPE_STRING)) {
				pValue = value;
			} else if (key.startsWith(TAG_TYPE_NUMBER)) {
				pValue = Double.parseDouble(value);
			} else if (key.startsWith(TAG_TYPE_INTEGER)) {
				pValue = Integer.parseInt(value);
			} else if (key.startsWith(TAG_TYPE_LONG)) {
				pValue = Long.parseLong(value);
			} else if (key.startsWith(TAG_TYPE_FLOAT)) {
				pValue = Float.parseFloat(value);
			} else if (key.startsWith(TAG_TYPE_DOUBLE)) {
				pValue = Double.parseDouble(value);
			} else if (key.startsWith(TAG_TYPE_BOOLEAN)) {
				pValue = Boolean.parseBoolean(value);
			}
		} catch (NumberFormatException ne) {
		}
		return new Property(getKey(key), pValue, getValueType(key));
	}

	/**
	 * Determine if a given tag is an activity instruction that signifies activity start/end.
	 *
	 * @param attrs
	 *            activity attributes
	 * @return true if activity instruction.
	 */
	public static boolean isActivityInstruction(Map<String, String> attrs) {
		return attrs.get(PARAM_BEGIN_LABEL) != null || attrs.get(PARAM_END_LABEL) != null;
	}

	/**
	 * Process a given event into a TNT4J activity object {@link TrackingActivity}
	 *
	 * @param logger
	 *            tracking logger instance
	 * @param category
	 *            name associated with the set of attributes
	 * @param attrs
	 *            a set of name/value pairs
	 * @param level
	 *            logging level
	 * @param ex
	 *            exception associated with this event
	 *
	 * @return tnt4j tracking activity object
	 */
	public static TrackingActivity processActivityAttrs(TrackingLogger logger, String category,
			Map<String, String> attrs, OpLevel level, Throwable ex) {
		Snapshot snapshot = null;
		TrackingActivity activity = logger.getCurrentActivity();

		for (Map.Entry<String, String> entry : attrs.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.equalsIgnoreCase(PARAM_CORRELATOR_LABEL)) {
				activity.setCorrelator(value);
			} else if (key.equalsIgnoreCase(PARAM_LOCATION_LABEL)) {
				activity.setLocation(value);
			} else if (key.equalsIgnoreCase(PARAM_RESOURCE_LABEL)) {
				activity.setResource(value);
			} else if (key.equalsIgnoreCase(PARAM_USER_LABEL)) {
				activity.setUser(value);
			} else if (key.equalsIgnoreCase(PARAM_SEVERITY_LABEL)) {
				activity.setSeverity(OpLevel.valueOf(value));
			} else if (key.equalsIgnoreCase(PARAM_EXCEPTION_LABEL)) {
				activity.setException(value);
			} else if (key.equalsIgnoreCase(PARAM_BEGIN_LABEL) || key.equals(PARAM_END_LABEL)
					|| key.equals(PARAM_APPL_LABEL)) {
				// skip and process later
			} else if ((activity != null) && !Utils.isEmpty(key) && !Utils.isEmpty(value)) {
				// add unknown attribute into snapshot
				if (snapshot == null) {
					snapshot = logger.newSnapshot(category, activity.getName());
					activity.addSnapshot(snapshot);
				}
				snapshot.add(AppenderTools.toProperty(key, value));
			}
		}

		Object activityName = attrs.get(PARAM_BEGIN_LABEL);
		if (attrs.get(PARAM_END_LABEL) != null && !activity.isNoop()) {
			activity.setStatus(ex != null ? ActivityStatus.EXCEPTION : ActivityStatus.END);
			activity.stop(ex);
			logger.tnt(activity);
		} else if (activityName != null) {
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
	 * Parse a given message into a map of key/value pairs. Tags are identified by '#key=value .. #keyN=value' sequence.
	 * String values should be enclosed in single quotes.
	 *
	 * @param tags
	 *            a set of name/value pairs
	 * @param msg
	 *            string message to be parsed
	 * @param delm
	 *            tag eye catcher
	 * @return a map of parsed name/value pairs from a given string message.
	 */
	public static Map<String, String> parseEventMessage(Map<String, String> tags, String msg, char delm) {
		int curPos = 0;
		while (curPos < msg.length()) {
			if (msg.charAt(curPos) != delm) {
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
				} else if (c == '\'' && inValue) {
					// the double quote we just read was not escaped, so include it in value
					if (quotedValue) {
						// found closing quote
						curPos++;
						break;
					} else {
						quotedValue = true;
					}
				} else if (Character.isWhitespace(c) && !quotedValue) {
					break;
				}
				curPos++;
			}

			if (curPos > start) {
				String[] curTag = msg.substring(start, curPos).split("=");
				String name = curTag[0].trim().replace("'", "");
				String value = (curTag.length > 1 ? curTag[1] : "");
				if (value.startsWith("'") && value.endsWith("'")) {
					value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
				}
				tags.put(name, value);
			}
		}
		return tags;
	}
}
