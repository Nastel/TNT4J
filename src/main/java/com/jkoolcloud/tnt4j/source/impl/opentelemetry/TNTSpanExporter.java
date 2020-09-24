/*
 * Copyright 2014-2019 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.source.impl.opentelemetry;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.jkoolcloud.tnt4j.TrackingLogger;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class TNTSpanExporter implements SpanExporter {
	/*
	 * Tracking logger instance where all messages are logged
	 */
	private TrackingLogger logger;

	private TNTSpanExporter(String source) {
		this.logger = TrackingLogger.getInstance(source);
	}

	@Override
	public CompletableResultCode export(Collection<SpanData> spans) {
		for (SpanData span: spans) {
			exportEvents(span.getEvents());
			exportLinks(span.getLinks());
		}
		return CompletableResultCode.ofSuccess();
	}

	private void exportLinks(List<Link> links) {
		for (Link link: links) {
			link.getAttributes();
		}
	}

	private void exportEvents(List<Event> events) {
		for (Event event: events) {
			event.getAttributes();
		}
	}

	@Override
	public CompletableResultCode flush() {
		CompletableResultCode resultCode = new CompletableResultCode();
		try {
			logger.getEventSink().flush();
			resultCode.succeed();
		} catch (IOException e) {
			resultCode.fail();
		}
		return resultCode;
	}

	@Override
	public CompletableResultCode shutdown() {
		logger.close();
		return CompletableResultCode.ofSuccess();
	}
	
	public TNTSpanExporter open() throws IOException {
		logger.open();
		return this;
	}

	public static class Builder {
		String appName;
		
		public Builder(String appName) {
			this.appName = appName;
		}
		
		public TNTSpanExporter build() throws IOException {
			TNTSpanExporter exporter = new TNTSpanExporter(appName);
			return exporter.open();
		}
	}
}
