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

import java.util.Map;
import java.util.Properties;

import com.nastel.jkool.tnt4j.config.ConfigException;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.format.JSONFormatter;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>Concrete implementation of {@link EventSinkFactory} interface, which
 * creates instances of {@link EventSink}. This factory uses {@link SocketEventSink}
 * as the underlying sink provider provider and by default uses {@link JSONFormatter} to 
 * format log messages.</p>
 *
 *
 * @see EventSink
 * @see EventFormatter
 * @see JSONFormatter
 *
 * @version $Revision: 5 $
 *
 */
public class SocketEventSinkFactory  extends AbstractEventSinkFactory  {
	private String hostName = System.getProperty("tnt4j.sink.factory.socket.host", "localhost");
	private int port = Integer.getInteger("tnt4j.sink.factory.socket.port", 6400);
	
	private EventSinkFactory eventSinkFactory = DefaultEventSinkFactory.getInstance();
	/**
	 * Create a socket event sink factory.
	 * Same as <code>SocketEventSinkFactory("localhost", 6400)</code>.
	 * 
	 */
	public SocketEventSinkFactory() {
	}
	
	/**
	 * Create a socket event sink factory with
	 * host name set to localhost.
	 * 
	 * @param portNo port number 
	 * 
	 */
	public SocketEventSinkFactory(int portNo) {
		port = portNo;
	}
	
	/**
	 * Create a socket event sink factory with
	 * 
	 * @param host host name used to connect to
	 * @param portNo port number 
	 * 
	 */
	public SocketEventSinkFactory(String host, int portNo) {
		hostName = host;
		port = portNo;
	}
	
	@Override
    public EventSink getEventSink(String name) {
	    return configureSink(new SocketEventSink(name, hostName, port, new JSONFormatter(false), 
	    		eventSinkFactory.getEventSink(name, System.getProperties(), new JSONFormatter())));
    }

	@Override
    public EventSink getEventSink(String name, Properties props) {
	    return configureSink(new SocketEventSink(name, hostName, port, new JSONFormatter(false),
	    		eventSinkFactory.getEventSink(name, props, new JSONFormatter())));
    }

	@Override
    public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
	    return configureSink(new SocketEventSink(name, hostName, port, frmt, 
	    		eventSinkFactory.getEventSink(name, props, new JSONFormatter())));
    }

	/**
	 * Obtain an instance of {@link EventSink} by name and 
	 * custom properties
	 *
	 * @param name name of the category associated with the event log
	 * @param props properties associated with the event logger (implementation specific).
	 * @param frmt event formatter object to format events before writing to log
	 * @param pipedSink piped sink used to route events
	 * 
	 * @return event sink instance with specified arguments
	 * @see EventSink
	 * @see EventFormatter
	 */
    public EventSink getEventSink(String name, Properties props, EventFormatter frmt, EventSink pipedSink) {
	    return configureSink(new SocketEventSink(name, hostName, port, frmt, pipedSink));
    }

	@Override
    public void setConfiguration(Map<String, Object> settings) throws ConfigException {
		super.setConfiguration(settings);
		hostName = settings.get("Host") != null? settings.get("Host").toString(): hostName;
		port = settings.get("Port") != null? Integer.parseInt(settings.get("Port").toString()): port;
		eventSinkFactory = (EventSinkFactory) Utils.createConfigurableObject("eventSinkFactory", 
					"eventSinkFactory.", settings);
		eventSinkFactory = eventSinkFactory == null? DefaultEventSinkFactory.getInstance(): eventSinkFactory;
    }
}
