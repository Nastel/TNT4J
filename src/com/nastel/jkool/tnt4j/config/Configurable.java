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
package com.nastel.jkool.tnt4j.config;

import java.util.Map;

/**
 * <p>
 * This interface defines classes that can be configured using
 * properties.
 * </p>
 * @version $Revision: 1 $
 * 
 */
public interface Configurable {
	/**
	 * Obtain current configuration settings
	 *  
	 * @return current configuration settings
	 */
	public Map<String, Object> getConfiguration();
	
	/**
	 * Apply given configuration settings
	 *  
	 *  @param settings apply given settings as configuration (name, value pairs)
	 */
	public void setConfiguration(Map<String, Object> settings);
}
