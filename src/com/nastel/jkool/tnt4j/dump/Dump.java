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

import com.nastel.jkool.tnt4j.core.PropertySnapshot;

/**
 * <p>
 * This class provides a concrete implementation of <code>DumpCollection</code> interface.
 * </p>
 * 
 * 
 * @version $Revision: 10 $
 * 
 * @see DumpProvider
 * @see DumpCollection
 */

public class Dump extends PropertySnapshot implements DumpCollection {
	/**
     * 
     */
	private static final long serialVersionUID = 1L;
	DumpProvider dProv;
	Throwable reason = null;

	/**
	 * Create a new instance of <code>DefaultTrackerFactory</code> with a specific <code>EventSinkFactory</code>
	 * instance.
	 * 
	 * @param name
	 *            of the generated dump
	 * @param prvd
	 *            dump provider that generates the dump
	 * 
	 * @see DumpProvider
	 */
	public Dump(String name, DumpProvider prvd) {
		this(name, prvd, null, 16);
	}

	/**
	 * Create a new instance of <code>DefaultTrackerFactory</code> with a specific <code>EventSinkFactory</code>
	 * instance.
	 * 
	 * @param name
	 *            of the generated dump
	 * @param prvd
	 *            dump provider that generates the dump
	 * @param capacity
	 *            initial capacity of this collection
	 * 
	 * @see DumpProvider
	 */
	public Dump(String name, DumpProvider prvd, Throwable rsn, int capacity) {
		super(prvd.getCategoryName(), name, capacity);
		dProv = prvd;
		reason = rsn;
	}

	@Override
	public DumpProvider getDumpProvider() {
		return dProv;
	}

	@Override
	public String toString() {
		return "{Name: " + getName() 
			+ ", Size: " + size() 
			+ ", Time: " + getTime() 
			+ ", Provider: " + dProv
			+ ", Reason: " + reason
	        + "}";
	}

	@Override
    public Throwable getReason() {
	    return reason;
    }

	@Override
    public void setReason(Throwable rsn) {
		reason = rsn;
	}
}
