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
package com.nastel.jkool.tnt4j.dump;

import java.io.IOException;

import com.nastel.jkool.tnt4j.sink.Sink;

/**
 * <p>
 * This interface defines a dump destination end point. Dump destination
 * allows writing of <code>DumpCollection</code> instances. Classes that implement
 * this interface should handle formatting and forwarding to the actual destination that 
 * can handle dump collections such as cloud services, analyzer service, central logging
 * services, files, etc.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see DumpCollection
 */

public interface DumpSink extends Sink {
	/**
	 * This method allows writing of <code>DumpCollection</code> objects
	 * to the underlying destination.
	 * 
	 * @see DumpCollection
	 * @throws IOException
	 */
	public void write(DumpCollection dump) throws IOException;
}
