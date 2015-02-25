Why TNT4J 
=====================================
TNT4J is about tracking and tracing activities, transactions, behavior and performance via an easy to use API that behaves much like a logging framework.

Why track and trace your apps?
* Trace application behavior, performance to improve diagnostics
* Track end-user behavior to improve usability, customer satisfaction
* Track topology, communications, relationships between entities
* Track messages, binary, text, video, image, voice, etc.
* Track location, mobility, GPS of your applications, users
* Track anything worth tracking in your application
* Know what happend, when, why 

<b>Several key features make TNT4J a prime choice for your java application:</b>

### Log4j Integration
TNT4J integrates with log4j or any other logging framework via a concept of an `EventSink`. TNT4J default integration is with log4j. 

First, all TNT4J messages can be routed via a log4j event sink and therefore can take advantage of the whole log4j framework. 

Second, TNT4J includes `TNT4JAppender` for log4j which allows developers to send log4j messages to TNT4J via this appender and take advantage of TNT4j functionality.

Developers may also enrich log4j messages and pass context to TNT4J using hashtag enrichment scheme. Hashtags are used to decorate log4j messages with important meta data about each log message. This meta data is used to generate TNT4J tracking events (same tags can be passed using log4j `MDC` object):
```java
logger.info("Starting a tnt4j activity #beg=Test, #app=" + Log4JTest.class.getName());
logger.warn("First log message #app=" + Log4JTest.class.getName() + ", #msg='1 Test warning message'");
logger.error("Second log message #app=" + Log4JTest.class.getName() + ", #msg='2 Test error message'", new Exception("test exception"));
logger.info("Ending a tnt4j activity #end=Test, #app=" + Log4JTest.class.getName() + " #%i/order-no=" + orderNo);
```
Above example groups messages between first and last into a related logical collection called `Activity`. Activity is a collection of logically related events/messages. Hashtags `#beg` and `#end` are used to demarcate activity boundaries. This method also supports nested activities. User defined fields can be reported as well using `#[type-qualifier]metric=value` convetion (e.g. `#%i/order-no=62627`). `TNT4JAppender` supports the following type qualifiers:
```
	%i/ -- integer
	%l/ -- long
	%d/ -- double
	%f/ -- float
	%b/ -- boolean
	%n/ -- number
	%s/ -- string
```
Not specifying a qualifier defaults to auto detection of type by `TNT4JAppender`. First `number` qualifier is tested and defaults to `string` if the test fails.

Below is a sample log4j appender configuration:
```
### Default TNT4J Appender configuration
log4j.appender.tnt4j=com.nastel.jkool.tnt4j.logger.TNT4JAppender
log4j.appender.tnt4j.SourceName=com.log4j.Test
log4j.appender.tnt4j.SourceType=APPL
log4j.appender.tnt4j.MetricsOnException=true
log4j.appender.tnt4j.MetricsFrequency=60
log4j.appender.tnt4j.layout=org.apache.log4j.PatternLayout
log4j.appender.tnt4j.layout.ConversionPattern=%d{ABSOLUTE} %-5p [%c{1}] %m%n
```
### Performance
No need to concatenate messages before logging. String concatenation is expensive especialy in loops. Simply log using message patterns as follows and TNT4J will resolve the message only if it actually gets logged:
```java
logger.debug("My message {0}, {1}, {2}", arg1, arg2, arg3);
```
TNT4J enhances logging performance by supporting asynchronous pooled logging, which delegates logging to a dedicated thread pool. Use `BufferedEventSinkFactory` in your `tnt4.properties` configuration to enable this feature. See example below: 
```
...
event.sink.factory: com.nastel.jkool.tnt4j.sink.BufferedEventSinkFactory
event.sink.factory.EventSinkFactory: com.nastel.jkool.tnt4j.logger.Log4JEventSinkFactory
...
```
### Simplicity & Clean Code
No need to check for `isDebugEnabled()` before logging messages. Just register your own `SinkEventFilter` and consolidate all checking into a single listener.
```java	
logger.addSinkEventFilter(new MyLogFilter()); 
...
logger.debug("My message {0}, {1}, {2}", arg1, arg2, arg3);
```

All conditional logging can be consolidated into a single listener object. 

