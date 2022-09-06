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
package com.jkoolcloud.tnt4j;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;

/**
 * <p>
 * This class implements a runnable task implementation scheduled by {@link ActivityScheduler}.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see TrackingLogger
 * @see TrackingActivity
 * @see com.jkoolcloud.tnt4j.core.ActivityListener
 * @see OpLevel
 */
public class ActivityTask implements Runnable {
	protected String activityName;
	protected TrackingLogger logger;
	protected TrackingActivity activity;
	protected OpLevel level;

	/**
	 * Create a task for a specific logger, default activity name and {@link OpLevel#INFO} severity
	 * 
	 * @param lg
	 *            tracking logger instance
	 */
	protected ActivityTask(TrackingLogger lg) {
		this(lg, "ActivityTask", OpLevel.INFO);
	}

	/**
	 * Create a task for a specific logger, activity name and {@link OpLevel#INFO} severity
	 * 
	 * @param lg
	 *            tracking logger instance
	 * @param name
	 *            activity name
	 */
	protected ActivityTask(TrackingLogger lg, String name) {
		this(lg, name, OpLevel.INFO);
	}

	/**
	 * Create a task for a specific logger, activity name and severity
	 * 
	 * @param lg
	 *            tracking logger instance
	 * @param name
	 *            activity name
	 * @param level
	 *            severity level
	 */
	protected ActivityTask(TrackingLogger lg, String name, OpLevel level) {
		logger = lg;
		activityName = name;
		this.level = level;
		startActivity();
	}

	/**
	 * Obtain current activity instance associated with this task
	 * 
	 * @return current activity instance
	 */
	protected TrackingActivity getActivity() {
		return activity;
	}

	/**
	 * This method is called when activity is started Override this method to change behavior when activity starts
	 */
	protected void startActivity() {
		activity = logger.newActivity(level, activityName);
		activity.start();
	}

	/**
	 * This method is called when activity ends Override this method to change behavior when activity ends. This method
	 * also reports activity via configured {@code TrackingLogger} instance conditional upon {@link #doSample()}
	 * returning {@code true}.
	 * 
	 * @return elapsed time of the activity in microseconds.
	 */
	protected long endActivity() {
		activity.stop();
		if (doSample()) {
			logger.tnt(activity);
		}
		return activity.getElapsedTimeUsec();
	}

	/**
	 * This method is called when activity ends {@link #endActivity()}. Override this method to change behavior when
	 * activity ends. Return {@code true} to allow tracking of current activity, {@code false} - to ignore tracking. By
	 * default if {@code OpType.NOOP} are ignored.
	 *
	 * @return true to track current activity, false to ignore
	 */
	protected boolean doSample() {
		return true;
	}

	@Override
	public void run() {
		try {
			endActivity();
		} catch (Throwable e) {
		} finally {
			startActivity();
		}
	}
}