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
package com.jkoolcloud.tnt4j.dump;

/**
 * <p>
 * This interface defines a factory that creates instances of {@link DumpSink}
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see DumpFormatter
 * @see DumpSink
 */
public interface DumpSinkFactory {
	/**
	 * Obtain a dump destination based on default settings. Dumps are formatted using {@link DefaultDumpFormatter}
	 * 
	 * @return dump destination instance
	 */
	DumpSink getInstance();

	/**
	 * Obtain a dump destination based on given URI. Dumps are formatted using {@link DefaultDumpFormatter} Same as
	 * {@code getInstance(url, true, new DefaultDumpFormatter())}
	 * 
	 * @param url
	 *            for generating a dump destination instance
	 * @see DumpSink
	 * @return dump destination instance
	 */
	DumpSink getInstance(String url);

	/**
	 * Obtain a dump destination based on given URI and append flag. Dumps are formatted using
	 * {@link DefaultDumpFormatter}. Same as {@code getInstance(url, append, new DefaultDumpFormatter())}
	 *
	 * @param url
	 *            for generating a dump destination instance
	 * @param append
	 *            append to the underlying destination
	 * @see DumpSink
	 * @return dump destination instance
	 */
	DumpSink getInstance(String url, boolean append);

	/**
	 * Obtain a dump destination based on given URI, append flag. and a given {@link DumpFormatter}.
	 * 
	 * @param url
	 *            for generating a dump destination instance
	 * @param append
	 *            append to the underlying destination
	 * @param frm
	 *            user defined dump formatter
	 * @see DumpSink
	 * @see DumpFormatter
	 * @return dump destination instance
	 */
	DumpSink getInstance(String url, boolean append, DumpFormatter frm);
}
