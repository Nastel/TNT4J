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
 * This class implements a default dump provider. This is an abstract class and 
 * designed to be used for creating extension sub classes.
 * </p>
 *
 * @see DumpProvider
 *
 * @version $Revision: 1 $
 *
 */

abstract public class DefaultDumpProvider implements DumpProvider {
	String pname, category;
	
	/**
	 * Create a new default dump provider with given name and category
	 *
	 *@param name provider name
	 */
	public DefaultDumpProvider(String name, String cat) {
		pname = name;
		category = cat;
	}

	@Override
    public String getCategoryName() {
	    return category;
    }

	@Override
    public String getProviderName() {
	    return pname;
    }
}
