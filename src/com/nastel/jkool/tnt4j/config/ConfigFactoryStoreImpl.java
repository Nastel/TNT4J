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
 * This class implements a default configuration factory backed by <code>TrackerConfigStore</code> class.
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
 * @version $Revision: 3 $
 * 
 */
public class ConfigFactoryStoreImpl implements ConfigFactory {

	protected ConfigFactoryStoreImpl() {
	}

	@Override
	public TrackerConfig getConfig() {
		return new TrackerConfigStore(System.getProperty("tn4j.source.name", "com.default.App"));
	}

	@Override
	public TrackerConfig getConfig(String source) {
		return new TrackerConfigStore(source);
	}

	@Override
	public TrackerConfig getConfig(Source source) {
		return new TrackerConfigStore(source);
	}

	@Override
	public TrackerConfig getConfig(String source, String configName) {
		return new TrackerConfigStore(source, configName);
	}

	@Override
	public TrackerConfig getConfig(Source source, String configName) {
		return new TrackerConfigStore(source, configName);
	}

	@Override
	public TrackerConfig getConfig(Class<?> clazz) {
		return getConfig(clazz.getName());
	}

	@Override
	public TrackerConfig getConfig(Class<?> clazz, String configName) {
		return getConfig(clazz.getName(), configName);
	}

}
