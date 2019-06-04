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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.LoggedEventSink;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements {@link EventSink} with socket as the underlying sink implementation.
 * </p>
 *
 *
 * @version $Revision: 16 $
 *
 * @see TrackingActivity
 * @see TrackingEvent
 * @see OpLevel
 * @see EventSink
 * @see EventFormatter
 */
public class SocketEventSink extends LoggedEventSink {
	private Socket socketSink = null;
	private DataOutputStream outStream = null;
	private String hostName = "localhost";
	private int portNo = 6400;

	/**
	 * Create a socket event sink based on a given host, port and formatter. Another sink can be associated with this
	 * sink where all events are routed.
	 *
	 * @param name
	 *            logical name assigned to this sink
	 * @param host
	 *            name where all messages are sent
	 * @param port
	 *            number where all messages are sent
	 * @param frm
	 *            event formatter associated with this sink
	 * @param sink
	 *            piped sink where all events are piped
	 */
	public SocketEventSink(String name, String host, int port, EventFormatter frm, EventSink sink) {
		super(name, frm, sink);
		hostName = host;
		portNo = port;
	}

	@Override
	public Object getSinkHandle() {
		return socketSink;
	}

	@Override
	public boolean isOpen() {
		return socketSink != null && socketSink.isConnected();// && outStream != null;
	}

	@Override
	protected synchronized void _open() throws IOException {
		socketSink = new Socket(hostName, portNo);
		outStream = new DataOutputStream(socketSink.getOutputStream());
		super._open();
	}

	@Override
	protected synchronized void _close() throws IOException {
		try {
			if (isOpen()) {
				outStream.close();
				socketSink.close();
			}
		} finally {
			outStream = null;
			socketSink = null;
			super._close();
		}
	}

	@Override
	public synchronized void flush() throws IOException {
		if (isOpen()) {
			outStream.flush();
		}
	}

	@Override
	public String toString() {
		return super.toString() + "{host: " + hostName + ", port: " + portNo + ", socket: " + socketSink
				+ ", formatter: " + getEventFormatter() + "}";
	}

	@Override
	protected void writeLine(String msg) throws IOException {
		writeLine(msg, false);
	}

	private synchronized void writeLine(String msg, boolean retrying) throws IOException {
		if (Utils.isEmpty(msg)) {
			return;
		}

		_checkState();

		try {
			byte[] bytes = msg.getBytes();
			incrementBytesSent(bytes.length);
			outStream.write(bytes, 0, bytes.length);
			if (!msg.endsWith("\n")) {
				outStream.write('\n');
			}
			outStream.flush();
		} catch (IOException e) {
			if (retrying) {
				throw e;
			} else {
				retryWrite(msg, e);
			}
		}
	}

	private void retryWrite(String msg, Throwable e) throws IOException {
		try {
			reopen();
			writeLine(msg, true);
		} catch (IOException ioe) {
			if (e != null) {
				ioe.initCause(e);
			}
			throw ioe;
		}
	}

	@Override
	protected void _checkState() throws IllegalStateException {
		if (!isOpen()) {
			throw new IllegalStateException("Sink closed: " + hostName + ":" + this.portNo + ", socket=" + socketSink);
		}
	}
}
