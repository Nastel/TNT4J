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

import com.nastel.jkool.tnt4j.format.Formatter;

/**
 * <p>
 * Classes that implement this interface provide implementation for the <code>DumpFormatter</code> interface.
 * Dump formatters are used to format dumps: <code>DumpCollection</code> instances.
 * </p>
 * 
 * 
 * @version $Revision: 2 $
 * 
 * @see DefaultDumpFormatter
 * @see DumpCollection
 */
public interface DumpFormatter extends Formatter {
	/**
	 * Obtain a formatted opening stanza
	 *
	 * @param sink dump sink
	 */
	public String getOpenStanza(DumpSink sink);
	
	/**
	 * Obtain a formatted closing stanza
	 *
	 * @param sink dump sink
	 */
	public String getCloseStanza(DumpSink sink);
	
	/**
	 * Obtain a formatted header associated with the given dump collection
	 *
	 * @param dump user specified dump collection
	 */
	public String getHeader(DumpCollection dump);
	
	/**
	 * Obtain a formatted footer associated with the given dump collection
	 *
	 * @param dump user specified dump collection
	 */
	public String getFooter(DumpCollection dump);

	/**
	 * Format a given dump collection and return a string
	 *
	 * @param dump user specified dump collection
	 */
	public String format(DumpCollection dump);
}
