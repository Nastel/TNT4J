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
package com.nastel.jkool.tnt4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.nastel.jkool.tnt4j.config.DefaultConfigFactory;
import com.nastel.jkool.tnt4j.config.TrackerConfig;
import com.nastel.jkool.tnt4j.core.OpCompCode;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.core.Source;
import com.nastel.jkool.tnt4j.dump.DefaultDumpSinkFactory;
import com.nastel.jkool.tnt4j.dump.DumpCollection;
import com.nastel.jkool.tnt4j.dump.DumpEvent;
import com.nastel.jkool.tnt4j.dump.DumpListener;
import com.nastel.jkool.tnt4j.dump.DumpProvider;
import com.nastel.jkool.tnt4j.dump.DumpSink;
import com.nastel.jkool.tnt4j.dump.DumpSinkFactory;
import com.nastel.jkool.tnt4j.dump.MXBeanDumpProvider;
import com.nastel.jkool.tnt4j.dump.PropertiesDumpProvider;
import com.nastel.jkool.tnt4j.dump.ThreadDeadlockDumpProvider;
import com.nastel.jkool.tnt4j.dump.ThreadDumpProvider;
import com.nastel.jkool.tnt4j.sink.DefaultEventSinkFactory;
import com.nastel.jkool.tnt4j.sink.SinkEventFilter;
import com.nastel.jkool.tnt4j.sink.SinkErrorListener;
import com.nastel.jkool.tnt4j.sink.SinkLogEventListener;
import com.nastel.jkool.tnt4j.tracker.DefaultTrackerFactory;
import com.nastel.jkool.tnt4j.tracker.Tracker;
import com.nastel.jkool.tnt4j.tracker.TrackerFactory;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;
import com.nastel.jkool.tnt4j.utils.Utils;


