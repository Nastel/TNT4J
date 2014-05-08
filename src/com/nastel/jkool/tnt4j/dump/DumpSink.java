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

import com.nastel.jkool.tnt4j.sink.Sink;

/**
 * <p>
 * This interface defines a dump destination end point. Dump destination
 * allows writing of <code>DumpCollection</code> instances. Classes that implement
 * this interface should handle formatting and forwarding to the actual destination that 
 * can handle dump collections such as cloud services, analyzer service, central logging
 * services, files, etc.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see DumpCollection
 */

public interface DumpSink extends Sink {
	/**
	 * This method allows writing of <code>DumpCollection</code> objects
	 * to the underlying destination.
	 * 
	 * @see DumpCollection
	 * @throws IOException
	 */
	public void write(DumpCollection dump) throws IOException;
}
