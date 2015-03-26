/*
 * Copyright 2014-2015 JKOOL, LLC.
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
package com.nastel.jkool.tnt4j.dump;

import com.nastel.jkool.tnt4j.format.Formatter;

/**
 * <p>
 * Classes that implement this interface provide implementation for the <code>DumpFormatter</code> interface.
 * Dump formatters are used to format dumps: <code>DumpCollection</code> instances.
 * </p>
 * 
 * 
 * @version $Revision: 2 $
 * 
 * @see DefaultDumpFormatter
 * @see DumpCollection
 */
public interface DumpFormatter extends Formatter {
	/**
	 * Obtain a formatted opening stanza
	 *
	 * @param sink dump sink
	 */
	public String getOpenStanza(DumpSink sink);
	
	/**
	 * Obtain a formatted closing stanza
	 *
	 * @param sink dump sink
	 */
	public String getCloseStanza(DumpSink sink);
	
	/**
	 * Obtain a formatted header associated with the given dump collection
	 *
	 * @param dump user specified dump collection
	 */
	public String getHeader(DumpCollection dump);
	
	/**
	 * Obtain a formatted footer associated with the given dump collection
	 *
	 * @param dump user specified dump collection
	 */
	public String getFooter(DumpCollection dump);

	/**
	 * Format a given dump collection and return a string
	 *
	 * @param dump user specified dump collection
	 */
	public String format(DumpCollection dump);
}
