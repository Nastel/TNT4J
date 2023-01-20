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
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * This class implements {@link EventSink} with file {@link FileSink} as the underlying storage.
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
public class FileEventSink extends AbstractEventSink {

	FileSink fileSink;

	/**
	 * Create a file based event sink instance.
	 * 
	 * @param nm
	 *            logical event sink name
	 * @param fileName
	 *            associated with the sink where all entries are recorded
	 * @param append
	 *            true to append to file, false otherwise (file recreated)
	 * @param frm
	 *            event formatter to be used for formatting event entries
	 */
	public FileEventSink(String nm, String fileName, boolean append, EventFormatter frm) {
		super(nm, frm);
		fileSink = new FileSink(fileName, append, frm);
	}

	@Override
	public Object getSinkHandle() {
		return fileSink;
	}

	@Override
	public boolean isOpen() {
		return Utils.isOpen(fileSink);
	}

	@Override
	protected void _open() throws IOException {
		fileSink.open();
	}

	@Override
	protected void _close() throws IOException {
		fileSink.close();
	}

	@Override
	protected void _write(Object msg, Object... args) throws IOException, InterruptedException {
		_writeLog(getEventFormatter().format(msg, args));
	}

	@Override
	protected void _log(TrackingEvent event) throws IOException {
		_writeLog(getEventFormatter().format(event));
	}

	@Override
	protected void _log(TrackingActivity activity) throws IOException {
		_writeLog(getEventFormatter().format(activity));
	}

	@Override
	protected void _log(Snapshot snapshot) {
		_writeLog(getEventFormatter().format(snapshot));
	}

	@Override
	protected void _log(long ttl, Source src, OpLevel sev, String msg, Object... args) {
		_writeLog(getEventFormatter().format(ttl, src, sev, msg, args));
	}

	protected synchronized void _writeLog(String msg) {
		_checkState();

		incrementBytesSent(msg.length());
		fileSink.print_(msg);
	}

	@Override
	public void flush() {
		if (isOpen()) {
			fileSink.flush();
		}
	}

	@Override
	public String toString() {
		return super.toString()//
				+ "{fileSink: " + fileSink //
				+ "}";
	}
}
