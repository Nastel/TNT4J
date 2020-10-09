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

package com.jkoolcloud.tnt4j.sink.impl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import com.jkoolcloud.tnt4j.core.KeyValueStats;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSink;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * Broadcasting event sink that allows writes to multiple event sinks at once.
 * 
 * @author albert
 * @see AbstractEventSink
 */
public class BroadcastingEventSink extends AbstractEventSink {

	public static final String KEY_SINK_SIZE = "broadcast-sink-count";
	public static final String KEY_OPEN_COUNT = "broadcast-open-sinks";

	final Collection<EventSink> eventSinks = Collections.synchronizedList(new ArrayList<>(3));
	OpenSinksPolicy openSinksPolicy = OpenSinksPolicy.ANY;

	/**
	 * Create broadcasting event sink factory
	 * 
	 * @param brdFactory
	 *            broadcasting event sink factory instance
	 * @param name
	 *            sink name
	 * @see BroadcastingEventSinkFactory
	 */
	public BroadcastingEventSink(BroadcastingEventSinkFactory brdFactory, String name) {
		super(name);
		for (EventSinkFactory fc : brdFactory.getEventSinkFactories()) {
			eventSinks.add(fc.getEventSink(name));
		}
	}

	/**
	 * Create broadcasting event sink factory
	 * 
	 * @param brdFactory
	 *            broadcasting event sink factory instance
	 * @param name
	 *            sink name
	 * @param props
	 *            event sink properties
	 * @see BroadcastingEventSinkFactory
	 */
	public BroadcastingEventSink(BroadcastingEventSinkFactory brdFactory, String name, Properties props) {
		super(name);
		for (EventSinkFactory fc : brdFactory.getEventSinkFactories()) {
			eventSinks.add(fc.getEventSink(name, props));
		}
	}

	/**
	 * Create broadcasting event sink factory
	 * 
	 * @param brdFactory
	 *            broadcasting event sink factory instance
	 * @param name
	 *            sink name
	 * @param props
	 *            event sink properties
	 * @param frmt
	 *            event formatter instance
	 * @see BroadcastingEventSinkFactory
	 */
	public BroadcastingEventSink(BroadcastingEventSinkFactory brdFactory, String name, Properties props,
			EventFormatter frmt) {
		super(name, frmt);
		for (EventSinkFactory fc : brdFactory.getEventSinkFactories()) {
			eventSinks.add(fc.getEventSink(name, props, frmt));
		}
	}

	/**
	 * Sets opens sinks policy, how to treat this sink is open if subset of broadcasting sinks are open.
	 * 
	 * @param ospName
	 *            open sinks policy name
	 * @return instance of this sink
	 * @throws IllegalArgumentException
	 *             if provided open sinks policy name is not recognized
	 */
	public BroadcastingEventSink setOpenSinksPolicy(String ospName) throws IllegalArgumentException {
		this.openSinksPolicy = Utils.isEmpty(ospName) ? OpenSinksPolicy.ANY
				: OpenSinksPolicy.valueOf(ospName.toUpperCase());

		return this;
	}

	/**
	 * Sets opens sinks policy, how to treat this sink is open if subset of broadcasting sinks are open.
	 * 
	 * @param osp
	 *            open sinks policy
	 * @return instance of this sink
	 */
	public BroadcastingEventSink setOpenSinksPolicy(OpenSinksPolicy osp) {
		this.openSinksPolicy = osp == null ? OpenSinksPolicy.ANY : osp;

		return this;
	}

	@Override
	public Map<String, Object> getStats() {
		LinkedHashMap<String, Object> stats = new LinkedHashMap<>(32);
		getStats(stats);
		return stats;
	}

	@Override
	public KeyValueStats getStats(Map<String, Object> stats) {
		super.getStats(stats);
		stats.put(Utils.qualify(this, KEY_SINK_SIZE), eventSinks.size());
		stats.put(Utils.qualify(this, KEY_OPEN_COUNT), openCount());
		for (EventSink sink : eventSinks) {
			sink.getStats(stats);
		}
		return this;
	}

