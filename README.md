Why TNT4J 
=====================================
Several key features make TNT4J a prime logging choice for java applications:

### Performance
No need to concatenate messages before logging. String concatenation is expensive especialy in loops. Simply log using message patterns as follows and TNT4J will resolve the message only if it actually gets logged:

	logger.debug("My message {0}, {1}, {2}", arg1, arg2, arg3); 

### Simplicity & Clean Code
No need to check for `isDebugEnabled()` before logging messages. Just register your own `SinkEventFilter` and consolidate all checking into a single listener.
	
	logger.addSinkEventFilter(new MyLogFilter()); 
	...
	logger.debug("My message {0}, {1}, {2}", arg1, arg2, arg3); 

All conditional logging can be consolidated into a single listener object. 

### Flexible Filtering
Filter out not only based on category/severity (as log4j), but also based on performance objectives. Example: log events only if their elapsed time or wait times are greater than a ceratin value. TNT4J allows users to register filters within `tnt4j.properties` without changing application code. Create your own filters which would allow you to filter events out based on user defined criteria and inject filters using `tnt4j.properties`.
See  `tnt4j.properties` and `com.nastel.jkool.tnt4j.filters.EventLevelTimeFilter` for details.
Register filters via declarations in `tnt4j.properties` or in your application by creating your own event filter.

	logger.addSinkEventFilter(new MyLogFilter());

### Granular conditional logging
Log only what matters. Increase performance of your apps by decreasing the amount of logging your app produces and yet increasing relevance and quality of the output.

	logger.isSet(OpLevel.DEBUG);
	logger.isSet(OpLevel.DEBUG, "myapp.mykey", myvalue);

Checking a global debug level is not granular enough for most applications. Many java apps require granular logging to log only what matters. Consolidate these checks into `SinkEventFilter` implementation and register with the logger instance.

	logger.addSinkEventFilter(new MyLogFilter());

### Share logging context across apps
Pass logging context across apps programatically or via a shared cache.
	
	logger.set(OpLevel.DEBUG, "myapp.mykey", myvalue);
	
Imagine writing an application that has to pass logging flag to apps downstream, how would you do that?
TNT lets you do that using this method.
	
Check log context by calling:
	
	logger.isSet(OpLevel.DEBUG, "myapp.mykey", myvalue);

### State logging
Log application state to improve diagnostics of performance, resource utilization and other tough problems which are difficult  to trace using standard event logging techniques. Simply register your state dump listener and export state variables specific to you application. State dump listeners can be called on VM shutdown or on demand.

Generate application dump on demand.

	TrackingLogger.dumpState();

### Measurements & Metrics
TNT4J is not just about logging messages, it is also about measurements and metrics. Metrics such as elpased time, CPU, memory, block/wait times as well as user defined metrics. TNT4J allows you to asnwer what was performance at the time of the logged event or what was the value of a user defined metric.

### Correlation & Topology
Relate event message together by grouping or passing context (correlator). Most if not all logging frameworks completely miss the correlation angle. TNT4J allows attachement of correlators when reporting tracking events see `TrackingLogger.tnt(..)` calls for details. The API also allows relating tracking events across application and runtime boundaries using the same paradigm. 

`TrackingLogger.tnt(..)` also allows developers to specify the flow of messages using `OpType.SEND` and `OpType.RECEIVE` modifiers. These modifiers allows developer understand information flow and topology.

### Logging Statistics
TNT4J keeps detailed statistics about logging activities. Each logger instance maintains counts of logged events, messages, errors if any and many more.
	* Call `logger.getStats();` to obtain a map of all available key/value pairs.
	* Call `logger.resetStats();` to reset all counters.

## Quick Examples
Here is a simple example of using TNT4J:

```java
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
try {
   ...
} catch (Exception e) {
   logger.error("Failed to process request={0}", request_id, ex);
}
```
Below is an example of using correlator and a relative class marker to locate caller's method call on the stack -- `$my.package.myclass:0`. This marker will resolve to the caller's method name above all the calls that start with `$my.package.myclass`.

