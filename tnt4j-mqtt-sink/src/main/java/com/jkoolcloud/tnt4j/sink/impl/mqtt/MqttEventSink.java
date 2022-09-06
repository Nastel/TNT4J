/*
 * Copyright 2014-2022 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.sink.impl.mqtt;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSink;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * This class implements {@link EventSink} with MQTT as the underlying sink implementation.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see OpLevel
 * @see EventFormatter
 * @see AbstractEventSink
 * @see MqttEventSinkFactory
 */
public class MqttEventSink extends AbstractEventSink {

	MqttClient mqttClient;
	MqttEventSinkFactory factory;

	/**
	 * Create MQTT event sink
	 * 
	 * @param fc
	 *            event sink factory
	 * @param name
	 *            event sink name
	 */
	protected MqttEventSink(MqttEventSinkFactory fc, String name) {
		super(name);
		factory = fc;
	}

	/**
	 * Create MQTT event sink
	 * 
	 * @param fc
	 *            event sink factory
	 * @param name
	 *            event sink name
	 * @param props
	 *            event sink properties
	 */
	protected MqttEventSink(MqttEventSinkFactory fc, String name, Properties props) {
		super(name);
		factory = fc;
	}

	/**
	 * Create MQTT event sink
	 * 
	 * @param fc
	 *            event sink factory
	 * @param name
	 *            event sink name
	 * @param props
	 *            event sink properties
	 * @param frmt
	 *            event sink formatter
	 */
	protected MqttEventSink(MqttEventSinkFactory fc, String name, Properties props, EventFormatter frmt) {
		super(name, frmt);
		factory = fc;
	}

	@Override
	public Object getSinkHandle() {
		return mqttClient;
	}

	@Override
	public boolean isOpen() {
		return mqttClient != null && mqttClient.isConnected();
	}

	@Override
	protected void _open() throws IOException {
		try {
			mqttClient = factory.newMqttClient();
		} catch (MqttException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void _close() throws IOException {
		if (mqttClient != null) {
			try {
				mqttClient.close();
				mqttClient.disconnect();
			} catch (MqttException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	protected void _log(TrackingEvent event) throws IOException {
		writeLine(getEventFormatter().format(event));
	}

	@Override
	protected void _log(TrackingActivity activity) throws IOException {
		writeLine(getEventFormatter().format(activity));
	}

	@Override
	protected void _log(Snapshot snapshot) throws IOException {
		writeLine(getEventFormatter().format(snapshot));
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) throws IOException {
		writeLine(getEventFormatter().format(ttl, src, sev, msg, args));
	}

	@Override
	protected void _write(Object msg, Object... args) throws IOException, InterruptedException {
		writeLine(getEventFormatter().format(msg, args));
	}

	private void writeLine(String msg) throws IOException {
		incrementBytesSent(msg.length());
		try {
			MqttMessage message = factory.newMqttMessage(msg);
			factory.publish(this, mqttClient, message);
		} catch (MqttException mqe) {
			throw new IOException(mqe);
		}
	}
}
