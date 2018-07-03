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

import java.io.IOException;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.core.Snapshot;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.sink.AbstractEventSink;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.tracker.TrackingActivity;
import com.jkoolcloud.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * This class implements {@link EventSink} with NOOP (empty) underlying output. All events written to this sink are
 * discarded.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see OpLevel
 * @see FileSink
 * @see EventFormatter
 * @see AbstractEventSink
 */
public class NullEventSink extends AbstractEventSink {

	public NullEventSink(String nm) {
		super(nm);
	}

	public NullEventSink(String nm, EventFormatter fmt) {
		super(nm, fmt);
	}

	@Override
	public Object getSinkHandle() {
		return this;
	}

	@Override
	public void open() throws IOException {
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	protected void _checkState() throws IllegalStateException {
	}

	@Override
	protected void _write(Object msg, Object... args) throws IOException, InterruptedException {
	}

	@Override
	protected void _log(TrackingEvent event) throws Exception {
	}

	@Override
	protected void _log(TrackingActivity activity) throws Exception {
	}

	@Override
	protected void _log(Snapshot snapshot) throws Exception {
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) throws Exception {
	}
}
