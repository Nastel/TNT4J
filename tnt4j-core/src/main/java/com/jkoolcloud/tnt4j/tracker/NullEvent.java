/*
 * Copyright 2014-2023 JKOOL, LLC.
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

import com.jkoolcloud.tnt4j.core.OpCompCode;
import com.jkoolcloud.tnt4j.core.UsecTimestamp;

/**
 * This class represents an empty Null/NOOP event returned by tracker when
 * {@link TrackingFilter#isTrackingEnabled(Tracker, com.jkoolcloud.tnt4j.core.OpLevel, Object...)} returns false. This
 * is done to stub out activity timing and reduce tracking overhead when tracking is disabled. NOOP events are never
 * logged by a tracker.
 *
 * @see OpCompCode
 * @see UsecTimestamp
 *
 * @version $Revision: 5 $
 *
 */
public class NullEvent extends TrackingEvent {

	protected NullEvent(TrackerImpl tr) {
		super(tr);
	}

	@Override
	public void start(long startTime) {
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void stop(long elapsedTime) {
	}

	@Override
	public void stop(long endTimeUsec, long elapsedUsec) {
	}

	@Override
	public void stop(Throwable opEx) {
	}

	@Override
	public void stop(Throwable opEx, long elapsedUsec) {
	}

	@Override
	public void stop(OpCompCode ccode, int rcode) {
	}

	@Override
	public void stop(OpCompCode ccode, Throwable opEx) {
	}

	@Override
	public void stop(OpCompCode ccode, int rcode, Throwable opEx) {
	}

	@Override
	public void stop(OpCompCode ccode, int rcode, Throwable opEx, long endTime) {
	}

	@Override
	public void stop(OpCompCode ccode, int rcode, Throwable opEx, long endTime, long elapsedUsec) {
	}

	@Override
	public void stop(OpCompCode ccode, int rcode, Throwable opEx, UsecTimestamp endTime, long elapsedUsec) {
	}
}
