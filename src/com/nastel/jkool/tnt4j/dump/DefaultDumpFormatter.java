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


import com.nastel.jkool.tnt4j.core.Property;
import com.nastel.jkool.tnt4j.core.UsecTimestamp;
import com.nastel.jkool.tnt4j.utils.TimeService;
import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements a default dump formatter. Dumps are formatted as follows using JSON.
 * 
 * <pre>
 * {@code
 * {
 * "dump.status": "START",
 * "server.name": "XOMEGA",
 * "server.address": "ip-address",
 * "vm.name": "123036@server",
 * "vm.pid": 123036,
 * "dump.sink": "com.nastel.jkool.tnt4j.dump.FileDumpSink@45d64c37{file: .\123036@server.dump, append: true, is.open: true}",
 * "dump.time.string": "<time-stamp-string>"
 * }
 * {
 * "dump.reason": "java.lang.Exception: VM-Shutdown"
 * "dump.name": "runtimeMetrics",
 * "dump.provider": "com.nastel.TradeApp",
 * "dump.category": "ApplRuntime",
 * "dump.time.string": "<time-stamp-string>",
 * "dump.time.stamp": 1394115455190,
 * "dump.snapshot": {
 *  ....
 * }
 * "dump.elapsed.ms=": 4
 * }
 *  .... (next dump)
 *  .... (next dump)
 *  .... (next dump)
 * {
 * "dump.status": "END",
 * "server.name": "XOMEGA",
 * "server.address": "ip-address",
 * "vm.name": "123036@server",
 * "vm.pid": 123036,
 * "dump.sink": "com.nastel.jkool.tnt4j.dump.FileDumpSink@45d64c37{file: .\123036@server.dump, append: true, is.open: true}",
 * "dump.time.string": "<time-stamp-string>"
 * "dump.elapsed.ms=": 1334
 * }
 * }
 * </pre>
 * </p>
 * 
 * 
 * @version $Revision: 10 $
 * 
 * @see DumpSink
 * @see DumpFormatter
 * @see DumpCollection
 */
public class DefaultDumpFormatter implements DumpFormatter {
	private static ThreadLocal<Long> TIME_TABLE = new ThreadLocal<Long>();
	private static final String INDENT = "\t";
	private static final String NEWLINE = "\n";
		
	private String _format(DumpCollection dump, String padding) {
		StringBuilder buffer = new StringBuilder(1024);
		
		buffer.append(padding).append(Utils.quote("dump.name")).append(": ").append(Utils.quote(dump.getName())).append(",\n");
		buffer.append(padding).append(Utils.quote("dump.category")).append(": ").append(Utils.quote(dump.getCategory())).append(",\n");
		buffer.append(padding).append(Utils.quote("dump.provider")).append(": ").append(Utils.quote(dump.getDumpProvider().getProviderName())).append(",\n");
		buffer.append(padding).append(Utils.quote("dump.provider.category")).append(": ").append(Utils.quote(dump.getDumpProvider().getCategoryName())).append(",\n");
		buffer.append(padding).append(Utils.quote("dump.time.string")).append(": ").append(Utils.quote(UsecTimestamp.getTimeStamp(dump.getTime()))).append(",\n");
		buffer.append(padding).append(Utils.quote("dump.time.stamp")).append(": ").append(dump.getTime()).append(",\n");
		buffer.append(padding).append(Utils.quote("dump.snapshot")).append(": {\n");
		int startLen = buffer.length();
		
		String subPadding = padding + INDENT;
		for (Property entry : dump.getSnapshot()) {
			if (buffer.length() > startLen) {
				buffer.append(",\n");
			}
			Object value = entry.getValue();
			if (value instanceof DumpCollection) {
				buffer.append(subPadding).append(Utils.quote("dump.collection")).append(": {\n");
				buffer.append(_format((DumpCollection)value, subPadding + INDENT));
				buffer.append(NEWLINE).append(subPadding).append("}");
			} else if (value instanceof Number) {
				buffer.append(subPadding).append(Utils.quote(entry.getKey())).append(": ").append(value);
			} else {
				buffer.append(subPadding).append(Utils.quote(entry.getKey())).append(": ").append(Utils.quote(value));				
			}
		}
		buffer.append(NEWLINE).append(padding).append("}");
		return buffer.toString();		
	}
	
