TNT4J -- Track and Trace API for Java
=======================================

-----------------------

**NOTE:** `tnt4j` version `3.0.0` migrated to Java 11. It also migrated SLF4J from `1.7.x` to `2.x`.

Latest Java 8 compliant `tnt4j` version is `2.16.2`.

-----------------------

TNT4J is about tracking and tracing applications, activities, transactions, behavior and performance via an easy-to-use API that behaves 
much like a logging framework.

Why track and trace your applications?
* Track application behavior, performance to improve diagnostics
* Track end-user behavior to improve usability, customer satisfaction
* Track topology, communications, relationships between entities
* Track messages, binary, text, JSON, XML, video, image, voice.
* Track location, mobility, GPS of your applications, users
* Track anything worth tracking in your application
* Know your application: what, where, when, why 
* Events may have TTL (time-to-live) for sinks that support event expiration
* Event rate limiter (throttle control) based on mps, bps (message/sec, bytes/sec)

**Several key features make TNT4J a prime choice for your java application:**

## Quick Examples
Use Maven dependency:
```xml
    <dependency>
        <groupId>com.jkoolcloud</groupId>
        <artifactId>tnt4j-core</artifactId>
        <version>3.0.0</version>
    </dependency>
```

Here is a simple example of using TNT4J:

```java
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
try {
   ...
} catch (Exception e) {
   logger.error("Failed to process request={0}", request_id, ex);
}
```
Below is an example of using correlator and a relative class marker to locate caller's method call on the stack -- `$my.package.myclass:0`. 
This marker will resolve to the caller's method name above all the calls that start with `$my.package.myclass`.

```java
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
try {
   ...
} catch (Exception e) {
   logger.tnt(OpLevel.ERROR, "$my.package.myclass:0", myCorrelator, 
   	"Failed to process request={0}", request_id, ex);
}
```
Consolidate all conditional logging checks into a single listener. Why call `isDebugEnabled()` before each log entry?

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
	public boolean filter(EventSink sink, TrackingEvent event) {
		return logger.isSet(event.getSeverity(), "myappl.token");
	}

	@Override
	public boolean filter(EventSink sink, TrackingActivity activity) {
		return logger.isSet(activity.getSeverity(), "myappl.token");
	}

	@Override
	public boolean filter(EventSink sink, Snapshot snapshot) {
		return logger.isSet(snapshot.getSeverity(), "myappl.token");
	}

	@Override
	public boolean filter(EventSink sink, long ttl, Source src, OpLevel level, String msg, Object... args) {
		return logger.isSet(level, "myappl.token");
	}
}
```
Embed TNT4J into your application and realize the benefits in a matter if minutes. 
TNT4J can take advantage of other lower level logging frameworks such as `slf4j`, `log4j` and `JUL`.
Default TNT4J binding is based on `JUL`.

### TNT4J Stream Configuration
TNT4J stream configuration is defined in `tnt4j.properties` which is located by specifying `tnt4j.config` java property (example 
`-Dtnt4j.config=./config/tnt4j.properties`).

In case TNT4J configuration is split to separate external configuration files, use `tnt4j.config.path` java property to define root path, 
common for all these files (e.g. `-Dtnt4j.config.path=./config/`). Then `tnt4j.properties` file defined `import`s will be treated as 
relative paths to java property `tnt4j.config.path` defined path (e.g. `import: tnt4j-common.properties` will be like 
`./config/tnt4j-common.properties`).

By default, common configuration files root path is resolved in following sequence:
* defined using system property `tnt4j.config.path`
* parent path of system property `tnt4j.config` defined configuration file
* path defined by `./config` value

This configuration file defines all event sources, target event sink bindings such as file, MQTT, Kafka, HTTPS etc. as well stream specific 
attributes. Here is example of a sample stream configuration:
```properties
; TNT4J Common Definitions
{
	; import common tnt4j logger definitions
	source: common.base
	import: tnt4j-common.properties
}

