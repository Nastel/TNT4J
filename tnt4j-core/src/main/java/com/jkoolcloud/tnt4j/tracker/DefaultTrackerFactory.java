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

import java.util.Map;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.config.TrackerConfig;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * {@link DefaultTrackerFactory} lets developers obtain an instance to the {@link Tracker} logger. Source should obtain
 * a logger instance per thread
 * </p>
 *
 * <p>
 * A {@link TrackingEvent} represents a specific tracking event that application creates for every discrete activity
 * such as JDBC, JMS, SOAP or any other relevant application activity. Source developers must obtain a {@link Tracker}
 * instance via {@link DefaultTrackerFactory}, create instances of {@link TrackingActivity} and use
 * {@code Tracker.tnt()} to report application activities.
 *
 * <p>
 * {@code TrackingActivity start()/stop()} calls are used to mark application activity boundaries.
 * </p>
 *
 * @see Tracker
 * @see TrackingEvent
 * @see TrackingActivity
 * @see TrackerConfig
 *
 * @version $Revision: 9 $
 *
 */
public class DefaultTrackerFactory implements TrackerFactory, Configurable {

	boolean defaultKeepContext = false;
	protected Map<String, ?> config = null;

	/**
	 * Create a new instance of {@link DefaultTrackerFactory} with a default tracker configuration {@link TrackerConfig}
	 * instance.
	 * 
	 * @see TrackerConfig
	 */
	public DefaultTrackerFactory() {
	}

	@Override
	public Tracker getInstance(TrackerConfig tconfig) {
		return new TrackerImpl(tconfig, defaultKeepContext);
	}

	@Override
	public void close(Tracker tr) {
		Utils.close(tr);
	}

	@Override
	public Map<String, ?> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, ?> props) throws ConfigException {
		config = props;
		defaultKeepContext = Utils.getBoolean("KeepThreadContext", props, defaultKeepContext);
	}
}
