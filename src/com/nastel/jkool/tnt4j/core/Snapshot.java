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
 * CopyrightVersion 1.0
 *
 */
package com.nastel.jkool.tnt4j.core;

import java.util.Collection;


/**
 * This interface defines a snapshot construct, which
 * has a name and a time stamp and collection of elements.
 *
 * @see UsecTimestamp
 * @version $Revision: 5 $
 */
public interface Snapshot<E> {
	/**
	 * Obtain a name of the snapshot
	 * 
	 * @return name of the snapshot.
	 */
	public String getName();

	/**
	 * Obtain a snapshot category name
	 * 
	 * @return name of the snapshot category
	 */
	public String getCategory();

	/**
	 * Obtain the time stamp of the snapshot.
	 * 
	 * @return time stamp in ms
	 */
	public long getTime();

	/**
	 * Obtain a fully qualified time stamp object
	 * 
	 * @return time stamp object
	 */
	public UsecTimestamp getTimeStamp();
	
	
	/**
	 * Obtain a collection containing snapshot elements
	 * 
	 * @return collection containing snapshot elements
	 */
	public Collection<E> getSnapshot();
}