;Stanza used for sources that start with com.jkoolcloud
{
	source: com.myappl
	like: default.logger
	source.factory.RootSSN: tnt4j-samples

	tracker.default.snapshot.category: DefaultCategory
	event.sink.factory: com.jkoolcloud.tnt4j.sink.impl.BufferedEventSinkFactory
	event.sink.factory.EventSinkFactory: com.jkoolcloud.tnt4j.sink.impl.FileEventSinkFactory
	event.sink.factory.PooledLoggerFactory: com.jkoolcloud.tnt4j.sink.impl.PooledLoggerFactoryImpl
	event.sink.factory.Filter: com.jkoolcloud.tnt4j.filters.EventLevelTimeFilter
	event.sink.factory.Filter.Level: INFO
	event.formatter: com.jkoolcloud.tnt4j.format.SimpleFormatter
	activity.listener: com.jkoolcloud.tnt4j.tracker.DefaultActivityListener
}

;Stanza used for sources that start with org
{
	source: org
	; import settings from com.myappl stanza
	like: com.myappl
}
```
**NOTE:** `import` property allows defining *absolute file path* or file path *relative to common configuration files root path*. If some 
files are out of common configuration files root path scope, use configuration property `import.path`, allowing to define individual path 
for `import` defined file within same stanza, e.g.:
```properties
; TNT4J Common Definitions
{
	; import common tnt4j logger definitions
	source: common.base
	import: tnt4j-common.properties
	import.path: ../../personal-config/
}
```

### Stream over socket
Stream your events over socket using `com.jkoolcloud.tnt4j.sink.impl.SocketEventSinkFactory` event sink factory.
Configure event sink in `tnt4j.properties` as follows:

```properties
...
; Use buffered event sink around Socket event sink factory
event.sink.factory: com.jkoolcloud.tnt4j.sink.impl.BufferedEventSinkFactory
event.sink.factory.PooledLoggerFactory: com.jkoolcloud.tnt4j.sink.impl.PooledLoggerFactoryImpl

event.sink.factory.EventSinkFactory: com.jkoolcloud.tnt4j.sink.impl.SocketEventSinkFactory
event.sink.factory.EventSinkFactory.Host: localhost
event.sink.factory.EventSinkFactory.Port: 6408
...
```

#### Using proxy (SOCKSv5)
Streaming over socket can be combined with use of proxy. Proxy authentication properties `ProxyUser` and `ProxyPass` are optional.
Configure event sink in `tnt4j.properties` to use proxy as follows:

```properties
...
; Use buffered event sink around Socket event sink factory
event.sink.factory: com.jkoolcloud.tnt4j.sink.impl.BufferedEventSinkFactory
event.sink.factory.PooledLoggerFactory: com.jkoolcloud.tnt4j.sink.impl.PooledLoggerFactoryImpl

event.sink.factory.EventSinkFactory: com.jkoolcloud.tnt4j.sink.impl.SocketEventSinkFactory
event.sink.factory.EventSinkFactory.Host: localhost
event.sink.factory.EventSinkFactory.Port: 6408
event.sink.factory.EventSinkFactory.ProxyHost: proxy.host.com
event.sink.factory.EventSinkFactory.ProxyPort: 6666
# Optional proxy properties
event.sink.factory.EventSinkFactory.ProxyUser: proxy-user
event.sink.factory.EventSinkFactory.ProxyPass: proxy-pass
...
```
Proxy authentication credentials can be also passed through JVM system properties `java.net.socks.username` and `java.net.socks.password`, 
e.g.:
```cmd
-Djava.net.socks.username=proxy-user -Djava.net.socks.password=proxy-pass
```

Also see [Java SE documentation](https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html#proxies) for additional 
proxy configuration details using JVM system properties.

### Stream over Kafka
Use Maven dependency:
```xml
    <dependency>
        <groupId>com.jkoolcloud</groupId>
        <artifactId>tnt4j-kafka-sink</artifactId>
        <version>3.0.0</version>
    </dependency>
