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
package com.jkoolcloud.tnt4j.mqtt;

import java.util.Map;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.format.JSONFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>Concrete implementation of {@link EventSinkFactory} interface over MQTT, which
 * creates instances of {@link EventSink}. This factory uses {@link MqttEventSink}
 * as the underlying provider.</p>
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
	 * MQTT message retention
	 */
	boolean retainMsg = false; 
	
	/**
	 * MQTT connection options
	 */
	MqttConnectOptions options;
	
	@Override
    public EventSink getEventSink(String name) {
	    return new MqttEventSink(this, name, null, new JSONFormatter(false));
    }

	@Override
    public EventSink getEventSink(String name, Properties props) {
	    return new MqttEventSink(this, name, props, new JSONFormatter(false));
    }

	@Override
    public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
	    return new MqttEventSink(this, name, props, frmt);
    }

	@Override
    public void setConfiguration(Map<String, Object> settings) throws ConfigException {
		super.setConfiguration(settings);
		serverURI = Utils.getString("mqtt-server-url", settings, "tcp://localhost:1883");
		topic = Utils.getString("mqtt-topic", settings, "tnt4j/eventStream");
		clientid = Utils.getString("mqtt-clientid", settings, MqttClient.generateClientId());
		version = Utils.getInt("mqtt-version", settings, MqttConnectOptions.MQTT_VERSION_DEFAULT);
		userName = Utils.getString("mqtt-user", settings, userName);
		userPwd = Utils.getString("mqtt-pwd", settings, userPwd);
		keepAlive = Utils.getInt("mqtt-keepalive", settings, keepAlive);
		connTimeout = Utils.getInt("mqtt-timeout", settings, connTimeout);
		cleanSession = Utils.getBoolean("mqtt-clean-session", settings, cleanSession);

		// message attributes
		qos = Utils.getInt("mqtt-qos", settings, qos);
		retainMsg = Utils.getBoolean("mqtt-retain", settings, retainMsg);

		options = new MqttConnectOptions();
		Properties connProps = new Properties();
		connProps.putAll(settings);
		options.setSSLProperties(connProps);
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
	 */
	public MqttClient newMqttClient() throws MqttException {
	    MqttClient client = new MqttClient(serverURI, clientid, new MemoryPersistence());
	    client.connect(options);
	    return client;
    }

	/**
	 * Create a new MQTT message with specific contents
	 * 
	 * @return new MQTT message with specific contents
	 */
	public MqttMessage newMqttMessage(byte[] bytes) {
	    MqttMessage msg = new MqttMessage(bytes);
	    msg.setRetained(retainMsg);
	    msg.setQos(qos);
	    return msg;
    }
	
	/**
	 * Publish message to a given MQTT client
	 * 
	 * @param client MQTT client
	 * @param msg MQTT message instance
	 * 
	 * @return new MQTT message with specific contents
	 */
	public void publish(MqttClient client, MqttMessage msg) throws MqttPersistenceException, MqttException {
		client.publish(topic, msg);
	}
}
