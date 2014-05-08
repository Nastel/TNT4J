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
 * This interface defines a handle interface, which can be opened, closed.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 */
public interface Handle extends java.io.Closeable {
	/**
	 * This method opens and prepares message destination for writing.
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException;

	/**
	 * This method determines of the message destination is in open state 
	 * and ready for writing.
	 * 
	 * @return true if open, false otherwise.
	 */
	public boolean isOpen();
}
