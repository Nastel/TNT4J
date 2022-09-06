/*
 * Copyright 2014-2022 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.dump;

import java.util.EventObject;
import java.util.List;

/**
 * <p>
 * An event class for reporting dump state change events.
 * </p>
 * <p>
 * The following standard events are generated by typical dump (event types are defined in
 * {@link com.jkoolcloud.tnt4j.dump.DumpProvider}):
 * </p>
 * 
 * <p>
 * DUMP_BEFORE -- generated before dump is written to destination(s)
 * </p>
 * <p>
 * DUMP_AFTER -- generated after dump is written to destination(s)
 * </p>
 * <p>
 * DUMP_COMPLETE -- all dump processing has been completed
 * </p>
 * <p>
 * DUMP_ERROR -- error occurred during dump process
 * </p>
 * 
 * @see DumpSink
 * @see DumpCollection
 * @see DumpProvider
 * 
 * @version $Revision: 6 $
 * 
 */
public class DumpEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TYPE_STRING[] = { "DUMP_BEFORE", "DUMP_AFTER", "DUMP_COMPLETE", "DUMP_ERROR",
			"DUMP_UNKNOWN" };
	private static final int LAST_EVENT_INDEX = (TYPE_STRING.length - 1);

	private int type;
	private DumpCollection dumpCollection;
	private List<DumpSink> dumpDest;
	private Throwable exception = null;

	/**
	 * Create a dump event with specific parameters
	 * 
	 * @param source
	 *            of the event
	 * @param tp
	 *            event type described in {@link com.jkoolcloud.tnt4j.dump.DumpProvider}
	 * @param dump
	 *            collection on which event occurred
	 * @param list
	 *            of dump destinations for which dump event is generated
	 * @see DumpCollection
	 * @see DumpSink
	 */
	public DumpEvent(Object source, int tp, DumpCollection dump, List<DumpSink> list) {
		this(source, tp, dump, list, null);
	}

	/**
	 * Create a dump event with specific parameters
	 * 
	 * @param source
	 *            of the event
	 * @param tp
	 *            event type described in {@link com.jkoolcloud.tnt4j.dump.DumpProvider}
	 * @param dump
	 *            collection on which event occurred
	 * @param list
	 *            of dump destinations for which dump event is generated
	 * @param ex
	 *            error occurred during the dump process
	 * @see DumpCollection
	 * @see DumpSink
	 * 
	 */
	public DumpEvent(Object source, int tp, DumpCollection dump, List<DumpSink> list, Throwable ex) {
		super(source);
		type = tp;
		dumpCollection = dump;
		dumpDest = list;
		exception = ex;
	}

	/**
	 * Get the associated event type
	 * 
	 * @return event type as defined in {@link com.jkoolcloud.tnt4j.dump.DumpProvider}
	 * 
	 */
	public int getType() {
		return type;
	}

	/**
	 * Get the associated event type string
	 * 
	 * @return event type as defined in {@link com.jkoolcloud.tnt4j.dump.DumpProvider}
	 * 
	 */
	public String getTypeString() {
		return (type >= 0 && type <= DumpProvider.DUMP_ERROR) ? TYPE_STRING[type] : TYPE_STRING[LAST_EVENT_INDEX];
	}

	/**
	 * Get key associated with the event
	 * 
	 * @return key
	 * 
	 */
	public DumpCollection getDump() {
		return dumpCollection;
	}

	/**
	 * Get value associated with the event
	 * 
	 * @return value
	 * 
	 */
	public List<DumpSink> getDestinations() {
		return dumpDest;
	}

	/**
	 * Get error associated with the event
	 * 
	 * @return error
	 * 
	 */
	public Throwable getCause() {
		return exception;
	}

	@Override
	public String toString() {
		return super.toString() 
			+ "{Type:" + getTypeString() 
			+ ", Dump: " + dumpCollection 
			+ ", SinkList: " + dumpDest
			+ ", Exception: " + (exception != null) 
			+ "}";
	}

}
