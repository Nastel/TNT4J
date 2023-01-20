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

import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.core.TTL;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.limiter.DefaultLimiterFactory;
import com.jkoolcloud.tnt4j.limiter.Limiter;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Abstract event sink factory class that all sink factories should subclass from. Derived classes should call
 * {@link #configureSink(EventSink)} before returning {@link EventSink} instances back using {@code #getEventSink()}
 * method calls. See example below:
 * </p>
 * 
 * <pre>
 * public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
 * 	return configureSink(new MyEventSinkImpl(name, props, frmt));
 * }
 * </pre>
 *
 * @see TTL
 * @see EventSink
 * @see Configurable
 *
 * @version $Revision: 1 $
 */
public abstract class AbstractEventSinkFactory implements EventSinkFactory, Configurable {
	private long ttl = TTL.TTL_CONTEXT;
	private SinkEventFilter eventFilter = null;
	private SinkErrorListener errorListener = null;
	private SinkLogEventListener eventListener = null;
	private EventLimiter limiter = null;
	private ResourceBundle defBundle = null;
	private EventFormatter evFormatter = null;

	protected Map<String, ?> config = null;
	private Set<String> tags = new HashSet<>();

	/**
	 * Obtain the default instance of {@link SinkEventFilter} configured for this factory.
	 *
	 * @return default sink event filter instance
	 */
	public SinkEventFilter getDefaultEventFilter() {
		return eventFilter;
	}

	/**
	 * Obtain the default instance of {@link SinkErrorListener} configured for this factory.
	 *
	 * @return default sink error listener instance
	 */
	public SinkErrorListener getDefaultErrorListener() {
		return errorListener;
	}

	/**
	 * Obtain the default instance of {@link SinkLogEventListener} configured for this factory.
	 *
	 * @return default sink event listener instance
	 */
	public SinkLogEventListener getDefaultEventListener() {
		return eventListener;
	}

	/**
	 * Obtain the default instance of {@link EventLimiter} configured for this factory.
	 *
	 * @return default sink event limiter instance
	 */
	public EventLimiter getDefaultEventLimiter() {
		return limiter;
	}

	/**
	 * Obtain the instance of {@link EventFormatter} configured for this factory or fallback to default one provided by
	 * {@code dFormatter} parameter.
	 * 
	 * @param dFormatter
	 *            default formatter instance
	 *
	 * @return formatter instance to be used by this factory
	 */
	public EventFormatter getDefaultEventFormatter(EventFormatter dFormatter) {
		return evFormatter == null ? dFormatter : evFormatter;
	}

	/**
	 * Configure a given event sink based on default settings
	 *
	 * @param sink
	 *            event sink
	 * @return configured event sink
	 */
	protected EventSink configureSink(EventSink sink) {
		if (eventFilter != null) {
			sink.addSinkEventFilter(eventFilter);
		}
		if (errorListener != null) {
			sink.addSinkErrorListener(errorListener);
		}
		if (eventListener != null) {
			sink.addSinkLogEventListener(eventListener);
		}
		if (defBundle != null) {
			sink.setResourceBundle(defBundle);
		}
		if (limiter != null) {
			sink.setLimiter(limiter);
		}
		if (evFormatter != null) {
			sink.setEventFormatter(evFormatter);
		}
		sink.setTTL(ttl);
		sink.setTag(tags);
		return sink;
	}

	@Override
	public void setConfiguration(Map<String, ?> props) throws ConfigException {
		config = props;
		setTTL(Utils.getLong("TTL", props, getTTL()));
		setTags(Utils.getString("Tag", props, null));
		boolean enabled = Utils.getBoolean("RateLimit", props, false);
		if (enabled) {
			double maxmps = Utils.getDouble("RateMaxMPS", props, Limiter.MAX_RATE);
			double maxbps = Utils.getDouble("RateMaxBPS", props, Limiter.MAX_RATE);
			long timeout = Utils.getLong("RateTimeout", props, EventLimiter.BLOCK_UNTIL_GRANTED);

			limiter = newEventLimiterImpl(maxmps, maxbps, enabled, timeout);
		}

		String bundleName = Utils.getString("ResourceBundle", props, null);
		if (bundleName != null) {
			defBundle = ResourceBundle.getBundle(bundleName);
		}

		eventFilter = (SinkEventFilter) Utils.createConfigurableObject("Filter", "Filter.", config);
		errorListener = (SinkErrorListener) Utils.createConfigurableObject("ErrorListener", "ErrorListener.", config);
		eventListener = (SinkLogEventListener) Utils.createConfigurableObject("EventListener", "EventListener.",
				config);
		evFormatter = (EventFormatter) Utils.createConfigurableObject("Formatter", "Formatter.", config);
	}

	@Override
	public long getTTL() {
		return ttl;
	}

	@Override
	public void setTTL(long ttl) {
		this.ttl = ttl;
	}

	public void setTags(String tag) {
		tags.clear();

		if (tag != null) {
			String[] sTags = tag.split(",");

			for (String stg : sTags) {
				String ttg = stg.trim();
				if (!ttg.isEmpty()) {
					this.tags.add(ttg);
				}
			}
		}
	}

	@Override
	public Map<String, ?> getConfiguration() {
		return config;
	}

	protected EventLimiter newEventLimiterImpl(double maxmps, double maxbps, boolean enabled, long timeout) {
		EventLimiter eLimit = new EventLimiter(DefaultLimiterFactory.getInstance().newLimiter(maxmps, maxbps, enabled),
				timeout, TimeUnit.MILLISECONDS);
		return eLimit;
	}
}