### Flexible Filtering
Filter out not only based on category/severity (as log4j), but also based on performance objectives. Example: log events only if their elapsed time or wait times are greater than a ceratin value. TNT4J allows users to register filters within `tnt4j.properties` without changing application code. Create your own filters which would allow you to filter events out based on user defined criteria and inject filters using `tnt4j.properties`.
See  `tnt4j.properties` and `com.nastel.jkool.tnt4j.filters.EventLevelTimeFilter` for details.
Register filters via declarations in `tnt4j.properties` or in your application by creating your own event filter.
```java
logger.addSinkEventFilter(new MyLogFilter());
```

### Granular conditional logging
Log only what matters. Increase performance of your apps by decreasing the amount of logging your app produces and yet increasing relevance and quality of the output.

```java
logger.isSet(OpLevel.DEBUG);
logger.isSet(OpLevel.DEBUG, "myapp.mykey", myvalue);
```

Checking a global debug level is not granular enough for most applications. Many java apps require granular logging to log only what matters. Consolidate these checks into `SinkEventFilter` implementation and register with the logger instance.
```java
logger.addSinkEventFilter(new MyLogFilter());
```

### Share logging context across apps
Pass logging context across apps programatically or via a shared cache.
```java
logger.set(OpLevel.DEBUG, "myapp.mykey", myvalue);
```

Imagine writing an application that has to pass logging flag to apps downstream, how would you do that?
TNT lets you do that using this method.
	
Check log context by calling:
```java
logger.isSet(OpLevel.DEBUG, "myapp.mykey", myvalue);
```

### State logging
Log application state to improve diagnostics of performance, resource utilization and other tough problems which are difficult  to trace using standard event logging techniques. Simply register your state dump provider (see `DumpProvider` interface) and export state variables specific to you application. Dump providers can be called on VM shutdown or on demand.

Generate application dump on demand.
```java
// register your dump provider
TrackingLogger.addDumpProvider(new MyDumpProvider());
...
TrackingLogger.dumpState();
```

### Measurements & Metrics
TNT4J is not just about logging messages, it is also about measurements and metrics. Metrics such as elpased time, CPU, memory, block/wait times as well as user defined metrics. TNT4J allows you to answer the state of CPU, memory, user defined metrics at the time of the logged event.
Below is an example of creating a snapshot (collection of metrics) and relate it to an activity:
```java
// post processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
TrackingActivity activity = logger.newActivity(OpLevel.INFO, "MyActivity");
...
PropertySnapshot snapshot = logger.newSnapshot("MyCategory", "MySnapshot");
snapshot.add("metric1", myMetric1);
snapshot.add("metric2", myMetric2);
activity.add(snapshot); // add property snapshot associated with this activity
logger.tnt(activity); // report activity and associated snapshots as a single entity
```
Below is an example of reporting a snapshot which are related to a given activity:
```java
// post processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
TrackingActivity activity = logger.newActivity(OpLevel.INFO, "MyActivity");
...
PropertySnapshot snapshot = logger.newSnapshot("MyCategory", "MySnapshot");
snapshot.add("metric1", myMetric1);
snapshot.add("metric2", myMetric2);
activity.tnt(snapshot); // add and report property snapshot associated with this activity
```
Below is an example of reporting standalone snapshot:
```java
// post processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
...
PropertySnapshot snapshot = logger.newSnapshot("MyCategory", "MySnapshot");
snapshot.add("metric1", myMetric1);
snapshot.add("metric2", myMetric2);
logger.tnt(snapshot); // report a property snapshot
```
### Correlation, Topology, Time Synchronization
Relate event message together by grouping or passing context (correlator). Most if not all logging frameworks completely miss the correlation angle. TNT4J allows attachement of correlators when reporting tracking events see `TrackingLogger.tnt(..)` calls for details. The API also allows relating tracking events across application and runtime boundaries using the same paradigm.

`TrackingLogger.tnt(..)` also allows developers to specify the flow of messages using `OpType.SEND` and `OpType.RECEIVE` modifiers. These modifiers let developers specify message flow, direction.
Below is an example of a sender application:
```java
// post processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());

// report sending an order with a specific correlator (order_id)
logger.tnt(OpLevel.INFO, OpType.SEND, "SendOrder", order_id, 
	elasped_time, "Sending order to={0}", destination);
// sending logic
....
....
```
Below is an example of a receiver application:
```java
// post processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
...
// report received an order with a specific correlator (order_id)
logger.tnt(OpLevel.INFO, OpType.RECEIVE, "ReceiveOrder", order_id,
	elasped_time, "Received order from={0}", source);
```