```

Stream your events over Apache Kafka using `com.jkoolcloud.tnt4j.sink.impl.kafka.KafkaEventSinkFactory` event sink factory.
Configure event sink in `tnt4j.properties` as follows:

```properties
...
; Use buffered event sink around Kafka event sink factory
event.sink.factory: com.jkoolcloud.tnt4j.sink.impl.BufferedEventSinkFactory
event.sink.factory.PooledLoggerFactory: com.jkoolcloud.tnt4j.sink.impl.PooledLoggerFactoryImpl

event.sink.factory.EventSinkFactory: com.jkoolcloud.tnt4j.sink.impl.kafka.KafkaEventSinkFactory
event.sink.factory.EventSinkFactory.propFile: <your path>/tnt4j-kafka.properties
event.sink.factory.EventSinkFactory.topic: tnt4j-topic
event.formatter: com.jkoolcloud.tnt4j.format.JSONFormatter
...
```
`tnt4j-topic` refers to a Kafka topic which must be created and available. Below is a sample `tnt4j-kafka.properties` configuration:
```properties
bootstrap.servers=localhost:9092
acks=all
retries=0
linger.ms=1
buffer.memory=33554432
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

### Stream over MQTT
Use Maven dependency:
```xml
    <dependency>
        <groupId>com.jkoolcloud</groupId>
        <artifactId>tnt4j-mqtt-sink</artifactId>
        <version>3.0.0</version>
    </dependency>
```

Stream your events over MQTT using `com.jkoolcloud.tnt4j.sink.impl.mqtt.MqttEventSinkFactory` event sink factory.
Configure event sink in `tnt4j.properties` as follows:
```properties
...
; Use buffered event sink around MQTT event sink factory
event.sink.factory: com.jkoolcloud.tnt4j.sink.impl.BufferedEventSinkFactory
event.sink.factory.PooledLoggerFactory: com.jkoolcloud.tnt4j.sink.impl.PooledLoggerFactoryImpl

event.sink.factory: com.jkoolcloud.tnt4j.sink.impl.mqtt.MqttEventSinkFactory
event.sink.factory.mqtt-server-url: tcp://localhost:1883
event.sink.factory.mqtt-topic: tnt4jStream
event.sink.factory.mqtt-user: mqtt-user
event.sink.factory.mqtt-pwd: mqtt-pwd
...
```

### SLF4J Event Sink Integration
TNT4J provides default logging integration over `SLF4J` API.
TNT4J provides SLF4J event sink implementation via `com.jkoolcloud.tnt4j.sink.impl.slf4j.SLF4JEventSinkFactory` event sink factory.
Other logging frameworks can be supported by implementing `EventSinkFactory` & `EventSink` interfaces.

All TNT4J messages can be routed via a SLF4J event sink and therefore can take advantage of the underlying logging frameworks supported by 
SLF4J.

Developers may also enrich event messages and pass context to TNT4J using hashtag enrichment scheme.
Hashtags are used to decorate event messages with important metadata about each log message. 
This metadata is used to generate TNT4J tracking events:
```java
logger.info("Starting a tnt4j activity #beg=Test, #app=" + Log4JTest.class.getName());
logger.warn("First log message #app=" + Log4JTest.class.getName() + ", #msg='1 Test warning message'");
logger.error("Second log message #app=" + Log4JTest.class.getName() + ", #msg='2 Test error message'", new Exception("test exception"));
logger.info("Ending a tnt4j activity #end=Test, #app=" + Log4JTest.class.getName() + " #%i/order-no=" + orderNo  + " #%d:currency/amount=" + amount);
```
Above example groups messages between first and last into a related logical collection called `Activity`. Activity is a collection of 
logically related events/messages. Hashtags `#beg`, `#end` are used to demarcate activity boundaries. This method also supports nested 
activities.

User defined fields can be reported using `#[data-type][:value-type]/your-metric-name=your-value` convention (e.g. `#%i/order-no=62627` or 
`#%d:currency/amount=50.45`).

