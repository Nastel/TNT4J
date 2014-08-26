/*
 * Copyright 2014 Nastel Technologies, Inc.
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
package com.nastel.jkool.tnt4j.examples;

import org.apache.log4j.Logger;

public class Log4JTest {
	private static final Logger logger = Logger.getLogger(Log4JTest.class);
	
	public static void main(String[] args) {
		logger.info("Starting a tnt4j activity #beg=Test, #app=" + Log4JTest.class.getName());
		logger.warn("First log message #app=" + Log4JTest.class.getName() + ", #msg='1 Test warning message'");
		logger.error("Second log message #app=" + Log4JTest.class.getName() + ", #msg='2 Test error message'", new Exception("test exception"));
		logger.info("Ending a tnt4j activity #end=Test, #app=" + Log4JTest.class.getName());
		
		logger.debug("First datagram message #app=" + Log4JTest.class.getName() + ", #msg='Test datagram message'");
		logger.trace("Second datagram message #app=" + Log4JTest.class.getName() + ", #msg='Test datagram message'");
		logger.trace("Whole datagram message #app=" + Log4JTest.class.getName());
	}
}
