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
package com.nastel.jkool.tnt4j.filters;

import java.util.Map;

import com.nastel.jkool.tnt4j.config.Configurable;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.sink.EventSink;
import com.nastel.jkool.tnt4j.sink.SinkEventFilter;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * This class implements a simple event filter based on severity level threshold & time performance
 * such as elapsed and wait times. 
 * Use this class to filter out events/messages based time/level combination.
 * A given severity must be greater than or equal to the given level threshold to 
 * pass this filter. A given activity must be greater or equal to the given elapsed/waited 
 * time. Set time objectives to -1 to disable time based filtering.
 * </p>
 *
 * @see OpLevel
 * @see EventSink
 *
 * @version $Revision: 1 $
 *
 */
public class EventLevelTimeFilter implements SinkEventFilter, Configurable {
	OpLevel sevLimit;
	long	elapsedUsec = -1;
	long	waitUsec = -1;
	private Map<String, Object> config = null;
	
	/**
	 * Create a default filter with <code>OpLevel.INFO</code> as default
	 * threshold.
	 *
	 */
	public EventLevelTimeFilter() {
		sevLimit = OpLevel.INFO;		
	}
	
	/**
	 * Create a default filter with a given level threshold.
	 *
	 *@param threshold severity level threshold
	 *@param elapsedUsc elapsed time threshold (-1 disable)
	 *@param waitUsc wait time threshold (-1 disable)
	 */
	public EventLevelTimeFilter(OpLevel threshold, long elapsedUsc, long waitUsc) {
		sevLimit = threshold;	
		elapsedUsec = elapsedUsc;
		waitUsec = waitUsc;
	}
	
	@Override
	public boolean filter(EventSink sink, TrackingEvent event) {
		if (elapsedUsec >= 0) {
			if (event.getOperation().getElapsedTime() < elapsedUsec) return false;
		}
		if (waitUsec >= 0) {
			if (event.getOperation().getWaitTime() < waitUsec) return false;
		}
		return (event.getSeverity().ordinal() >= sevLimit.ordinal()) && sink.isSet(event.getSeverity());
	}

	@Override
	public boolean filter(EventSink sink, TrackingActivity activity) {
		if (elapsedUsec >= 0) {
			if (activity.getElapsedTime() < elapsedUsec) return false;
		}
		if (waitUsec >= 0) {
			if (activity.getWaitTime() < waitUsec) return false;
		}
		return (activity.getSeverity().ordinal() >= sevLimit.ordinal()) && sink.isSet(activity.getSeverity());
	}

	@Override
	public boolean filter(EventSink sink, OpLevel level, String msg, Object... args) {
		return (level.ordinal() >= sevLimit.ordinal()) && sink.isSet(level);
	}

	@Override
	public Map<String, Object> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> settings) {
		config = settings;
		Object levelString = config.get("Level");
		sevLimit = (levelString != null? OpLevel.valueOf(levelString): sevLimit);
		
		Object elaspedStr = config.get("ElapsedUsec");
		elapsedUsec = (elaspedStr != null? Long.parseLong(elaspedStr.toString()): elapsedUsec);
		
		Object waitStr = config.get("WaitUsec");
		waitUsec = (waitStr != null? Long.parseLong(waitStr.toString()): waitUsec);
	}	
}
