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
package com.nastel.jkool.tnt4j.sink;

import java.util.Map;

import com.nastel.jkool.tnt4j.config.Configurable;
import com.nastel.jkool.tnt4j.config.ConfigurationException;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>Abstract event sink factory class that all sink factories should subclass from.
 * Derived classes should call <code>configure()</code> before returning <code>EventSink</code>
 * instances back using <code>getEventSink()</code> method calls. See example below:
 * </p>
 *<pre>
 *{@code
 *	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
 *		return configureSink(new MyEventSinkImpl(name, props, frmt));
 *	}
 *}
 *</pre>
 *
 * @see EventSink
 * @see Configurable
 * 
 * @version $Revision: 1 $
 *
 */

abstract public class AbstractEventSinkFactory implements EventSinkFactory, Configurable {
	private SinkEventFilter eventFilter = null;
	private SinkErrorListener errorListener = null;
	private SinkLogEventListener eventListener = null;

	protected Map<String, Object> config = null;

	/**
	 * Obtain the default instance of <code>SinkEventFilter</code> configured for this factory.
	 * 
	 * @return default sink event filter instance
	 */
	public SinkEventFilter getDefaultEventFilter() {
		return eventFilter;
	}

	/**
	 * Configure a given event sink based on default settings
	 * 
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
		return sink;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> props) throws ConfigurationException {
		config = props;
		eventFilter = (SinkEventFilter) Utils.createConfigurableObject("Filter", "Filter.", config);
		errorListener = (SinkErrorListener) Utils.createConfigurableObject("ErrorListener", "ErrorListener.", config);
		eventListener = (SinkLogEventListener) Utils.createConfigurableObject("EventListener", "EventListener.", config);
	}
}
