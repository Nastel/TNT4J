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
package com.jkoolcloud.tnt4j.filters;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.core.TTL;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.SinkEventFilter;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements a simple event filter based on severity level threshold & time performance such as elapsed,
 * wait/wall times and message pattern (regex). Use this class to filter out events/messages based time/level
 * combination. A given severity must be greater than or equal to the given level threshold to pass this filter. A given
 * activity must be greater or equal to the given elapsed/wait/wall time. Set time objectives to -1 to disable time
 * based filtering.
 * </p>
 * 
 * @see OpLevel
 * @see EventSink
 * @see SinkEventFilter
 * @see Configurable
 * 
 * @version $Revision: 1 $
 * 
 */
public class EventLevelTimeFilter implements SinkEventFilter, Configurable {
	public static final String LEVEL = "Level";
	public static final String ELAPSED_USEC = "ElapsedUsec";
	public static final String WAIT_USEC = "WaitUsec";
	public static final String WALL_USEC = "WallUsec";
	public static final String TTL_SEC = "TTL";
	public static final String DUPS_SUPPRESS = "SuppressDups";
	public static final String DUPS_TIMEOUT = "SuppressTimeSec";
	public static final String DUPS_CACHE_SIZE = "SuppressCacheSize";
	public static final String MSG_PATTERN = "MsgRegex";
	public static final String OFF_LEVEL_LABEL = "OFF";
	public static final int OFF_LEVEL_INT = 100;

	long elapsedUsec = -1;
	long waitUsec = -1;
	long wallUsec = -1;

	boolean suppressDups = false;
	long dupTimeoutSec = 30, dupCacheSize = 100;

	Pattern msgPattern;
	String msgRegx = null;
	long ttl = TTL.TTL_CONTEXT;
	int minLevel = OpLevel.INFO.ordinal();

	ConcurrentMap<String, AtomicLong> pastMsgs;
	Map<String, Object> config;

	/**
	 * Create a default filter with {@link OpLevel#INFO} as default threshold.
	 * 
	 */
	public EventLevelTimeFilter() {
		minLevel = OpLevel.INFO.ordinal();
	}

	/**
	 * Create a default filter with a given minimum level threshold.
	 * 
	 * @param mLevel
	 *            minimum severity level threshold
	 * @param elapsedUsc
	 *            elapsed time threshold (-1 disable)
	 * @param waitUsc
	 *            wait time threshold (-1 disable)
	 * @param wallUsc
	 *            wall time threshold (-1 disable)
	 */
	public EventLevelTimeFilter(OpLevel mLevel, long elapsedUsc, long waitUsc, long wallUsc) {
		this(mLevel, elapsedUsc, waitUsc, wallUsc, null);
	}

	/**
	 * Create a default filter with a given minimum level threshold.
	 * 
	 * @param mLevel
	 *            minimum severity level threshold
	 * @param elapsedUsc
	 *            elapsed time threshold (-1 disable)
	 * @param waitUsc
	 *            wait time threshold (-1 disable)
	 * @param wallUsc
	 *            wall time threshold (-1 disable)
	 * @param msgRegex
	 *            message regex (null means all)
	 */
	public EventLevelTimeFilter(OpLevel mLevel, long elapsedUsc, long waitUsc, long wallUsc, String msgRegex) {
		minLevel = mLevel.ordinal();
		elapsedUsec = elapsedUsc;
		waitUsec = waitUsc;
		wallUsec = wallUsc;
		msgRegx = msgRegex;
		if (msgRegx != null) {
			msgPattern = Pattern.compile(msgRegx);
		}
	}

