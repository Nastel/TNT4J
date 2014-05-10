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

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.tracker.TrackingActivity;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * <p>
 * Default implementation of <code>Formatter</code> interface provides
 * default formatting of <code>TrackingActvity</code> and <code>TrackingEvent</code>
 * as well as any object passed to <code>format()</code> method call.
 * </p>
 * 
 * 
 * @version $Revision: 4 $
 * 
 * @see Formatter
 * @see TrackingActivity
 * @see TrackingEvent
 */
public class DefaultFormatter implements EventFormatter {

	@Override
	public String format(Object obj) {
		if (obj instanceof TrackingActivity) {
			return format((TrackingActivity) obj);
		} else if (obj instanceof TrackingEvent) {
			return format((TrackingEvent) obj);
		} else {
			return String.valueOf(obj);
		}
	}

	@Override
	public String format(TrackingEvent event) {
		return event.toString();
	}
	
	@Override
	public String format(TrackingActivity activity) {
		return activity.getStatus() + "-" + activity + "," + activity.getSource();
	}

	@Override
    public String format(OpLevel level, Object msg) {
	    return format(msg);
    }

	@Override
    public String format(OpLevel level, Object msg, Throwable ex) {
	    return format(msg);
    }
}
