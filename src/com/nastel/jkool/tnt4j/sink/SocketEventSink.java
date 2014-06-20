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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * This class implements <code>EventSink</code> with socket as  the underlying
 * sink implementation.
 * </p>
 * 
 * 
 * @version $Revision: 14 $
 * 
 * @see TrackingActivity
 * @see TrackingEvent
 * @see OpLevel
 * @see EventSink
 * @see EventFormatter
 */
public class SocketEventSink extends DefaultEventSink {
	private Socket socketSink = null;
	private DataOutputStream outStream = null;
	private EventFormatter formatter = null;
	private EventSink logSink = null;
	private String hostName = "localhost";
	private int portNo = 6400;

	/**
	 * Create a socket event sink based on a given host, port and formatter.
	 * Another sink can be associated with this sink where all events are routed.
	 * 
	 * @param host name where all messages are sent
	 * @param port number where all messages are sent
	 * @param frm event formatter associated with this sink
	 * @param sink piped sink where all events are piped
	 * 
	 */
	public SocketEventSink(String host, int port, EventFormatter frm, EventSink sink) {
		hostName = host;
		portNo = port;
		formatter = frm;
		logSink = sink;
	}

	@Override
	public void log(TrackingActivity activity) {
		if (!acceptEvent(activity)) return;
		
		if (logSink != null) {
			logSink.log(activity);
		}
		if (isOpen()) {
			writeLine(formatter.format(activity));
			super.log(activity);
		}
	}

	@Override
	public void log(TrackingEvent event) {
		if (!acceptEvent(event)) return;
		
		if (logSink != null) {
			logSink.log(event);
		}
		if (isOpen()) {
			writeLine(formatter.format(event));
			super.log(event);
		}
	}

	@Override
	public void log(OpLevel sev, String msg, Object...args) {
		if (!acceptEvent(sev, msg)) return;

		if (logSink != null) {
			logSink.log(sev, msg, args);
		}
		if (isOpen()) {
			writeLine(formatter.format(sev, msg, args));
			super.log(sev, msg, args);
		}
	}

	@Override
	public void write(Object msg, Object...args) throws IOException {
		if (isOpen()) {
			writeLine(formatter.format(msg, args));
		}
	}

	@Override
	public Object getSinkHandle() {
		return socketSink;
	}

	@Override
	public boolean isOpen() {
		return socketSink == null ? false : socketSink.isConnected();
	}

	@Override
	public void open() throws IOException {
		socketSink = new Socket(hostName, portNo);
		outStream = new DataOutputStream(socketSink.getOutputStream());
		if (logSink != null) {
			logSink.open();
		}
	}
	
	@Override
	public void close() throws IOException {
		try {
			if (isOpen()) {
				if (logSink != null) {
					logSink.close();
				}
				outStream.close();
				socketSink.close();
			}
		} finally {
			outStream = null;
			socketSink = null;
		}
	}	
	
	@Override
	public String toString() {
		return super.toString() 
			+ "{host: " + hostName 
			+ ", port: " + portNo 
			+ ", socket: " + socketSink 
			+ ", formatter: " + formatter 
			+ ", piped.sink: " + logSink 
			+ "}";
	}
	
	private void writeLine(String msg) {
		try {
			String lineMsg = msg.endsWith("\n")? msg: msg + "\n";
			byte [] bytes = lineMsg.getBytes();
			outStream.write(bytes, 0, bytes.length);
			outStream.flush();
		} catch (Throwable io) {
			super.notifyListeners(msg, io);
		} 
	}

	@Override
    public boolean isSet(OpLevel sev) {
	    return logSink != null? logSink.isSet(sev): true;
    }
}
