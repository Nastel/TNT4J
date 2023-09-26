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

package com.jkoolcloud.tnt4j.sink;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * Sink error listener implementation performing simple {@link com.jkoolcloud.tnt4j.sink.SinkError} logging to the
 * logger.
 * 
 * @version $Revision: 1 $
 */
public class ErrorLoggingListener implements SinkErrorListener {
	private static final EventSink logger = DefaultEventSinkFactory.defaultEventSink(ErrorLoggingListener.class);

	@Override
	public void sinkError(SinkError ev) {
		SinkLogEvent sEvent = ev.getSinkEvent();
		EventSink sink = sEvent.getEventSink();
		logger.log(OpLevel.ERROR, "Sink error: count={4}, vm.name={0}, tid={1}, out.sink={2}, source={3}",
				Utils.getVMName(), Thread.currentThread().getId(), sink, sink.getSource(), sink.getErrorCount(),
				ev.getCause());
	}
}
