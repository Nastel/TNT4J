/*
 * Copyright 2014-2018 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.utils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.core.Handle;
import com.jkoolcloud.tnt4j.core.Property;
import com.jkoolcloud.tnt4j.core.Snapshot;

/**
 * General utility methods.
 *
 * @version $Revision: 5 $
 */
public class Utils {

	/**
	 * Current stack frame class marker prefix
	 */
	public static final String OP_STACK_MARKER_PREFIX = "$";

	/**
	 * ASCII character set.
	 */
	public static final String ASCII = "US-ASCII";

	/**
	 * URL character set.
	 */
	public static final String UTF8 = "UTF-8";

	/**
	 * 16-bit USC character set.
	 */
	public static final String UTF16 = "UTF-16BE";

	/**
	 * Line feed.
	 */
	private static final String LINE_FEED = "\n";

	/**
	 * JVM runtime name
	 */
	public static final String VM_NAME = ManagementFactory.getRuntimeMXBean().getName();

	/**
	 * JVM process ID
	 */
	public static final long VM_PID = initVMID();

	/**
	 * Random number generator
	 */
	private static Random rand = new Random();

	/**
	 * Class method search empty parameters array.
	 */
	public static final Class<?>[] NO_PARAMS_C = {};
	/**
	 * Method invocation empty parameters array.
	 */
	public static final Object[] NO_PARAMS_O = {};

	public static final int CLIENT_CODE_STACK_INDEX;

	/**
	 * Replacements configuration RegEx pattern.
	 */
	public static final Pattern REP_CFG_PATTERN = Pattern
			.compile("\"(\\s*([^\"\\\\]|\\\\.)+\\s*)\"->\"(\\s*([^\"\\\\]|\\\\.)+\\s*)\"");