`TNT4JAppender` supports the following optional `data-type` qualifiers:
```
	%i/ -- integer
	%l/ -- long
	%d/ -- double
	%f/ -- float
	%b/ -- boolean
	%n/ -- number
	%s/ -- string
```
All `value-type` qualifiers are defined in `com.jkoolcloud.tnt4j.core.ValueTypes`. Examples:
```
	currency 	-- generic currency
	flag 		-- boolean flag
	age 		-- age in time units
	guid 		-- globally unique identifier
	guage		-- numeric gauge
	counter		-- numeric counter
	percent		-- percent
	timestamp	-- timestamp
	addr 		-- generic address
```
Not specifying a qualifier defaults to auto-detection of type by `TNT4JAppender`. 
First `number` qualifier is tested and defaults to `string` if the test fails (e.g. `#order-no=62627`).

### JUL (java.util.logging) Event Sink integration

Use JVM system property `java.util.logging.config.file` to define JUL configuration file (e.g. `logging.properties`) reference:
```cmd
    -Djava.util.logging.config.file=./config/logging.properties
```

### Performance
No need to concatenate messages before logging. String concatenation is expensive especially in loops. Simply log using message patterns as 
follows and TNT4J will resolve the message only if it actually gets logged:
```java
logger.debug("My message {0}, {1}, {2}", arg0, arg1, arg3);
```
TNT4J enhances logging performance by supporting asynchronous pooled logging, which delegates logging to a dedicated thread pool. Use 
`BufferedEventSinkFactory` in your `tnt4.properties` configuration to enable this feature. See example below: 
```properties
...
event.sink.factory: com.jkoolcloud.tnt4j.sink.impl.BufferedEventSinkFactory
event.sink.factory.PooledLoggerFactory: com.jkoolcloud.tnt4j.sink.impl.PooledLoggerFactoryImpl
event.sink.factory.EventSinkFactory: com.jkoolcloud.tnt4j.sink.impl.slf4j.SLF4JEventSinkFactory
...
```
### Simplicity & Clean Code
No need to check for `isDebugEnabled()` before logging messages. Just register your own `SinkEventFilter` and consolidate all checking into 
a single listener.
```java	
logger.addSinkEventFilter(new MyLogFilter()); 
...
logger.debug("My message {0}, {1}, {2}", arg0, arg1, arg2);
```

All conditional logging can be consolidated into a single listener object.

### Flexible Filtering
Filter out not only based on category/severity (as slf4j), but also based on performance objectives. Example: log events only if their 
elapsed time or wait times are greater than a certain value. TNT4J allows users to register filters within `tnt4j.properties` without 
changing application code. Create your own filters which would allow you to filter events out based on user defined criteria and inject 
filters using `tnt4j.properties`. See  `tnt4j.properties` and `com.jkoolcloud.tnt4j.filters.EventLevelTimeFilter` for details.
Register filters via declarations in `tnt4j.properties` or in your application by creating your own event filter.
```java
logger.addSinkEventFilter(new ThresholdEventFilter(OpLevel.WARNING));
```
Below is an example of an event sink filter `ThresholdEventFilter` which must implement `SinkEventFilter` interface.
```java
public class ThresholdEventFilter implements SinkEventFilter {
	OpLevel threshold = OpLevel.INFO;

	public ThresholdEventFilter() {
	}

	public ThresholdEventFilter(OpLevel level) {
		this.threshold = level;
	}

	@Override
	public boolean filter(EventSink sink, TrackingEvent event) {
		return (event.getSeverity().ordinal() >= threshold.ordinal()) && sink.isSet(event.getSeverity());
	}

	@Override
	public boolean filter(EventSink sink, TrackingActivity activity) {
		return (activity.getSeverity().ordinal() >= threshold.ordinal()) && sink.isSet(activity.getSeverity());
	}

	@Override
	public boolean filter(EventSink sink, Snapshot snapshot) {
		return (snapshot.getSeverity().ordinal() >= threshold.ordinal()) && sink.isSet(snapshot.getSeverity());
	}

	@Override
	public boolean filter(EventSink sink, long ttl, Source source, OpLevel level, String msg, Object... args) {
		return (level.ordinal() >= threshold.ordinal()) && sink.isSet(level);
	}
}
```

