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
 * This interface defines a provider interface for generating dump collections.
 * Each application should creates implementations of this interface to generate
 * application specific dumps. 
 * Providers instances are registered with <code>TrackingLogger</code> class and 
 * triggered on <code>TrackingLogger.dump()</code> method call. 
 * <code>DumpProvider.getDump()</code> is called when application specific dump 
 * need to be obtained by the underlying framework.
 * </p>
 *
 * @see DumpCollection
 *
 * @version $Revision: 1 $
 *
 */

public interface DumpProvider {
	/**
	 * DUMP_BEFORE generated before dump is written to one or more destination(s) 
	 */
	public static final int DUMP_BEFORE = 0;
	
	/**
	 * DUMP_AFTER generated after dump is written to one or more destination(s) 
	 */
	public static final int DUMP_AFTER = 1;

	/**
	 * DUMP_COMPLETE generated after all dumps are written to one or more destination(s) 
	 */
	public static final int DUMP_COMPLETE = 2;
	
	/**
	 * DUMP_ERROR generated when and error is encountered during dump generation 
	 */
	public static final int DUMP_ERROR = 3;

	/**
	 * Name of the dump provider
	 * 
	 * @return name of the dump provider
	 */
	public String getProviderName();

	/**
	 * Name of the dump provider category
	 * 
	 * @return name of the dump provider category
	 * 
	 */
	public String getCategoryName();

	/**
	 * Return the dump collection associated with this provider
	 * 
	 * @return dump collection associated with this dump provider
	 * 
	 */
	public DumpCollection getDump();
}