	static {
		int index = 0;
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement frame : stack) {
			index++;
			if (frame.getClassName().equals(Utils.class.getName())) {
				break;
			}
		}
		CLIENT_CODE_STACK_INDEX = index;
	}

	private static long initVMID() {
		String _vm_pid_del = System.getProperty("tnt4j.java.vm.pid.dlm", "@");
		String vm_name = ManagementFactory.getRuntimeMXBean().getName();
		try {
			int index = vm_name.indexOf(_vm_pid_del);
			if (index > 0) {
				return Long.parseLong(vm_name.substring(0, index));
			}
		} catch (Throwable e) {
		}
		return 0;
	}

	/**
	 * Obtain a message from a resource bundle
	 *
	 * @param bundle
	 *            resource bundle
	 * @param key
	 *            message key
	 * @return message associated with a given key
	 */
	public static String getString(ResourceBundle bundle, Object key) {
		return getString(bundle, String.valueOf(key));
	}

	/**
	 * Obtain a message from a resource bundle.
	 *
	 * @param bundle
	 *            resource bundle
	 * @param key
	 *            message key
	 * @return message associated with a given <tt>key</tt>, or <tt>key</tt> if message is not resolved
	 */
	public static String getString(ResourceBundle bundle, String key) {
		String resolved = null;
		try {
			resolved = bundle != null ? bundle.getString(key) : key;
		} catch (RuntimeException exc) {
		}

		return resolved != null ? resolved : key;
	}

	/**
	 * Constructs a {@link Locale} object parsed from provided locale string.
	 *
	 * @param localeStr
	 *            locale string representation
	 *
	 * @see org.apache.commons.lang3.LocaleUtils#toLocale(String)
	 * @return parsed locale object, or {@code null} if can't parse localeStr.
	 */
	public static Locale getLocale(String localeStr) {
		if (isEmpty(localeStr)) {
			return null;
		}

		// NOTE: adapting to LocaleUtils notation
		String l = localeStr.replace('-', '_');
		return LocaleUtils.toLocale(l);
	}

	/**
	 * Random number generator within a specified range (max, min inclusive)
	 *
	 * @param min
	 *            bottom of the range
	 * @param max
	 *            top of the range
	 * @return random number between the specified range
	 */
	public static int randomRange(int min, int max) {
		if (max < min) {
			throw new IllegalArgumentException("max < min");
		}
		int range = max - min + 1;
		return rand.nextInt(range) + min;
	}

	/**
	 * Resolve variable given name to a global variable. Global variables are referenced using: $var convention.
	 *
	 * @param name
	 *            object to use
	 * @param defValue
	 *            default value if name undefined
	 * @return resolved variable or itself if not a variable
	 */
	public static String resolve(String name, String defValue) {
		if (name.startsWith("$")) {
			return System.getProperty(name.substring(1), defValue);
		} else {
			return name;
		}
	}

	/**
	 * Resolve variable given name to a global variable. Global variables are referenced using: $var convention.
	 *
	 * @param props
	 *            a set of properties
	 * @param name
	 *            object to use
	 * @param defValue
	 *            default value if name undefined
	 * @return resolved variable or itself if not a variable
	 */
	public static String resolve(Properties props, String name, String defValue) {
		if (name.startsWith("$")) {
			return props.getProperty(name.substring(1), defValue);
		} else {
			return name;
		}
	}

	/**
	 * Qualify given key with a given object class
	 *
	 * @param obj
	 *            object to use
	 * @param key
	 *            key to qualify
	 * @return string representation
	 */
	public static String qualify(Object obj, String key) {
		return obj.getClass().getSimpleName() + "/" + key;
	}

	/**
	 * Qualify given key with a given object class
	 *
	 * @param obj
	 *            object to use
	 * @param pfix
	 *            key prefix
	 * @param key
	 *            key to qualify
	 * @return string representation
	 */
	public static String qualify(Object obj, String pfix, String key) {
		return obj.getClass().getSimpleName() + "/" + pfix + "/" + key;
	}

	/**
	 * Gets string from string builder and resets it to {@code 0} position;
	 *
	 * @param sb
	 *            string builder to get string from
	 * @return string retrieved from string builder
	 *
	 * @see StringBuilder#toString()
	 * @see StringBuilder#setLength(int)
	 */
	public static String getString(StringBuilder sb) {
		String str = sb.toString();
		sb.setLength(0);

		return str;
	}

	/**
	 * Return current client stack index that identifies the calling stack frame return by
	 * {@code Thread.currentThread().getStackTrace()}
	 *
	 * @return return current stack frame
	 */
	public int getClientCodeStackIndex() {
		return CLIENT_CODE_STACK_INDEX;
	}

	/**
	 * Obtain string representation of a throwable object
	 *
	 * @param ex
	 *            exception
	 * @return string representation including stack trace
	 */
	public static String printThrowable(Throwable ex) {
		try {
			if (ex == null) {
				return null;
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(os);
			ex.printStackTrace(ps);
			return os.toString(UTF8);
		} catch (UnsupportedEncodingException e) {
			return ex.toString();
		}
	}

	/**
	 * Print given message, stack trace to the underlying print stream
	 *
	 * @param msg
	 *            user defined message
	 * @param trace
	 *            stack trace
	 * @param out
	 *            print stream where output is written
	 */
	public static void printStackTrace(String msg, StackTraceElement[] trace, PrintStream out) {
		Exception ex = new Exception(msg);
		ex.setStackTrace(trace);
		ex.printStackTrace(out);
	}

	/**
	 * Print given message, stack trace to the underlying print writer
	 *
	 * @param msg
	 *            user defined message
	 * @param trace
	 *            stack trace
	 * @param out
	 *            print writer where output is written
	 */
	public static void printStackTrace(String msg, StackTraceElement[] trace, PrintWriter out) {
		Exception ex = new Exception(msg);
		ex.setStackTrace(trace);
		ex.printStackTrace(out);
	}

	/**
	 * Return current stack frame which is executing this call
	 *
	 * @return return current stack frame
	 */
	public static StackTraceElement getCurrentStackFrame() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		int index = CLIENT_CODE_STACK_INDEX;
		return stack.length > index ? stack[index] : stack[stack.length - 1];
	}

	/**
	 * Return calling stack frame, which is right above in the current stack frame.
	 *
	 * @return return calling stack frame, right above the current call.
	 */
	public static StackTraceElement getCallingStackFrame() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		int index = CLIENT_CODE_STACK_INDEX + 1;
		return stack.length > index ? stack[index] : stack[stack.length - 1];
	}

	/**
	 * Return a specific stack frame with a given stack offset. offset 0 -- current, 1 -- calling, and so on.
	 *
	 * @param offset
	 *            offset index within the calling stack
	 * @return return current stack frame
	 */
	public static StackTraceElement getStackFrame(int offset) {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		int index = CLIENT_CODE_STACK_INDEX + offset;
		return stack.length > index ? stack[index] : stack[stack.length - 1];
	}

	/**
	 * Return a specific stack frame with a given stack offset. offset 0 -- current, 1 -- calling, and so on.
	 *
	 * @param stack
	 *            frame list
	 * @param offset
	 *            offset index within the calling stack
	 * @return return current stack frame
	 */
	public static StackTraceElement getStackFrame(StackTraceElement[] stack, int offset) {
		int index = CLIENT_CODE_STACK_INDEX + offset;
		return stack.length > index ? stack[index] : stack[stack.length - 1];
	}

	/**
	 * Return calling stack frame which is right above a given class marker plus the offset.
	 *
	 * @param classMarker
	 *            class marker on the stack
	 * @param offset
	 *            offset on the stack from the marker
	 * @return Return calling stack frame which is right above a given class marker
	 */
	public static StackTraceElement getStackFrame(String classMarker, int offset) {
		int index = 0;
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		StackTraceElement first = null, found = stack[stack.length - 1];
		for (StackTraceElement item : stack) {
			if (first == null && item.getClassName().startsWith(classMarker)) {
				first = item;
			} else if (first != null && !item.getClassName().startsWith(classMarker)) {
				found = stack[index + offset];
				break;
			}
			index++;
		}
		return found;
	}

	/**
	 * Format a given string pattern and a list of arguments as defined by {@link MessageFormat}
	 *
	 * @param pattern
	 *            format string
	 * @param args
	 *            arguments for format
	 * @return formatted string
	 */
	public static String format(String pattern, Object... args) {
		if (args != null && args.length > 0) {
			return MessageFormat.format(pattern, args);
		} else {
			return String.valueOf(pattern);
		}
	}

	/**
	 * Return a {@link Throwable} object if it is the last element in the object array
	 *
	 * @param args
	 *            list of objects
	 * @return Throwable exception
	 */
	public static Throwable getThrowable(Object args[]) {
		if ((args != null) && (args.length > 0) && (args[args.length - 1] instanceof Throwable)) {
			return (Throwable) args[args.length - 1];
		}
		return null;
	}

	/**
	 * Return process ID associated with the current VM.
	 *
	 * @return process id associated with the current VM
	 */
	public static long getVMPID() {
		return VM_PID;
	}

	/**
	 * Return a name associated with the current VM.
	 *
	 * @return name associated with the current VM
	 */
	public static String getVMName() {
		return VM_NAME;
	}

	/**
	 * Creates an ASCII Encoder to encoding strings in ASCII.
	 *
	 * @return ASCII encoder
	 */
	public static CharsetEncoder getAsciiEncoder() {
		return Charset.forName(ASCII).newEncoder();
	}

	/**
	 * Encodes the specified URL string. This is a wrapper around {@link URLEncoder#encode(String, String)}
	 *
	 * @param url
	 *            URL string
	 * @return encoded URL string
	 * @see URLEncoder#encode(String, String)
	 */
	public static String encodeURL(String url) {
		if (url == null) {
			return null;
		}
		try {
			return URLEncoder.encode(url, UTF8);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Decodes the specified URL string. This is a wrapper around {@link URLDecoder#decode(String, String)}
	 *
	 * @param url
	 *            URL string
	 * @return decoded URL string
	 * @see URLDecoder#decode(String, String)
	 */
	public static String decodeURL(String url) {
		if (url == null) {
			return null;
		}
		try {
			return URLDecoder.decode(url, UTF8);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Gets a MD5 message digester object
	 *
	 * @return MD5 message digester
	 */
	public static MessageDigest getMD5Digester() {
		MessageDigest msgDigest = null;

		try {
			msgDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		return msgDigest;
	}

	/**
	 * Hide some parts of the string and leave only certain number of characters at the end of the string. Example:
	 * XXXXX47478 {@code hide("1234-47478", "X", 5)}
	 *
	 * @param str
	 *            string to be replaced
	 * @param hideChars
	 *            hide chars to use during replacement
	 * @param lastNo
	 *            number of chars to leave at the end
	 * @return String with characters at start of string replaced with specified {@code hideChars}
	 */
	public static String hide(String str, String hideChars, int lastNo) {
		if (str == null) {
			return str;
		}

		int length = str.length() - lastNo;
		if (length > 0) {
			String fake = str.substring(0, length);
			if (!fake.isEmpty()) {
				fake = fake.replaceAll(".", hideChars);
			}
			return fake + str.substring(length, str.length());
		}
		return str;
	}

	/**
	 * Hide some parts of the string and leave only certain number of characters at the start of the string. Example:
	 * 1234-XXXXX {@code hideEnd("1234-47478", "X", 5)}
	 *
	 * @param str
	 *            string to be replaced
	 * @param hideChars
	 *            hide chars to use during replacement
	 * @param startNo
	 *            number of chars to leave at the start
	 * @return String with characters at end of string replaced with specified {@code hideChars}
	 */
	public static String hideEnd(String str, String hideChars, int startNo) {
		if (str == null) {
			return str;
		}

		int length = str.length() - startNo;
		if (length > 0) {
			String fake = str.substring(startNo, str.length());
			if (!fake.isEmpty()) {
				fake = fake.replaceAll(".", hideChars);
			}
			return str.substring(0, startNo) + fake;
		}
		return str;
	}

	/**
	 * Returns a quoted string, surrounded with double quote
	 *
	 * @param str
	 *            string handle
	 * @return Returns a quoted string, surrounded with double quote
	 */
	public static String quote(String str) {
		return surround(str, "\"");
	}

	/**
	 * Returns a quoted string, surrounded with double quote
	 *
	 * @param obj
	 *            object handle
	 * @return Returns a quoted string, surrounded with double quote
	 */
	public static String quote(Object obj) {
		return surround(String.valueOf(obj), "\"");
	}

	/**
	 * Returns a surrounded string with a given surround token
	 *
	 * @param str
	 *            string handle
	 * @param sur
	 *            surround string
	 * @return Returns a surrounded string with a given surround token
	 */
	public static String surround(String str, String sur) {
		return sur + str + sur;
	}

	/**
	 * Appends a quoted string, surrounded with double quote, into provided string builder.
	 *
	 * @param str
	 *            string handle
	 * @param sb
	 *            string buffer to append
	 * @return appended string builder instance
	 */
	public static StringBuilder quote(String str, StringBuilder sb) {
		return surround(str, "\"", sb);
	}

	/**
	 * Appends a quoted string, surrounded with double quote, into provided string builder.
	 *
	 * @param obj
	 *            object handle
	 * @param sb
	 *            string buffer to append
	 * @return appended string builder instance
	 */
	public static StringBuilder quote(Object obj, StringBuilder sb) {
		return quote(String.valueOf(obj), sb);
	}

	/**
	 * Appends surrounded string with a given surround token into provided string builder.
	 *
	 * @param str
	 *            string handle
	 * @param sur
	 *            surround string
	 * @param sb
	 *            string buffer to append
	 * @return appended string builder instance
	 */
	public static StringBuilder surround(String str, String sur, StringBuilder sb) {
		sb.append(sur).append(str).append(sur);
		return sb;
	}

	/**
	 * Returns true if two specified objects are equal
	 *
	 * @param obj1
	 *            first object
	 * @param obj2
	 *            second object
	 * @return true if two specified objects are equal
	 */
	public static boolean equal(Object obj1, Object obj2) {
		return obj1 == obj2 || (obj1 != null && obj1.equals(obj2));
	}

	/**
	 * Returns true if the string is null or empty
	 *
	 * @param str
	 *            string handle
	 * @return true if the string is null or empty
	 */
	public static boolean isEmpty(String str) {
		return (StringUtils.isEmpty(str));
	}

	/**
	 * Returns true if a collection is null or empty
	 *
	 * @param col
	 *            collection to be tested if empty
	 * @return true if the collection is null or empty
	 */
	public static boolean isEmpty(Collection<?> col) {
		if (col == null) {
			return true;
		}
		return col.isEmpty();
	}

	/**
	 * Returns true if a map is null or empty
	 *
	 * @param map
	 *            map to be tested if empty
	 * @return true if the collection is null or empty
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		if (map == null) {
			return true;
		}
		return map.isEmpty();
	}

	/**
	 * Gets resolved name of the method that triggered the operation using current stack frame.
	 *
	 * @param opName
	 *            operation name
	 * @return name triggering operation
	 */
	public static String getMethodNameFromStack(String opName) {
		if (!opName.startsWith(OP_STACK_MARKER_PREFIX)) {
			return opName;
		} else {
			String marker = opName.substring(1);
			String[] pair = marker.split(":");
			int offset = pair.length == 2 ? Integer.parseInt(pair[1]) : 0;
			StackTraceElement item = Utils.getStackFrame(pair[0], offset);
			return item.toString();
		}
	}

	/**
	 * Gets resolved name of the method that triggered the operation using current stack frame.
	 *
	 * @param marker
	 *            class marker to be used to locate the stack frame
	 * @param offset
	 *            offset from the located stack frame (must be &gt;= 0)
	 * @return name triggering operation
	 */
	public static String getMethodNameFromStack(String marker, int offset) {
		StackTraceElement item = Utils.getStackFrame(marker, offset);
		return item.toString();
	}

	/**
	 * Gets resolved name of the method that triggered the operation using current stack frame.
	 *
	 * @param classMarker
	 *            class marker to be used to locate the stack frame
	 * @return name triggering operation
	 */
	public static String getMethodNameFromStack(Class<?> classMarker) {
		StackTraceElement item = Utils.getStackFrame(classMarker.getName(), 0);
		return item.toString();
	}

	/**
	 * Gets resolved name of the method that triggered the operation using current stack frame.
	 *
	 * @param classMarker
	 *            class marker to be used to locate the stack frame
	 * @param offset
	 *            offset from the located stack frame (must be &gt;= 0)
	 * @return name triggering operation
	 */
	public static String getMethodNameFromStack(Class<?> classMarker, int offset) {
		StackTraceElement item = Utils.getStackFrame(classMarker.getName(), offset);
		return item.toString();
	}

	/**
	 * Resolves the specified host name to its IP Address. If no host name is given, then resolves local host IP
	 * Address.
	 *
	 * @param hostName
	 *            host name to resolve
	 * @return string representation of IP Address
	 */
	public static String resolveHostNameToAddress(String hostName) {
		String hostIp = null;
		InetAddress host;
		try {
			if (isEmpty(hostName)) {
				host = InetAddress.getLocalHost();
			} else {
				host = InetAddress.getByName(hostName);
			}

			hostIp = host.getHostAddress();
		} catch (UnknownHostException e) {
		}
		return hostIp;
	}

	/**
	 * Resolves the specified IP Address to a host name. If no IP Address is given, then resolves local host name.
	 *
	 * @param hostIp
	 *            string representation of host IP Address to resolve
	 * @return host name
	 */
	public static String resolveAddressToHostName(String hostIp) {
		byte[] addr = null;
		if (!isEmpty(hostIp)) {
			String[] addrBytes = hostIp.split("\\.");
			addr = new byte[addrBytes.length];
			for (int i = 0; i < addr.length; i++) {
				addr[i] = (byte) Integer.parseInt(addrBytes[i]);
			}
		}

		return resolveAddressToHostName(addr);
	}

	/**
	 * Resolves the specified IP Address to a host name. If no IP Address is given, then resolves local host name.
	 *
	 * @param hostIp
	 *            host IP Address to resolve
	 * @return host name
	 */
	public static String resolveAddressToHostName(byte[] hostIp) {
		String hostName = null;
		InetAddress host;
		try {
			if (hostIp == null || hostIp.length == 0) {
				host = InetAddress.getLocalHost();
			} else {
				host = InetAddress.getByAddress(hostIp);
			}

			hostName = host.getHostName();
		} catch (UnknownHostException e) {
		}

		return hostName;
	}

	/**
	 * Determines the host name of the local server.
	 *
	 * @return name of local server
	 */
	public static String getLocalHostName() {
		String hostName = "localhost";
		InetAddress host;
		try {
			host = InetAddress.getLocalHost();
			hostName = host.getHostName();
		} catch (UnknownHostException e) {
		}

		return hostName;
	}

	/**
	 * Determines the IP Address of the local server.
	 *
	 * @return string representation of IP Address
	 */
	public static String getLocalHostAddress() {
		String hostIp = "127.0.0.1";
		InetAddress host;
		try {
			host = InetAddress.getLocalHost();
			hostIp = host.getHostAddress();
		} catch (UnknownHostException e) {
		}

		return hostIp;
	}

	/**
	 * Generates a detailed description of an exception, including stack trace of both the exception and the underlying
	 * root cause of the exception, if any.
	 *
	 * @param ex
	 *            exception to process
	 * @return String representation of exception
	 * @see #getStackTrace(Throwable)
	 */
	public static String getExceptionDetail(Throwable ex) {
		if (ex == null) {
			return "";
		}

		String detail = getStackTrace(ex);

		// find the root cause of exception
		Throwable rootCause = null;
		for (;;) {
			Throwable cause = ex.getCause();
			if (cause == null) {
				try {
					Method method = ex.getClass().getMethod("getTargetException", NO_PARAMS_C);
					Object target = method.invoke(ex, NO_PARAMS_O);
					if (target instanceof Throwable) {
						cause = (Throwable) target;
					}
				} catch (Exception exx) {
				}
			}

			if (cause == null) {
				break;
			}

			ex = cause;
			rootCause = cause;
		}

		if (rootCause != null) {
			detail += LINE_FEED + "Root cause:" + LINE_FEED + getStackTrace(rootCause);
		}

		return detail;
	}

	/**
	 * Generates a string representation of the stack trace for an exception.
	 *
	 * @param ex
	 *            exception to process
	 * @return stack trace as a string
	 */
	public static String getStackTrace(Throwable ex) {
		if (ex == null) {
			return "";
		}

		String result = ex + LINE_FEED;

		StackTraceElement[] trace = ex.getStackTrace();
		for (StackTraceElement aTrace : trace) {
			result += "\tat " + aTrace + LINE_FEED;
		}

		return result;
	}

	/**
	 * Generates a string representing exception and exception root cause messages.
	 *
	 * @param ex
	 *            exception to process
	 * @return exception messages string
	 */
	public static String getExceptionMessages(Throwable ex) {
		Throwable root = ExceptionUtils.getRootCause(ex);
		String msgsStr = ex == null ? "" : ex.toString();
		if (root != null) {
			msgsStr += ", root cause: " + root.toString();
		}

		return msgsStr;
	}

	/**
	 * Formats the specified time interval as an interval string with format: "d days hh:mm:ss.SSS"
	 *
	 * @param intervalMsec
	 *            time interval, in milliseconds
	 * @return formatted interval string
	 */
	public static String formatInterval(long intervalMsec) {
		long rem = intervalMsec;

		long days = rem / 86400000;
		rem -= days * 86400000;
		long hours = rem / 3600000;
		rem = -hours * 3600000;
		long min = rem / 60000;
		rem -= min * 60000;
		long sec = rem / 1000;
		rem -= sec * 1000;
		long msec = rem;

		return String.format("%d days %2d:%2d:%2d.%3s", days, hours, min, sec, msec);
	}

	/**
	 * <p>
	 * Returns the current time in microseconds.
	 * </p>
	 * <p>
	 * This is a wrapper around {@link com.jkoolcloud.tnt4j.utils.Useconds#get()}, returning the value in microsecond
	 * resolution.
	 * </p>
	 *
	 * @return the difference, measured in microseconds, between the current time and midnight, January 1, 1970 UTC
	 * @see com.jkoolcloud.tnt4j.utils.Useconds#get()
	 */
	public static long currentTimeUsec() {
		return Useconds.CURRENT.get();
	}

	/**
	 * Close an object without exceptions
	 *
	 * @param obj
	 *            object to close
	 */
	public static void close(Closeable obj) {
		try {
			if (obj != null) {
				obj.close();
			}
		} catch (Throwable e) {
		}
	}

	/**
	 * Check if a handle is open
	 *
	 * @param handle
	 *            IO handle
	 * @return true if handle is open, false otherwise
	 */
	public static boolean isOpen(Handle handle) {
		return handle != null && handle.isOpen();
	}

	/**
	 * Get integer value from a map of objects
	 *
	 * @param key
	 *            property key name
	 * @param props
	 *            map of property key/object pairs
	 * @param defValue
	 *            default value if key not found
	 * @return value associated with the given key in the map
	 */
	public static int getInt(String key, Map<?, ?> props, int defValue) {
		Object value = props.get(key);
		int iVal = value == null ? defValue
				: value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
		return iVal;
	}

	/**
	 * Get long value from a map of objects
	 *
	 * @param key
	 *            property key name
	 * @param props
	 *            map of property key/object pairs
	 * @param defValue
	 *            default value if key not found
	 * @return value associated with the given key in the map
	 */
	public static long getLong(String key, Map<?, ?> props, long defValue) {
		Object value = props.get(key);
		long iVal = value == null ? defValue
				: value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
		return iVal;
	}

	/**
	 * Get boolean value from a map of objects
	 *
	 * @param key
	 *            property key name
	 * @param props
	 *            map of property key/object pairs
	 * @param defValue
	 *            default value if key not found
	 * @return value associated with the given key in the map
	 */
	public static boolean getBoolean(String key, Map<?, ?> props, boolean defValue) {
		Object value = props.get(key);
		boolean bVal = value == null ? defValue
				: value instanceof Boolean ? ((Boolean) value) : Boolean.parseBoolean(value.toString());
		return bVal;
	}

	/**
	 * Get double value from a map of objects
	 *
	 * @param key
	 *            property key name
	 * @param props
	 *            map of property key/object pairs
	 * @param defValue
	 *            default value if key not found
	 * @return value associated with the given key in the map
	 */
	public static double getDouble(String key, Map<?, ?> props, double defValue) {
		Object value = props.get(key);
		double dVal = value == null ? defValue
				: value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
		return dVal;
	}

	/**
	 * Get float value from a map of objects
	 *
	 * @param key
	 *            property key name
	 * @param props
	 *            map of property key/object pairs
	 * @param defValue
	 *            default value if key not found
	 * @return value associated with the given key in the map
	 */
	public static float getFloat(String key, Map<?, ?> props, float defValue) {
		Object value = props.get(key);
		float fVal = value == null ? defValue
				: value instanceof Number ? ((Number) value).floatValue() : Float.parseFloat(value.toString());
		return fVal;
	}

	/**
	 * Get string value from a map of objects
	 *
	 * @param key
	 *            property key name
	 * @param props
	 *            map of property key/object pairs
	 * @param defValue
	 *            default value if key not found
	 * @return value associated with the given key in the map
	 */
	public static String getString(String key, Map<?, ?> props, String defValue) {
		Object value = props.get(key);
		String sVal = value == null ? defValue : value.toString();
		return sVal;
	}

	/**
	 * Create and apply a configurable object
	 *
	 * @param classProp
	 *            name of the property that contains class name (must exist in config)
	 * @param prefix
	 *            property prefix to be used for configuring a new object
	 * @param config
	 *            a map containing all configuration including class name
	 * @return configuration object
	 * @throws ConfigException
	 *             if error creating or applying configuration
	 *
	 */
	public static Object createConfigurableObject(String classProp, String prefix, Map<String, Object> config)
			throws ConfigException {
		Object className = config.get(classProp);
		if (className == null) {
			return null;
		}
		try {
			Object obj = Utils.createInstance(className.toString());
			return Utils.applyConfiguration(prefix, config, obj);
		} catch (ConfigException ce) {
			throw ce;
		} catch (Throwable e) {
			ConfigException ce = new ConfigException(e.getMessage(), config);
			ce.initCause(e);
			throw ce;
		}
	}

	/**
	 * Create and apply a configurable object
	 *
	 * @param classProp
	 *            name of the property that contains class name (must exist in config)
	 * @param prefix
	 *            property prefix to be used for configuring a new object
	 * @param config
	 *            a map containing all configuration including class name
	 * @return configuration object
	 * @throws ConfigException
	 *             if error instantiating configurable object
	 */
	public static Object createConfigurableObject(String classProp, String prefix, Properties config)
			throws ConfigException {
		Object className = config.get(classProp);
		if (className == null) {
			return null;
		}
		try {
			Object obj = Utils.createInstance(className.toString());
			return Utils.applyConfiguration(prefix, config, obj);
		} catch (ConfigException ce) {
			throw ce;
		} catch (Throwable e) {
			ConfigException ce = new ConfigException(e.getMessage(), config);
			ce.initCause(e);
			throw ce;
		}
	}

	/**
	 * Apply settings to a configurable object
	 *
	 * @param prefix
	 *            prefix to be used for pattern matching
	 * @param prop
	 *            list of properties used for patter matching
	 * @param obj
	 *            configurable object to apply settings to
	 * @return configuration object
	 * @throws ConfigException
	 *             if error applying configuration
	 */
	public static Object applyConfiguration(String prefix, Map<String, Object> prop, Object obj)
			throws ConfigException {
		if (obj instanceof Configurable) {
			return applyConfiguration(prefix, prop, (Configurable) obj);
		}
		return obj;
	}

	/**
	 * Apply settings to a configurable object
	 *
	 * @param prefix
	 *            prefix to be used for pattern matching
	 * @param prop
	 *            list of properties used for patter matching
	 * @param obj
	 *            configurable object to apply settings to
	 * @return configuration object
	 * @throws ConfigException
	 *             if error applying configuration
	 */
	public static Object applyConfiguration(String prefix, Properties prop, Object obj) throws ConfigException {
		if (obj instanceof Configurable) {
			return applyConfiguration(prefix, prop, (Configurable) obj);
		}
		return obj;
	}

	/**
	 * Apply settings to a configurable object
	 *
	 * @param prefix
	 *            prefix to be used for pattern matching
	 * @param prop
	 *            list of properties used for patter matching
	 * @param cfg
	 *            configurable object to apply settings to
	 * @return configuration object
	 * @throws ConfigException
	 *             if error applying configuration
	 */
	public static Configurable applyConfiguration(String prefix, Map<String, Object> prop, Configurable cfg)
			throws ConfigException {
		cfg.setConfiguration(getAttributes(prefix, prop));
		return cfg;
	}

	/**
	 * Apply settings to a configurable object
	 *
	 * @param prefix
	 *            prefix to be used for pattern matching
	 * @param prop
	 *            list of properties used for patter matching
	 * @param cfg
	 *            configurable object to apply settings to
	 * @return configuration object
	 * @throws ConfigException
	 *             if error applying configuration
	 */
	public static Configurable applyConfiguration(String prefix, Properties prop, Configurable cfg)
			throws ConfigException {
		cfg.setConfiguration(getAttributes(prefix, prop));
		return cfg;
	}

	/**
	 * Get object properties that match a certain prefix. New set of keys in the resulting map will exclude the prefix.
	 *
	 * @param prefix
	 *            prefix to be used for pattern matching
	 * @param p
	 *            list of properties used for patter matching
	 *
	 * @return a map containing only those attributes that match a prefix.
	 */
	public static Map<String, Object> getAttributes(String prefix, Map<String, Object> p) {
		HashMap<String, Object> settings = new HashMap<String, Object>(11);
		for (Entry<String, Object> entry : p.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(prefix)) {
				settings.put(key.substring(prefix.length()), entry.getValue());
			}
		}
		return settings;
	}

	/**
	 * Get object properties that match a certain prefix. New set of keys in the resulting map will exclude the prefix.
	 *
	 * @param prefix
	 *            prefix to be used for pattern matching
	 * @param p
	 *            list of properties used for patter matching
	 *
	 * @return a map containing only those attributes that match a prefix.
	 */
	public static Map<String, Object> getAttributes(String prefix, Properties p) {
		HashMap<String, Object> settings = new HashMap<String, Object>(11);
		for (Entry<Object, Object> entry : p.entrySet()) {
			String key = entry.getKey().toString();
			if (key.startsWith(prefix)) {
				settings.put(key.substring(prefix.length()), entry.getValue());
			}
		}
		return settings;
	}

	/**
	 * Create object instance based on specific class name
	 *
	 * @param className
	 *            name of the class
	 *
	 * @return instance of the objects specified by the class name
	 * @throws IllegalAccessException
	 *             if the class or its nullary constructor is not accessible
	 * @throws InstantiationException
	 *             if class is abstract, an interface, an array class, a primitive type, or void; or if the class has no
	 *             nullary constructor; or if the instantiation fails for some other reason
	 * @throws ClassNotFoundException
	 *             if the class cannot be located
	 */
	public static Object createInstance(String className)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (className == null) {
			return null;
		}
		Class<?> classObj = Class.forName(className);
		return classObj.newInstance();
	}

	/**
	 * Create object instance based on specific class name and given parameters
	 *
	 * @param className
	 *            name of the class
	 * @param args
	 *            arguments to be passed to the constructor
	 * @param types
	 *            list of parameter types
	 *
	 * @return instance of the objects specified by the class name
	 * @throws Exception
	 *             if error creating instance
	 */
	public static Object createInstance(String className, Object[] args, Class<?>... types) throws Exception {
		if (className == null) {
			return null;
		}
		Class<?> classObj = Class.forName(className);
		Constructor<?> ct = classObj.getConstructor(types);
		return ct.newInstance(args);
	}

	/**
	 * Serialize a given object into a byte array
	 *
	 * @param obj
	 *            serializable object
	 *
	 * @return byte array containing flat object
	 */
	public static byte[] serialize(Serializable obj) {
		return SerializationUtils.serialize(obj);
	}

	/**
	 * Serialize a given object into a byte array
	 *
	 * @param bytes
	 *            containing serializable object
	 *
	 * @return object
	 */
	public static Object deserialize(byte[] bytes) {
		return SerializationUtils.deserialize(bytes);
	}

	/**
	 * Checks whether provided number value is special: {@code 'Infinity'} or {@code 'NaN'}.
	 *
	 * @param value
	 *            number to check
	 * @return {@code true} if number value is {@code 'Infinity'} or {@code 'NaN'}, {@code false} - otherwise
	 *
	 * @see Float#isInfinite()
	 * @see Float#isNaN()
	 * @see Double#isInfinite(double)
	 * @see Double#isNaN(double)
	 */
	public static boolean isSpecialNumberValue(Number value) {
		if (value instanceof Double) {
			Double dValue = (Double) value;

			return dValue.isInfinite() || dValue.isNaN();
		} else if (value instanceof Float) {
			Float fValue = (Float) value;

			return fValue.isInfinite() || fValue.isNaN();
		} else {
			double dValue = value.doubleValue();

			return Double.isInfinite(dValue) || Double.isNaN(dValue);
		}
	}

	/**
	 * Returns the appropriate string representation for the specified object.
	 *
	 * @param value
	 *            object to convert to string representation
	 *
	 * @return string representation of object
	 */
	public static String toString(Object value) {
		if (value instanceof int[]) {
			return Arrays.toString((int[]) value);
		}
		if (value instanceof byte[]) {
			return Arrays.toString((byte[]) value);
		}
		if (value instanceof char[]) {
			return Arrays.toString((char[]) value);
		}
		if (value instanceof long[]) {
			return Arrays.toString((long[]) value);
		}
		if (value instanceof float[]) {
			return Arrays.toString((float[]) value);
		}
		if (value instanceof short[]) {
			return Arrays.toString((short[]) value);
		}
		if (value instanceof double[]) {
			return Arrays.toString((double[]) value);
		}
		if (value instanceof boolean[]) {
			return Arrays.toString((boolean[]) value);
		}
		if (value instanceof Object[]) {
			return toStringDeep((Object[]) value);
		}
		if (value instanceof Collection) {
			Collection<?> c = (Collection<?>) value;
			return toString(c.toArray());
		}
		if (value instanceof Map) {
			Map<?, ?> m = (Map<?, ?>) value;
			return toString(m.entrySet());
		}

		return String.valueOf(value);
	}

	/**
	 * Returns the appropriate string representation for the specified array.
	 *
	 * @param a
	 *            array to convert to string representation
	 *
	 * @return string representation of array
	 */
	public static String toStringDeep(Object[] a) {
		if (a == null) {
			return "null"; // NON-NLS
		}

		int iMax = a.length - 1;
		if (iMax == -1) {
			return "[]"; // NON-NLS
		}

		StringBuilder b = new StringBuilder(128);
		b.append('[');
		for (int i = 0;; i++) {
			b.append(toString(a[i]));
			if (i == iMax) {
				return b.append(']').toString();
			}
			b.append(", "); // NON-NLS
		}
	}

	/**
	 * Performs <tt>source</tt> string contents <tt>os</tt> replacement with provided new fragment <tt>ns</tt>.
	 *
	 * @param source
	 *            source string
	 * @param os
	 *            fragment to be replaced
	 * @param ns
	 *            replacement fragment
	 * @return string having replaced content
	 */
	public static String replace(String source, String os, String ns) {
		if (source == null) {
			return null;
		}
		int i = 0;
		if ((i = source.indexOf(os, i)) >= 0) {
			char[] sourceArray = source.toCharArray();
			char[] nsArray = ns.toCharArray();
			int oLength = os.length();
			StringBuilder buf = new StringBuilder(sourceArray.length);
			buf.append(sourceArray, 0, i).append(nsArray);
			i += oLength;
			int j = i;
			// Replace all remaining instances of oldString with newString.
			while ((i = source.indexOf(os, i)) > 0) {
				buf.append(sourceArray, j, i - j).append(nsArray);
				i += oLength;
				j = i;
			}
			buf.append(sourceArray, j, sourceArray.length - j);
			source = Utils.getString(buf);
		}
		return source;
	}

	/**
	 * Performs provided string contents replacement using symbols defined in {@code replacements} map.
	 *
	 * @param str
	 *            string to apply replacements
	 * @param replacements
	 *            replacements map
	 * @return string having applied replacements
	 *
	 * @see #replace(String, String, String)
	 */
	public static String replace(String str, Map<String, String> replacements) {
		if (StringUtils.isNotEmpty(str)) {
			for (Map.Entry<String, String> kre : replacements.entrySet()) {
				str = replace(str, kre.getKey(), kre.getValue());
			}
		}

		return str;
	}

	/**
	 * Parses replacements configuration string and puts replacements into provided map.
	 *
	 * @param repCfgStr
	 *            replacements configuration string
	 * @param replacementsMap
	 *            replacements map to alter
	 */
	public static void parseReplacements(String repCfgStr, Map<String, String> replacementsMap) {
		Matcher m = Utils.REP_CFG_PATTERN.matcher(repCfgStr);

		while (m.find()) {
			replacementsMap.put(StringEscapeUtils.unescapeJava(m.group(1)), StringEscapeUtils.unescapeJava(m.group(3)));
		}
	}

	/**
	 * Returns an input stream for reading the specified resource.
	 * <p>
	 * Does all the same as {@link #getResourceAsStream(Class, String)}, just setting resource loader {@code clazz} to
	 * {@code null}.
	 *
	 * @param resourceName
	 *            name (path) of the desired resource
	 * @return input stream to read the resource, or {@code null} if the resource could not be found
	 *
	 * @see #getResourceAsStream(Class, String)
	 */
	public static InputStream getResourceAsStream(String resourceName) {
		return getResourceAsStream(null, resourceName);
	}

	/**
	 * Returns an input stream for reading the specified resource.
	 * <p>
	 * This method takes provided {@code resourceName} builds two additional resource paths: absolute and relative. Then
	 * tries to load resource using this sequence:
	 * <ul>
	 * <li>over {@link java.lang.ClassLoader} using provided resource name (path)</li>
	 * <li>over {@link java.lang.ClassLoader} using absolute resource path</li>
	 * <li>over {@link java.lang.Class} using provided resource name (path)</li>
	 * <li>over {@link java.lang.Class} using absolute resource path</li>
	 * <li>over {@link java.lang.Class} using relative resource path</li>
	 * </ul>
	 *
	 * @param clazz
	 *            class to use for resource loading, or {@code null} to use {@link Thread#getContextClassLoader()}
	 * @param resourceName
	 *            name (path) of the desired resource
	 * @return input stream to read the resource, or {@code null} if the resource could not be found
	 *
	 * @see java.lang.ClassLoader#getResourceAsStream(String)
	 * @see Class#getResourceAsStream(String)
	 * @see Thread#getContextClassLoader()
	 */
	public static InputStream getResourceAsStream(Class<?> clazz, String resourceName) {
		if (StringUtils.isEmpty(resourceName)) {
			return null;
		}

		String aResourceName;
		String rResourceName;
		if (resourceName.startsWith("/")) {
			aResourceName = resourceName.substring(1);
			rResourceName = resourceName;
		} else {
			aResourceName = resourceName;
			rResourceName = "/" + resourceName;
		}

		Class<?> lClass = clazz == null ? Thread.currentThread().getClass() : clazz;
		ClassLoader cl = clazz == null ? Thread.currentThread().getContextClassLoader() : clazz.getClassLoader();

		InputStream ins = cl.getResourceAsStream(resourceName);
		if (ins == null && StringUtils.isNotEmpty(aResourceName)) {
			ins = cl.getResourceAsStream(aResourceName);
		}
		if (ins == null) {
			ins = lClass.getResourceAsStream(resourceName);
		}
		if (ins == null && StringUtils.isNotEmpty(aResourceName)) {
			ins = lClass.getResourceAsStream(aResourceName);
		}
		if (ins == null && StringUtils.isNotEmpty(rResourceName)) {
			ins = lClass.getResourceAsStream(rResourceName);
		}

		return ins;
	}

	/**
	 * Finds snapshot contained property by defined property name ignoring case.
	 *
	 * @param snapshot
	 *            property snapshot instance
	 * @param propName
	 *            property name
	 * @return snapshot contained property
	 */
	public static Property getSnapPropertyIgnoreCase(Snapshot snapshot, String propName) {
		if (snapshot != null) {
			for (Property prop : snapshot.getSnapshot()) {
				if (prop.getKey().equalsIgnoreCase(propName)) {
					return prop;
				}
			}
		}

		return null;
	}
}
