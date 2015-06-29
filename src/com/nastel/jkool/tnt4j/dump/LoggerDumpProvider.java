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

import java.util.List;
import java.util.Map;

import com.nastel.jkool.tnt4j.TrackingLogger;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * This class is a dump provider for TNT4J registered loggers
 * 
 * </p>
 * 
 * @see DumpCollection
 * 
 * @version $Revision: 3 $
 * 
 */
public class LoggerDumpProvider extends DefaultDumpProvider {
	public static final String DUMP_LOGGER_SOURCE = "source";
	/**
	 * Create a new logger dump provider with a given name
	 * 
	 *@param name
	 *            provider name
	 */
	public LoggerDumpProvider(String name) {
	    super(name, "Logger");
    }

	/**
	 * Create a new logger dump provider with a given name
	 * and category
	 * 
	 *@param name
	 *            provider name
	 *@param cat
	 *            category name
	 */
	public LoggerDumpProvider(String name, String cat) {
	    super(name, cat);
    }

	@Override
	public DumpCollection getDump() {
		Dump rootDump = new Dump("LoggerStats", this);		
		List<TrackingLogger> list = TrackingLogger.getAllTrackers();
		for (TrackingLogger logger: list) {
			Dump dump = new Dump(logger.getId(), this);
			Map<String, Object> stats = logger.getStats();
			dump.addAll(stats);
			
			String config = String.valueOf(logger.getConfiguration().getProperty("source"));
			dump.add(Utils.qualify(logger, DUMP_LOGGER_SOURCE), logger.getSource().getFQName());
			
			Dump propDump = new Dump("LoggerConfig", config, this);	
			propDump.addAll(logger.getConfiguration().getProperties());
			
			dump.add(config, propDump);
			rootDump.add(logger.getId(), dump);
		}
		return rootDump;
	}
}
