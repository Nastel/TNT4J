/*
 * Copyright 2018 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.sink.impl;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.utils.Utils;



/**
 * Broadcasting event sink factory allows creation of event sinks that can write to multiple event sinks
 * at once. The factory wraps around multiple event sink factories.
 *  
 * @author albert
 *
 */
public class BroadcastingEventSinkFactory extends AbstractEventSinkFactory {

	List<EventSinkFactory> sinkFactories = new Vector<EventSinkFactory>(3, 3);
	
	/**
	 * Create a default broadcasting sink factory
	 *
	 */
	public BroadcastingEventSinkFactory() {
	}

	/**
	 * Create a default broadcasting sink factory
	 * 
	 * @param sf list of event sink factories
	 *
	 */
	public BroadcastingEventSinkFactory(List<EventSinkFactory> sf) {
		sinkFactories.addAll(sf);
	}

	protected List<EventSinkFactory> getEventSinkFactories() {
		return sinkFactories;
	}
	
	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new BroadcastingEventSink(this, name));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new BroadcastingEventSink(this, name, props));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new BroadcastingEventSink(this, name, props, frmt));
	}

	@Override
	public void setConfiguration(Map<String, ?> props) throws ConfigException {
		super.setConfiguration(props);
		
		int counter = 0;
		EventSinkFactory sinkFactory = null;
		do  {
			sinkFactory = (EventSinkFactory) Utils.createConfigurableObject("EventSinkFactory." + counter, "EventSinkFactory." + counter + ".", props);
			if (sinkFactory != null) sinkFactories.add(sinkFactory);
			counter++;
		} while (sinkFactory != null);
	}
}
