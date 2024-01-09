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

package com.jkoolcloud.tnt4j.core;

/**
 * Log entry defines a named list of properties.
 * 
 * @version $Revision: 1 $
 */
public class LogEntry extends PropertySnapshot {

	/**
	 * Create a log entry instance.
	 * 
	 * @param name
	 *            log entry name
	 */
	public LogEntry(String name) {
		this(OpType.LOG.name(), name);
	}

	/**
	 * Create a log entry instance.
	 * 
	 * @param cat
	 *            log entry category
	 * @param name
	 *            log entry name
	 */
	public LogEntry(String cat, String name) {
		this(cat, name, UsecTimestamp.now());
	}

	/**
	 * Create a log entry instance.
	 * 
	 * @param cat
	 *            log entry category
	 * @param name
	 *            log entry name
	 * @param time
	 *            timestamp
	 */
	public LogEntry(String cat, String name, UsecTimestamp time) {
		super(cat, name, OpLevel.NONE, OpType.LOG, time);
	}
}
