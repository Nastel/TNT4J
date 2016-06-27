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
package com.jkoolcloud.tnt4j.sink;

import java.util.Map;
import java.util.Properties;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.core.UsecTimestamp;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.format.SimpleFormatter;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>Concrete implementation of {@link EventSinkFactory} interface, which
 * creates instances of {@link EventSink}. This factory uses {@link FileEventSink}
 * as the underlying sink provider provider and by default uses {@link SimpleFormatter} to 
 * format log messages.</p>
 *
 *
 * @see EventSink
 * @see SimpleFormatter
 * @see FileEventSink
 *
 * @version $Revision: 1 $
 *
 */
public class FileEventSinkFactory extends AbstractEventSinkFactory {

	boolean append = true;;
	String fileName = UsecTimestamp.getTimeStamp("yyyy-MM-dd") + ".log";
	
	/**
	 * Create a default sink factory with default file name based on
	 * current timestamp: yyyy-MM-dd.log.
	 */
	public FileEventSinkFactory() {
	}
	
	/**
	 * Create a sink factory with a given file name.
	 * 
	 * @param fname file name
	 */
	public FileEventSinkFactory(String fname) {
		fileName = fname;
	}
	
	@Override
	public EventSink getEventSink(String name) {
		return configureSink(new FileEventSink(name, fileName, append, new SimpleFormatter("{0} | {1} | {2}")));
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return configureSink(new FileEventSink(name, fileName, append, new SimpleFormatter("{0} | {1} | {2}")));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		return configureSink(new FileEventSink(name, fileName, append, frmt));
	}

	@Override
	public void setConfiguration(Map<String, Object> props) throws ConfigException {
		fileName = Utils.getString("FileName", props, fileName);		
		append = Utils.getBoolean("Append", props, append);
		super.setConfiguration(props);
	}
}