```java
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
try {
   ...
} catch (Exception e) {
   logger.tnt(OpLevel.ERROR, "$my.package.myclass:0", myCorrelator, 
   	"Failed to process request={0}", request_id, ex);
}
```
Consolidate all conditional logging checks into a single listener. Why call `isDebugEnabled()' before each log entry?

```java
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
logger.addSinkEventFilter(new MyEventFilter(logger));
try {
   logger.debug("My debug message {0}, {1}", arg0, arg1); // no need to gate this call
   ...
} catch (Exception e) {
   logger.error("Failed to process request={0}", request_id, ex);
}

class MyEventFilter implements SinkEventFilter {
	TaskLogger logger;

	MyEventFilter(TaskLogger lg) {
		logger = lg;
	}

	@Override
    public boolean filter(EventSink arg0, TrackingEvent arg1) {
	    return logger.isSet(arg1.getSeverity(), "myappl.token");
    }

	@Override
    public boolean filter(EventSink arg0, TrackingActivity arg1) {
	    return logger.isSet(arg1.getSeverity(), "myappl.token");
    }

	@Override
    public boolean filter(EventSink arg0, OpLevel arg1, String arg2, Object... arg3) {
	    return logger.isSet(arg1, "myappl.token");
    }
}
```
Embed TNT4J into your application and realize the benefits in matter if minutes. TNT4J can take advantage of other lower level logging frameworks such as log4j. Default TNT4J binding is based on log4j.

About TNT4J
======================================

Track and Trace 4 Java API, Application logging framework for correlation, diagnostics and tracking of application activities within and across <b>multiple applications, runtimes, servers, geo locations. This API is specifically designed to troubleshoot distributed, concurrent, multi-threaded, composite applications</b> and includes activity correlation, application state dumps, performance and user defined metrics.

Here is short list of TNT4J features:

* Simple programming model to facilitate fast root-cause, log analysis
* Automated timing of application activities and sub-activities (elapsed, idle time, message age)
* Application state dump framework for reporting internal variables, data structures
* Granular conditional logging based on application tokens, patterns, that can be shared accross applications, runtimes
* Share logging context across application, thread, runtime boundaries
* Inter-log correlation of log entries (correlators and tags) between multiple related applications
* Intra-log correlation of related activities and sub-activities between multiple applications and threads
* Event location tags such as GPS, server etc.
* Message flow direction for composite applications that exchange messages (e.g. SOAP, JMS, and SQL etc.)
* User defined properties such as CPU, memory logging, thread statistics per process/thread
* Extensible activity, sink, error listeners for pre, post event processing
* Granular context such as thread id, process id, server, application name

See TNT4J tutorial (http://www.slideshare.net/AlbertMavashev/open-source-application-behavior-tnt4j-tutorial)
See Getting Started (https://github.com/Nastel/TNT4J/wiki/Getting-Started) for quick reference on TNT4J.
Wiki is available at https://github.com/Nastel/TNT4J/wiki

TNT4J Mission
=======================================
* Standard way to track application behavior, activities accross users, apps, servers, devices, threads
* Dramatically reduce time it takes to troubleshoot application behavior using logging paradigm
* Performance metrics and application state to reduce diagnostic time
* Simple programming model for ease of use
* Improve quality and readability of logs to accelerate diagnostics
* Enrich log entries for automated analysis. Manual analysis is just painfully long
* Decrease or eliminate development of custom code required to track behavior and activities
* Independent of the underlying storage, formats

TNT4J Concepts
========================================
TNT4J is fully plug-in and play tracking, tracing and logging framework that consits of the folliwng basic constructs:

* <b>Tracker</b> -- high level object that allows developer to track, trace and log application activities
* <b>Actvity</b> -- a collection of related tracking events (TrackingEvent) and other sub-activities, relation is established via a grouping specified by a developer or set of correlators (across thread, application boundaries). Activities may have a set of uder defined properties which are grouped into property snapshots (PropertySnapshot).
* <b>Tracking Event</b> -- a message with associated start/stop time stamps, severity, user defined message, correlator, tag, location (such as GPS, server etc) and other event properties.
* <b>Property</b> -- key, value pair
* <b>Property snapshot</b> -- a collection of properties with category, name and a timestamp associated with when snapshot is taked. Actvities may have one or more property snapshots.
* <b>Formatter</b> -- an object responsible for formatting underlying TNT4J objects such as Activity, Tracking Event and convert into a formatted string.
* <b>Tracking Selector</b> -- an object associated with a Tracker that allows developers to perform conditional logging based on a given set of severity, key, value combination. Such combinations are stored in token repository.
* <b>Token Repository</b> -- an underlying storage used by tracking selector that actually stores and maintains severity, key, value combinations. Such repository can be backed by a file, cache, memory or any other desired medium. Token repositories can be shared accross application boundaries and therefore conditional logging can span multiple applications, runtimes, geo locations.
* <b>Sink</b> -- sink is a basic destination where obejcts can be written.
* <b>Event Sink</b> -- destination where events, activities and messages are recorded. Such destination can be file, socket, etc.
Sinks are usually associated with formatters which are called to format objects before writting to the sink.
* <b>Dump Sink</b> -- sink where application dumps are recorded.
* <b>Dump</b> -- a property snapshot that deals with application state (name, value pairs). Application can generate user defined dumps to report application specific metrics during diagnostics, on demand or VM shutdown.
* <b>Dump Provider</b> -- user defined implmenetation that actually generated application Dumps.


How to Build TNT4J
=========================================

Requirements
* JDK 1.6+
* ANT (http://ant.apache.org/)

TNT4J depends on the following external packages:
* Apache commons configuration 1.10 (http://commons.apache.org/proper/commons-configuration/)
* Apache commons lang 2.6 (http://commons.apache.org/proper/commons-lang/)
* Apache commons lang3 3.0.1 (http://commons.apache.org/proper/commons-lang/)
* Apache commons logging 1.2.17 (http://commons.apache.org/proper/commons-logging/)
* Apache Log4J 1.2.17 (http://logging.apache.org/log4j/1.2/)

To build TNT4J:
* Download the above libraries and place into the tnt4j-master/lib folder
* Compile and build using ANT: 
	* ant all (run "ant clean" for clean builds)
	* Check ../build/tnt4j for output
	* JavaDoc will be located under ../build/tnt4j/doc
	

Verify TNT4J
===============================================
Run a sample program (`com.nastel.jkool.tnt4j.examples.TNT4JTest`):
	
	CD to ../build/tnt4j
	
	java -javaagent:tnt4j-api.jar -Dlog4j.configuration=file:log4j.properties -Dtnt4j.dump.on.vm.shutdown=true
	-Dtnt4j.dump.provider.default=true -Dtnt4j.formatter.json.newline=true -classpath tnt4j-api-final-all.jar
	com.nastel.jkool.tnt4j.examples.TNT4JTest com.myco.TestApp MYSERVER "Test log message" correlator1 "TestCommand"  TestLocation

`-javaagent:tnt4j-api.jar` command line option is required by `ObjectDumpProvider` to calculate object deep and shallow memory sizes. Use this only if your application makes use of ObjectDumpProvider to dump object state.

`-Dtnt4j.dump.provider.default=true` java property allows application state dumps generated automatically upon VM shutdown.

`-Dtnt4j.formatter.json.newline=true` java property directs `JSONFormatter` to append new line when formatting log entries.

See `tnt4j-event.log` and vmid.dump files for output produced by `com.nastel.jkool.tnt4j.examples.TNT4JTest` program.

See `tnt4j.properties` for TNT4J configuration: factories, formatters, listeners, etc. See Wiki for more information.
