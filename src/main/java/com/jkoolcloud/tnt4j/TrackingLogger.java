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
package com.jkoolcloud.tnt4j;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.jkoolcloud.tnt4j.config.DefaultConfigFactory;
import com.jkoolcloud.tnt4j.config.TrackerConfig;
import com.jkoolcloud.tnt4j.core.*;
import com.jkoolcloud.tnt4j.dump.*;
import com.jkoolcloud.tnt4j.selector.TrackingSelector;
import com.jkoolcloud.tnt4j.sink.*;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.source.SourceType;
import com.jkoolcloud.tnt4j.tracker.*;
import com.jkoolcloud.tnt4j.utils.Useconds;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * {@code TrackingLogger} is a helper class with calls to {@link Tracker} logging interface.
 * </p>
 * Application should use this helper class instead of obtaining a {@link Tracker} logger instance per thread using
 * {@link TrackerFactory}. {@code TrackingLogger} obtains the {@link Tracker} logger instance and stores it in thread
 * local associated for each thread.
 *
 * <p>
 * A {@link TrackingEvent} represents a specific tracking event that application creates for every discrete activity
 * such as JDBC, JMS, SOAP or any other relevant application activity. Application developers must obtain a
 * {@link Tracker} instance via {@link TrackerFactory}, create instances of {@link TrackingEvent} and use {@code log()}
 * calls to report tracking activities, events and log messages.
 *
 * <p>
 * {@link TrackingActivity} {@code start()/stop()} method calls used to mark application activity boundaries.
 * Applications must create instances of {@link TrackingEvent} using {@code TrackingLogger.newEvent()} method to time
 * individual sub-activities and report them using {@code TrackerLogger.tnt()} method call.
 * </p>
 *
 * <p>
 * Instrumenting typical application logic:
 * </p>
 *
 * <pre>
 * {@code 
 * TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(source);
 * TrackingLogger tracker = TrackingLogger.getInstance(config.build()); // register and obtain Tracker logger instance
 * TrackingActivity activity = tracker.newActivity(); // create a new activity instance
 * activity.start(); // start application activity timing
 * TrackingEvent event = tracker.newEvent(OpLevel.INFO, "SQL-SELECT", "SQL customer lookup"); // create a tracking event
 * TrackingEvent jms_event = tracker.newEvent(OpLevel.INFO, OpType.SEND, "JmsSend", "correlator", "Sending Message"); // create a tracking event
 * event.start(); // start timing a tracking event
 * try {
 * 	...
 * 	...
 * 	event.stop(); // stop timing tracking event
 * 	jms_event.start();
 * 	...
 * 	...
 * 	jms_event.stop(); // stop timing tracking event
 * } catch (SQLException e) {
 * 	event.stop(e); // stop timing tracking event and associate an exception
 * 	jms_event.stop(e); // stop timing tracking event and associate an exception
 * 	...
 * } finally {
 * 	activity.stop(); // end activity timing
 * 	activity.tnt(event); // track and trace tracking event within given activity
 * 	activity.tnt(jms_event); // track and trace tracking event within given activity
 * 	tracker.tnt(activity); // report a tracking activity
 * }
 * }
 * </pre>
 *
 * Source may take advantage of {@code TrackingLogger} conditional logging using {@code TrackingLogger.isSet()} based on
 * applications specific tokens. Below is an example of conditional logging:
 *
 * <pre>
 * {@code 
 * TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(source);
 * TrackingLogger tracker = TrackingLogger.getInstance(config.build()); // register and obtain Tracker logger instance
 * TrackingActivity activity = tracker.newActivity(); // create a new activity instance
 * activity.start(); // start application activity timing
 * TrackingEvent event = tracker.newEvent(OpLevel.NOTICE, "SQL-SELECT", "SQL customer lookup"); // create a tracking event
 * TrackingEvent jms_event = tracker.newEvent(OpLevel.NOTICE, OpType.SEND, "JmsSend", "correlator", "Sending Message"); // create a tracking event
 * event.start(); // start timing a tracking event
 * try {
 * 	...
 * 	...
 * 	event.stop(); // stop timing tracking event
 * 	jms_event.start();
 * 	...
 * 	...
 * 	jms_event.stop(); // stop timing tracking event
 * } catch (SQLException e) {
 * 	event.stop(e); // stop timing tracking event and associate an exception
 * 	jms_event.stop(e); // stop timing tracking event and associate an exception
 * 	...
 * } finally {
 * 	activity.stop(); // end activity timing
 *	// conditional logging using isSet() method to check if a given token matches
 *	if (tracker.isSet(OpLevel.INFO, "com.jkoolcloud.appl.corr", "correlator")) {
 *		activity.tnt(event); // track and trace tracking event within given activity
 *		activity.tnt(jms_event); // track and trace tracking event within given activity
 *	}
 * 	tracker.tnt(activity); // report a tracking activity
 * }
 * }
 * </pre>
 *
 * {@code TrackingLogger} provides a capability to simplify and automate application specific dump handling. An
 * application dump is a collection of application's internal metrics that can be used for problem diagnostics. Source
 * must create an instance of {@code DumpProvider} and register it with {@code TrackingLogger} optionally associate it
 * with a given dump destination {@code DumpSink}(where dump is written to). Dumps can be generated using
 * {@code TrackingLogger.dump()} or can be triggered on JVM shutdown using {@code TrackingLogger.dumpOnShutdown(true)}.
 * By default, {@code TrackingLogger} uses file based {@code DefaultDumpSinkFactory} to generate instances of
 * {@code DumpSink}.
 *
 * <pre>
 * {@code
 * // associated dump provider with a default dump destination (file)
 * TrackingLogger.addDumpProvider(new MyDumpProvider());
 * TrackingLogger.dumpOnShutdown(true);
 * ...
 * // associated dump provider with a user define dump file
 * TrackingLogger.addDumpProvider(TrackingLogger.getDumpDestinationFactory().getInstance("my-dump.log"), new MyDumpProvider());
 * TrackingLogger.dumpOnShutdown(true);
 * ...
 * TrackingLogger.dumpState(); // MyDumpProvider will be called when dumpState() is called.
 * }
 * </pre>
 *
 *
 * @see OpLevel
 * @see OpType
 * @see Tracker
 * @see TrackingEvent
 * @see TrackingActivity
 * @see TrackerFactory
 * @see DumpProvider
 * @see DumpSink
 * @see DumpListener
 * @see SinkErrorListener
 *
 * @version $Revision: 21 $
 *
 */
