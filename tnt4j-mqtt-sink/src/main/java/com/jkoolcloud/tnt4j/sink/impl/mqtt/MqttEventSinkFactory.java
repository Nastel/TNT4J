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

import java.util.Map;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.format.JSONFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Concrete implementation of {@link EventSinkFactory} interface over MQTT, which creates instances of
 * {@link EventSink}. This factory uses {@link MqttEventSink} as the underlying provider.
 * </p>
 *
 *
 * @see EventSink
 * @see MqttEventSink
 *
 * @version $Revision: 1 $
 *
 */
public class MqttEventSinkFactory extends AbstractEventSinkFactory {

	/**
	 * MQTT server URL
	 */
	String serverURI;

	/**
	 * MQTT client id
	 */
	String clientid;

	/**
	 * MQTT user name
	 */
	String userName;

	/**
	 * MQTT user password
	 */
	String userPwd;

	/**
	 * MQTT topic
	 */
	String topic;

	/**
	 * MQTT version
	 */
	int version = MqttConnectOptions.MQTT_VERSION_DEFAULT;

	/**
	 * MQTT quality of service
	 */
	int qos = 1;

	/**
	 * MQTT keep alive interval in seconds
	 */
	int keepAlive = 60;

	/**
	 * MQTT connection timeout in seconds
	 */
	int connTimeout = 30;

	/**
	 * MQTT connection clean session flag
	 */
	boolean cleanSession = true;

	/**
	 * MQTT enable SSL
	 */
	boolean ssl = false;

	/**
	 * MQTT message retention
	 */
	boolean retainMsg = false;

	/**
	 * MQTT connection options
	 */
	MqttConnectOptions options = new MqttConnectOptions();

	@Override
	public EventSink getEventSink(String name) {
		return getEventSink(name, null);
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return getEventSink(name, props, new JSONFormatter(false));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new MqttEventSink(this, name, props, frmt));
	}

	@Override
	public void setConfiguration(Map<String, ?> settings) throws ConfigException {
		super.setConfiguration(settings);
		serverURI = Utils.getString("mqtt-server-url", settings, "tcp://localhost:1883");
		clientid = Utils.getString("mqtt-clientid", settings, MqttClient.generateClientId());
		version = Utils.getInt("mqtt-version", settings, MqttConnectOptions.MQTT_VERSION_DEFAULT);
		topic = Utils.getString("mqtt-topic", settings, topic);
		userName = Utils.getString("mqtt-user", settings, userName);
		userPwd = Utils.getString("mqtt-pwd", settings, userPwd);
		keepAlive = Utils.getInt("mqtt-keepalive", settings, keepAlive);
		connTimeout = Utils.getInt("mqtt-timeout", settings, connTimeout);
		cleanSession = Utils.getBoolean("mqtt-clean-session", settings, cleanSession);
		ssl = Utils.getBoolean("mqtt-ssl", settings, ssl);

		// message attributes
		qos = Utils.getInt("mqtt-qos", settings, qos);
		retainMsg = Utils.getBoolean("mqtt-retain", settings, retainMsg);

		if (ssl) {
			Properties connProps = new Properties();
			connProps.putAll(settings);
			options.setSSLProperties(connProps);
		}
		if (userName != null) {
			options.setUserName(userName);
		}
		if (userPwd != null) {
			options.setPassword(userPwd.toCharArray());
		}
		options.setKeepAliveInterval(keepAlive);
		options.setConnectionTimeout(connTimeout);
		options.setMqttVersion(version);
		options.setCleanSession(cleanSession);
	}

	/**
	 * Create and connect MQTT client
	 * 
	 * @return MQTT client instance, connected
	 *
	 * @throws org.eclipse.paho.client.mqttv3.MqttException
	 *             when server communication or security error occurs
	 */
	public MqttClient newMqttClient() throws MqttException {
		MqttClient client = new MqttClient(serverURI, clientid, new MemoryPersistence());
		client.connect(options);
		return client;
	}

	/**
	 * Create a new MQTT message with specific contents
	 * 
	 * @param bytes
	 *            message contents
	 * @return new MQTT message with specific contents
	 */
	public MqttMessage newMqttMessage(byte[] bytes) {
		MqttMessage msg = new MqttMessage(bytes);
		msg.setRetained(retainMsg);
		msg.setQos(qos);
		return msg;
	}

	/**
	 * Create a new MQTT message with specific contents
	 * 
	 * @param contents
	 *            message contents
	 * @return new MQTT message with specific contents
	 */
	public MqttMessage newMqttMessage(String contents) {
		return newMqttMessage(contents.getBytes());
	}

	/**
	 * Publish message to a given MQTT client
	 * 
	 * @param evSink
	 *            event sink
	 * @param client
	 *            MQTT client
	 * @param msg
	 *            MQTT message instance
	 *
	 * @throws org.eclipse.paho.client.mqttv3.MqttPersistenceException
	 *             when a problem with storing the message
	 * @throws org.eclipse.paho.client.mqttv3.MqttException
	 *             for other errors encountered while publishing the message. For instance client not connected
	 */
	public void publish(EventSink evSink, MqttClient client, MqttMessage msg)
			throws MqttPersistenceException, MqttException {
		String mqttTopic = (topic == null ? evSink.getName() : topic);
		client.publish(mqttTopic, msg);
	}
}
