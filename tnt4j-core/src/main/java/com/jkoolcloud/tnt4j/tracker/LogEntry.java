/*
 * Copyright 2014-2024 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkoolcloud.tnt4j.tracker;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.OpType;
import com.jkoolcloud.tnt4j.core.PropertySnapshot;
import com.jkoolcloud.tnt4j.core.UsecTimestamp;
import com.jkoolcloud.tnt4j.source.Source;

import java.util.Collection;

/**
 * Log entry defines a named list of properties.
 * 
 * @version $Revision: 1 $
 */
public class LogEntry extends TrackingEvent {

	public LogEntry(TrackerImpl tr) {
		super(tr);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, String opName, String msg, Object... args) {
		super(tr, src, severity, opName, (String) null, msg, args);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, String opName, byte[] msg, Object... args) {
		super(tr, src, severity, opName, (String) null, msg, args);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName, String tag, String msg, Object... args) {
		super(tr, src, severity, opType, opName, (String) null, tag, msg, args);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName, Collection<String> tags, String msg, Object... args) {
		super(tr, src, severity, opType, opName, (Collection<String>) null, tags, msg, args);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName, Collection<String> tags, byte[] msg, Object... args) {
		super(tr, src, severity, opType, opName, (Collection<String>) null, tags, msg, args);
	}

	public LogEntry(TrackerImpl tr, Source src, OpLevel severity, OpType opType, String opName, String tag, byte[] msg, Object... args) {
		super(tr, src, severity, opType, opName, (String) null, tag, msg, args);
	}
}
