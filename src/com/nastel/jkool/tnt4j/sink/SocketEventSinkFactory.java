/*
 * Copyright (c) 2013 Nastel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Nastel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with Nastel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *
 */
package com.nastel.jkool.tnt4j.sink;

import java.util.Map;
import java.util.Properties;

import com.nastel.jkool.tnt4j.config.Configurable;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.format.JSONFormatter;
import com.nastel.jkool.tnt4j.selector.DefaultTrackingSelector;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>Concrete implementation of <code>EventSinkFactory</code> interface, which
 * creates instances of <code>EventSink</code>. This factory uses <code>SocketEventSink</code>
 * as the underlying sink provider provider and by default uses <code>JSONFormatter</code> to 
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
public class SocketEventSinkFactory implements EventSinkFactory, Configurable {
	private static EventSink logger = DefaultEventSinkFactory.defaultEventSink(DefaultTrackingSelector.class);
	private String hostName = System.getProperty("tnt4j.sink.factory.socket.host", "localhost");
	private int port = Integer.getInteger("tnt4j.sink.factory.socket.port", 6400);
	
	private Map<String, Object> config = null;
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
	    return new SocketEventSink(hostName, port, new JSONFormatter(false), 
	    		eventSinkFactory.getEventSink(name, System.getProperties(), new JSONFormatter()));
    }

	@Override
    public EventSink getEventSink(String name, Properties props) {
	    return new SocketEventSink(hostName, port, new JSONFormatter(false),
	    		eventSinkFactory.getEventSink(name, props, new JSONFormatter()));
    }

	@Override
    public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
	    return new SocketEventSink(hostName, port, frmt, 
	    		eventSinkFactory.getEventSink(name, props, new JSONFormatter()));
    }

	/**
	 * Obtain an instance of <code>EventSink</code> by name and 
	 * custom properties
	 *
	 * @param name name of the category associated with the event log
	 * @param props properties associated with the event logger (implementation specific).
	 * @param frmt event formatter object to format events before writing to log
	 * @param pipedSink piped sink used to route events
	 * 
	 * @see EventSink
	 * @see EventFormatter
	 */
    public EventSink getEventSink(String name, Properties props, EventFormatter frmt, EventSink pipedSink) {
	    return new SocketEventSink(hostName, port, frmt, pipedSink);
    }

	@Override
    public Map<String, Object> getConfiguration() {
	    return config;
    }

	@Override
    public void setConfiguration(Map<String, Object> settings) {
		config = settings;
		hostName = config.get("Host") != null? config.get("Host").toString(): hostName;
		port = config.get("Port") != null? Integer.parseInt(config.get("Port").toString()): port;
		try {
			eventSinkFactory = (EventSinkFactory) Utils.createConfigurableObject("eventSinkFactory", 
					"eventSinkFactory.", config);
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, "Unable to process settings=" + settings, e);
		}
		eventSinkFactory = eventSinkFactory == null? DefaultEventSinkFactory.getInstance(): eventSinkFactory;
    }
}
