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

import com.nastel.jkool.tnt4j.core.Property;
import com.nastel.jkool.tnt4j.core.Snapshot;


/**
 * <p>
 * Classes that implement this interface provide implementation for 
 * the <code>DumpCollection</code> interface, which defines a container for 
 * holding application dump information as a snapshot. 
 * </p>
 * 
 * 
 * @version $Revision: 5 $
 * 
 * @see DumpProvider
 */

public interface DumpCollection extends Snapshot<Property>{

	/**
	 * Obtain <code>DumpProvider</code> instance associated with the dump.
	 * Dump provider generates instances of the <code>DumpCollection</code> 
	 * 
	 * @return a dump provider associated with this dump.
	 */
	public DumpProvider getDumpProvider();
	

	/**
	 * Obtain reason why the dump was triggered or generated
	 * 
	 * @return reason for dump being generated
	 */
	public Throwable getReason();
	
	/**
	 * Set reason why the dump was triggered or generated
	 * 
	 * @param reason of what caused dump generation
	 */
	public void setReason(Throwable reason);
	
}
