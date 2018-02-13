/*
 * Copyright 2014-2018 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.dump;

import com.jkoolcloud.tnt4j.core.Property;
import com.jkoolcloud.tnt4j.core.UsecTimestamp;
import com.jkoolcloud.tnt4j.utils.TimeService;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements a default dump formatter. Dumps are formatted as follows using JSON.
 *
 * <pre>
 * {
 * "dump.status": "START",
 * "server.name": "XOMEGA",
 * "server.address": "ip-address",
 * "vm.name": "123036@server",
 * "vm.pid": 123036,
 * "dump.sink": "com.jkoolcloud.tnt4j.dump.FileDumpSink@45d64c37{file: .\123036@server.dump, append: true, is.open: true}",
 * "dump.time.string": "&lt;time-stamp-string&gt;"
 * }
 * {
 * "dump.reason": "java.lang.Exception: VM-Shutdown"
 * "dump.name": "runtimeMetrics",
 * "dump.provider": "com.nastel.TradeApp",
 * "dump.category": "ApplRuntime",
 * "dump.time.string": "&lt;time-stamp-string&gt;",
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
 * "dump.sink": "com.jkoolcloud.tnt4j.dump.FileDumpSink@45d64c37{file: .\123036@server.dump, append: true, is.open: true}",
 * "dump.time.string": "&lt;time-stamp-string&gt;"
 * "dump.elapsed.ms=": 1334
 * }
 * </pre>
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
	private static final String END_ATTR = ",\n";

	private String _format(DumpCollection dump, String padding) {
		StringBuilder buffer = new StringBuilder(1024);

		buffer.append(padding);
		Utils.quote("dump.name", buffer).append(": ");
		Utils.quote(dump.getName(), buffer).append(END_ATTR);
		buffer.append(padding);
		Utils.quote("dump.category", buffer).append(": ");
		Utils.quote(dump.getCategory(), buffer).append(END_ATTR);
		buffer.append(padding);
		Utils.quote("dump.provider", buffer).append(": ");
		Utils.quote(dump.getDumpProvider().getProviderName(), buffer).append(END_ATTR);
		buffer.append(padding);
		Utils.quote("dump.provider.category", buffer).append(": ");
		Utils.quote(dump.getDumpProvider().getCategoryName(), buffer).append(END_ATTR);
		buffer.append(padding);
		Utils.quote("dump.time.string", buffer).append(": ");
		Utils.quote(UsecTimestamp.getTimeStamp(dump.getTime(), 0), buffer).append(END_ATTR);
		buffer.append(padding);
		Utils.quote("dump.time.stamp", buffer).append(": ").append(dump.getTime()).append(END_ATTR);
		buffer.append(padding);
		Utils.quote("dump.snapshot", buffer).append(": {\n");
		int startLen = buffer.length();

		String subPadding = padding + INDENT;
		for (Property entry : dump.getSnapshot()) {
			if (entry.isTransient()) {
				continue;
			}

			if (buffer.length() > startLen) {
				buffer.append(END_ATTR);
			}
			Object value = entry.getValue();
			if (value instanceof DumpCollection) {
				buffer.append(subPadding);
				Utils.quote("dump.collection", buffer).append(": {\n");
				buffer.append(_format((DumpCollection) value, subPadding + INDENT));
				buffer.append(NEWLINE).append(subPadding).append("}");
			} else if (value instanceof Number) {
				buffer.append(subPadding);
				Utils.quote(entry.getKey(), buffer).append(": ").append(value);
			} else {
				buffer.append(subPadding);
				Utils.quote(entry.getKey(), buffer).append(": ");
				Utils.quote(value, buffer);
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
	public String format(Object obj, Object... args) {
		return Utils.format(String.valueOf(obj), args);
	}

	@Override
	public String getHeader(DumpCollection dump) {
		StringBuilder buffer = new StringBuilder(1024);
		Throwable reason = dump.getReason();
		buffer.append("{\n");
		Utils.quote("dump.reason", buffer).append(": ");
		Utils.quote(reason, buffer);
		if (reason != null) {
			StackTraceElement[] stack = reason.getStackTrace();
			for (int i = 0; i < stack.length; i++) {
				buffer.append(NEWLINE);
				Utils.quote("stack.frame[" + i + "]", buffer).append(": ");
				Utils.quote(stack[i], buffer);
			}
		}
		return buffer.toString();
	}

	@Override
	public String getFooter(DumpCollection dump) {
		StringBuilder buffer = new StringBuilder(1024);
		Utils.quote("dump.elapsed.ms", buffer).append(": ").append((TimeService.currentTimeMillis() - dump.getTime()));
		buffer.append("\n}");
		return buffer.toString();
	}

	@Override
	public String getCloseStanza(DumpSink sink) {
		StringBuilder buffer = new StringBuilder(1024);
		buffer.append("{\n");
		Utils.quote("dump.status", buffer).append(": ");
		Utils.quote("END", buffer).append(END_ATTR);
		Utils.quote("server.name", buffer).append(": ");
		Utils.quote(Utils.getLocalHostName(), buffer).append(END_ATTR);
		Utils.quote("server.address", buffer).append(": ");
		Utils.quote(Utils.getLocalHostAddress(), buffer).append(END_ATTR);
		Utils.quote("vm.name", buffer).append(": ");
		Utils.quote(Utils.getVMName(), buffer).append(END_ATTR);
		Utils.quote("vm.pid", buffer).append(": ").append(Utils.getVMPID()).append(END_ATTR);
		Utils.quote("dump.sink", buffer).append(": ");
		Utils.quote(sink, buffer).append(END_ATTR);
		Utils.quote("dump.time.string", buffer).append(": ");
		Utils.quote(UsecTimestamp.getTimeStamp(), buffer).append(END_ATTR);
		long elapsed_ms = TimeService.currentTimeMillis() - TIME_TABLE.get();
		Utils.quote("dump.elapsed.ms", buffer).append(": ").append(elapsed_ms);
		buffer.append("\n}");
		return buffer.toString();
	}

	@Override
	public String getOpenStanza(DumpSink sink) {
		StringBuilder buffer = new StringBuilder(1024);
		TIME_TABLE.set(TimeService.currentTimeMillis());
		buffer.append("{\n");
		Utils.quote("dump.status", buffer).append(": ");
		Utils.quote("START", buffer).append(END_ATTR);
		Utils.quote("server.name", buffer).append(": ");
		Utils.quote(Utils.getLocalHostName(), buffer).append(END_ATTR);
		Utils.quote("server.address", buffer).append(": ");
		Utils.quote(Utils.getLocalHostAddress(), buffer).append(END_ATTR);
		Utils.quote("vm.name", buffer).append(": ");
		Utils.quote(Utils.getVMName(), buffer).append(END_ATTR);
		Utils.quote("vm.pid", buffer).append(": ").append(Utils.getVMPID()).append(END_ATTR);
		Utils.quote("dump.sink", buffer).append(": ");
		Utils.quote(sink, buffer).append(END_ATTR);
		Utils.quote("dump.time.string", buffer).append(": ");
		Utils.quote(UsecTimestamp.getTimeStamp(), buffer);
		buffer.append("\n}");
		return buffer.toString();
	}
}
