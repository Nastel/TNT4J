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
package com.jkoolcloud.tnt4j.sink.impl.kafka;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.format.JSONFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * Concrete implementation of {@link EventSinkFactory} interface over Kafka, which creates instances of
 * {@link EventSink}. This factory uses {@link KafkaEventSink} as the underlying provider.
 *
 *
 * @see EventSink
 * @see KafkaEventSink
 *
 * @version $Revision: 1 $
 *
 */
public class KafkaEventSinkFactory extends AbstractEventSinkFactory {
	public static String DEFAULT_KAFKA_TOPIC = "tnt4j-topic";
	public static String DEFAULT_KAFKA_PROP_FILE = "tnt4j-kafka.properties";

	private Properties kafkaProps = new Properties();
	private String kafkaTopic = DEFAULT_KAFKA_TOPIC;
	private String kafkaPropFile = null;

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
		return configureSink(new KafkaEventSink(kafkaTopic, kafkaProps, frmt));
	}

	@Override
	public void setConfiguration(Map<String, ?> settings) throws ConfigException {
		super.setConfiguration(settings);
		kafkaTopic = Utils.getString("topic", settings, DEFAULT_KAFKA_TOPIC);
		kafkaPropFile = Utils.getString("propFile", settings, kafkaPropFile);
		if (kafkaPropFile != null) {
			loadKafkaProps(kafkaPropFile, settings);
		} else {
			Map<String, Object> pProps = new HashMap<>(settings);
			pProps.remove("topic");
			pProps.remove("propFile");

			kafkaProps.putAll(pProps);
		}
	}

	protected void loadKafkaProps(String fname, Map<String, ?> settings) throws ConfigException {
		try {
			kafkaProps.load(Files.newInputStream(new File(fname).toPath()));
		} catch (Throwable e) {
			ConfigException error = new ConfigException(e.toString(), settings);
			error.initCause(e);
			throw error;
		}
	}
}