### Granular conditional logging
Log only what matters. Increase performance of your applications by decreasing the amount of logging your application produces while 
increasing the relevance and quality of the output.

```java
if (logger.isSet(OpLevel.DEBUG)) {
	logger.debug("My message {0}, {1}, {2}", arg0, arg1, arg2);
}
if (logger.isSet(OpLevel.DEBUG, "myapp.mykey", myvalue)) {
	logger.debug("My message {0}, {1}, my.value={2}", arg0, arg1, myvalue);
}
```

Checking a global debug flag is not granular enough for most applications. Many java apps require granular tracking to log only what 
matters based on specific context. Consolidate conditional checks (`logger.isSet()`) into `SinkEventFilter` implementation and register 
with the tracker instance.
```java
logger.addSinkEventFilter(new MyLogFilter());
```

### Share logging context across apps
Pass logging context across threads or applications.
```java
logger.set(OpLevel.DEBUG, "myapp.mykey", myvalue);
```

Imagine writing an application that has to pass tracking context/flag to applications downstream, how would you do that?
TNT4J lets you set and get conditional variables within and across application boundaries.

Set and check tracking context as follows (track all requests matching a specific zip code only):
```java
// set level, key & value pair
logger.set(OpLevel.DEBUG, "zip-code", trackZipCode);
..
..
// check for sev, key & value pair match
String zipCode = request.getZipCode();  // example request containing a zip code
if (logger.isSet(OpLevel.DEBUG, "zip-code", zipCode)) {
	// your conditional logic here
}
```

### State logging
Log application state to improve diagnostics of performance, resource utilization and other tough problems which are difficult  to trace 
using standard event logging techniques. Simply register your state dump provider (see `DumpProvider` interface) and export state variables 
specific to you application. Dump providers can be called on VM shutdown or on demand.

Generate application dump on demand.
```java
// register your dump provider
TrackingLogger.addDumpProvider(new MyDumpProvider());
...
TrackingLogger.dumpState();
```

### Measurements & Metrics
TNT4J is not just about logging messages, it is also about measurements and metrics such as response time, CPU, memory, block/wait times as 
well as user defined metrics. TNT4J lets you report metrics at the time of the logged event. Below is an example of creating a snapshot 
(collection of metrics) and attaching it to an activity:
```java
// post-processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
TrackingActivity activity = logger.newActivity(OpLevel.INFO, "Order");
...
PropertySnapshot snapshot = logger.newSnapshot("Order", "Payment");
snapshot.add("order-no", orderNo, ValueTypes.VALUE_TYPE_ID);
snapshot.add("order-amount", orderAmount, ValueTypes.VALUE_TYPE_CURRENCY);
activity.add(snapshot); // add property snapshot associated with this activity
...
logger.tnt(activity); // report activity and associated snapshots as a single entity
```
A `Snapshot` is a collection of name, value pairs called `Property`. Each `Property` can be further qualified with a value type defined in 
`ValueTypes` class.

Below is an example of reporting a snapshot:
```java
// post-processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
TrackingActivity activity = logger.newActivity(OpLevel.INFO, "Order");
...
PropertySnapshot snapshot = logger.newSnapshot("Order", "Payment");
snapshot.add("order-no", orderNo, ValueTypes.VALUE_TYPE_ID);
snapshot.add("order-amount", orderAmount, ValueTypes.VALUE_TYPE_CURRENCY);
activity.tnt(snapshot); // add and report property snapshot associated with this activity
```
Below is an example of reporting standalone snapshot:
```java
// post-processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
...
PropertySnapshot snapshot = logger.newSnapshot("Order", "Payment");
snapshot.add("order-no", orderNo, ValueTypes.VALUE_TYPE_ID);
snapshot.add("order-amount", orderAmount, ValueTypes.VALUE_TYPE_CURRENCY);
logger.tnt(snapshot); // report a property snapshot
```
### Correlation, Topology, Time Synchronization
Developers can relate events by grouping them into activities (activity is a collection of related events and sub-activities) or passing 
context -- correlator(s). Activity grouping and correlators create connectivity between events across thread, applications, server, runtime, 
location boundaries. TNT4J allows attachment of correlators when reporting tracking events: see `TrackingLogger.tnt(..)` calls for details. 
The API also allows relating tracking events across application and runtime boundaries using the same mechanism.

