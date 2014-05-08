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
    public void close() {
		if (isOpen()) {
			printer.println(formatter.getCloseStanza(this));
			printer.flush();			
		}
		super.close();
	}

	@Override
    public void open() throws IOException {
		super.open();
		if (isOpen()) {
			printer.println(formatter.getOpenStanza(this));
			printer.flush();
		}
    }

	@Override
    public void write(DumpCollection dump) throws IOException {
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
