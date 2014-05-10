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

import java.lang.management.ManagementFactory;



/**
 * <p>
 * This class implements a default dump destination factory based on file based dump destination
 * backed by <code>FileDumpSink</code> implementation. By default dump destinations are 
 * name using this convention: ManagementFactory.getRuntimeMXBean().getName() + ".dump".
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see FileDumpSink
 */
public class DefaultDumpSinkFactory implements DumpSinkFactory {

	@Override
    public DumpSink getInstance() {
	    return new FileDumpSink(ManagementFactory.getRuntimeMXBean().getName() + ".dump");
    }

	@Override
    public DumpSink getInstance(String url) {
	    return new FileDumpSink(url);
    }

	@Override
    public DumpSink getInstance(String url, boolean append) {
	    return new FileDumpSink(url, append);
    }

	@Override
    public DumpSink getInstance(String url, boolean append, DumpFormatter frm) {
	    return new FileDumpSink(url, append, frm);
    }
}