	@Override
	public boolean filter(EventSink sink, TrackingEvent event) {
		if (elapsedUsec >= 0 && event.getOperation().getElapsedTimeUsec() < elapsedUsec) {
			return false;
		}
		if (waitUsec >= 0 && event.getOperation().getWaitTimeUsec() < waitUsec) {
			return false;
		}
		if (msgPattern != null && !msgPattern.matcher(event.getMessagePattern()).matches()) {
			return false;
		}
		if (checkForDups(event.getMessagePattern()) > 1) {
			return false;
		}
		if (ttl != TTL.TTL_CONTEXT) {
			event.setTTL(ttl);
		}
		return passLevel(event.getSeverity(), sink);
	}

	@Override
	public boolean filter(EventSink sink, TrackingActivity activity) {
		if (elapsedUsec >= 0 && activity.getElapsedTimeUsec() < elapsedUsec) {
			return false;
		}
		if (waitUsec >= 0 && activity.getWaitTimeUsec() < waitUsec) {
			return false;
		}
		if (wallUsec >= 0 && activity.getWallTimeUsec() < wallUsec) {
			return false;
		}
		if (ttl != TTL.TTL_CONTEXT) {
			activity.setTTL(ttl);
		}
		return passLevel(activity.getSeverity(), sink);
	}

	@Override
	public boolean filter(EventSink sink, Snapshot snapshot) {
		if (ttl != TTL.TTL_CONTEXT) {
			snapshot.setTTL(ttl);
		}
		return passLevel(snapshot.getSeverity(), sink);
	}

	@Override
	public boolean filter(EventSink sink, long ttl, Source source, OpLevel level, String msg, Object... args) {
		if (checkForDups(msg) > 1) {
			return false;
		} else if (msgPattern != null && !msgPattern.matcher(msg).matches()) {
			return false;
		}
		return passLevel(level, sink);
	}

	@Override
	public Map<String, Object> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> settings) {
		config = settings;

		String levelStr = Utils.getString(LEVEL, settings, OpLevel.INFO.toString());
		minLevel = levelStr.equalsIgnoreCase(OFF_LEVEL_LABEL) ? OFF_LEVEL_INT : OpLevel.valueOf(levelStr).ordinal();

		elapsedUsec = Utils.getLong(ELAPSED_USEC, settings, elapsedUsec);
		waitUsec = Utils.getLong(WAIT_USEC, settings, waitUsec);
		wallUsec = Utils.getLong(WALL_USEC, settings, wallUsec);
		ttl = Utils.getLong(TTL_SEC, settings, ttl);

		// configure duplicate detection
		dupTimeoutSec = Utils.getLong(DUPS_TIMEOUT, settings, dupTimeoutSec);
		dupCacheSize = Utils.getLong(DUPS_CACHE_SIZE, settings, dupCacheSize);
		suppressDups = Utils.getBoolean(DUPS_SUPPRESS, settings, suppressDups);
		if (suppressDups) {
			Cache<String, AtomicLong> cacheImpl = CacheBuilder.newBuilder().recordStats()
					.expireAfterWrite(dupTimeoutSec, TimeUnit.SECONDS).maximumSize(dupCacheSize).build();
			pastMsgs = cacheImpl.asMap();
		}

		msgRegx = Utils.getString(MSG_PATTERN, settings, null);
		if (msgRegx != null) {
			msgPattern = Pattern.compile(msgRegx);
		}
	}

	/**
	 * Returns true if a given level passes the filter, false otherwise
	 * 
	 * @param level
	 *            event severity level
	 * @param sink
	 *            event sink where filter request is coming from
	 * @return true if level passed all filters, false otherwise
	 * @see OpLevel
	 * @see EventSink
	 */
	private boolean passLevel(OpLevel level, EventSink sink) {
		return (level.ordinal() >= minLevel) && sink.isSet(level);
	}

	private long checkForDups(String msg) {
		if (pastMsgs != null) {
			AtomicLong ocCounter = pastMsgs.get(msg);
			if (ocCounter == null) {
				AtomicLong newCounter = new AtomicLong(0);
				pastMsgs.putIfAbsent(msg, newCounter);
				ocCounter = pastMsgs.get(msg);
			}
			return ocCounter.incrementAndGet();
		}
		return 1;
	}
}