`TrackingLogger.tnt(..)` also allows developers to specify the flow of messages using `OpType.SEND` and `OpType.RECEIVE` modifiers. These 
modifiers let developers specify message flow & direction. This is especially useful for applications that pass information via network, 
middleware, messaging or any other communication mechanism. Tracking events with such modifiers specify graph/topology information required 
for root cause analysis as well as visualization of message flow.

Below is an example of a sender application:
```java
// post-processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());

// report sending an order with a specific correlator (order_id)
logger.tnt(OpLevel.INFO, OpType.SEND, "SendOrder", order_id, 
	elapsed_time, "Sending order to={0}", destination);
// sending logic
....
....
```
Here is an example of a receiver application:
```java
// post-processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
...
// report received an order with a specific correlator (order_id)
logger.tnt(OpLevel.INFO, OpType.RECEIVE, "ReceiveOrder", order_id,
	elapsed_time, "Received order from={0}", source);
```

**NOTE:** TNT4J uses NTP natively to synchronize times across servers to enable cross server event correlation in time. To enable NTP time 
synchronization define java property `-Dtnt4j.time.server=ntp-server:123`.

**TIP:** Developers should use `TimeServer.currentTimeMillis()` instead of `System.currentTimeMillis()` to obtain time adjusted to NTP time. 
TNT4J also maintains a microsecond resolution clock using `Useconds.CURRENT.get()` which returns the number of microseconds between the 
current time and midnight, January 1, 1970, UTC (NTP adjusted). TNT4J automatically measures and adjusts clock drift between NTP, 
`System.currentTimeMillis()` and `System.nanoTime()` clocks to ensure accurate microsecond precision/accuracy timing spanning VMs, devices, 
servers, geo-locations.

### Tracking Associations
TNT4J allows developers to track associations between sources. Source is a logical definition of an entity such as application, server, 
network, geo-location. 
Here is an example of a source: `APP=WebAppl#SERVER=MYSERVER#DATACENTER=DC1#GEOADDR=New York`, which means application `WebAppl` deployed on 
server `MYSERVER`, located on data center `DC1`, located in `New York`. Say you want track an association between 2 applications that 
exchange data, where one application sends data to another:
```java
// post-processing of activity: enrich activity with application metrics
TrackingLogger logger = TrackingLogger.getInstance(this.getClass());
...
TrackingEvent event = logger.newEvent(...);
// create an association between 2 applications
event.relate2(DefaultSourceFactory.getInstance().newFromFQN("APP=Orders#SERVER=HOST1#DATACENTER=DC1#GEOADDR=New York, NY"),
	DefaultSourceFactory.getInstance().newFromFQN("APP=Billing#SERVER=HOST2#DATACENTER=DC1#GEOADDR=London, UK",
	OpType.SEND);
logger.tnt(event);
```
The snippet above creates the following association: `Orders->SEND->Billing`, which expresses flow between `Orders` and `Billing` 
applications, where `Orders` and `Billing` are nodes and `SEND` is an edge.

### Logging Statistics
TNT4J keeps detailed statistics about logging activities. Each logger instance maintains counts of logged events, messages, errors, overhead 
in usec and more. Do you know the overhead of your logging framework on your application?

