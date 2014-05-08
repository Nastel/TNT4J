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



/**
 * <p>
 * This interface defines a factory that creates instances of <code>DumpSink</code>
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see DumpFormatter
 * @see DumpSink
 */
public interface DumpSinkFactory {
	/**
	 * Obtain a dump destination based on default settings.
	 * Dumps are formatted using <code>DefautDumpFormatter</code>
	 * 
	 * @return dump destination instance
	 */
	public DumpSink getInstance();

	/**
	 * Obtain a dump destination based on given URI.
	 * Dumps are formatted using <code>DefautDumpFormatter</code>
	 * Same as <code>getInstance(url, true, new DefaultDumpFormatter())</code>
	 * 
	 * @param url for generating a dump destination instance
	 * @see DumpSink
	 * @return dump destination instance
	 */
	public DumpSink getInstance(String url);
	
	/**
	 * Obtain a dump destination based on given URI and append flag.
	 * Dumps are formatted using <code>DefautDumpFormatter</code>.
	 * Same as <code>getInstance(url, append, new DefaultDumpFormatter())</code>
	 *
	 * @param url for generating a dump destination instance
	 * @param append append to the underlying destination
	 * @see DumpSink
	 * @return dump destination instance
	 */
	public DumpSink getInstance(String url, boolean append);

	/**
	 * Obtain a dump destination based on given URI, append flag.
	 * and a given <code>DumpFormatter</code>.
	 * 
	 * @param url for generating a dump destination instance
	 * @param append append to the underlying destination
	 * @param frm user defined dump formatter
	 * @see DumpSink
	 * @see DumpFormatter
	 * @return dump destination instance
	 */
	public DumpSink getInstance(String url, boolean append, DumpFormatter frm);
}
