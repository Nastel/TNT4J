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
package com.nastel.jkool.tnt4j.dump;

import java.io.IOException;

import com.nastel.jkool.tnt4j.sink.FileSink;


/**
 * <p>
 * This class implements <code>DumpSink</code> with file as the underlying storage for
 * dump collections.
 * </p>
 * 
 * 
 * @version $Revision: 3 $
 * 
 * @see DumpSink
 * @see DumpFormatter
 * @see DumpCollection
 */

public class FileDumpSink extends FileSink implements DumpSink {
	private DumpFormatter formatter = null;
	
	/**
	 * Create a dump destination based on given filename, append flag.
	 * and a <code>DefaultDumpFormatter</code>.
	 * 
	 * @param filename for generating a dump destination instance
	 */
	public FileDumpSink(String filename) {
		this(filename, true, new DefaultDumpFormatter());
	}

	/**
	 * Create a dump destination based on given filename, append flag.
	 * and a <code>DefaultDumpFormatter</code>.
	 * 
	 * @param filename for generating a dump destination instance
	 * @param appnd append to the underlying destination
	 */
	public FileDumpSink(String filename, boolean appnd) {
		this(filename, appnd, new DefaultDumpFormatter());
	}

	/**
	 * Create a dump destination based on given filename, append flag.
	 * and a given <code>DumpFormatter</code>.
	 * 
	 * @param filename for generating a dump destination instance
	 * @param appnd append to the underlying destination
	 * @param format user defined dump formatter
	 * @see DumpFormatter
	 */
	public FileDumpSink(String filename, boolean appnd, DumpFormatter format) {
		super(filename, appnd, format);
		formatter = format;
	}
	
	@Override
    public synchronized void close() {
		if (isOpen()) {
			printer.println(formatter.getCloseStanza(this));
			printer.flush();			
		}
		super.close();
	}

	@Override
    public synchronized void open() throws IOException {
		super.open();
		if (isOpen()) {
			printer.println(formatter.getOpenStanza(this));
			printer.flush();
		}
    }

	@Override
    public synchronized void write(DumpCollection dump) throws IOException {
		if (isOpen()) {
			printer.println(formatter.getHeader(dump));
			printer.println(formatter.format(dump));
			printer.println(formatter.getFooter(dump));
			printer.flush();
		} else {
			throw new IOException("Dump sink is closed");
		}
	}
}
