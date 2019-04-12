/*
 * Copyright 2018 JKOOL, LLC.
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

	BroadcastingEventSinkFactory brFactory;
	final Collection<EventSink> eventSinks = Collections.synchronizedList(new ArrayList<EventSink>(3));

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
		brFactory = brdFactory;
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
		brFactory = brdFactory;
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
		this.brFactory = brdFactory;
		for (EventSinkFactory fc : brdFactory.getEventSinkFactories()) {
			eventSinks.add(fc.getEventSink(name, props, frmt));
		}
	}

	@Override
	public Map<String, Object> getStats() {
		LinkedHashMap<String, Object> stats = new LinkedHashMap<String, Object> (32);
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
		boolean flag = false;
		for (EventSink sink : eventSinks) {
			if (sink.isOpen()) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	@Override
	protected void _open() throws IOException {
		int openCount = 0;
		IOException lastE = null;
		for (EventSink sink : eventSinks) {
			try {
				sink.open();
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
		for (EventSink sink : eventSinks) {
			sink.log(event);
		}
	}

	@Override
	protected void _log(TrackingActivity activity) throws IOException {
		for (EventSink sink : eventSinks) {
			sink.log(activity);
		}
	}

	@Override
	protected void _log(Snapshot snapshot) throws IOException {
		for (EventSink sink : eventSinks) {
			sink.log(snapshot);
		}
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) throws IOException {
		for (EventSink sink : eventSinks) {
			sink.log(ttl, src, sev, msg, args);
		}
	}

	@Override
	protected void _write(Object msg, Object... args) throws IOException, InterruptedException {
		for (EventSink sink : eventSinks) {
			sink.write(msg, args);
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
}
