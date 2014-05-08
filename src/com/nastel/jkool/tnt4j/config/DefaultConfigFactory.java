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


/**
 * <p>
 * This class provides a static way to get default configuration factory.
 * </p>
 * 
 * <pre>
 * {@code
 * TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(source);
 * TrackingLogger.register(config.build());
 * ...
 * }
 * </pre>
 * 
 * @see TrackerConfigStore
 * @see TrackerConfig
 * 
 * @version $Revision: 5 $
 * 
 */
public class DefaultConfigFactory  {
	private static ConfigFactory factory = new ConfigFactoryStoreImpl();
	
	private DefaultConfigFactory() {
	}
	
	/**
	 * Set a default configuration factory implementation
	 * 
	 * @return <code>ConfigFactory</code> instance
	 */
	public static ConfigFactory setDefaultConfigFactory(ConfigFactory fac) {
		factory = fac;
		return factory;
	}
	
	/**
	 * Obtain a default tracking configuration factory
	 * 
	 * @return default <code>ConfigFactory</code> instance
	 */
	public static ConfigFactory getInstance() {
		return factory;
	}	
}