public class TrackingLogger implements Tracker {
	private static final String TRACKER_CONFIG = System.getProperty("tnt4j.tracking.logger.config");
	private static final String TRACKER_SOURCE = System.getProperty("tnt4j.tracking.logger.source",
			TrackingLogger.class.getName());

	private static final ConcurrentHashMap<DumpProvider, List<DumpSink>> DUMP_DEST_TABLE = new ConcurrentHashMap<>(49);
	private static final Map<TrackingLogger, StackTraceElement[]> TRACKERS = Collections
			.synchronizedMap(new WeakHashMap<TrackingLogger, StackTraceElement[]>(89));

	private static final List<DumpProvider> DUMP_PROVIDERS = new ArrayList<>(10);
	private static final List<DumpSink> DUMP_DESTINATIONS = new ArrayList<>(10);
	private static final List<DumpListener> DUMP_LISTENERS = new ArrayList<>(10);

	private static final DumpHook dumpHook = new DumpHook();
	private static final FlushShutdown flushShutdown = new FlushShutdown();
	private static DumpSinkFactory dumpFactory = null;
	private static DumpSink defaultDumpSink = null;
	private static TrackerFactory factory = null;

	private Tracker logger;
	private TrackingSelector selector;

	static {
		// load configuration and initialize default factories
		initJavaTiming();
		TrackerConfig config = DefaultConfigFactory.getInstance()
				.getConfig(TRACKER_SOURCE, SourceType.APPL, TRACKER_CONFIG).build();
		DefaultEventSinkFactory.setDefaultEventSinkFactory(config.getDefaultEvenSinkFactory());
		factory = config.getTrackerFactory();
		dumpFactory = config.getDumpSinkFactory();
		defaultDumpSink = dumpFactory.getInstance();

		boolean enableDefaultDumpProviders = config.getBoolean("tracker.dump.provider.default",
				Boolean.getBoolean("tnt4j.dump.provider.default"));
		boolean dumpOnVmHook = config.getBoolean("tracker.dump.on.vm.shutdown",
				Boolean.getBoolean("tnt4j.dump.on.vm.shutdown"));
		boolean dumpOnException = config.getBoolean("tracker.dump.on.exception",
				Boolean.getBoolean("tnt4j.dump.on.exception"));
		boolean flushOnVmHook = config.getBoolean("tracker.flush.on.vm.shutdown",
				Boolean.getBoolean("tnt4j.flush.on.vm.shutdown"));

		if (enableDefaultDumpProviders) {
			addDumpProvider(defaultDumpSink, new PropertiesDumpProvider(Utils.VM_NAME));
			addDumpProvider(defaultDumpSink, new MXBeanDumpProvider(Utils.VM_NAME));
			addDumpProvider(defaultDumpSink, new ThreadDumpProvider(Utils.VM_NAME));
			addDumpProvider(defaultDumpSink, new ThreadDeadlockDumpProvider(Utils.VM_NAME));
			addDumpProvider(defaultDumpSink, new LoggerDumpProvider(Utils.VM_NAME));
		}
		if (dumpOnVmHook) {
			dumpOnShutdown(dumpOnVmHook);
		}
		if (dumpOnException) {
			dumpOnUncaughtException();
		}
		if (flushOnVmHook) {
			flushOnShutdown(flushOnVmHook);
		}
	}

	/** Cannot instantiate. */
	private TrackingLogger(Tracker trg) {
		logger = trg;
		selector = logger.getTrackingSelector();
	}