Obtain a map of all available key/value pairs:
```java
Map<String, Long> stats = logger.getStats();
System.out.println("Logger stats: " + stats);
...
System.out.println("Resetting logger stats");
logger.resetStats();
```
Obtain metrics for all available trackers:
```java
List<TrackingLogger> loggers = TrackingLogger.getAllTrackers();
for (TrackingLogger lg: loggers) {
	Map<String, Long> stats = lg.getStats();
	printStats(stats); // your call to print out tracker statistics
	...
}
```
TNT4J keeps track of stack traces for all `TrackingLogger` allocations. Below is an example of how to get stack frames for a set of 
`TrackingLogger` instances:
```java
// obtain all available tracker instances
List<TrackingLogger> loggers = TrackingLogger.getAllTrackers();
for (TrackingLogger lg: loggers) {
	StackTraceElement[] stack = TrackingLogger.getTrackerStackTrace(lg);
	Utils.printStackTrace("Tracker stack trace", stack, System.out);
	...
}
```

About TNT4J
======================================
Track and Trace 4 Java API, Application logging framework for correlation, diagnostics and tracking of application activities within and 
across **multiple applications, runtime, servers, geo-locations. This API is specifically designed to troubleshoot distributed, concurrent, 
multi-threaded, composite applications** and includes activity correlation, application state dumps, performance and user defined metrics.

Here is short list of TNT4J features:

* Simple programming model to facilitate fast root-cause, log analysis
* Automated timing of application activities and sub-activities (elapsed, idle time, message age)
* Application state dump framework for reporting internal variables, data structures
* Granular conditional logging based on application tokens, patterns, that can be shared across applications, runtime
* Share logging context across application, thread, runtime boundaries
* Inter-log correlation of log entries (correlators and tags) between multiple related applications
* Intra-log correlation of related activities and sub-activities between multiple applications and threads
* Event location tags such as GPS, server etc.
* Message flow direction for composite applications that exchange messages (e.g. SOAP, JMS, and SQL etc.)
* User defined properties such as CPU, memory logging, thread statistics per process/thread
* Extensible activity, sink, error listeners for pre, post event processing
* Granular context such as thread id, process id, server, application name

