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
package com.jkoolcloud.tnt4j.sink;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.jkoolcloud.tnt4j.core.TTL;
import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.limiter.DefaultLimiterFactory;
import com.jkoolcloud.tnt4j.limiter.Limiter;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>Abstract event sink factory class that all sink factories should subclass from.
 * Derived classes should call {@code configure()} before returning {@link EventSink}
 * instances back using {@code getEventSink()} method calls. See example below:
 * </p>
 *<pre>
 *<code>
 *	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
 *		return configureSink(new MyEventSinkImpl(name, props, frmt));
 *	}
 *</code>
 *</pre>
 *
 * @see TTL
 * @see EventSink
 * @see Configurable
 *
 * @version $Revision: 1 $
 *
 */

public abstract class AbstractEventSinkFactory implements EventSinkFactory, Configurable {
	private SinkEventFilter eventFilter = null;
	private SinkErrorListener errorListener = null;
	private SinkLogEventListener eventListener = null;
	private EventLimiter limiter = null;
	private long ttl = TTL.TTL_CONTEXT;

	protected Map<String, Object> config = null;

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
	 * Configure a given event sink based on default settings
	 *
	 * @param sink event sink
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
		sink.setTTL(ttl);
		sink.setLimiter(limiter);
		return sink;
	}


	@Override
	public long getTTL() {
		return ttl;
	}
	
	@Override
	public void setTTL(long ttl) {
		this.ttl = ttl;
	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> props) throws ConfigException {
		config = props;
		setTTL(Utils.getLong("TTL", props, getTTL()));
		double maxmps = Utils.getDouble("RateMaxMPS", props, Limiter.MAX_RATE);
		double maxbps = Utils.getDouble("RateMaxBPS", props, Limiter.MAX_RATE);
		boolean enabled = Utils.getBoolean("RateLimit", props, false);
		long timeout = Utils.getLong("RateTimeout", props, EventLimiter.BLOCK_UNTIL_GRANTED);
		if (enabled) {
			limiter = newEventLimiterImpl(maxmps, maxbps, enabled, timeout);
		}
		eventFilter = (SinkEventFilter) Utils.createConfigurableObject("Filter", "Filter.", config);
		errorListener = (SinkErrorListener) Utils.createConfigurableObject("ErrorListener", "ErrorListener.", config);
		eventListener = (SinkLogEventListener) Utils.createConfigurableObject("EventListener", "EventListener.", config);
	}
	
	protected EventLimiter newEventLimiterImpl(double maxmps, double maxbps, boolean enabled, long timeout) {
		EventLimiter eLimit = new EventLimiter(DefaultLimiterFactory.getInstance().newLimiter(maxmps, maxbps, enabled), timeout, TimeUnit.MILLISECONDS);
		return eLimit;
	}
}
