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

import com.nastel.jkool.tnt4j.core.Source;

/**
 * <p>
 * This interfaces defines configuration factory that allows creation of underlying configuration objects used to
 * configure the framework.
 * </p>
 * 
 * 
 * @see TrackerConfig
 * 
 * @version $Revision: 5 $
 * 
 */
public interface ConfigFactory {
	/**
	 * Create a default tracking configuration
	 * 
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig();

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source name
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(String source);

	/**
	 * Create a default tracking configuration based on a given class
	 * 
	 * @param clazz
	 *            class for which to obtain configuration
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(Class<?> clazz);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(Source source);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source name
	 * @param configName
	 *            configuration name where configuration elements are read from (e.g. filename)
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(String source, String configName);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param clazz
	 *            class for which to obtain configuration
	 * @param configName
	 *            configuration name where configuration elements are read from (e.g. filename)
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(Class<?> clazz, String configName);

	/**
	 * Create a default tracking configuration
	 * 
	 * @param source
	 *            user defined source
	 * @param configName
	 *            configuration name where configuration elements are read from (e.g. filename)
	 * @see TrackerConfig
	 * @return new <code>TrackerConfig</code> instance with default values and factories
	 */
	public TrackerConfig getConfig(Source source, String configName);
}
