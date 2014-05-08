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

import java.util.Properties;
import java.util.Map.Entry;

/**
 * <p>
 * This class is a dump provider for dumping java properties using System.getProperties();
 * 
 * </p>
 * 
 * @see DumpCollection
 * 
 * @version $Revision: 2 $
 * 
 */
public class PropertiesDumpProvider extends DefaultDumpProvider {
	Properties props;

	/**
	 * Create a new java properties dump provider with a given name and System.getProperties().
	 * 
	 *@param name
	 *            provider name
	 */
	public PropertiesDumpProvider(String name) {
		this(name, null);
	}

	/**
	 * Create a new java properties dump provider with a given name and user specified properties.
	 * 
	 *@param name
	 *            provider name
	 *@param pr
	 *            properties
	 */
	public PropertiesDumpProvider(String name, Properties pr) {
		super(name, "System");
		props = pr;
	}

	@Override
	public DumpCollection getDump() {
		Dump dump = new Dump("Properties", this);
		Properties p = props != null ? props : System.getProperties();
		for (Entry<Object, Object> entry : p.entrySet()) {
			dump.add(entry.getKey().toString(), entry.getValue());
		}
		return dump;
	}
}