See [Getting Started](https://github.com/Nastel/TNT4J/wiki/Getting-Started) for quick reference on TNT4J.
Also [Wiki](https://github.com/Nastel/TNT4J/wiki) is available.

TNT4J Mission
=======================================
* Easy way to track application behavior, activities across users, apps, servers, devices, threads
* Dramatically reduce time it takes to troubleshoot application behavior using logging paradigm
* Performance metrics and application state to reduce diagnostic time
* Simple programming model for ease of use
* Improve quality and readability of logs to accelerate diagnostics
* Enrich log entries for automated analysis. Manual analysis is just painfully long
* Decrease or eliminate development of custom code required to track behavior and activities
* Independent of the underlying storage, formats

TNT4J Concepts
========================================
TNT4J is fully plug-in and play tracking, tracing and logging framework that consists of the following basic constructs:

* **Tracker** -- high level object that allows developer to track, trace and log application activities
* **Activity** -- a collection of related tracking events (TrackingEvent) and other sub-activities, relation is established via a grouping 
specified by a developer or set of correlators (across thread, application boundaries). Activities may have a set of under defined 
properties which are grouped into property snapshots (PropertySnapshot).
* **Tracking Event** -- a message with associated start/stop time stamps, severity, user defined message, correlator, tag, location (such as 
GPS, server etc.) and other event properties.
* **Property** -- a single user defined key, value, type pair for reporting custom metric or attribute. Properties can be packed into event, 
activity or snapshot.
* **Property snapshot** -- a collection of properties with category, name and a time stamp associated with when snapshot is taken. 
Activities may have one or more property snapshots.
* **Formatter** -- an object responsible for formatting underlying TNT4J objects such as Activity, Tracking Event and convert into a 
formatted string. It can be defined globally for a `source` using `event.formatter` property or for particular `sink` using 
`event.sink.factory.Formatter` property.
* **Tracking Selector** -- an object associated with a Tracker that allows developers to perform conditional logging based on a given set of 
severity, key, value combination. Such combinations are stored in token repository.
* **Token Repository** -- an underlying storage used by tracking selector that actually stores and maintains severity, key, value 
combinations. Such repository can be backed by a file, cache, memory or any other desired medium. Token repositories can be shared across 
application boundaries and therefore conditional logging can span multiple applications, runtimes, geo-locations.
* **Sink** -- sink is a basic destination where objects can be written (e.g. file, socket, http, etc.)
* **Event Sink** -- destination where events, activities and messages are recorded. Such destination can be a file, socket. Sinks are 
associated with formatters which are called to format objects before writing to the sink.
* **Dump Sink** -- sink where application dumps are recorded.
* **Dump** -- a property snapshot that deals with application state (name, value pairs). Application can generate user defined dumps to 
report application specific metrics during diagnostics, on demand or VM shutdown.
* **Dump Provider** -- user defined implementation that actually generates application Dumps.

How to Build TNT4J
=========================================

Requirements
* JDK 1.8+

TNT4J depends on the following external packages:
* [Apache commons configuration2](http://commons.apache.org/proper/commons-configuration2/)
* [Apache commons beanutils](http://commons.apache.org/proper/commons-beanutils/)
* [Apache commons lang3](http://commons.apache.org/proper/commons-lang/)
* [Apache commons net](http://commons.apache.org/proper/commons-net/)
* [Apache commons text](http://commons.apache.org/proper/commons-text/)
* [Apache commons codec](http://commons.apache.org/proper/commons-codec/)
* [Google Guava Libraries](https://github.com/google/guava/)
* [Java UUID Generator (JUG)](https://github.com/cowtowncoder/java-uuid-generator/)
* [SLF4J](http://www.slf4j.org/)

MQTT sink [module](tnt4j-mqtt-sink) additionally depends on:
* [Eclipse Paho MQTTv3](http://www.eclipse.org/paho/)

Kafka sink [module](tnt4j-kafka-sink) additionally depends on:
* [Kafka Clients](https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients/)

To build TNT4J:
* Please use JCenter or Maven and these dependencies will be downloaded automatically.
    * To build the project, run Maven goals `clean package`
    * To build the project and install to local repo, run Maven goals `clean install`
    * To make distributable release assemblies use one of profiles: `pack-bin` or `pack-all`:
        * containing only binary (including `test` package) distribution: run `mvn -P pack-bin`
        * containing binary (including `test` package), `source` and `javadoc` distribution: run `mvn -P pack-all`
    * To make Maven required `source` and `javadoc` packages, use profile `pack-maven`
    * To make Maven central compliant release having `source`, `javadoc` and all signed packages, use `maven-release` profile
* You will need to bind TNT4J to configuration properties file (usually `tnt4j.properties`) via the Java system property `tnt4j.config` (as
  `java` CLI argument `-Dtnt4j.config`). This property file is located here in GitHub under the `/config` directory. If using JCenter or
  Maven, it can be found in the zip assembly along with the source code and javadoc.

Known Projects Using TNT4J
===============================================
* jKool Event Streaming Library - [JESL](https://github.com/Nastel/JESL/)
* [TNT4J/Servlet-Filter](https://github.com/Nastel/tnt4j-servlet-filter/)
* [TNT4J/Stream-GC](https://github.com/Nastel/tnt4j-stream-gc/)
* [TNT4J/Stream-JMX](https://github.com/Nastel/tnt4j-stream-jmx/)
* [TNT4J/Syslogd](https://github.com/Nastel/tnt4j-syslogd/)
* [TNT4J/Log4j](https://github.com/Nastel/tnt4j-log4j/)
* [TNT4J/Logback](https://github.com/Nastel/tnt4j-logback/)
* [TNT4J/Spark](https://github.com/Nastel/tnt4j-spark/)
* [TNT4J/Examples](https://github.com/Nastel/tnt4j-examples/)
* Log & Metric Analytics Service - [jkoolcloud.com](https://www.jkoolcloud.com/)
* Application Performance Monitoring - [AutoPilot M6](http://www.nastel.com/products/autopilot-m6.html)
