/*
 * Copyright 2014 Nastel Technologies, Inc.
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
