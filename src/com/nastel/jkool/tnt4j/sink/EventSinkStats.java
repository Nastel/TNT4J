/*
 * Copyright 2014-2015 JKOOL, LLC.
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
package com.nastel.jkool.tnt4j.sink;

/**
 * This interface defines common {@link EventSink} statistic keys returned by
 * {@code EventSink.getStats()} method.
 *
 *
 * @version $Revision: 1 $
 *
 */
public interface EventSinkStats {
	static final String KEY_SINK_ERROR_COUNT = "sink-errors";
	static final String KEY_SINK_ERROR_STATE = "sink-error-state";
	static final String KEY_LOGGED_MSGS = "sink-messages";
	static final String KEY_SINK_WRITES= "sink-direct-writes";
	static final String KEY_LOGGED_EVENTS = "sink-events";
	static final String KEY_LOGGED_ACTIVITIES = "sink-activities";
	static final String KEY_LOGGED_SNAPSHOTS = "sink-snapshots";
	static final String KEY_SKIPPED_COUNT = "sink-skipped";
	static final String KEY_LAST_TIMESTAMP = "sink-last-timestamp";
	static final String KEY_LAST_AGE = "sink-last-age-ms";

	static final String KEY_LIMITER_ENABLED = "limiter-enabled";
	static final String KEY_LIMITER_MPS = "limiter-mps";
	static final String KEY_LIMITER_BPS = "limiter-bps";
	static final String KEY_LIMITER_MAX_MPS = "limiter-max-mps";
	static final String KEY_LIMITER_MAX_BPS = "limiter-max-bps";;
	static final String KEY_LIMITER_TOTAL_MSGS = "limiter-total-msgs";
	static final String KEY_LIMITER_TOTAL_BYTES = "limiter-total-bytes";
	static final String KEY_LIMITER_TOTAL_DENIED = "limiter-total-denied";
	static final String KEY_LIMITER_TOTAL_DELAYS = "limiter-total-delays";
	static final String KEY_LIMITER_LAST_DELAY_TIME = "limiter-last-delay-sec";
	static final String KEY_LIMITER_TOTAL_DELAY_TIME = "limiter-total-delay-time-sec";
}
