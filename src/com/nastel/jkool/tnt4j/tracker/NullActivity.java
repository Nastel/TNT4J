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
package com.nastel.jkool.tnt4j.tracker;

import java.util.UUID;

import com.nastel.jkool.tnt4j.core.ActivityStatus;
import com.nastel.jkool.tnt4j.core.OpCompCode;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.core.Trackable;

/**
 * This class represents an empty Null/NOOP activity returned by tracker
 * when <code>TrackingFilter.isTrackingEnabled()</code> returns false.
 * This is done to stub out  activity timing and reduce tracking overhead
 * when tracking is disabled. NOOP activities are never logged by a tracker.
 *
 * @see NullEvent
 * @see ActivityStatus
 *
 * @version $Revision: 5 $
 *
 */
public class NullActivity extends TrackingActivity {
	protected NullActivity() {
		super(OpLevel.NONE, UUID.randomUUID().toString());
		super.setType(OpType.NOOP);
	}
	
	@Override
	public void setType(OpType type) {
		super.setType(OpType.NOOP);
	}

	@Override
	public void start() {
	}

	public void start(long startTime, int startTimeUsec) {
	}

	@Override
	public void stop() {
	}

	@Override
	public void stop(Throwable ex) {
	}

	@Override
	public void stop(ActivityStatus status, Throwable ex) {
	}

	@Override
	public void stop(ActivityStatus status, OpCompCode ccode, Throwable ex) {
	}

	@Override
	public void stop(long stopTime, long stopTimeUsec) {
	}

	@Override
	public TrackingActivity appendDefaultSnapshot(boolean flag) {
		super.appendDefaultSnapshot(false);
		return this;
	}

	@Override
	public void tnt(TrackingEvent event) {
	}
	
	@Override
	public void add(Trackable item) {
	}
}
