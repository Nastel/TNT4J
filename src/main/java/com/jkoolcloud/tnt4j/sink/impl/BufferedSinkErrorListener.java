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
package com.jkoolcloud.tnt4j.sink.impl;

import com.jkoolcloud.tnt4j.sink.Sink;
import com.jkoolcloud.tnt4j.sink.SinkError;
import com.jkoolcloud.tnt4j.sink.SinkErrorListener;

/**
 * This class implements a default error handler for {@link BufferedEventSink}. The handler attempts to re-queue events
 * on which exceptions occurred to avoid event loss.
 *
 *
 * @see PooledLogger
 * @see SinkErrorListener
 * 
 * @version $Revision: 1 $
 *
 */
public class BufferedSinkErrorListener implements SinkErrorListener {
	private BufferedEventSink bSink;

	public BufferedSinkErrorListener(BufferedEventSink bSink) {
		this.bSink = bSink;
	}

	@Override
	public void sinkError(SinkError ev) {
		Sink evSink = ev.getSink();

		if (evSink instanceof BufferedEventSink) {
			BufferedEventSink sink = (BufferedEventSink) evSink;
			sink.handleError(ev);
		} else {
			bSink.handleError(ev);
		}
	}
}