	@Override
	public String format(DumpCollection dump) {
		return _format(dump, "");
	}

	@Override
	public String format(Object obj, Object...args) {
		return Utils.format(String.valueOf(obj), args);
	}

	@Override
	public String getHeader(DumpCollection dump) {
		StringBuilder buffer = new StringBuilder(1024);
		Throwable reason = dump.getReason();
		buffer.append("{\n");
		buffer.append(Utils.quote("dump.reason")).append(": ").append(Utils.quote(reason));
		if (reason != null) {
			StackTraceElement[] stack = reason.getStackTrace();
			for (int i=0; i < stack.length; i++) {
				buffer.append(NEWLINE).append(Utils.quote("stack.frame[" + i + "]")).append(": ").append(Utils.quote(stack[i]));
			}
		}
		return buffer.toString();
	}

	@Override
	public String getFooter(DumpCollection dump) {
		StringBuilder buffer = new StringBuilder(1024);
		buffer.append(Utils.quote("dump.elapsed.ms")).append(": ").append((TimeService.currentTimeMillis() - dump.getTime()));
		buffer.append("\n}");
		return buffer.toString();
	}

	@Override
    public String getCloseStanza(DumpSink sink) {
		StringBuilder buffer = new StringBuilder(1024);
		buffer.append("{\n");
		buffer.append(Utils.quote("dump.status")).append(": ").append(Utils.quote("END")).append(",\n");
		buffer.append(Utils.quote("server.name")).append(": ").append(Utils.quote(Utils.getLocalHostName())).append(",\n");
		buffer.append(Utils.quote("server.address")).append(": ").append(Utils.quote(Utils.getLocalHostAddress())).append(",\n");
		buffer.append(Utils.quote("vm.name")).append(": ").append(Utils.quote(Utils.getVMName())).append(",\n");
		buffer.append(Utils.quote("vm.pid")).append(": ").append(Utils.getVMPID()).append(",\n");
		buffer.append(Utils.quote("dump.sink")).append(": ").append(Utils.quote(sink)).append(",\n");
		buffer.append(Utils.quote("dump.time.string")).append(": ").append(Utils.quote(UsecTimestamp.getTimeStamp())).append(",\n");
		long elapsed_ms = TimeService.currentTimeMillis() - TIME_TABLE.get();
		buffer.append(Utils.quote("dump.elapsed.ms")).append(": ").append(elapsed_ms);
		buffer.append("\n}");
		return buffer.toString();
    }

	@Override
    public String getOpenStanza(DumpSink sink) {
		StringBuilder buffer = new StringBuilder(1024);
		TIME_TABLE.set(TimeService.currentTimeMillis());
		buffer.append("{\n");
		buffer.append(Utils.quote("dump.status")).append(": ").append(Utils.quote("START")).append(",\n");
		buffer.append(Utils.quote("server.name")).append(": ").append(Utils.quote(Utils.getLocalHostName())).append(",\n");
		buffer.append(Utils.quote("server.address")).append(": ").append(Utils.quote(Utils.getLocalHostAddress())).append(",\n");
		buffer.append(Utils.quote("vm.name")).append(": ").append(Utils.quote(Utils.getVMName())).append(",\n");
		buffer.append(Utils.quote("vm.pid")).append(": ").append(Utils.getVMPID()).append(",\n");
		buffer.append(Utils.quote("dump.sink")).append(": ").append(Utils.quote(sink)).append(",\n");
		buffer.append(Utils.quote("dump.time.string")).append(": ").append(Utils.quote(UsecTimestamp.getTimeStamp()));
		buffer.append("\n}");
		return buffer.toString();
    }
}
