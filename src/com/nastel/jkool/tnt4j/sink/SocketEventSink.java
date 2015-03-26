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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.Snapshot;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.source.Source;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;
import com.nastel.jkool.tnt4j.utils.Utils;

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
public class SocketEventSink extends AbstractEventSink {
	private Socket socketSink = null;
	private DataOutputStream outStream = null;
	private EventSink logSink = null;
	private String hostName = "localhost";
	private int portNo = 6400;

	/**
	 * Create a socket event sink based on a given host, port and formatter.
	 * Another sink can be associated with this sink where all events are routed.
	 * 
	 * @param name logical name assigned to this sink
	 * @param host name where all messages are sent
	 * @param port number where all messages are sent
	 * @param frm event formatter associated with this sink
	 * @param sink piped sink where all events are piped
	 */
	public SocketEventSink(String name, String host, int port, EventFormatter frm, EventSink sink) {
		super(name, frm);
		hostName = host;
		portNo = port;
		logSink = sink;
	}

	@Override
	protected void _log(TrackingActivity activity) throws IOException {
		if (logSink != null) {
			logSink.log(activity);
		}
		writeLine(getEventFormatter().format(activity));
	}

	@Override
	protected void _log(TrackingEvent event) throws IOException {
		if (logSink != null) {
			logSink.log(event);
		}
		writeLine(getEventFormatter().format(event));
	}

	@Override
    protected void _log(Snapshot snapshot) throws IOException {
		if (logSink != null) {
			logSink.log(snapshot);
		}
		writeLine(getEventFormatter().format(snapshot));		
	}
	
	@Override
	protected void _log(Source src, OpLevel sev, String msg, Object...args)  throws IOException {
		if (logSink != null) {
			logSink.log(src, sev, msg, args);
		}
		writeLine(getEventFormatter().format(src, sev, msg, args));
	}

	@Override
	protected void _write(Object msg, Object...args) throws IOException {
		if (isOpen()) {
			writeLine(getEventFormatter().format(msg, args));
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
	public synchronized void open() throws IOException {
		socketSink = new Socket(hostName, portNo);
		outStream = new DataOutputStream(socketSink.getOutputStream());
		if (logSink != null) {
			logSink.open();
		}
	}
	
	@Override
	public synchronized void close() throws IOException {
		try {
			if (isOpen()) {
				Utils.close(logSink);
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
			+ ", formatter: " + getEventFormatter() 
			+ ", piped.sink: " + logSink 
			+ "}";
	}
	
	private synchronized void writeLine(String msg) throws IOException {
		String lineMsg = msg.endsWith("\n")? msg: msg + "\n";
		byte [] bytes = lineMsg.getBytes();
		outStream.write(bytes, 0, bytes.length);
		outStream.flush();
	}

	@Override
    public boolean isSet(OpLevel sev) {
	    return logSink != null? logSink.isSet(sev): true;
    }

	@Override
    protected void _checkState() throws IllegalStateException {
		if (!isOpen())
			throw new IllegalStateException("Sink closed: " + hostName + ":" + this.portNo + ", socket=" + socketSink);
    }
}
