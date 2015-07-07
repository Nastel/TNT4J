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
package com.nastel.jkool.tnt4j.utils;

import java.io.Closeable;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.SerializationUtils;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.nastel.jkool.tnt4j.config.ConfigException;
import com.nastel.jkool.tnt4j.config.Configurable;

/**
 * General utility methods.
 *
 * @version $Revision: 5 $
 */
public class Utils {

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
	 * JVM runtime name
	 */
	public static final String VM_NAME = ManagementFactory.getRuntimeMXBean().getName();

	/**
	 * JVM process ID
	 */
	public static final long VM_PID = initVMID();

	public static final int CLIENT_CODE_STACK_INDEX;

	private static TimeBasedGenerator uuidGenerator;

	static {
		EthernetAddress nic = EthernetAddress.fromInterface();
		uuidGenerator = Generators.timeBasedGenerator(nic);
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
				long pid = Long.parseLong(vm_name.substring(0, index));
				return pid;
			}
		} catch (Throwable e) {
		}
		return 0;
	}


	/**
	 * Qualify given key with a given object class
	 *
	 * @param obj object to use
	 * @param key key to qualify
	 * @return return GUID string representation
	 */
	public static String qualify(Object obj, String key) {
		String newKey = obj.getClass().getSimpleName() + "-" + key;
		return newKey;
	}

	/**
	 * Return a new GUID as string
	 *
	 * @return return GUID string representation
	 */
	public static String newUUID() {
		return uuidGenerator.generate().toString();
	}

	/**
	 * Return current client stack index that identifies the
	 * calling stack frame return by <code>Thread.currentThread().getStackTrace()</code>
	 *
	 * @return return current stack frame
	 */
    public int getClientCodeStackIndex() {
    	return CLIENT_CODE_STACK_INDEX;
    }

	/**
	 * Print given message, stack trace to the underlying print stream
	 *
	 * @param msg user defined message
	 * @param trace stack trace
	 * @param out print stream where output is written
	 */
   public static void printStackTrace(String msg, StackTraceElement[] trace, PrintStream out) {
    	Exception ex = new Exception(msg);
    	ex.setStackTrace(trace);
    	ex.printStackTrace(out);
    }

	/**
	 * Print given message, stack trace to the underlying print writer
	 *
	 * @param msg user defined message
	 * @param trace stack trace
	 * @param out print writer where output is written
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
		return stack.length > index? stack[index]: stack[stack.length-1];
	}

	/**
	 * Return calling stack frame, which is right
	 * above in the current stack frame.
	 *
	 * @return return calling stack frame, right above the current call.
	 */
	public static StackTraceElement getCallingStackFrame() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		int index = CLIENT_CODE_STACK_INDEX + 1;
		return stack.length > index? stack[index]: stack[stack.length-1];
	}

	/**
	 * Return a specific stack frame with a given stack offset.
	 * offset 0 -- current, 1 -- calling, and so on.
	 *
	 * @param offset offset index within the calling stack
	 * @return return current stack frame
	 */
	public static StackTraceElement getStackFrame(int offset) {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		int index = CLIENT_CODE_STACK_INDEX + offset;
		return stack.length > index? stack[index]: stack[stack.length-1];
	}

	/**
	 * Return a specific stack frame with a given stack offset.
	 * offset 0 -- current, 1 -- calling, and so on.
	 *
	 * @param stack frame list
	 * @param offset offset index within the calling stack
	 * @return return current stack frame
	 */
	public static StackTraceElement getStackFrame(StackTraceElement[] stack, int offset) {
		int index = CLIENT_CODE_STACK_INDEX + offset;
		return stack.length > index? stack[index]: stack[stack.length-1];
	}

	/**
	 * Return calling stack frame which is right above a given class marker plus
	 * the offset.
	 *
	 * @param classMarker class marker on the stack
	 * @param offset offset on the stack from the marker
	 * @return Return calling stack frame which is right above a given class marker
	 */
	public static StackTraceElement getStackFrame(String classMarker, int offset) {
		int index = 0;
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		StackTraceElement first = null, found = stack[stack.length-1];
		for (StackTraceElement item: stack) {
			if (first == null && item.getClassName().startsWith(classMarker)) {
				first = item;
			} else if (first != null && !item.getClassName().startsWith(classMarker)) {
				found = stack[index+offset];
				break;
			}
			index++;
		}
		return found;
	}

	/**
	 * Format a given string pattern and a list of arguments
	 * as defined by <code>MessageFormat</code>
	 *
	 * @param pattern format string
	 * @param args arguments for format
	 * @return formatted string
	 */
	public static String format(String pattern, Object...args) {
		if (args != null && args.length > 0) {
			return MessageFormat.format(pattern, args);
		} else return String.valueOf(pattern);
	}

	/**
	 * Return a <code>Throwable</code> object if it is the last element
	 * in the object array
	 *
	 * @param args list of objects
	 * @return Throwable exception
	 */
	public static Throwable getThrowable(Object args[]) {
    	if ((args != null)
    			&& (args.length > 0)
    			&& (args[args.length-1] instanceof Throwable)) {
    		return (Throwable) args[args.length-1];
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
		if (url == null)
			return null;
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
		if (url == null)
			return null;
		try {
			return URLDecoder.decode(url, UTF8);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final String LINE_FEED = "\n";

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
	 * Returns true if the string is null or empty
	 *
	 * @param str
	 *            string handle
	 * @return Returns true if the string is null or empty
	 */
	public static boolean isEmpty(String str) {
		boolean empty = (str == null || str.trim().length() == 0);
		return empty;
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
			if (isEmpty(hostName))
				host = InetAddress.getLocalHost();
			else
				host = InetAddress.getByName(hostName);

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
			if (hostIp == null || hostIp.length == 0)
				host = InetAddress.getLocalHost();
			else
				host = InetAddress.getByAddress(hostIp);

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
		if (ex == null)
			return "";

		String detail = getStackTrace(ex);

		// find the root cause of exception
		Throwable rootCause = null;
		for (;;) {
			Throwable cause = ex.getCause();
			if (cause == null) {
				try {
					Method method = ex.getClass().getMethod("getTargetException", (Class[]) null);
					Object target = method.invoke(ex, (Object[]) null);
					if (target instanceof Throwable)
						cause = (Throwable) target;
				} catch (Exception exx) {
				}
			}

			if (cause == null)
				break;

			ex = cause;
			rootCause = cause;
		}

		if (rootCause != null)
			detail += LINE_FEED + "Root cause:" + LINE_FEED + getStackTrace(rootCause);

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
		if (ex == null)
			return "";

		String result = ex + LINE_FEED;

		StackTraceElement[] trace = ex.getStackTrace();
		for (int i = 0; i < trace.length; i++)
			result += "\tat " + trace[i] + LINE_FEED;

		return result;
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
	 * This is a wrapper around {@link com.nastel.jkool.tnt4j.utils.Useconds#get()}, returning the value in microsecond
	 * resolution.
	 * </p>
	 *
	 * @return the difference, measured in microseconds, between the current time and midnight, January 1, 1970 UTC
	 * @see com.nastel.jkool.tnt4j.utils.Useconds#get()
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
	 * Create and apply a configurable object
	 *
	 *@param classProp
	 *            name of the property that contains class name (must exist in config)
	 *@param prefix
	 *            property prefix to be used for configuring a new object
	 *@param config
	 *            a map containing all configuration including class name
	 *@return configuration object
	 *@throws ConfigException
	 *			  if error creating or applying configuration
	 *
	 */
	public static Object createConfigurableObject(String classProp, String prefix, Map<String, Object> config)
	        throws ConfigException {
		Object className = config.get(classProp);
		if (className == null) return null;
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
	 *@param classProp
	 *            name of the property that contains class name (must exist in config)
	 *@param prefix
	 *            property prefix to be used for configuring a new object
	 *@param config
	 *            a map containing all configuration including class name
	 *@return configuration object
	 *@throws ConfigException
	 *			  if error instantiating configurable object
	 */
	public static Object createConfigurableObject(String classProp, String prefix, Properties config)
	        throws  ConfigException {
		Object className = config.get(classProp);
		if (className == null) return null;
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
	 *@param prefix
	 *            prefix to be used for pattern matching
	 *@param prop
	 *            list of properties used for patter matching
	 *@param obj
	 *            configurable object to apply settings to
	 *@return configuration object
	 *@throws ConfigException
	 *			  if error applying configuration
	 */
	public static Object applyConfiguration(String prefix, Map<String, Object> prop, Object obj)  throws ConfigException {
		if (obj instanceof Configurable) {
			return applyConfiguration(prefix, prop, (Configurable) obj);
		}
		return obj;
	}

	/**
	 * Apply settings to a configurable object
	 *
	 *@param prefix
	 *            prefix to be used for pattern matching
	 *@param prop
	 *            list of properties used for patter matching
	 *@param obj
	 *            configurable object to apply settings to
	 *@return configuration object
	 *@throws ConfigException
	 *			  if error applying configuration
	 */
	public static Object applyConfiguration(String prefix, Properties prop, Object obj)  throws ConfigException {
		if (obj instanceof Configurable) {
			return applyConfiguration(prefix, prop, (Configurable) obj);
		}
		return obj;
	}

	/**
	 * Apply settings to a configurable object
	 *
	 *@param prefix
	 *            prefix to be used for pattern matching
	 *@param prop
	 *            list of properties used for patter matching
	 *@param cfg
	 *            configurable object to apply settings to
	 *@return configuration object
	 *@throws ConfigException
	 *			  if error applying configuration
	 */
	public static Configurable applyConfiguration(String prefix, Map<String, Object> prop, Configurable cfg) throws ConfigException {
		cfg.setConfiguration(getAttributes(prefix, prop));
		return cfg;
	}

	/**
	 * Apply settings to a configurable object
	 *
	 *@param prefix
	 *            prefix to be used for pattern matching
	 *@param prop
	 *            list of properties used for patter matching
	 *@param cfg
	 *            configurable object to apply settings to
	 *@return configuration object
	 *@throws ConfigException
	 *			  if error applying configuration
	 */
	public static Configurable applyConfiguration(String prefix, Properties prop, Configurable cfg) throws ConfigException {
		cfg.setConfiguration(getAttributes(prefix, prop));
		return cfg;
	}

	/**
	 * Get object properties that match a certain prefix. New set of keys in the resulting map will exclude the prefix.
	 *
	 *@param prefix
	 *            prefix to be used for pattern matching
	 *@param p
	 *            list of properties used for patter matching
	 *
	 *@return a map containing only those attributes that match a prefix.
	 */
	public static Map<String, Object> getAttributes(String prefix, Map<String, Object> p) {
		HashMap<String, Object> settings = new HashMap<String, Object>(11);
		for (Entry<String, Object> entry : p.entrySet()) {
			String key = entry.getKey().toString();
			if (key.startsWith(prefix)) {
				settings.put(key.substring(prefix.length()), entry.getValue());
			}
		}
		return settings;
	}

	/**
	 * Get object properties that match a certain prefix. New set of keys in the resulting map will exclude the prefix.
	 *
	 *@param prefix
	 *            prefix to be used for pattern matching
	 *@param p
	 *            list of properties used for patter matching
	 *
	 *@return a map containing only those attributes that match a prefix.
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
	 *@param className
	 *            name of the class
	 *
	 *@return instance of the objects specified by the class name
	 *@throws Exception if error instantiating class
	 */
	public static Object createInstance(String className) throws Exception {
		if (className == null)
			return null;
		Class<?> classObj = Class.forName(className);
		return classObj.newInstance();
	}

	/**
	 * Create object instance based on specific class name and given parameters
	 *
	 *@param className
	 *            name of the class
	 *@param args
	 *            arguments to be passed to the constructor
	 *@param types
	 *            list of parameter types
	 *
	 *@return instance of the objects specified by the class name
	 *@throws Exception if error creating instance
	 */
	public static Object createInstance(String className, Object[] args, Class<?>... types) throws Exception {
		if (className == null)
			return null;
		Class<?> classObj = Class.forName(className);
		Constructor<?> ct = classObj.getConstructor(types);
		return ct.newInstance(args);
	}


	/**
	 * Serialize a given object into a byte array
	 *
	 *@param obj
	 *            serializable object
	 *
	 *@return byte array containing flat object
	 */
	public static byte[] serialize(Serializable obj) {
	    return SerializationUtils.serialize(obj);
    }

	/**
	 * Serialize a given object into a byte array
	 *
	 *@param bytes
	 *            containing serializable object
	 *
	 *@return object
	 */
	public static Object deserialize(byte[] bytes) {
	    return SerializationUtils.deserialize(bytes);
    }

}
