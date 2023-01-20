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
package com.jkoolcloud.tnt4j.sink;

/**
 * This interface defines common {@link EventSink} statistic keys returned by
 * {@link com.jkoolcloud.tnt4j.sink.EventSink#getStats()} method.
 *
 * @version $Revision: 1 $
 *
 */
public interface EventSinkStats {
	String KEY_SINK_ERROR_COUNT = "sink-errors";
	String KEY_SINK_ERROR_STATE = "sink-error-state";
	String KEY_SINK_ERROR_MSG = "sink-error-msg";
	String KEY_SINK_ERROR_TIMESTAMP = "sink-error-timestamp";
	String KEY_LOGGED_MSGS = "sink-logged-messages";
	String KEY_SINK_WRITES = "sink-direct-writes";
	String KEY_LOGGED_EVENTS = "sink-events";
	String KEY_LOGGED_ACTIVITIES = "sink-activities";
	String KEY_LOGGED_SNAPSHOTS = "sink-snapshots";
	String KEY_SKIPPED_COUNT = "sink-skipped";
	String KEY_LAST_TIMESTAMP = "sink-last-timestamp";
	String KEY_LAST_AGE = "sink-last-age-ms";
	String KEY_BYTES_COUNT = "sink-sent-bytes";

	String KEY_LIMITER_ENABLED = "limiter-enabled";
	String KEY_LIMITER_MPS = "limiter-mps";
	String KEY_LIMITER_BPS = "limiter-bps";
	String KEY_LIMITER_MAX_MPS = "limiter-max-mps";
	String KEY_LIMITER_MAX_BPS = "limiter-max-bps";
	String KEY_LIMITER_TOTAL_MSGS = "limiter-total-msgs";
	String KEY_LIMITER_TOTAL_BYTES = "limiter-total-bytes";
	String KEY_LIMITER_TOTAL_DENIED = "limiter-total-denied";
	String KEY_LIMITER_TOTAL_DELAYS = "limiter-total-delays";
	String KEY_LIMITER_LAST_DELAY_TIME = "limiter-last-delay-sec";
	String KEY_LIMITER_TOTAL_DELAY_TIME = "limiter-total-delay-time-sec";
}
