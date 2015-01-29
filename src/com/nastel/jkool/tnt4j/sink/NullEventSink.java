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
package com.nastel.jkool.tnt4j.sink;

import java.io.IOException;

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.Snapshot;
import com.nastel.jkool.tnt4j.format.EventFormatter;
import com.nastel.jkool.tnt4j.source.Source;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * This class implements <code>EvenSink</code> with NULL (empty) underlying output.
 * All events written to this sink are discarded.
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
	public boolean isSet(OpLevel sev) {
		return false;
	}

	@Override
	public Object getSinkHandle() {
		return this;
	}

	@Override
	public void write(Object msg, Object... args) throws IOException, InterruptedException {
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
	protected void _log(TrackingEvent event) throws Exception {
	}

	@Override
	protected void _log(TrackingActivity activity) throws Exception {
	}

	@Override
	protected void _log(Snapshot snapshot) throws Exception {
	}

	@Override
	protected void _log(Source src, OpLevel sev, String msg, Object... args) throws Exception {
	}
}
