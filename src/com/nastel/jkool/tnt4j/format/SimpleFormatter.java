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
package com.nastel.jkool.tnt4j.format;

import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * Simple implementation of <code>Formatter</code> interface provides
 * simple/minimal formatting of <code>TrackingActvity</code> and <code>TrackingEvent</code>
 * as well as any object passed to <code>format()</code> method call.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see DefaultFormatter
 * @see TrackingActivity
 * @see TrackingEvent
 */

public class SimpleFormatter extends DefaultFormatter {
	@Override
	public String format(TrackingEvent event) {
		return 	"{'" + event.getSeverity() + "',"
		+ "event='" + event.getStringMessage() + "',"
		+ "name='" + event.getOperation().getName() + "',"
		+ "usec='" + event.getOperation().getElapsedTime() + "',"
		+ "ccode='" + event.getOperation().getCompCode() + "',"
		+ "rcode='" + event.getOperation().getReasonCode() + "',"
		+ "corr-id='" + event.getCorrelator() + "',"
		+ "error='" + event.getOperation().getExceptionString() + "',"
		+ "track-id='" + event.getTrackingId() + "',"
		+ "parent-id='" + (event.getParentItem() != null? event.getParentItem().getTrackingId(): "none") + "'"
		+ "}";
	}
	
	@Override
	public String format(TrackingActivity activity) {
		return activity.getStatus() + "-" + activity.toString();
	}
}
