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

import com.jkoolcloud.tnt4j.core.Message;

/**
 * Implements a null factory which returns null signatures
 *
 * @version $Revision: 1 $
 */
public class NullSignFactoryImpl implements SignFactory {

	@Override
	public String sign(Object obj) {
		return null;
	}

	@Override
	public String sign(Message obj) {
		return null;
	}
}
