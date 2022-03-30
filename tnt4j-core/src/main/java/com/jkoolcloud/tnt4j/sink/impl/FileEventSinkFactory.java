/*
 * Copyright 2014-2021 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.sink.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
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
	private static final String WORK_DIR = "." + File.separator;

	public static final String TMP_DIR = System.getProperty("java.io.tmpdir", WORK_DIR);
	public static final String FILE_SINK_FACTORY_DEF_FILE = System.getProperty("tnt4j.file.event.sink.factory.file");
	public static final String FILE_SINK_FACTORY_LOG_EXT = System.getProperty("tnt4j.file.event.sink.factory.logext",
			".log");
	public static final String FILE_SINK_FACTORY_DEF_FOLDER;

	static {
		Path defLogPath = FileSystems.getDefault().getPath(TMP_DIR, Utils.getVMName());
		FILE_SINK_FACTORY_DEF_FOLDER = System.getProperty("tnt4j.file.event.sink.factory.folder",
				defLogPath.toString());
	}

	protected boolean append = true;
	protected String fileName = FILE_SINK_FACTORY_DEF_FILE;
	protected String logFolder = FILE_SINK_FACTORY_DEF_FOLDER;

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

	public FileEventSinkFactory setFolder(String folder) {
		logFolder = folder;
		return this;
	}

	public FileEventSinkFactory setFileName(String fn) {
		fileName = fn;
		return this;
	}

	public FileEventSinkFactory setAppend(boolean flag) {
		append = flag;
		return this;
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
		String fname = (fileName != null) ? fileName : (name + FILE_SINK_FACTORY_LOG_EXT);
		fname = FileSystems.getDefault().getPath(logFolder, fname).toString();
		return configureSink(new FileEventSink(name, fname, append, frmt));
	}

	@Override
	protected EventSink configureSink(EventSink sink) {
		super.configureSink(sink);

		return sink;
	}

	@Override
	public void setConfiguration(Map<String, ?> props) throws ConfigException {
		super.setConfiguration(props);

		String fName = Utils.getString("FileName", props, null);
		String folder = Utils.getString("Folder", props, null);
		if (fName != null) {
			File f = new File(fName);
			fName = f.getName();
			if (f.isAbsolute()) {
				folder = f.getParent();
			} else {
				if (folder == null) {
					try {
						folder = f.getCanonicalFile().getAbsoluteFile().getParent();
					} catch (IOException | SecurityException exc) {
						folder = f.getAbsoluteFile().getParent();
					}
				}
			}
		}

		setFileName(fName == null ? fileName : fName);
		setFolder(folder == null ? logFolder : folder);
		setAppend(Utils.getBoolean("Append", props, append));
	}
}