/**
 * <p>
 * <code>TrackingLogger</code> is a helper class with static calls to <code>Tracker</code> logging interface.
 * </p>
 * Source may this helper class instead of obtaining a <code>Tracker</code> logger instance per thread using
 * <code>TrackerFactory</code>. <code>TrackingLogger</code> obtains the <code>Tracker</code> logger instance and stores
 * it in thread local associated for each thread.
 * 
 * <p>
 * A <code>TrackingEvent</code> represents a specific tracking event that application creates for every discrete
 * activity such as JDBC, JMS, SOAP or any other relevant application activity. Source developers must obtain a
 * <code>Tracker</code> instance via <code>TrackerFactory</code>, create instances of <code>TrackingEvent</code> and use
 * report() to report tracking activities.
 * 
 * <p>
 * <code>TrackingActivity</code> <code>start()/stop()</code> method calls used to mark application activity boundaries.
 * Source create instances of <code>TrackingEvent</code> using <code>TrackingLogger.newEvent()</code> method to
 * time individual sub-activities and report them using <code>TrackerLogger.tnt()</code> method call.
 * </p>
 * 
 * <p>
 * Instrumenting typical application logic:
 * </p>
 * 
 * <pre>
 * {@code
 * TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(source);
 * TrackingLogger.register(config.build()); // register and obtain Tracker logger instance
 * TrackingActivity activity = TrackingLogger.newActivity(); // create a new activity instance
 * activity.start(); // start application activity timing
 * TrackingEvent event = TrackingLogger.newEvent(OpLevel.SUCCESS, &quot;SQL customer lookup&quot;, &quot;SQL-SELECT&quot;); // create a tracking event
 * TrackingEvent jms_event = TrackingLogger.newEvent(OpLevel.SUCCESS, OpType.SEND, �correlator�, &quot;Sending Message&quot;, &quot;JmsSend&quot;); // create a tracking event
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
 * 	TrackingLogger.tnt(activity); // report a tracking activity
 * }
 * }
 * </pre>
 * 
 * Source may take advantage of <code>TrackingLogger</code> conditional logging using <code>TrackingLogger.isSet()</code> 
 * based on applications specific tokens. Below is an example of conditional logging:
 * 
 * <pre>
 * {@code
 * TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(source);
 * TrackingLogger.register(config.build()); // register and obtain Tracker logger instance
 * TrackingActivity activity = TrackingLogger.newActivity(); // create a new activity instance
 * activity.start(); // start application activity timing
 * TrackingEvent event = TrackingLogger.newEvent(OpLevel.SUCCESS, &quot;SQL customer lookup&quot;, &quot;SQL-SELECT&quot;); // create a tracking event
 * TrackingEvent jms_event = TrackingLogger.newEvent(OpLevel.SUCCESS, OpType.SEND, �correlator�, &quot;Sending Message&quot;, &quot;JmsSend&quot;); // create a tracking event
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
 *	if (TrackingLogger.isSet(OpLevel.INFO, "com.nastel.appl.corr", �correlator�)) {
 *		activity.tnt(event); // track and trace tracking event within given activity 
 *		activity.tnt(jms_event); // track and trace tracking event within given activity 
 *	}
 * 	TrackingLogger.tnt(activity); // report a tracking activity
 * }
 * }
 * </pre>
 * 
 * <code>TrackingLogger</code> provides a capability to simplify and automate application specific dump handling. An application
 * dump is a collection of application's internal metrics that can be used for problem diagnostics. Source must create
 * an instance of <code>DumpProvider</code> and register it with <code>TrackingLogger</code> optionally associate it with a given
 * dump destination <code>DumpSink</code>(where dump is written to). Dumps can be generated using <code>TrackingLogger.dump()</code>
 * or can be triggered on JVM shutdown using <code>TrackingLogger.dumpOnShutdown(true)</code>. By default, <code>TrackingLogger</code>
 * uses file based <code>DefaultDumpSinkFactory</code> to generate instances of <code>DumpSink</code>.
 * 
 * <pre>
 * {@code
 * // associated dump provider with a default dump destination (file)
 * TrackingLogger.addDumpProvider(new MyDumpProvider()); 
 * TrackingLogger.dumpOnShutdown(true);
 * ...
 * // associated dump provider with a user define dump file
 * TrackingLogger.addDumpProvider(TrackinLogger.getDumpDestinationFactory().getInstance("my-dump.log"), new MyDumpProvider()); 
 * TrackingLogger.dumpOnShutdown(true);
 * ...
 * TrackingLogger.dump(); // MyDumpProvider will be called when dump() is called.
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
public class TrackingLogger {
	private static ThreadLocal<Tracker> loggers = new ThreadLocal<Tracker>();

	private static final String TRACKER_SOURCE = System.getProperty("tnt4j.tracking.logger.source", TrackingLogger.class.getName());
	private static final String TRACKER_CONFIG = System.getProperty("tnt4j.tracking.logger.config");
	
	private static Vector<DumpProvider> DUMP_PROVIDERS = new Vector<DumpProvider>(10, 10);
	private static Vector<DumpSink> DUMP_DESTINATIONS = new Vector<DumpSink>(10, 10);
	private static ConcurrentHashMap<DumpProvider, List<DumpSink>> DUMP_DEST_TABLE = new ConcurrentHashMap<DumpProvider, List<DumpSink>>(
	        49);

	private static Vector<DumpListener> dumpListeners = new Vector<DumpListener>(10, 10);

	private static TrackerFactory factory = null;
	private static DumpSinkFactory dumpFactory = null;
	private static DumpSink defaultDumpDestination = null;
	private static DumpHook dumpHook = new DumpHook();

	static {
		// load configuration and initialize default factories
		TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(TRACKER_SOURCE, TRACKER_CONFIG).build();
		DefaultEventSinkFactory.setDefaultEventSinkFactory(config.getDefaultEvenSinkFactory());
		factory = config.getTrackerFactory();
		dumpFactory = config.getDumpSinkFactory();
		
		String dumpLocation = System.getProperty("tnt4j.dump.location", "./" + Utils.VM_NAME + ".dump");
		defaultDumpDestination = dumpFactory.getInstance(dumpLocation);
		boolean enableDefaultDumpProviders = Boolean.getBoolean("tnt4j.dump.provider.default");
		boolean dumpOnVmHook = Boolean.getBoolean("tnt4j.dump.on.vm.shutdown");
		
		if (enableDefaultDumpProviders) {
			addDumpProvider(defaultDumpDestination, new PropertiesDumpProvider(Utils.VM_NAME));
			addDumpProvider(defaultDumpDestination, new MXBeanDumpProvider(Utils.VM_NAME));
			addDumpProvider(defaultDumpDestination, new ThreadDumpProvider(Utils.VM_NAME));
			addDumpProvider(defaultDumpDestination, new ThreadDeadlockDumpProvider(Utils.VM_NAME));
		}
		if (dumpOnVmHook) dumpOnShutdown(dumpOnVmHook);
	}

    /** Cannot instantiate. */
    private TrackingLogger() {}
    
	/**
	 * Register a user defined tracker factory. Default is <code>DefaultTrackerFactory</code>.
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
	 * Register a user defined dump destination factory used to generate instances of
	 * <code>DumpSink</code>. Default is <code>DefaultDumpSinkFactory</code>.
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
		defaultDumpDestination = (defDest != null ? defDest : defaultDumpDestination);		
	}

	/**
	 * Return currently registered dump destination factory.
	 * Default is <code>DefaultDumpSinkFactory</code>.
	 * 
	 * @return currently registered dump destination factory
	 * @see DumpSinkFactory
	 * @see DefaultDumpSinkFactory
	 */
	public static DumpSinkFactory getDumpDestinationFactory() {
		return dumpFactory;
	}

	/**
	 * Determine of a particular sev/key/value combination is trackable Use this method to determine if tracking is
	 * enabled/disabled for a specific key/value pair. Example, checking if order id "723772" is trackable:
	 * 
	 * <code>TrackingLogger.isSet(OpLevel.INFO, "orderapp.order.id", "723772");</code>
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
	public static boolean isSet(OpLevel sev, Object key, Object value) {
		Tracker lg = loggers.get();
		if (lg != null)
			return lg.getTrackingSelector().isSet(sev, key, value);
		return false;
	}

	/**
	 * Determine of a particular sev/key is trackable Use this method to determine if tracking is enabled/disabled for a
	 * specific severity. This call is equivalent to <code>TrackingLogger.isSet(sev, key, null);</code>
	 * 
	 * @param sev
	 *            severity of to be checked
	 * 
	 * @see OpLevel
	 */
	public static boolean isSet(OpLevel sev, Object key) {
		Tracker lg = loggers.get();
		if (lg != null) {
			return lg.getTrackingSelector().isSet(sev, key);
		}
		return false;
	}

	/**
	 * Determine of a particular sev for the registered application name used in <code>TrackingLogger.register()</code> call.
	 * Use this method to determine if tracking is enabled/disabled for a
	 * specific severity. This call is equivalent to 
	 * <code>TrackingLogger.isSet(sev, TrackingLogger.getTracker().getSource().getName(), null);</code>
	 * 
	 * @param sev
	 *            severity of to be checked
	 * 
	 * @see OpLevel
	 */
	public static boolean isSet(OpLevel sev) {
		Tracker lg = loggers.get();
		if (lg != null) {
			return lg.getTrackingSelector().isSet(sev, lg.getSource().getName());
		}
		return false;
	}

	/**
	 * Set sev/key/value combination for tracking
	 * 
	 * @param sev severity of to be checked
	 * @param key key associated with tracking activity
	 * @param value associated value with a given key
	 * 
	 * @see OpLevel
	 */
	public static void set(OpLevel sev, Object key, Object value){
		Tracker lg = loggers.get();
		if (lg != null) {
			lg.getTrackingSelector().set(sev, key, value);
		}
	}

	/**
	 * Set sev/key combination for tracking. This is the same as calling
	 * <code>set(sev, key, null)</code>, where value is null.
	 * 
	 * @param sev severity of to be checked
	 * @param key key associated with tracking activity
	 * 
	 * @see OpLevel
	 */
	public static void set(OpLevel sev, Object key) {
		Tracker lg = loggers.get();
		if (lg != null) {
			lg.getTrackingSelector().set(sev, key);
		}		
	}
	
	/**
	 * Register an instance of <code>Tracker</code> logger with the current thread. Existing <code>Tracker</code> logger
	 * (if already registered) is closed and released. Only one registered <code>Tracker</code> logger instance is
	 * active per thread.
	 * 
	 * @param config
	 *            tracking configuration to be used to create a tracker instance
	 * @see TrackerConfig
	 */
	public static void register(TrackerConfig config) {
		Tracker lg = loggers.get();
		if (lg != null)
			factory.close(lg);
		loggers.set(factory.getInstance(config));
	}


	/**
	 * Deregister an instance of <code>Tracker</code> logger with the current thread. Existing <code>Tracker</code> logger
	 * (if already registered) is closed and released. Only one registered <code>Tracker</code> logger instance is
	 * active per thread.
	 * 
	 * @see TrackerConfig
	 */
	public static void deregister() {
		Tracker lg = loggers.get();
		if (lg != null)
			factory.close(lg);
		loggers.remove();
	}


	/**
	 * Report a single tracking activity. Call after instance of <code>TrackingActivity</code>
	 * has been completed using <code>TrackingActivity.stop()</code> and <code>TrackingActivity.tnt()</code>
	 * calls.
	 * 
	 * @param activity
	 *            tracking activity to be reported
	 * @see TrackingActivity
	 */
	public static void tnt(TrackingActivity activity) {
		if (activity == null) return;
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		logger.tnt(activity);
	}

	/**
	 * Report a single tracking event as a single activity. Call after instance of <code>TrackingEvent</code>
	 * has been completed using <code>TrackingEvent.stop()</code> call.
	 * @param event
	 *            tracking event to be reported as a single activity
	 * @see TrackingEvent
	 */
	public static void tnt(TrackingEvent event) {
		if (event == null) return;
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		logger.tnt(event);
	}

	/**
	 * Report a single tracking event
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param correlator
	 *            event correlator
	 * @param msg
	 *            event text message
	 * @see TrackingActivity
	 * @see OpLevel
	 */
	public static void tnt(OpLevel severity, String correlator, String msg) {
		tnt(severity, correlator, msg, "NOOP", 0, null);
	}

	/**
	 * Report a single tracking event
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param correlator
	 *            event correlator
	 * @param msg
	 *            event text message
	 * @param opName
	 *            operation name associated with the event message
	 * @see TrackingActivity
	 * @see OpLevel
	 */
	public static void tnt(OpLevel severity, String correlator, String msg, String opName) {
		tnt(severity, correlator, msg, opName, 0, null);
	}

	/**
	 * Report a single tracking event
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param correlator
	 *            event correlator
	 * @param msg
	 *            event text message
	 * @param opName
	 *            operation name associated with the event message
	 * @param ex
	 *            exception associated with the event, or null of none.
	 * @see TrackingActivity
	 * @see OpLevel
	 */
	public static void tnt(OpLevel severity, String correlator, String msg, String opName, Throwable ex) {
		tnt(severity, correlator, msg, opName, 0, ex);
	}

	/**
	 * Report a single tracking event
	 * 
	 * @param severity
	 *            severity level of the reported message
	 * @param correlator
	 *            event correlator
	 * @param msg
	 *            event text message
	 * @param opName
	 *            operation name associated with the event message
	 * @param elapsed
	 *            elapsed time of the event in milliseconds.
	 * @param ex
	 *            exception associated with the event, or null of none.
	 * @see TrackingActivity
	 * @see OpLevel
	 */
	public static void tnt(OpLevel severity, String correlator, String msg, String opName, long elapsed,
	        Throwable ex) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		long endTime = System.currentTimeMillis();
		TrackingActivity activity = logger.newActivity();
		TrackingEvent event = logger.newEvent(severity, OpType.CALL, correlator, msg, opName);
		event.start(endTime - elapsed);
		event.stop(ex != null ? OpCompCode.WARNING : OpCompCode.SUCCESS, 0, ex, endTime);
		activity.start(event.getOperation().getStartTime());
		activity.stop(event.getOperation().getEndTime());
		logger.tnt(activity);
	}

	/**
	 * Create a new application activity via <code>TrackingActivity</code> object instance.
	 * 
	 * @return a new application activity object instance
	 * @see TrackingActivity
	 */
	public static TrackingActivity newActivity() {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		return logger.newActivity();
	}

	/**
	 * Create a new application activity via <code>TrackingActivity</code> object instance.
	 * 
	 * @param signature
	 *            user defined activity signature (should be unique)
	 * @return a new application activity object instance
	 * @see TrackingActivity
	 */
	public static TrackingActivity newActivity(String signature) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		return logger.newActivity();
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported. This constructor will assign a unique
	 * event signature using newUUID() call
	 * 
	 * @param severity
	 *            severity level
	 * @param correlator
	 *            associated with this event (could be unique or passed from a correlated activity)
	 * @param msg
	 *            text message associated with this event
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @see OpLevel
	 * @see TrackingEvent
	 */
	public static TrackingEvent newEvent(OpLevel severity, String correlator, String msg, String opName) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		return logger.newEvent(severity, correlator, msg, opName);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported. This constructor will assign a unique
	 * event signature using newUUID() call
	 * 
	 * @param severity
	 *            severity level
	 * @param msg
	 *            text message associated with this event
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @see TrackingEvent
	 * @see OpType
	 * @see OpLevel
	 */
	public static TrackingEvent newEvent(OpLevel severity, String msg, String opName) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		return logger.newEvent(severity, msg, opName);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported. This constructor will assign a unique
	 * event signature using newUUID() call
	 * 
	 * @param severity
	 *            severity level
	 * @param opType
	 *            operation type
	 * @param msg
	 *            text message associated with this event
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @see TrackingEvent
	 * @see OpType
	 * @see OpLevel
	 */
	public static TrackingEvent newEvent(OpLevel severity, OpType opType, String msg, String opName) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		return logger.newEvent(severity, opType, msg, opName);
	}

	/**
	 * Create a new instance of tracking event that can be timed and reported. This constructor will assign a unique
	 * event signature using newUUID() call
	 * 
	 * @param severity
	 *            severity level
	 * @param opType
	 *            operation type
	 * @param correlator
	 *            associated with this event (could be unique or passed from a correlated activity)
	 * @param msg
	 *            text message associated with this event
	 * @param opName
	 *            operation name associated with this event (tracking event name)
	 * @see TrackingEvent
	 * @see OpType
	 * @see OpLevel
	 */
	public static TrackingEvent newEvent(OpLevel severity, OpType opType, String correlator,
	        String msg, String opName) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		return logger.newEvent(severity, opType, correlator, msg, opName);
	}

	/**
	 * Returns currently registered <code>Tracker</code> logger associated with the current thread. <code>Tracker</code>
	 * logger is associated with the current thread after the register() call. <code>Tracker</code> logger instance is
	 * not thread safe.
	 * 
	 * @return <code>Tracker</code> logger associated with the current thread or null of non available.
	 * @see Tracker
	 */
	public static Tracker getTracker() {
		Tracker logger = loggers.get();
		return logger;
	}

	/**
	 * Add a sink log listener, which is triggered log activities
	 * occurs when writing to the event sink.
	 * 
	 * @param listener user supplied sink log listener
	 * @see SinkErrorListener
	 */
	public static void addSinkLogEventListener(SinkLogEventListener listener) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		logger.getEventSink().addSinkLogEventListener(listener);
	}
	
	/**
	 * Remove a sink log listener, which is triggered log activities
	 * occurs when writing to the event sink.
	 * 
	 * @param listener user supplied sink log listener
	 * @see SinkErrorListener
	 */
	public static void removeSinkLogEventListener(SinkLogEventListener listener) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		logger.getEventSink().removeSinkLogEventListener(listener);
	}
	
	/**
	 * Add and register a sink error listener, which is triggered error
	 * occurs when writing to the event sink.
	 * 
	 * @param listener user supplied sink error listener
	 * @see SinkErrorListener
	 */
	public static void addSinkErrorListener(SinkErrorListener listener) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		logger.getEventSink().addSinkErrorListener(listener);
	}
	
	/**
	 * Remove a sink error listener, which is triggered error
	 * occurs when writing to the event sink.
	 * 
	 * @param listener user supplied sink error listener
	 * @see SinkErrorListener
	 */
	public static void removeSinkErrorListener(SinkErrorListener listener) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		logger.getEventSink().removeSinkErrorListener(listener);
	}
	
	/**
	 * Add and register a sink filter, which is used to filter
	 * out events written to the underlying sink.
	 * 
	 * @param filter user supplied sink filter
	 * @see SinkEventFilter
	 */
	public static void addSinkEventFilter(SinkEventFilter filter) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		logger.getEventSink().addSinkEventFilter(filter);
	}
	
	/**
	 * Remove sink filter, which is used to filter
	 * out events written to the underlying sink.
	 * 
	 * @param filter user supplied sink filter
	 * @see SinkEventFilter
	 */
	public static void removeSinkEventFilter(SinkEventFilter filter) {
		Tracker logger = loggers.get();
		if (logger == null)
			throw new RuntimeException("register() never called for this thread");
		logger.getEventSink().removeSinkEventFilter(filter);
	}
	
	/**
	 * Add and register a dump listener, which is triggered when dump is generated by
	 * <code>dump()</code> call.
	 * 
	 * @param lst user supplied dump listener
	 * @see DumpListener
	 */
	public static void addDumpListener(DumpListener lst) {
		dumpListeners.add(lst);
	}

	/**
	 * Remove a dump listener, which is triggered when dump is generated by
	 * <code>dump()</code> call.
	 * 
	 * @param lst user supplied dump listener
	 * @see DumpListener
	 */
	public static void removeDumpListener(DumpListener lst) {
		dumpListeners.remove(lst);
	}

	/**
	 * Add and register a dump provider. Instances of <code>DumpProvider</code>
	 * provide implementation for underlying classes that generate application 
	 * specific dumps. By default supplied dump provider is associated with a
	 * default <code>DumpSink</code>.
	 * 
	 * @param dp user supplied dump provider
	 * 
	 * @see DumpProvider
	 * @see DumpSink
	 */
	public static void addDumpProvider(DumpProvider dp) {
		addDumpProvider(defaultDumpDestination, dp);
	}

	/**
	 * Add and register a dump provider with a user specified <code>DumpSink</code>.
	 * Instances of <code>DumpProvider</code> interface
	 * provide implementation for underlying classes that generate application 
	 * specific dumps. This dump provider will be triggered for the specified
	 * <code>DumpSink</code> only. Instance of <code>DumpSink</code> can be
	 * created by <code>DumpDestinatonFactory</code>. 
	 * By default <code>PropertiesDumpProvider</code>, 
	 * <code>MXBeanDumpProvider</code>, <code>ThreadDumpProvider</code>,
	 * <code>ThreadDeadlockDumpProvider</code> are auto registered with
	 * <code>FileDumpSink<code> during initialization of
	 * <code>TrackingLogger</code> class.
	 * 
	 * @param df user supplied dump destination associated with dump provider
	 * @param dp user supplied dump provider
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
			destList = new ArrayList<DumpSink>(10);
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
	 * Generate dumps backed by registered <code>DumpProvider</code> instances
	 * written to registered <code>DumpSink</code> instances. The method
	 * first opens all registered dump destinations and then iterates over all
	 * dump providers to obtain dumps of instance <code>DumpCollection</code>. 
	 * Registered instances of <code>DumpListener</code> are triggered for 
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
	 * Generate dumps backed by registered <code>DumpProvider</code> instances
	 * written to registered <code>DumpSink</code> instances. The method
	 * first opens all registered dump destinations and then iterates over all
	 * dump providers to obtain dumps of instance <code>DumpCollection</code>. 
	 * Registered instances of <code>DumpListener</code> are triggered for 
	 * before, after, error, complete conditions during this call.
	 * 
	 * @param reason reason why dump is generated
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
				DumpSink cDest = null;
				Throwable error = reason;
				try {
					dump = dumpProvider.getDump();
					if (dump != null && reason != null) {
						dump.setReason(reason);
					}
					notifyDumpListeners(DumpProvider.DUMP_BEFORE, dumpProvider, dump, dlist, reason);
					if (dump != null) {
						for (DumpSink dest : dlist) {
							cDest = dest;
							cDest.write(dump);
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
	 * Enable or disable VM shutdown hook that will automatically trigger a dump.
	 * 
	 * @param flag enable/disable VM shutdown hook that triggers a dump 
	 */
	public static void dumpOnShutdown(boolean flag) {
		if (flag)
			Runtime.getRuntime().addShutdownHook(dumpHook);
		else Runtime.getRuntime().removeShutdownHook(dumpHook);
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
				try { dest.close(); }
				catch (Throwable ex) {
					ArrayList<DumpSink> list = new ArrayList<DumpSink>(1);
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
		synchronized (dumpListeners) {
			for (DumpListener dls : dumpListeners) {
				dls.onDumpEvent(new DumpEvent(source, type, dump, dlist, ex));
			}
		}
	}
}

class DumpHook extends Thread {
	@Override
	public void run() {
		setName("TrackingLogger/DumpHook");
		TrackingLogger.dumpState(new Exception("VM-Shutdown"));
	}
}
