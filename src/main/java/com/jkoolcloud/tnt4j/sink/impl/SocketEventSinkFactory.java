/*
 * Copyright 2014-2019 JKOOL, LLC.
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

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.format.JSONFormatter;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.sink.LoggedEventSinkFactory;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Concrete implementation of {@link EventSinkFactory} interface, which creates instances of {@link EventSink}. This
 * factory uses {@link SocketEventSink} as the underlying sink provider provider and by default uses
 * {@link JSONFormatter} to format log messages.
 * </p>
 *
 *
 * @see EventSink
 * @see EventFormatter
 * @see JSONFormatter
 *
 * @version $Revision: 6 $
 *
 */
public class SocketEventSinkFactory extends LoggedEventSinkFactory {
	private String hostName = System.getProperty("tnt4j.sink.factory.socket.host", "localhost");
	private int port = Integer.getInteger("tnt4j.sink.factory.socket.port", 6400);
	private String proxyHost;
	private int proxyPort = 0;
	private String proxyUser;
	private String proxyPass;

	/**
	 * Create a socket event sink factory. Same as {@code SocketEventSinkFactory("localhost", 6400)}.
	 * 
	 */
	public SocketEventSinkFactory() {
	}

	/**
	 * Create a socket event sink factory with host name set to localhost.
	 * 
	 * @param portNo
	 *            port number
	 * 
	 */
	public SocketEventSinkFactory(int portNo) {
		port = portNo;
	}

	/**
	 * Create a socket event sink factory with
	 * 
	 * @param host
	 *            host name used to connect to
	 * @param portNo
	 *            port number
	 * 
	 */
	public SocketEventSinkFactory(String host, int portNo) {
		hostName = host;
		port = portNo;
	}

	@Override
	public EventSink getEventSink(String name) {
		return getEventSink(name, System.getProperties());
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new SocketEventSink(name, hostName, port, proxyHost, proxyPort, new JSONFormatter(false),
				getLogSink(name, props, new JSONFormatter())));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return getEventSink(name, props, frmt, getLogSink(name, props, frmt));
	}

	/**
	 * Obtain an instance of {@link EventSink} by name and custom properties
	 *
	 * @param name
	 *            name of the category associated with the event log
	 * @param props
	 *            properties associated with the event logger (implementation specific).
	 * @param frmt
	 *            event formatter object to format events before writing to log
	 * @param pipedSink
	 *            piped sink used to route events
	 * 
	 * @return event sink instance with specified arguments
	 * @see EventSink
	 * @see EventFormatter
	 */
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt, EventSink pipedSink) {
		return configureSink(new SocketEventSink(name, hostName, port, proxyHost, proxyPort, frmt, pipedSink));
	}

	@Override
	protected EventSink configureSink(EventSink sink) {
		if (StringUtils.isNotEmpty(proxyUser)) {
			if (System.getProperty("java.net.socks.username") == null) {
				System.setProperty("java.net.socks.username", proxyUser);
			}
			if (System.getProperty("java.net.socks.password") == null) {
				System.setProperty("java.net.socks.password", proxyPass);
			}
		}

		return super.configureSink(sink);
	}

	@Override
	public void setConfiguration(Map<String, ?> settings) throws ConfigException {
		super.setConfiguration(settings);

		hostName = Utils.getString("Host", settings, hostName);
		port = Utils.getInt("Port", settings, port);
		proxyHost = Utils.getString("ProxyHost", settings, proxyHost);
		proxyPort = Utils.getInt("ProxyPort", settings, proxyPort);
		proxyUser = Utils.getString("ProxyUser", settings, proxyUser);
		proxyPass = Utils.getString("ProxyPass", settings, proxyPass);
	}

	@Override
	protected boolean doInitDefaultLogger() {
		return true;
	}
}
