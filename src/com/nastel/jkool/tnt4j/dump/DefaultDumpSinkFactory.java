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

import java.io.File;
import java.util.Map;

import com.nastel.jkool.tnt4j.config.Configurable;
import com.nastel.jkool.tnt4j.config.ConfigurationException;
import com.nastel.jkool.tnt4j.utils.Utils;



/**
 * <p>
 * This class implements a default dump destination factory based on file based dump destination
 * backed by <code>FileDumpSink</code> implementation. By default dump destinations are 
 * name using this convention: <code>DEFAULT_DUMP_FOLDER + Utils.VM_NAME + ".dump"</code>.
 * Default dump directory location can be specified using <code>DumpLocation</code> configuration
 * attribute or java property <code>tnt4j.dump.folder=./</code>.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see FileDumpSink
 */
public class DefaultDumpSinkFactory implements DumpSinkFactory, Configurable {
	public static final String DEFAULT_DUMP_FOLDER = System.getProperty("tnt4j.dump.folder", "." + File.separator);

	protected Map<String, Object> config = null;
	private boolean append = true;
	private String dumpLocation;
	
	
	/**
	 * Create a default dump sink factory with default dump
	 * location.
	 */
	public DefaultDumpSinkFactory() {
		dumpLocation = DEFAULT_DUMP_FOLDER;
		if (!dumpLocation.endsWith(File.separator)) {
			dumpLocation += File.separator;
		}
		dumpLocation += Utils.getVMName() + ".dump";
	}
	
	/**
	 * Obtain default dump location URL.
	 *
	 * @return default dump location URL.
	 */
	public  String getDefaultLocation() {
		return dumpLocation;
	}
	
	@Override
    public DumpSink getInstance() {
	    return new FileDumpSink(dumpLocation, append);
    }

	@Override
    public DumpSink getInstance(String url) {
	    return new FileDumpSink(url, append);
    }

	@Override
    public DumpSink getInstance(String url, boolean append) {
	    return new FileDumpSink(url, append);
    }

	@Override
    public DumpSink getInstance(String url, boolean append, DumpFormatter frm) {
	    return new FileDumpSink(url, append, frm);
    }

	@Override
	public Map<String, Object> getConfiguration() {
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> props) throws ConfigurationException {
		config = props;
		Object flag  = props.get("Append");
		append = flag == null? append: Boolean.valueOf(flag.toString());

		Object dumpUrl = config.get("DumpLocation");
		dumpLocation = dumpUrl != null? dumpUrl.toString(): dumpLocation;
	}
}
