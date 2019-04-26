/*
 * Copyright 2014-2018 JKOOL, LLC.
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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.format.SimpleFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Concrete implementation of {@link EventSinkFactory} interface, which creates instances of {@link EventSink}. This
 * factory uses {@link FileEventSink} as the underlying sink provider provider and by default uses
 * {@link SimpleFormatter} to format log messages.
 * </p>
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
	public static final String FILE_SINK_FATORY_DEF_FILE = System.getProperty("tnt4j.file.event.sink.factory.file");
	public static final String FILE_SINK_FATORY_LOG_EXT = System.getProperty("tnt4j.file.event.sink.factory.logext", ".log");
	public static final String FILE_SINK_FATORY_DEF_FOLDER = System.getProperty("tnt4j.file.event.sink.factory.folder", "." + File.separator);
	
	boolean append = true;
	String fileName = FILE_SINK_FATORY_DEF_FILE;
	String logFolder = FILE_SINK_FATORY_DEF_FOLDER;

	/**
	 * Create a default sink factory with default file name based on current timestamp: yyyy-MM-dd.log.
	 */
	public FileEventSinkFactory() {
	}

	/**
	 * Create a sink factory with a given file name.
	 * 
	 * @param fname
	 *            file name
	 */
	public FileEventSinkFactory(String fname) {
		fileName = fname;
	}

	/**
	 * Create a sink factory with a given file name.
	 * 
	 * @param folder
	 *            directory where all files are created
	 * @param fname
	 *            file name
	 */
	public FileEventSinkFactory(String folder, String fname) {
		fileName = fname;
		setFolder(folder);
	}

	public void setFolder(String folder) {
		logFolder = folder;
		if (!logFolder.endsWith(File.separator)) {
			logFolder += File.separator;
		}		
	}
	
	@Override
	public EventSink getEventSink(String name) {
		return getEventSink(name, System.getProperties());
	}

	@Override
	public EventSink getEventSink(String name, Properties props) {
		return getEventSink(name, props, new SimpleFormatter("{0} | {1} | {2}"));
	}

	@Override
	public EventSink getEventSink(String name, Properties props, EventFormatter frmt) {
		String fname = (fileName != null)? fileName: name;
		return configureSink(new FileEventSink(name, logFolder + fname + FILE_SINK_FATORY_LOG_EXT, append, frmt));
	}

	@Override
	protected EventSink configureSink(EventSink sink) {
		super.configureSink(sink);
		try {
			sink.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sink;
	}

	@Override
	public void setConfiguration(Map<String, ?> props) throws ConfigException {
		super.setConfiguration(props);
		fileName = Utils.getString("FileName", props, fileName);
		setFolder(Utils.getString("Folder", props, logFolder));
		append = Utils.getBoolean("Append", props, append);
	}
}
