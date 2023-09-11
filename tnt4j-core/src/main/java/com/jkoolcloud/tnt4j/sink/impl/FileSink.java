/*
 * Copyright 2014-2023 JKOOL, LLC.
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
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jkoolcloud.tnt4j.format.DefaultFormatter;
import com.jkoolcloud.tnt4j.format.Formatter;
import com.jkoolcloud.tnt4j.sink.Sink;

/**
 * <p>
 * This class implements {@link Sink} with file as the underlying storage
 * </p>
 * 
 * 
 * @version $Revision: 4 $
 * 
 * @see Sink
 * @see Formatter
 * @see DefaultFormatter
 */

public class FileSink implements Sink {

	protected File file = null;
	protected PrintStream printer = null;
	protected Formatter formatter = null;
	protected boolean append = true;

	/**
	 * Create a file based sink based on given filename.
	 * 
	 * @param filename
	 *            for generating a sink instance
	 */
	public FileSink(String filename) {
		this(filename, true);
	}

	/**
	 * Create a file based sink based on given filename, append flag.
	 * 
	 * @param filename
	 *            for generating a sink instance
	 * @param append
	 *            append to the underlying destination
	 */
	public FileSink(String filename, boolean append) {
		this(filename, append, new DefaultFormatter());
	}

	/**
	 * Create a file based sink based on given filename, append flag, and a given {@link Formatter}.
	 * 
	 * @param filename
	 *            for writing to the sink
	 * @param append
	 *            append to the underlying destination
	 * @param format
	 *            user defined formatter
	 * @see Formatter
	 */
	public FileSink(String filename, boolean append, Formatter format) {
		this.append = append;
		file = new File(filename);
		formatter = format;
	}

	/**
	 * Obtain underlying print stream handle
	 * 
	 * @return print stream
	 */
	public PrintStream getPrintStream() {
		return printer;
	}

	/**
	 * Return the file name associated with this sink
	 * 
	 * @return return file name
	 */
	public String getFileName() {
		return file.getName();
	}

	@Override
	public Object getSinkHandle() {
		return printer;
	}

	@Override
	public synchronized void close() {
		if (printer != null) {
			printer.flush();
			printer.close();
		}
		printer = null;
	}

	@Override
	public synchronized void open() throws IOException {
		if (file != null) {
			File parent = file.getParentFile();
			if (parent != null) {
				try {
					parent.mkdirs();
				} catch (SecurityException exc) {
					throw new IOException("Could not verify/create parent path for sink.file=" + file, exc);
				}
			}
		}

		if (printer == null) {
			printer = new PrintStream(Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE,
					append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING));
		}
	}

	@Override
	public boolean isOpen() {
		return printer != null;
	}

	@Override
	public void write(Object msg, Object... args) throws IOException {
		if (isOpen()) {
			print_(formatter.format(msg, args));
		} else {
			throw new IOException("Sink is closed, sink.file=" + file);
		}
	}

	@Override
	public String toString() {
		return super.toString() + "{file: " + file + ", append: " + append + ", is.open: " + isOpen() + "}";
	}

	@Override
	public void flush() {
		if (isOpen()) {
			printer.flush();
		}
	}

	private static Lock lock = new ReentrantLock();

	void print_(String msg) {
		lock.lock();
		try {
			printer.println(msg);
		} finally {
			lock.unlock();
		}
		printer.flush();
	}
}