	/**
	 * Check and enable java timing for use by activities
	 *
	 */
	private static void initJavaTiming() {
		ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
		boolean cpuTimingSupported = tmbean.isCurrentThreadCpuTimeSupported();
		if (cpuTimingSupported) {
			tmbean.setThreadCpuTimeEnabled(cpuTimingSupported);
		}
		boolean contTimingSupported = tmbean.isThreadContentionMonitoringSupported();
		if (contTimingSupported) {
			tmbean.setThreadContentionMonitoringEnabled(contTimingSupported);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	private void checkState() {
		if (logger == null) {
			throw new IllegalStateException("tracker not initialized");
		}
	}

	private static void registerTracker(TrackingLogger tracker) {
		TRACKERS.put(tracker, Thread.currentThread().getStackTrace());
	}

	/**
	 * Obtain an allocation stack trace for the specified logger instance
	 *
	 * @param logger
	 *            instance
	 *
	 * @return an allocation stack trace for the logger instance
	 */
	public static StackTraceElement[] getTrackerStackTrace(TrackingLogger logger) {
		return TRACKERS.get(logger);
	}

	/**
	 * Obtain an a list of all registered/active logger instances.
	 *
	 * @return a list of all active tracking logger instances
	 */
	public static List<TrackingLogger> getAllTrackers() {
		synchronized (TRACKERS) {
			ArrayList<TrackingLogger> copy = new ArrayList<>(TRACKERS.size());
			for (TrackingLogger logger : TRACKERS.keySet()) {
				if (logger != null) {
					copy.add(logger);
				}
			}
			return copy;
		}
	}

	/**
	 * Flush all available trackers
	 *
	 */
	public static void flushAll() {
		List<TrackingLogger> trackers = getAllTrackers();
		for (TrackingLogger logger : trackers) {
			try {
				EventSink sink = logger.getEventSink();
				sink.flush();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Shutdown all available trackers and sinks.
	 */
	public static void shutdownAll() {
		List<TrackingLogger> trackers = getAllTrackers();
		for (TrackingLogger logger : trackers) {
			shutdown(logger);
		}
	}

	/**
	 * Shutdown defined tracking logger.
	 *
	 * @param logger
	 *            tracking logger to shut down
	 */
	public static void shutdown(TrackingLogger logger) {
		try {
			EventSink sink = logger.getEventSink();
			if (sink instanceof IOShutdown) {
				IOShutdown shut = (IOShutdown) sink;
				shut.shutdown(null);
			}
		} catch (IOException e) {
		}
	}

	/**
	 * Obtain a stack trace list for all tracker allocations to determine where the tracker instances have been
	 * instantiated
	 *
	 * @return a list of stack traces for each allocated tracker
	 */
	public static List<StackTraceElement[]> getAllTrackerStackTrace() {
		synchronized (TRACKERS) {
			ArrayList<StackTraceElement[]> copy = new ArrayList<>(TRACKERS.size());
			for (StackTraceElement[] trace : TRACKERS.values()) {
				if (trace != null) {
					copy.add(trace);
				}
			}
			return copy;
		}
	}

	/**
	 * Obtain an instance of {@code TrackingLogger} logger.
	 *
	 * @param config
	 *            tracking configuration to be used to create a tracker instance
	 * @return tracking logger instance
	 * @see TrackerConfig
	 */
	public static TrackingLogger getInstance(TrackerConfig config) {
		TrackingLogger tracker = new TrackingLogger(factory.getInstance(config));
		registerTracker(tracker);
		return tracker;
	}

	/**
	 * Obtain an instance of {@code TrackingLogger} logger.
	 *
	 * @param sourceName
	 *            application source name associated with this logger
	 * @return tracking logger instance
	 * @see TrackerConfig
	 */
	public static TrackingLogger getInstance(String sourceName) {
		TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(sourceName);
		TrackingLogger tracker = new TrackingLogger(factory.getInstance(config.build()));
		registerTracker(tracker);
		return tracker;
	}

	/**
	 * Obtain an instance of {@code TrackingLogger} logger.
	 *
	 * @param sourceName
	 *            application source name associated with this logger
	 * @param configMap
	 *            configuration map containing source/properties configuration
	 * @return tracking logger instance
	 * @see TrackerConfig
	 */
	public static TrackingLogger getInstance(String sourceName, Map<String, Properties> configMap) {
		TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(sourceName, SourceType.APPL, configMap);
		TrackingLogger tracker = new TrackingLogger(factory.getInstance(config.build()));
		registerTracker(tracker);
		return tracker;
	}

	/**
	 * Obtain an instance of {@code TrackingLogger} logger.
	 *
	 * @param sourceName
	 *            application source name associated with this logger
	 * @param type
	 *            application source type associated with this logger
	 * @return tracking logger instance
	 * @see TrackerConfig
	 */
	public static TrackingLogger getInstance(String sourceName, SourceType type) {
		TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(sourceName, type);
		TrackingLogger tracker = new TrackingLogger(factory.getInstance(config.build()));
		registerTracker(tracker);
		return tracker;
	}

	/**
	 * Obtain an instance of {@code TrackingLogger} logger.
	 *
	 * @param sourceName
	 *            application source name associated with this logger
	 * @param type
	 *            application source type associated with this logger
	 * @param configMap
	 *            configuration map containing source/properties configuration
	 * @return tracking logger instance
	 * @see TrackerConfig
	 */
	public static TrackingLogger getInstance(String sourceName, SourceType type, Map<String, Properties> configMap) {
		TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(sourceName, type, configMap);
		TrackingLogger tracker = new TrackingLogger(factory.getInstance(config.build()));
		registerTracker(tracker);
		return tracker;
	}

	/**
	 * Obtain an instance of {@code TrackingLogger} logger based on a given class.
	 *
	 * @param clazz
	 *            application class used as source name
	 * @return tracking logger instance
	 * @see TrackerConfig
	 */
	public static TrackingLogger getInstance(Class<?> clazz) {
		TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(clazz);
		TrackingLogger tracker = new TrackingLogger(factory.getInstance(config.build()));
		registerTracker(tracker);
		return tracker;
	}

	/**
	 * Obtain an instance of {@code TrackingLogger} logger based on a given class.
	 *
	 * @param clazz
	 *            application class used as source name
	 * @param configMap
	 *            configuration map containing source/properties configuration
	 * @return tracking logger instance
	 * @see TrackerConfig
	 */
	public static TrackingLogger getInstance(Class<?> clazz, Map<String, Properties> configMap) {
		TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(clazz, SourceType.APPL, configMap);
		TrackingLogger tracker = new TrackingLogger(factory.getInstance(config.build()));
		registerTracker(tracker);
		return tracker;
	}

	/**
	 * Register a user defined tracker factory. Default is {@code DefaultTrackerFactory}.
	 *
	 * @param fac
	 *            User defined tracker factory
	 * @see TrackerFactory
	 * @see DefaultTrackerFactory
	 */
	public static void setTrackerFactory(TrackerFactory fac) {
		factory = (fac != null ? fac : factory);
	}

	/**
	 * Register a user defined dump destination factory used to generate instances of {@code DumpSink}. Default is
	 * {@code DefaultDumpSinkFactory}.
	 *
	 * @param defFac
	 *            User default dump destination factory
	 * @param defDest
	 *            User default dump destination
	 * @see DumpSink
	 * @see DumpSinkFactory
	 * @see DefaultDumpSinkFactory
	 */
	public static void setDefaultDumpConfig(DumpSinkFactory defFac, DumpSink defDest) {
		dumpFactory = (defFac != null ? defFac : dumpFactory);
		defaultDumpSink = (defDest != null ? defDest : defaultDumpSink);
	}

	/**
	 * Return currently registered dump sink factory. Default is {@code DefaultDumpSinkFactory}.
	 *
	 * @return currently registered dump sink factory
	 * @see DumpSinkFactory
	 * @see DefaultDumpSinkFactory
	 */
	public static DumpSinkFactory getDumpSinkFactory() {
		return dumpFactory;
	}

	/**
	 * Determine of a particular sev/key/value combination is trackable. Use this method to determine if tracking is
	 * enabled/disabled for a specific key/value pair. Example, checking if order id "723772" is trackable:
	 *
	 * {@code logger.isSet(OpLevel.INFO, "orderapp.order.id", "723772");}
	 *
	 * @param sev
	 *            severity of to be checked
	 * @param key
	 *            key associated with tracking activity
	 * @param value
	 *            associated value with a given key
	 *
	 * @return true of combination is set, false otherwise
	 * @see OpLevel
	 */
	public boolean isSet(OpLevel sev, Object key, Object value) {
		if (logger != null) {
			return selector.isSet(sev, key, value);
		}
		return false;
	}

	/**
	 * Determine of a particular sev/key is trackable. Use this method to determine if tracking is enabled/disabled for
	 * a specific severity. This call is equivalent to {@code logger.isSet(sev, key, null);}
	 *
	 * @param sev
	 *            severity of to be checked
	 *
	 * @param key
	 *            key to be checked for being trackable
	 *
	 * @return true of combination is set, false otherwise
	 * @see OpLevel
	 */
	public boolean isSet(OpLevel sev, Object key) {
		if (logger != null) {
			return selector.isSet(sev, key);
		}
		return false;
	}

	/**
	 * Determine if a particular sev for the registered application name used in {@code TrackingLogger.getInstance()}
	 * call. Use this method to determine if tracking is enabled/disabled for a specific severity. This call is
	 * equivalent to {@code logger.getTracker().getEventSink().isSet(sev)}
	 *
	 * @param sev
	 *            severity of to be checked
	 *
	 * @return true of combination is set, false otherwise
	 * @see OpLevel
	 */
	public boolean isSet(OpLevel sev) {
		if (logger != null) {
			return logger.getEventSink().isSet(sev);
		}
		return false;
	}

	/**
	 * Set sev/key/value combination for tracking
	 *
	 * @param sev
	 *            severity of to be checked
	 * @param key
	 *            key associated with tracking activity
	 * @param value
	 *            associated value with a given key
	 *
	 * @see OpLevel
	 */
	public void set(OpLevel sev, Object key, Object value) {
		if (logger != null) {
			selector.set(sev, key, value);
		}
	}

	/**
	 * Set sev/key combination for tracking. This is the same as calling {@code set(sev, key, null)}, where value is
	 * null.
	 *
	 * @param sev
	 *            severity of to be checked
	 * @param key
	 *            key associated with tracking activity
	 *
	 * @see OpLevel
	 */
	public void set(OpLevel sev, Object key) {
		if (logger != null) {
			selector.set(sev, key);
		}
	}

	/**
	 * Get value associated with a given key from the tracking selector repository.
	 *
	 * @param key
	 *            key associated with tracking activity
	 * @return value for specified key, or {@code null} if key not found
	 */
	public Object get(Object key) {
		if (logger != null) {
			return selector.get(key);
		}
		return null;
	}

	/**
	 * Obtain a list of keys available in the selector
	 *
	 * @return iterator containing all available keys
	 */
	public Iterator<? extends Object> getKeys() {
		if (logger != null) {
			return selector.getKeys();
		}
		return null;
	}

	/**
	 * Close this instance of {@code TrackingLogger}. Existing {@link Tracker} logger (if already opened) is closed and
	 * released.
	 *
	 * @see TrackerConfig
	 */
	@Override
	public void close() {
		if (logger != null) {
			factory.close(logger);
			TRACKERS.remove(this);
		}
	}

	/**
	 * Log a single message with a given severity level and a number of user supplied arguments. Message pattern is
	 * based on the format defined by {@code MessageFormat}. This logging type is more efficient than string
	 * concatenation.
	 * 
	 * <pre>
	 * {@code 
	 * logger.log(OpLevel.DEBUG, "My message arg={0}, arg={1}", parm1, parm2);
	 * }
	 * </pre>
	 * 
	 * @param level
	 *            severity level
	 * @param msg
	 *            message or message pattern
	 * @param args
	 *            user defined arguments supplied along side given message
	 * @see OpLevel
	 * @see java.text.MessageFormat
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 */
	@Override
	public void log(OpLevel level, String msg, Object... args) {
		checkState();
		logger.log(level, msg, args);
	}

	/**
	 * Log a single DEBUG message and a number of user supplied arguments. Message pattern is based on the format
	 * defined by {@code MessageFormat}. This logging type is more efficient than string concatenation.
	 * 
	 * <pre>
	 * {@code 
	 * logger.debug("My message arg={0}, arg={1}", parm1, parm2);
	 * }
	 * </pre>
	 * 
	 * @param msg
	 *            message or message pattern
	 * @param args
	 *            user defined arguments supplied along side given message
	 * @see OpLevel
	 * @see java.text.MessageFormat
	 */
	public void debug(String msg, Object... args) {
		log(OpLevel.DEBUG, msg, args);
	}

	/**
	 * Log a single TRACE message and a number of user supplied arguments. Message pattern is based on the format
	 * defined by {@code MessageFormat}. This logging type is more efficient than string concatenation.
	 * 
	 * <pre>
	 * {@code 
	 * logger.trace("My message arg={0}, arg={1}", parm1, parm2);
	 * }
	 * </pre>
	 * 
	 * @param msg
	 *            message or message pattern
	 * @param args
	 *            user defined arguments supplied along side given message
	 * @see OpLevel
	 * @see java.text.MessageFormat
	 */
	public void trace(String msg, Object... args) {
		log(OpLevel.TRACE, msg, args);
	}

	/**
	 * Log a single ERROR message and a number of user supplied arguments. Message pattern is based on the format
	 * defined by {@code MessageFormat}. This logging type is more efficient than string concatenation.
	 * 
	 * <pre>
	 * {@code 
	 * logger.error("My error message arg={0}, arg={1}", parm1, parm2);
	 * }
	 * </pre>
	 * 
	 * @param msg
	 *            message or message pattern
	 * @param args
	 *            user defined arguments supplied along side given message
	 * @see OpLevel
	 * @see java.text.MessageFormat
	 */
	public void error(String msg, Object... args) {
		log(OpLevel.ERROR, msg, args);
	}

	/**
	 * Log a single FATAL message and a number of user supplied arguments. Message pattern is based on the format
	 * defined by {@code MessageFormat}. This logging type is more efficient than string concatenation.
	 * 
	 * <pre>
	 * {@code 
	 * logger.fatal("My error message arg={0}, arg={1}", parm1, parm2);
	 * }
	 * </pre>
	 * 
	 * @param msg
	 *            message or message pattern
	 * @param args
	 *            user defined arguments supplied along side given message
	 * @see OpLevel
	 * @see java.text.MessageFormat
	 */
	public void fatal(String msg, Object... args) {
		log(OpLevel.FATAL, msg, args);
	}

	/**
	 * Log a single HALT message and a number of user supplied arguments. Message pattern is based on the format defined
	 * by {@code MessageFormat}. This logging type is more efficient than string concatenation.
	 * 
	 * <pre>
	 * {@code 
	 * logger.halt("My error message arg={0}, arg={1}", parm1, parm2);
	 * }
	 * </pre>
	 * 
	 * @param msg
	 *            message or message pattern
	 * @param args
	 *            user defined arguments supplied along side given message
	 * @see OpLevel
	 * @see java.text.MessageFormat
	 */
	public void halt(String msg, Object... args) {
		log(OpLevel.HALT, msg, args);
	}

	/**
	 * Log a single WARNING message and a number of user supplied arguments. Message pattern is based on the format
	 * defined by {@code MessageFormat}. This logging type is more efficient than string concatenation.
	 * 
	 * <pre>
	 * {@code 
	 * logger.warn("My message arg={0}, arg={1}", parm1, parm2);
	 * }
	 * </pre>
	 * 
	 * @param msg
	 *            message or message pattern
	 * @param args
	 *            user defined arguments supplied along side given message
	 * @see OpLevel
	 * @see java.text.MessageFormat
	 */
	public void warn(String msg, Object... args) {
		log(OpLevel.WARNING, msg, args);
	}

	/**
	 * Log a single INFO message and a number of user supplied arguments. Message pattern is based on the format defined
	 * by {@code MessageFormat}. This logging type is more efficient than string concatenation.
	 * 
	 * <pre>
	 * {@code 
	 * logger.info("My message arg={0}, arg={1}", parm1, parm2);
	 * }
	 * </pre>
	 * 
	 * @param msg
	 *            message or message pattern
	 * @param args
	 *            user defined arguments supplied along side given message
	 * @see OpLevel
	 * @see java.text.MessageFormat
	 */
	public void info(String msg, Object... args) {
		log(OpLevel.INFO, msg, args);
	}

	/**
	 * Log a single NOTICE message and a number of user supplied arguments. Message pattern is based on the format
	 * defined by {@code MessageFormat}. This logging type is more efficient than string concatenation.
	 * 
	 * <pre>
	 * {@code 
	 * logger.notice("My message arg={0}, arg={1}", parm1, parm2);
	 * }
	 * </pre>
	 * 
	 * @param msg
	 *            message or message pattern
	 * @param args
	 *            user defined arguments supplied along side given message
	 * @see OpLevel
	 * @see java.text.MessageFormat
	 */
	public void notice(String msg, Object... args) {
		log(OpLevel.NOTICE, msg, args);
	}

	/**
	 * Report a single tracking activity. Call after instance of {@link TrackingActivity} has been completed using
	 * {@code TrackingActivity.stop()} and {@code TrackingActivity.tnt()} calls.
	 *
	 * @param activity
	 *            tracking activity to be reported
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see TrackingActivity
	 */
	@Override
	public void tnt(TrackingActivity activity) {
		if (activity == null) {
			return;
		}
		checkState();
		logger.tnt(activity);
	}

	/**
	 * Report a single tracking event as a single activity. Call after instance of {@link TrackingEvent} has been
	 * completed using {@code TrackingEvent.stop()} call.
	 * 
	 * @param event
	 *            tracking event to be reported as a single activity
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see TrackingEvent
	 */
	@Override
	public void tnt(TrackingEvent event) {
		if (event == null) {
			return;
		}
		checkState();
		logger.tnt(event);
	}

	/**
	 * Report a single snapshot.
	 *
	 * @param snapshot
	 *            snapshot to be tracked and logged
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see Snapshot
	 * @see Property
	 */
	@Override
	public void tnt(Snapshot snapshot) {
		if (snapshot == null) {
			return;
		}
		checkState();
		logger.tnt(snapshot);
	}

	/**
	 * Report a single tracking event
	 *
	 * @param severity
	 *            severity level of the reported message
	 * @param opName
	 *            operation name associated with the event message
	 * @param correlator
	 *            event correlator
	 * @param msg
	 *            event text message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see TrackingActivity
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, String opName, String correlator, String msg, Object... args) {
		tnt(severity, OpType.CALL, opName, correlator, 0, msg, args);
	}

	/**
	 * Report a single tracking event
	 *
	 * @param severity
	 *            severity level of the reported message
	 * @param opType
	 *            operation type
	 * @param opName
	 *            operation name associated with the event message
	 * @param correlator
	 *            event correlator
	 * @param elapsed
	 *            elapsed time of the event in microseconds.
	 * @param msg
	 *            event text message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see TrackingActivity
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, OpType opType, String opName, String correlator, long elapsed, String msg,
			Object... args) {
		tnt(severity, opType, opName, correlator, null, elapsed, msg, args);
	}

	/**
	 * Report a single tracking event
	 *
	 * @param severity
	 *            severity level of the reported message
	 * @param opType
	 *            operation type
	 * @param opName
	 *            operation name associated with the event message
	 * @param correlator
	 *            event correlator
	 * @param tag
	 *            message tag
	 * @param elapsed
	 *            elapsed time of the event in microseconds.
	 * @param msg
	 *            event text message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see TrackingActivity
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, OpType opType, String opName, String correlator, String tag, long elapsed,
			String msg, Object... args) {
		checkState();
		TrackingEvent event = logger.newEvent(severity, opType, opName, correlator, tag, msg, args);
		Throwable ex = Utils.getThrowable(args);
		event.stop(ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS, 0, ex, Useconds.CURRENT.get(), elapsed);
		logger.tnt(event);
	}

	/**
	 * Report a single tracking event using a binary message body
	 *
	 * @param severity
	 *            severity level of the reported message
	 * @param opName
	 *            operation name associated with the event message
	 * @param correlator
	 *            event correlator
	 * @param msg
	 *            event binary message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see TrackingActivity
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, String opName, String correlator, byte[] msg, Object... args) {
		tnt(severity, OpType.CALL, opName, correlator, 0, msg, args);
	}

	/**
	 * Report a single tracking event using a binary message body
	 *
	 * @param severity
	 *            severity level of the reported message
	 * @param opType
	 *            operation type
	 * @param opName
	 *            operation name associated with the event message
	 * @param correlator
	 *            event correlator
	 * @param elapsed
	 *            elapsed time of the event in microseconds.
	 * @param msg
	 *            event binary message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see TrackingActivity
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, OpType opType, String opName, String correlator, long elapsed, byte[] msg,
			Object... args) {
		tnt(severity, opType, opName, correlator, null, elapsed, msg, args);
	}

	/**
	 * Report a single tracking event using a binary message body
	 *
	 * @param severity
	 *            severity level of the reported message
	 * @param opType
	 *            operation type
	 * @param opName
	 *            operation name associated with the event message
	 * @param correlator
	 *            event correlator
	 * @param tag
	 *            message tag
	 * @param elapsed
	 *            elapsed time of the event in microseconds.
	 * @param msg
	 *            event binary message
	 * @param args
	 *            argument list, exception passed along side given message
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see TrackingActivity
	 * @see OpLevel
	 */
	public void tnt(OpLevel severity, OpType opType, String opName, String correlator, String tag, long elapsed,
			byte[] msg, Object... args) {
		checkState();
		TrackingEvent event = logger.newEvent(severity, opType, opName, correlator, tag, msg, args);
		Throwable ex = Utils.getThrowable(args);
		event.stop(ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS, 0, ex, Useconds.CURRENT.get(), elapsed);
		logger.tnt(event);
	}

	@Override
	public Snapshot newSnapshot(String name) {
		checkState();
		return logger.newSnapshot(name);
	}

	@Override
	public Snapshot newSnapshot(String cat, String name) {
		checkState();
		return logger.newSnapshot(cat, name);
	}

	@Override
	public Snapshot newSnapshot(String cat, String name, OpLevel level) {
		checkState();
		return logger.newSnapshot(cat, name, level);
	}

	@Override
	public Property newProperty(String key, Object val) {
		checkState();
		return logger.newProperty(key, val);
	}

	@Override
	public Property newProperty(String key, Object val, String valType) {
		checkState();
		return logger.newProperty(key, val, valType);
	}

	@Override
	public TrackingActivity newActivity() {
		checkState();
		return logger.newActivity(OpLevel.INFO);
	}

	@Override
	public TrackingActivity newActivity(OpLevel level) {
		checkState();
		return logger.newActivity(level);
	}

	@Override
	public TrackingActivity newActivity(OpLevel level, String name) {
		checkState();
		return logger.newActivity(level, name);
	}

	@Override
	public TrackingActivity newActivity(OpLevel level, String name, String signature) {
		checkState();
		return logger.newActivity(level, name, signature);
	}

	@Override
	public TrackingEvent newEvent(String opName, String msg, Object... args) {
		checkState();
		return logger.newEvent(opName, msg, args);
	}

	@Override
	public TrackingEvent newEvent(OpLevel severity, String opName, String correlator, String msg, Object... args) {
		checkState();
		return logger.newEvent(severity, opName, correlator, msg, args);
	}

	@Override
	public TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, String correlator, String tag,
			String msg, Object... args) {
		checkState();
		return logger.newEvent(severity, opType, opName, correlator, tag, msg, args);
	}

	@Override
	public TrackingEvent newEvent(OpLevel severity, String opName, String correlator, byte[] msg, Object... args) {
		checkState();
		return logger.newEvent(severity, opName, correlator, msg, args);
	}

	@Override
	public TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, String correlator, String tag,
			byte[] msg, Object... args) {
		checkState();
		return logger.newEvent(severity, opType, opName, correlator, tag, msg, args);
	}

	@Override
	public TrackingEvent newEvent(OpLevel severity, String opName, Collection<String> correlators, String msg,
			Object... args) {
		checkState();
		return logger.newEvent(severity, opName, correlators, msg, args);
	}

	@Override
	public TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, Collection<String> correlators,
			Collection<String> tags, String msg, Object... args) {
		checkState();
		return logger.newEvent(severity, opType, opName, correlators, tags, msg, args);
	}

	@Override
	public TrackingEvent newEvent(OpLevel severity, String opName, Collection<String> correlators, byte[] msg,
			Object... args) {
		checkState();
		return logger.newEvent(severity, opName, correlators, msg, args);
	}

	@Override
	public TrackingEvent newEvent(OpLevel severity, OpType opType, String opName, Collection<String> correlators,
			Collection<String> tags, byte[] msg, Object... args) {
		checkState();
		return logger.newEvent(severity, opType, opName, correlators, tags, msg, args);
	}

	/**
	 * Returns currently registered {@link Tracker} logger associated with the current thread. {@link Tracker} logger is
	 * associated with the current thread after the register() call. {@link Tracker} logger instance is not thread safe.
	 *
	 * @return {@link Tracker} logger associated with the current thread or null of non available.
	 * @see Tracker
	 */
	public Tracker getTracker() {
		return logger;
	}

	/**
	 * Register a tracking filter associated with the tracker. Tracking filter allows consolidation of all conditional
	 * tracking logic into a single class.
	 *
	 * @see TrackingFilter
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 */
	@Override
	public void setTrackingFilter(TrackingFilter filter) {
		checkState();
		logger.setTrackingFilter(filter);
	}

	/**
	 * Add a sink log listener, which is triggered log activities occurs when writing to the event sink.
	 *
	 * @param listener
	 *            user supplied sink log listener
	 * @see SinkErrorListener
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 */
	public void addSinkLogEventListener(SinkLogEventListener listener) {
		checkState();
		logger.getEventSink().addSinkLogEventListener(listener);
	}

	/**
	 * Remove a sink log listener, which is triggered log activities occurs when writing to the event sink.
	 *
	 * @param listener
	 *            user supplied sink log listener
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see SinkErrorListener
	 */
	public void removeSinkLogEventListener(SinkLogEventListener listener) {
		checkState();
		logger.getEventSink().removeSinkLogEventListener(listener);
	}

	/**
	 * Add and register a sink error listener, which is triggered error occurs when writing to the event sink.
	 *
	 * @param listener
	 *            user supplied sink error listener
	 * @see SinkErrorListener
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 */
	public void addSinkErrorListener(SinkErrorListener listener) {
		checkState();
		logger.getEventSink().addSinkErrorListener(listener);
	}

	/**
	 * Remove a sink error listener, which is triggered error occurs when writing to the event sink.
	 *
	 * @param listener
	 *            user supplied sink error listener
	 * @see SinkErrorListener
	 */
	public void removeSinkErrorListener(SinkErrorListener listener) {
		checkState();
		logger.getEventSink().removeSinkErrorListener(listener);
	}

	/**
	 * Add and register a sink filter, which is used to filter out events written to the underlying sink. Sink event
	 * listeners get called every time an event/activity or message is written to the underlying event sink.
	 *
	 * @param filter
	 *            user supplied sink filter
	 * @see SinkEventFilter
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 */
	public void addSinkEventFilter(SinkEventFilter filter) {
		checkState();
		logger.getEventSink().addSinkEventFilter(filter);
	}

	/**
	 * Remove sink filter, which is used to filter out events written to the underlying sink.
	 *
	 * @param filter
	 *            user supplied sink filter
	 * @throws IllegalStateException
	 *             when tracker is not initialized
	 * @see SinkEventFilter
	 */
	public void removeSinkEventFilter(SinkEventFilter filter) {
		checkState();
		logger.getEventSink().removeSinkEventFilter(filter);
	}

	/**
	 * Add and register a dump listener, which is triggered when dump is generated by {@code dump()} call.
	 *
	 * @param lst
	 *            user supplied dump listener
	 * @see DumpListener
	 */
	public static void addDumpListener(DumpListener lst) {
		DUMP_LISTENERS.add(lst);
	}

	/**
	 * Remove a dump listener, which is triggered when dump is generated by {@code dump()} call.
	 *
	 * @param lst
	 *            user supplied dump listener
	 * @see DumpListener
	 */
	public static void removeDumpListener(DumpListener lst) {
		DUMP_LISTENERS.remove(lst);
	}

	/**
	 * Add and register a dump provider. Instances of {@code DumpProvider} provide implementation for underlying classes
	 * that generate application specific dumps. By default supplied dump provider is associated with a default
	 * {@code DumpSink}.
	 *
	 * @param dp
	 *            user supplied dump provider
	 *
	 * @see DumpProvider
	 * @see DumpSink
	 */
	public static void addDumpProvider(DumpProvider dp) {
		addDumpProvider(defaultDumpSink, dp);
	}

	/**
	 * Add and register a dump provider with a user specified {@code DumpSink}. Instances of {@code DumpProvider}
	 * interface provide implementation for underlying classes that generate application specific dumps. This dump
	 * provider will be triggered for the specified {@code DumpSink} only. Instance of {@code DumpSink} can be created
	 * by {@code DumpDestinatonFactory}. By default {@code PropertiesDumpProvider}, {@code MXBeanDumpProvider},
	 * {@code ThreadDumpProvider}, {@code ThreadDeadlockDumpProvider} are auto registered with {@code FileDumpSink}
	 * during initialization of {@code TrackingLogger} class.
	 *
	 * @param df
	 *            user supplied dump destination associated with dump provider
	 * @param dp
	 *            user supplied dump provider
	 *
	 * @see DumpProvider
	 * @see DumpSink
	 * @see DumpSinkFactory
	 * @see PropertiesDumpProvider
	 * @see MXBeanDumpProvider
	 * @see ThreadDumpProvider
	 * @see ThreadDeadlockDumpProvider
	 */
	public static synchronized void addDumpProvider(DumpSink df, DumpProvider dp) {
		// add to dump->dest table second
		List<DumpSink> destList = DUMP_DEST_TABLE.get(dp);
		if (destList == null) {
			destList = new ArrayList<>(10);
			DUMP_PROVIDERS.add(dp);
		}
		boolean exists = destList.contains(df);
		if (!exists) {
			destList.add(df);
		}
		exists = DUMP_DESTINATIONS.contains(df);
		if (!exists) {
			DUMP_DESTINATIONS.add(df);
		}
		DUMP_DEST_TABLE.putIfAbsent(dp, destList);
	}

	/**
	 * Generate dumps backed by registered {@code DumpProvider} instances written to registered {@code DumpSink}
	 * instances. The method first opens all registered dump destinations and then iterates over all dump providers to
	 * obtain dumps of instance {@code DumpCollection}. Registered instances of {@code DumpListener} are triggered for
	 * before, after, error, complete conditions during this call.
	 *
	 * @see DumpListener
	 * @see DumpCollection
	 * @see DumpProvider
	 * @see DumpSink
	 * @see DumpSinkFactory
	 */
	public static synchronized void dumpState() {
		dumpState(null);
	}

	/**
	 * Generate dumps backed by registered {@code DumpProvider} instances written to registered {@code DumpSink}
	 * instances. The method first opens all registered dump destinations and then iterates over all dump providers to
	 * obtain dumps of instance {@code DumpCollection}. Registered instances of {@code DumpListener} are triggered for
	 * before, after, error, complete conditions during this call.
	 *
	 * @param reason
	 *            reason why dump is generated
	 *
	 * @see DumpListener
	 * @see DumpCollection
	 * @see DumpProvider
	 * @see DumpSink
	 * @see DumpSinkFactory
	 */
	public static synchronized void dumpState(Throwable reason) {
		try {
			openDumpSinks();
			for (DumpProvider dumpProvider : DUMP_PROVIDERS) {
				List<DumpSink> dlist = DUMP_DEST_TABLE.get(dumpProvider);
				DumpCollection dump = null;
				Throwable error = reason;
				try {
					dump = dumpProvider.getDump();
					if (dump != null && reason != null) {
						dump.setReason(reason);
					}
					notifyDumpListeners(DumpProvider.DUMP_BEFORE, dumpProvider, dump, dlist, reason);
					if (dump != null) {
						for (DumpSink dest : dlist) {
							dest.write(dump);
						}
					}
				} catch (Throwable ex) {
					ex.initCause(reason);
					error = ex;
				} finally {
					notifyDumpListeners(DumpProvider.DUMP_AFTER, dumpProvider, dump, dlist, error);
				}
			}
		} finally {
			closeDumpSinks();
		}
	}

	/**
	 * Enable/disable VM shutdown hook that will automatically trigger a dump.
	 *
	 * @param flag
	 *            enable/disable VM shutdown hook that triggers a dump
	 */
	public static void dumpOnShutdown(boolean flag) {
		if (flag) {
			Runtime.getRuntime().addShutdownHook(dumpHook);
		} else {
			Runtime.getRuntime().removeShutdownHook(dumpHook);
		}
	}

	/**
	 * Enable/disable VM shutdown hook that will automatically flush all registered trackers.
	 *
	 * @param flag
	 *            enable/disable VM shutdown hook that triggers a flush
	 */
	public static void flushOnShutdown(boolean flag) {
		if (flag) {
			Runtime.getRuntime().addShutdownHook(flushShutdown);
		} else {
			Runtime.getRuntime().removeShutdownHook(flushShutdown);
		}
	}

	/**
	 * Enable/disable {@code UncaughtExceptionHandler} hook that will automatically trigger a dump on uncaught thread
	 * exceptions for all threads.
	 *
	 */
	public static void dumpOnUncaughtException() {
		Thread.setDefaultUncaughtExceptionHandler(dumpHook);
	}

	private static void openDumpSinks() {
		for (DumpSink dest : DUMP_DESTINATIONS) {
			try {
				dest.open();
			} catch (Throwable ex) {
				notifyDumpListeners(DumpProvider.DUMP_ERROR, dest, null, DUMP_DESTINATIONS, ex);
			}
		}
	}

	private static void closeDumpSinks() {
		try {
			notifyDumpListeners(DumpProvider.DUMP_COMPLETE, Thread.currentThread(), null, DUMP_DESTINATIONS);
		} finally {
			for (DumpSink dest : DUMP_DESTINATIONS) {
				try {
					dest.close();
				} catch (Throwable ex) {
					ArrayList<DumpSink> list = new ArrayList<>(1);
					list.add(dest);
					notifyDumpListeners(DumpProvider.DUMP_ERROR, Thread.currentThread(), null, list, ex);
				}
			}
		}
	}

	private static void notifyDumpListeners(int type, Object source, DumpCollection dump, List<DumpSink> dlist) {
		notifyDumpListeners(type, source, dump, dlist, null);
	}

	private static void notifyDumpListeners(int type, Object source, DumpCollection dump, List<DumpSink> dlist,
			Throwable ex) {
		synchronized (DUMP_LISTENERS) {
			for (DumpListener dls : DUMP_LISTENERS) {
				dls.onDumpEvent(new DumpEvent(source, type, dump, dlist, ex));
			}
		}
	}

	@Override
	public TrackingActivity[] getActivityStack() {
		checkState();
		return logger.getActivityStack();
	}

	@Override
	public TrackerConfig getConfiguration() {
		checkState();
		return logger.getConfiguration();
	}

	@Override
	public TrackingActivity getCurrentActivity() {
		checkState();
		return logger.getCurrentActivity();
	}

	@Override
	public TrackingActivity getRootActivity() {
		checkState();
		return logger.getRootActivity();
	}

	@Override
	public EventSink getEventSink() {
		checkState();
		return logger.getEventSink();
	}

	@Override
	public Source getSource() {
		checkState();
		return logger.getSource();
	}

	@Override
	public int getStackSize() {
		checkState();
		return logger.getStackSize();
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		checkState();
		return logger.getStackTrace();
	}

	@Override
	public TrackingSelector getTrackingSelector() {
		checkState();
		return logger.getTrackingSelector();
	}

	@Override
	public boolean isOpen() {
		checkState();
		return logger.isOpen();
	}

	@Override
	public void open() throws IOException {
		checkState();
		logger.open();
	}

	@Override
	public void reopen() throws IOException {
		close();
		open();
	}

	@Override
	public Map<String, Object> getStats() {
		checkState();
		return logger.getStats();
	}

	@Override
	public KeyValueStats getStats(Map<String, Object> stats) {
		checkState();
		return logger.getStats(stats);
	}

	@Override
	public void resetStats() {
		checkState();
		logger.resetStats();
	}

	@Override
	public Tracker setKeepThreadContext(boolean flag) {
		checkState();
		return logger.setKeepThreadContext(flag);
	}

	@Override
	public boolean getKeepThreadContext() {
		checkState();
		return logger.getKeepThreadContext();
	}

	@Override
	public String getId() {
		checkState();
		return logger.getId();
	}

	@Override
	public String newUUID() {
		checkState();
		return logger.newUUID();
	}

	@Override
	public String newUUID(Object obj) {
		checkState();
		return logger.newUUID(obj);
	}

	@Override
	public String toString() {
		return super.toString() + "{logger: " + logger + "}";
	}
}
