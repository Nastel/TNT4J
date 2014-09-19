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
package com.nastel.jkool.tnt4j.examples;

import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

import com.nastel.jkool.tnt4j.TrackingLogger;
import com.nastel.jkool.tnt4j.config.DefaultConfigFactory;
import com.nastel.jkool.tnt4j.config.TrackerConfig;
import com.nastel.jkool.tnt4j.core.Activity;
import com.nastel.jkool.tnt4j.core.ActivityListener;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.PropertySnapshot;
import com.nastel.jkool.tnt4j.core.Snapshot;
import com.nastel.jkool.tnt4j.dump.DefaultDumpProvider;
import com.nastel.jkool.tnt4j.dump.Dump;
import com.nastel.jkool.tnt4j.dump.DumpCollection;
import com.nastel.jkool.tnt4j.dump.DumpEvent;
import com.nastel.jkool.tnt4j.dump.DumpListener;
import com.nastel.jkool.tnt4j.dump.DumpProvider;
import com.nastel.jkool.tnt4j.dump.ObjectDumpProvider;
import com.nastel.jkool.tnt4j.selector.TrackingSelector;
import com.nastel.jkool.tnt4j.sink.SinkError;
import com.nastel.jkool.tnt4j.sink.SinkErrorListener;
import com.nastel.jkool.tnt4j.sink.SinkLogEvent;
import com.nastel.jkool.tnt4j.sink.SinkLogEventListener;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;
import com.nastel.jkool.tnt4j.utils.TimeService;
import com.nastel.jkool.tnt4j.utils.Utils;


/**
 * <p> jKool TNT4J Test application that exercises TNT4J API. This application
 * generates a simulated activity with 10 tracking events.
 * Usage: app-name server-name event-msg correlator operation-name location
 * </p>
 * 
 * 
 * @version $Revision: 30 $
 * 
 * @see TrackingActivity
 * @see TrackingEvent
 */
public class TNT4JTest {
	private static final Logger logger = Logger.getLogger(TNT4JTest.class);
	private static final Random rand = new Random(System.currentTimeMillis());
	protected static int activityCount = 0, eventCount = 0;

	private static TrackingLogger tlogger = null;
	/**
	 * Run TNT4J Test application and generate simulated activity
	 * 
	 * @param args Usage: app-name server-name event-msg correlator operation-name
	 */
	public static void main(String[] args) {
		if (args.length < 6) {
			System.out.println("Usage: appl server msg correlator opname location");
			System.exit(-1);
		}
		System.out.println("Current call=" + Utils.getCurrentStackFrame() + ", caller=" + Utils.getCallingStackFrame());
		System.out.println("Time overhead: " + TimeService.getOverheadNanos());
		
		// register with the TNT4J framework
		TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(args[0]);
		config.setSinkLogEventListener(new MySinkLogHandler());
		config.setActivityListener(new MyActivityHandler());
		
		tlogger = TrackingLogger.getInstance(config.build()); 
		tlogger.addSinkErrorListener(new MySinkErrorHandler());

		Snapshot begin = tlogger.newSnapshot("Start", "TNT4JTest");
		begin.add("time-overhead-nanos", TimeService.getOverheadNanos());
		tlogger.tnt(begin);
		
		// optionally register application state dump
		// by default dumps are generated on JVM shutdown
		TrackingLogger.addDumpListener(new DumpNotify());
		TrackingLogger.addDumpProvider(new MyDumpProvider(args[0], "ApplRuntime"));

		// create and start an activity
		TrackingActivity activity = tlogger.newActivity(OpLevel.INFO, "LoggingBenchmark");
		TrackingLogger.addDumpProvider(new ObjectDumpProvider(args[0], activity));
		
		activityCount++;
		activity.start();
		for (int i=0; i < 10; i++) {
			TrackingEvent event = tlogger.newEvent(OpLevel.DEBUG, "runSampleActivity", args[3], "Running sample={0}", i);
			eventCount++;
			event.start(); // start timing current event
			try {
				runSampleActivity(args[2], args[3], args[4], args[5]);
			} finally {
				event.stop();
				activity.tnt(event); // associate current event with the current activity
			}
		}
		activity.stop(); // stop activity timing
		tlogger.tnt(activity);	// log and report activity	
		
		System.out.println("Logging stats: " + tlogger.getStats());
		System.out.println("Registered loggers: size=" + TrackingLogger.getAllTrackers().size());
		System.out.println("Registered loggers: size=" + TrackingLogger.getAllTrackers().size() + ", stack.size=" + TrackingLogger.getAllTrackerStackTrace().size());	
		
		Snapshot end = tlogger.newSnapshot("End", "TNT4JTest");
		end.add("loggers-size", TrackingLogger.getAllTrackers().size());
		end.add("stack-size", TrackingLogger.getAllTrackerStackTrace().size());
		end.setParentId(activity);
		tlogger.tnt(end);

		for (StackTraceElement stack[]: TrackingLogger.getAllTrackerStackTrace()) {
			Utils.printStackTrace("Tracker stack trace", stack, System.out);			
		}
		
		tlogger.close(); //deregister and release all logging resources
		System.out.println("Registered loggers: size=" + TrackingLogger.getAllTrackers().size() + ", list=" + TrackingLogger.getAllTrackers());
	}

