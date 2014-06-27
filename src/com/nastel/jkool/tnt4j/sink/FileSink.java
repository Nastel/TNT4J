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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.nastel.jkool.tnt4j.format.DefaultFormatter;
import com.nastel.jkool.tnt4j.format.Formatter;


/**
 * <p>
 * This class implements <code>Sink</code> with file as the underlying storage
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
	 * Create a file based sink based on given filename, append flag.
	 * 
	 * @param filename for generating a sink instance
	 */
	public FileSink(String filename) {
		this(filename, true);
	}

	/**
	 * Create a file based sink based on given filename, append flag.
	 * and a <code>DefaultFormatter</code>.
	 * 
	 * @param filename for generating a sink instance
	 * @param appnd append to the underlying destination
	 */
	public FileSink(String filename, boolean appnd) {
		this(filename, appnd, new DefaultFormatter());
	}

	/**
	 * Create a file based sink based on given filename, append flag.
	 * and a given <code>Formatter</code>.
	 * 
	 * @param filename for writing to the sink
	 * @param appnd append to the underlying destination
	 * @param format user defined formatter
	 * @see Formatter
	 */
	public FileSink(String filename, boolean appnd, Formatter format) {
		append = appnd;
		file = new File(filename);
		formatter = format;
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
	    return file;
    }

	@Override
    public synchronized void close() {
		if (printer != null) printer.close();
		printer = null;
	}

	@Override
    public synchronized void open() throws IOException {
		if (printer == null) {
			printer = new PrintStream(new FileOutputStream(file, append));
		}
    }

	@Override
    public boolean isOpen() {
	    return printer != null;
    }	

	@Override
    public void write(Object msg, Object...args) throws IOException {
		if (isOpen()) {
			printer.println(formatter.format(msg, args));		
			printer.flush();
		} else {
			throw new IOException("Sink is closed, sink.file=" + file);
		}
    }
	
	@Override
	public String toString() {
		return super.toString() + "{file: " + file + ", append: " + append + ", is.open: " + isOpen() + "}";
	}
}
