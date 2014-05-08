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

import java.io.IOException;

/**
 * <p>
 * This interface defines a message destination end point. Message destination is
 * an entity that can be opened/closed as well as written to.
 * </p>
 * 
 * 
 * @version $Revision: 3 $
 * 
 */
public interface Sink extends Handle {
	/**
	 * This method returns a connection handle associated with
	 * the message destination. 
	 * 
	 * @return underlying sink handle.
	 */
	public Object getSinkHandle();
		

	/**
	 * This method allows writing to the underlying message destination
	 * 
	 * @throws IOException
	 */
	public void write(Object msg) throws IOException;
}
