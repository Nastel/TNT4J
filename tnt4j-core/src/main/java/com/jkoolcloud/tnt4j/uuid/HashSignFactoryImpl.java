/*
 * Copyright 2014-2023 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jkoolcloud.tnt4j.uuid;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import com.jkoolcloud.tnt4j.config.ConfigException;
import com.jkoolcloud.tnt4j.config.Configurable;
import com.jkoolcloud.tnt4j.core.Message;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * Implements a hash based signature factory which returns signatures based on specified hash algorithm: MD5 SHA, etc.
 *
 * @version $Revision: 1 $
 */
public class HashSignFactoryImpl implements SignFactory, Configurable {
	public static final String DEFAULT_HASH_ALGO = "MD5";

	private String algo = DEFAULT_HASH_ALGO;
	private Map<String, ?> settings;

	/**
	 * Create a new signature factory using MD5 algorithm
	 * 
	 */
	public HashSignFactoryImpl() {
		this(DEFAULT_HASH_ALGO);
	}

	/**
	 * Create a new signature factory using a specified digest algorithm.
	 * 
	 * @param alg
	 *            digest algorithm (e.g. MD5)
	 * 
	 */
	public HashSignFactoryImpl(String alg) {
		this.algo = alg;
	}

	@Override
	public String sign(Object obj) throws NoSuchAlgorithmException {
		MessageDigest mdigest = MessageDigest.getInstance(algo);
		String msg = String.valueOf(obj);
		mdigest.update(msg.getBytes(), 0, msg.length());
		return mdigest.toString();
	}

	@Override
	public String sign(Message obj) throws NoSuchAlgorithmException {
		return sign(obj.getMessage());
	}

	@Override
	public Map<String, ?> getConfiguration() {
		return settings;
	}

	@Override
	public void setConfiguration(Map<String, ?> props) throws ConfigException {
		this.settings = props;
		algo = Utils.getString("Algorithm", settings, DEFAULT_HASH_ALGO);
	}
}
