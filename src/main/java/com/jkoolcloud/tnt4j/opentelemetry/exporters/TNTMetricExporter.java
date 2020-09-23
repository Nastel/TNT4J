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

package com.jkoolcloud.tnt4j.opentelemetry.exporters;

import java.util.Collection;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

public class TNTMetricExporter implements MetricExporter {

	private TNTMetricExporter(String appName) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public CompletableResultCode export(Collection<MetricData> metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableResultCode flush() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	
	public static class Builder {
		String appName;
		
		public Builder(String appName) {
			this.appName = appName;
		}
		
		public TNTMetricExporter build() {
			return new TNTMetricExporter(appName);
		}
	}
}