	static private TrackingActivity runSampleActivity(String msg, String cid, String opName, String location) {
		TrackingActivity activity = tlogger.newActivity(OpLevel.INFO, "runSampleActivity");
		activityCount++;
		activity.start();
		int runs = rand.nextInt(50);
		int sev = rand.nextInt(OpLevel.values().length);
		for (int i = 0; i < runs; i++) {
			int limit = rand.nextInt(10000000);
			TrackingEvent ev4j = runTNT4JEvent(msg, opName, OpLevel.valueOf(sev), cid, location, limit);
			ev4j.setCorrelator(cid);
			ev4j.setLocation(location);

			TrackingEvent log4j = runLog4JEvent(msg, opName, OpLevel.valueOf(sev), cid, location, limit);
			log4j.setCorrelator(cid);
			log4j.setLocation(location);
		
			if (tlogger.isSet(OpLevel.INFO, "tnt4j.test.location", location)){
				activity.tnt(ev4j);
				activity.tnt(log4j);
			}
		}
		activity.stop();
		tlogger.tnt(activity);	
		return activity;
	}
	
	static private TrackingEvent runTNT4JEvent(String msg, String opName, OpLevel sev, String cid, String location, int limit) {
		TrackingEvent event = tlogger.newEvent(sev, opName, cid, msg);
		eventCount++;
		TrackingSelector selector = tlogger.getTracker().getTrackingSelector();
		try {
			event.setTag(String.valueOf(Utils.getVMName()));
			event.setMessage("{0}, tnt4j.run.count={1}", msg, limit);
			event.start();
			for (int i = 0; i < limit; i++) {
				selector.isSet(OpLevel.INFO, "tnt4j.test.location", location);
			}
		} finally {
			event.stop();
			tlogger.info("runTNT4JEvent: runs=\"{0}\", elapsed.nsec={1}, nsec/call={2}", limit, event.getOperation().getElapsedTimeNano(), (event.getOperation().getElapsedTimeNano()/limit));
		}
		return event;
	}
	
	static private TrackingEvent runLog4JEvent(String msg, String opName, OpLevel sev, String cid, String location, int limit) {
		TrackingEvent event = tlogger.newEvent(sev, opName, cid, msg);
		eventCount++;
		try {
			event.setTag(String.valueOf(Utils.getVMName()));
			event.setMessage("{0}, log4j.run.count={1}", msg, limit);
			event.start();
			for (int i = 0; i < limit; i++) {
				logger.isDebugEnabled();
			}
		} finally {
			event.stop();
			tlogger.info("runLog4JEvent: runs={0}, elapsed.nsec={1}, nsec/call={2}, method={3}, caller={4}",
						limit, event.getOperation().getElapsedTimeNano(),
						(event.getOperation().getElapsedTimeNano()/limit),
						Utils.getCurrentStackFrame(), 
						Utils.getCallingStackFrame());
		}
		return event;
	}
}

class MyActivityHandler implements ActivityListener {
	@Override
    public void started(Activity activity) {
		System.out.println("activity.id=" + activity.getTrackingId() 
				+ ", activity.name=" + activity.getName() 
				+ ", started=" + activity.getStartTime());
	}

	@Override
    public void stopped(Activity activity) {
		// post processing of activity: enrich activity with application metrics
		PropertySnapshot snapshot = new PropertySnapshot("TestApp", "APPL_METRICS");
		snapshot.add("appl.activity.count", TNT4JTest.activityCount);
		snapshot.add("appl.event.count", TNT4JTest.eventCount);
		activity.add(snapshot); // add property snapshot to activity
		System.out.println("activity.id=" + activity.getTrackingId() 
				+ ", activity.name=" + activity.getName() 
				+ ", elasped.usec=" + activity.getElapsedTime() 
				+ ", snap.count=" + activity.getSnapshotCount() 
				+ ", id.count=" + activity.getIdCount()
				);
    }
}

class MySinkErrorHandler implements SinkErrorListener {
	public void sinkError(SinkError event) {
	    System.out.println("onSinkError: " + event);
	    if (event.getCause() != null) event.getCause().printStackTrace();		
	}
}

class MySinkLogHandler implements SinkLogEventListener {
	public void sinkLogEvent(SinkLogEvent event) {
	    // System.out.println("sink.LOG: sev=" + event.getSeverity() + ", source=" + event.getSource() + ", msg=" + event.getSinkObject());
	}
}

class MyDumpProvider extends DefaultDumpProvider {	
	private long startTime = 0;
	public MyDumpProvider(String name, String cat) {
	    super(name, cat);
	    startTime = TimeService.currentTimeMillis();
    }

	@Override
	public DumpCollection getDump() {
		Dump dump = new Dump("runtimeMetrics", this);
		dump.add("appl.start.time", new Date(startTime));
		dump.add("appl.elapsed.ms", (TimeService.currentTimeMillis() - startTime));
		dump.add("appl.activity.count", TNT4JTest.activityCount);
		dump.add("appl.event.count", TNT4JTest.eventCount);
		return dump;		
	}
}

class DumpNotify implements DumpListener {

	@Override
    public void onDumpEvent(DumpEvent event) {
		switch (event.getType()) {
		case DumpProvider.DUMP_BEFORE:
		case DumpProvider.DUMP_AFTER:
		case DumpProvider.DUMP_COMPLETE:
		case DumpProvider.DUMP_ERROR:
		    System.out.println("onDump: " + event);
		    if (event.getCause() != null) event.getCause().printStackTrace();
		}
    }
}
