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

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.jkoolcloud.tnt4j.tracker.TimeStats;
import com.jkoolcloud.tnt4j.tracker.TimeTracker;

/**
 * This class implements a dump handler for {@link TimeTracker}. It dumps the contents of a timing table
 * {@link TimeTracker}. The timings maintain the number of nanoseconds since last hit/miss on a given key.
 *
 * @version $Revision: 1 $
 */
public class TimeTrackerDumpProvider extends DefaultDumpProvider {
	private TimeTracker timeTracker;

	/**
	 * Create a new instance of {@link TimeTrackerDumpProvider} instance. which provides an implementation to dump the
	 * contents of {@link TimeTracker}
	 *
	 * @param name
	 *            name of the time tracker dump provider
	 * @param tTracker
	 *            time tracker instance
	 *
	 * @see DefaultDumpProvider
	 * @see TimeTracker
	 */
	public TimeTrackerDumpProvider(String name, TimeTracker tTracker) {
		this(name, "KeyTimings", tTracker);
	}

	/**
	 * Create a new instance of {@link TimeTrackerDumpProvider} instance. which provides an implementation to dump the
	 * contents of {@link TimeTracker}
	 *
	 * @param name
	 *            name of the time tracker dump provider
	 * @param cat
	 *            category of the time tracker dump provider
	 * @param tTracker
	 *            time tracker instance
	 *
	 * @see DefaultDumpProvider
	 * @see TimeTracker
	 */
	public TimeTrackerDumpProvider(String name, String cat, TimeTracker tTracker) {
		super(name, cat);
		this.timeTracker = tTracker;
	}

	@Override
	public DumpCollection getDump() {
		Dump dump = new Dump(getCategoryName() + "-Table", this);
		for (Entry<String, TimeStats> entry : timeTracker.getTimeStats().entrySet()) {
			dump.add(entry.getKey(),
					"h(" + entry.getValue().getHitCount() + "-" + entry.getValue().getHitAge(TimeUnit.MILLISECONDS)
							+ ")" + "m(" + entry.getValue().getMissCount() + "-"
							+ entry.getValue().getMissAge(TimeUnit.MILLISECONDS) + ")");
		}
		return dump;
	}
}
