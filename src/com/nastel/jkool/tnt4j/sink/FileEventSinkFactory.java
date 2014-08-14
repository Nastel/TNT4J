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

import java.util.Map;
import java.util.Properties;

import com.nastel.jkool.tnt4j.config.ConfigurationException;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.format.SimpleFormatter;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>Concrete implementation of <code>EventSinkFactory</code> interface, which
 * creates instances of <code>EventSink</code>. This factory uses <code>FileEventSink</code>
 * as the underlying sink provider provider and by default uses <code>SimpleFormatter</code> to 
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

	boolean append = false;;
	String fileName = Utils.getVMName() + ".log";
	
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
	public void setConfiguration(Map<String, Object> props) throws ConfigurationException {
		fileName = props.get("FileName") == null? fileName: props.get("FileName").toString();
		
		Object flag  = props.get("Append");
		append = flag == null? append: Boolean.valueOf(flag.toString());
		super.setConfiguration(props);
	}
}
