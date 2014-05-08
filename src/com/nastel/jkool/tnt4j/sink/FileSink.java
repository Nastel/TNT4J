/*
 * Copyright (c) 2014 Nastel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Nastel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with Nastel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *
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
	
	@Override
    public void close() {
		if (printer != null) printer.close();
		printer = null;
	}

	@Override
    public Object getSinkHandle() {
	    return file;
    }

	@Override
    public void open() throws IOException {
		printer = new PrintStream(new FileOutputStream(file, append));
    }

	@Override
    public boolean isOpen() {
	    return printer != null;
    }	

	@Override
    public void write(Object msg) throws IOException {
		if (isOpen()) {
			printer.println(formatter.format(msg));		
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