<b>NOTE:</b> TNT4J uses NTP natively to synchronize times across servers to enable cross server event correlation in time. To enable NTP time synchronization define java property `-Dtnt4j.time.server=ntp-server:123`. 

<b>TIP:</b> Developers should use `TimeServer.currentTimeMillis()` instead of `System.currentTimeMillis()` to obtain time adjusted to NTP time. TNT4J also maintains a microsecond resolution clock using `Useconds.CURRENT.get()` which returns the number of microseconds between the current time and midnight, January 1, 1970 UTC (NTP adjusted). TNT4J automatically measures and adjusts clock drift between NTP, `System.currentTimeMillis()` and `System.nanoTime()` clocks to ensure accurate microsecond precision/accuracy timing spanning VMs, devices, servers, geo locations.

### Logging Statistics
TNT4J keeps detailed statistics about logging activities. Each logger instance maintains counts of logged events, messages, errors, overhead in usec and more. Do you know the overhead of your logging framework on your application?

Obtain a map of all available key/value pairs:
```java
Map<String, Long> stats = logger.getStats();
System.out.println("Logger stats: " + stats);
...
System.out.println("Resetting logger stats");
logger.resetStats();
```
Here is an example to obtain metrics for all available loggers:
```java
List<TrackingLogger> loggers = TrackingLogger.getAllTrackers();
for (TrackingLogger lg: loggers) {
	Map<String, Long> stats = lg.getStats();
	...
}
```
TNT4J also keeps track of stack traces for all `TrackingLogger` allocations. Below is an example of how to get stack frames for a specific `TrackingLogger` instance:
```java
List<TrackingLogger> loggers = TrackingLogger.getAllTrackers();
for (TrackingLogger lg: loggers) {
	StackTraceElement[] stack = TrackingLogger.getTrackerStackTrace(lg);
	Utils.printStackTrace("Tracker stack trace", stack, System.out);
	...
}
```

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
* Apache commons lang3 3.3.2 (http://commons.apache.org/proper/commons-lang/)
* Apache commons logging 1.2.17 (http://commons.apache.org/proper/commons-logging/)
* Apache commons net 3.3 (http://commons.apache.org/proper/commons-net/)
* Apache commons codec 1.9 (http://commons.apache.org/proper/commons-codec/)
* Apache Log4J 1.2.17 (http://logging.apache.org/log4j/1.2/)
* Java UUID Generator (JUG) 3.1.3 (http://wiki.fasterxml.com/JugHome/)

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

```java	
	java -javaagent:tnt4j-api.jar -Dlog4j.configuration=file:log4j.properties -Dtnt4j.dump.on.vm.shutdown=true
	-Dtnt4j.dump.provider.default=true -Dtnt4j.formatter.json.newline=true -classpath tnt4j-api-final-all.jar
	com.nastel.jkool.tnt4j.examples.TNT4JTest com.myco.TestApp MYSERVER "Test log message" correlator1 "TestCommand"  TestLocation
```
`-javaagent:tnt4j-api.jar` command line option is required by `ObjectDumpProvider` to calculate object deep and shallow memory sizes. Use this only if your application makes use of ObjectDumpProvider to dump object state.

`-Dtnt4j.dump.provider.default=true` java property allows application state dumps generated automatically upon VM shutdown.

`-Dtnt4j.formatter.json.newline=true` java property directs `JSONFormatter` to append new line when formatting log entries.

See `tnt4j-event.log` and `<vmid>.dump` file for output produced by `com.nastel.jkool.tnt4j.examples.TNT4JTest`.

See `tnt4j.properties` for TNT4J configuration: factories, formatters, listeners, etc. See Wiki for more information.

Known Projects Using TNT4J
===============================================
* Simple Web End-User Monitoring -- TrackingFilter (https://github.com/Nastel/TrackingFilter)
* JMX Streaming Agent - PingJMX (https://github.com/Nastel/PingJMX)
* Cloud Event Streaming Library - JESL (https://github.com/Nastel/JESL)
* Streaming Analytics Service -- jkoolcloud.com (https://www.jkoolcloud.com)
* Application Performance Monitoring -- AutoPilot M6 (http://www.nastel.com/products/autopilot-m6.html)
