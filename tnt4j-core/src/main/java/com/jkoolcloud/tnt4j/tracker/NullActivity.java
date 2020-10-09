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
package com.jkoolcloud.tnt4j.tracker;

import com.jkoolcloud.tnt4j.core.*;

/**
 * This class represents an empty Null/NOOP activity returned by tracker when
 * {@link TrackingFilter#isTrackingEnabled(Tracker, com.jkoolcloud.tnt4j.core.OpLevel, Object...)} returns
 * {@code false}. This is done to stub out activity timing and reduce tracking overhead when tracking is disabled. NOOP
 * activities are never logged by a tracker.
 *
 * @see NullEvent
 * @see ActivityStatus
 *
 * @version $Revision: 5 $
 *
 */
public class NullActivity extends TrackingActivity {
	protected NullActivity() {
		super(OpLevel.NONE, Operation.NOOP);
		super.setType(OpType.NOOP);
	}

	@Override
	public void setType(OpType type) {
		super.setType(OpType.NOOP);
	}

	@Override
	public void start() {
	}

	@Override
	public void start(long startTimeUsec) {
	}

	@Override
	public void stop() {
	}

	@Override
	public void stop(long elapsedUsec) {
	}

	@Override
	public void stop(long stopTimeUsec, long elapsedUsec) {
	}

	@Override
	public void stop(Throwable ex) {
	}

	@Override
	public void stop(Throwable ex, long elapsedUsec) {
	}

	@Override
	public void stop(ActivityStatus status, Throwable ex) {
	}

	@Override
	public void stop(ActivityStatus status, Throwable ex, long elapsedUsec) {
	}

	@Override
	public void stop(ActivityStatus status, OpCompCode ccode, Throwable ex) {
	}

	@Override
	public void stop(ActivityStatus status, OpCompCode ccode, Throwable ex, long elapsedUsec) {
	}

	@Override
	public void tnt(TrackingEvent event) {
	}

	@Override
	public void tnt(Snapshot event) {
	}

	@Override
	public void tnt(OpLevel severity, OpType type, String opName, String cid, String tag, long elapsed, byte[] msg,
			Object... args) {
	}

	@Override
	public void tnt(OpLevel severity, OpType type, String opName, String cid, String tag, long elapsed, String msg,
			Object... args) {
	}

	@Override
	public void add(Trackable item) {
	}
}