	@Override
	public Object getSinkHandle() {
		return this;
	}

	@Override
	public boolean isOpen() {
		if (eventSinks.isEmpty()) {
			return false;
		}

		for (EventSink sink : eventSinks) {
			if (sink.isOpen()) {
				if (openSinksPolicy == OpenSinksPolicy.ANY) {
					return true;
				}
			} else {
				if (openSinksPolicy == OpenSinksPolicy.ALL) {
					return false;
				}
			}
		}
		return openSinksPolicy == OpenSinksPolicy.ALL ? true : false;
	}

	@Override
	protected void _open() throws IOException {
		int openCount = 0;
		IOException lastE = null;
		for (EventSink sink : eventSinks) {
			try {
				if (!sink.isOpen()) {
					sink.open();
				}
				openCount++;
			} catch (IOException e) {
				lastE = e;
			}
		}
		if (lastE != null && (openCount == 0)) {
			throw lastE;
		}
	}

	@Override
	protected void _close() throws IOException {
		IOException lastE = null;
		for (EventSink sink : eventSinks) {
			try {
				sink.close();
			} catch (IOException e) {
				lastE = e;
			}
		}
		if (lastE != null) {
			throw lastE;
		}
	}

	@Override
	protected void _log(TrackingEvent event) throws IOException {
		logSinkEntry(new SinkEntry() {
			@Override
			public void logEntry(EventSink sink) throws Throwable {
				sink.log(event);
			}
		});
	}

	@Override
	protected void _log(TrackingActivity activity) throws IOException {
		logSinkEntry(new SinkEntry() {
			@Override
			public void logEntry(EventSink sink) throws Throwable {
				sink.log(activity);
			}
		});
	}

	@Override
	protected void _log(Snapshot snapshot) throws IOException {
		logSinkEntry(new SinkEntry() {
			@Override
			public void logEntry(EventSink sink) throws Throwable {
				sink.log(snapshot);
			}
		});
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) throws IOException {
		logSinkEntry(new SinkEntry() {
			@Override
			public void logEntry(EventSink sink) throws Throwable {
				sink.log(ttl, src, sev, msg, args);
			}
		});
	}

	@Override
	protected void _write(Object msg, Object... args) throws IOException, InterruptedException {
		logSinkEntry(new SinkEntry() {
			@Override
			public void logEntry(EventSink sink) throws IOException, InterruptedException {
				sink.write(msg, args);
			}
		});
	}

	/**
	 * Writes sink entry to all broadcasting sinks.
	 * 
	 * @param entry
	 *            sink entry to log
	 */
	protected void logSinkEntry(SinkEntry entry) {
		CountDownLatch waitLatch = new CountDownLatch(eventSinks.size());
		for (EventSink sink : eventSinks) {
			Thread lt = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						entry.logEntry(sink);
					} catch (Throwable t) {
						sink.setErrorState(t);
					} finally {
						waitLatch.countDown();
					}
				}
			});
			lt.start();
		}
		try {
			waitLatch.await();
		} catch (InterruptedException ie) {
			// setErrorState (ie);
		}
	}

	private int openCount() {
		int openCount = 0;
		for (EventSink sink : eventSinks) {
			if (sink.isOpen()) {
				openCount++;
			}
		}
		return openCount;
	}

	/**
	 * Enumerates policies of broadcast sinks open state for {@link BroadcastingEventSink} to be treated as open.
	 */
	enum OpenSinksPolicy {
		/**
		 * Require all broadcast sinks to be open.
		 */
		ALL,
		/**
		 * Require just any one broadcast sink to be open.
		 */
		ANY
	}

	/**
	 * Interface defining generic sink entry logging function.
	 */
	private static interface SinkEntry {
		/**
		 * Performs actual sink entry logging.
		 * 
		 * @param sink
		 *            sink to log entry
		 * 
		 * @throws Throwable
		 *             if entry logging fails, e.g. sink is closed or not writable
		 */
		void logEntry(EventSink sink) throws Throwable;
	}
}
