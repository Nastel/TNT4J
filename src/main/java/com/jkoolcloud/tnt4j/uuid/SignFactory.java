/*
 * Copyright 2014-2018 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.uuid;

import java.security.NoSuchAlgorithmException;

import com.jkoolcloud.tnt4j.core.Message;

/**
 * Implementations of this interface provide implementation for generating message/event signatures/hashes for temper
 * protection/detection.
 *
 * @version $Revision: 1 $
 */
public interface SignFactory {
	/**
	 * Return a new signature/hash
	 * 
	 * @param obj
	 *            object instance for which hash is to be generated
	 * @return string value of signature associated with a given object
	 * @throws NoSuchAlgorithmException
	 *             if signature calculation algorithm is not provided by environment
	 */
	String sign(Object obj) throws NoSuchAlgorithmException;

	/**
	 * Return a new signature/hash
	 * 
	 * @param obj
	 *            message instance for which hash is to be generated
	 * @return string value of signature associated with a given object
	 * @throws NoSuchAlgorithmException
	 *             if signature calculation algorithm is not provided by environment
	 */
	String sign(Message obj) throws NoSuchAlgorithmException;
}
