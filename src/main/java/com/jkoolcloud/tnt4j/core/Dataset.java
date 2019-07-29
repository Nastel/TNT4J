/*
 * Copyright 2019 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.core;

/**
 * Data set defines a named list of properties
 *  
 * @author albert
 */
public class Dataset extends PropertySnapshot {
	public static final String CATEGORY_DATASET = "DATASET";

	/**
	 * Create a data set instance
	 * @param name dataset name
	 */
	public Dataset(String name) {
		super(CATEGORY_DATASET, name, OpLevel.NONE, OpType.DATASET, UsecTimestamp.now());
	}
	
	/**
	 * Create a data set instance
	 * 
	 * @param cat dataset category
	 * @param name dataset name
	 */
	public Dataset(String cat, String name) {
		super(cat, name, OpLevel.NONE, OpType.DATASET, UsecTimestamp.now());
	}
	
	/**
	 * Create a data set instance
	 * 
	 * @param cat dataset category
	 * @param name dataset name
	 * @param time timestamp
	 */
	public Dataset(String cat, String name, UsecTimestamp time) {
		super(cat, name, OpLevel.NONE, OpType.DATASET, time);
	}
}
