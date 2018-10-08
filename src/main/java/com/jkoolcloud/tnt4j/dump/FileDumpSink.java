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
package com.jkoolcloud.tnt4j.dump;

import java.io.IOException;

import com.jkoolcloud.tnt4j.sink.impl.FileSink;

/**
 * <p>
 * This class implements {@link DumpSink} with file as the underlying storage for dump collections.
 * </p>
 *
 * @version $Revision: 3 $
 *
 * @see DumpSink
 * @see DumpFormatter
 * @see DumpCollection
 */

public class FileDumpSink extends FileSink implements DumpSink {
	private DumpFormatter dumpFormatter = null;

	/**
	 * Create a dump destination based on given filename, append flag. and a {@link DefaultDumpFormatter}.
	 *
	 * @param filename
	 *            for generating a dump destination instance
	 */
	public FileDumpSink(String filename) {
		this(filename, true, new DefaultDumpFormatter());
	}

	/**
	 * Create a dump destination based on given filename, append flag. and a {@link DefaultDumpFormatter}.
	 *
	 * @param filename
	 *            for generating a dump destination instance
	 * @param append
	 *            append to the underlying destination
	 */
	public FileDumpSink(String filename, boolean append) {
		this(filename, append, new DefaultDumpFormatter());
	}

	/**
	 * Create a dump destination based on given filename, append flag. and a given {@link DumpFormatter}.
	 *
	 * @param filename
	 *            for generating a dump destination instance
	 * @param append
	 *            append to the underlying destination
	 * @param format
	 *            user defined dump formatter
	 * @see DumpFormatter
	 */
	public FileDumpSink(String filename, boolean append, DumpFormatter format) {
		super(filename, append, format);
		dumpFormatter = format;
	}

	@Override
	public synchronized void close() {
		if (isOpen()) {
			printer.println(dumpFormatter.getCloseStanza(this));
			printer.flush();
		}
		super.close();
	}

	@Override
	public synchronized void open() throws IOException {
		super.open();
		if (isOpen()) {
			printer.println(dumpFormatter.getOpenStanza(this));
			printer.flush();
		}
	}

	@Override
	public synchronized void write(DumpCollection dump) throws IOException {
		if (isOpen()) {
			printer.println(dumpFormatter.getHeader(dump));
			printer.println(dumpFormatter.format(dump));
			printer.println(dumpFormatter.getFooter(dump));
			printer.flush();
		} else {
			throw new IOException("Dump sink is closed, file=" + getFileName());
		}
	}
}
