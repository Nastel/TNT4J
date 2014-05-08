TNT4J
=====

Track and Trace 4 Java API, Application logging framework for correlation, diagnostics and tracking of application activities. This API is specifically designed to troubleshoot concurrent, mutithreaded, multi-user applications and includes
activity correlation, application state dumps, performance and user defined metrics.

Here is short list of TNT4J features:

* Simple programming model to facilitate fast root-cause, log analysis
* Automated timing of application activities and sub-activities (elapsed, idle time, message age)
* Application state dump framework for reporting internal variables, data structures
* Granular conditional logging based on application tokens, patterns
* Inter-log correlation of log entries (correlators and tags) between multiple related applications
* Intra-log correlation of related activities and sub-activities between multiple applications and threads
* Event location tags such as GPS, server etc.
* Message flow direction for composite applications that exchange messages (e.g. SOAP, JMS, and SQL etc.)
* User defined properties such as CPU, memory logging, thread statistics per process/thread
* Granular context such as thread id, process id, server, application name


TNT4J depends on the following external packages:
* Apache commons configuration 1.10
* Apache commons lang 2.6
* Apache commons lang3 3.0.1
* Apache commons logging 1.2.17
* Apache Log4J 1.2.17

These libraries can be obtained from Apache.org.
