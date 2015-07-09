/*
 * Copyright 2014-2015 JKOOL, LLC.
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
package com.nastel.jkool.tnt4j.examples;

import java.io.IOException;

import com.nastel.jkool.tnt4j.ActivityScheduler;
import com.nastel.jkool.tnt4j.core.Activity;
import com.nastel.jkool.tnt4j.core.ActivityListener;
import com.nastel.jkool.tnt4j.core.PropertySnapshot;

public class Pinger {
	/**
	 * Run TNT4J Pinger application to generate scheduled activity ping

	 * @param args Usage: pinger-name activity-name period-ms
	 * @throws InterruptedException if interrupted delaying for next ping interval
	 * @throws IOException if error opening pinger
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		if (args.length < 3) {
			System.out.println("Usage: pinger-name activity-name period-ms");
			System.exit(-1);
		}
		ActivityScheduler pinger = new ActivityScheduler(args[0], new PingHandler());
		pinger.open();
		long period = Long.parseLong(args[2]);
		pinger.schedule(args[1], period);
		try {
			Thread.sleep(period*10);
		} finally {
			pinger.close();
		}
	}
}

class PingHandler implements ActivityListener {
	long pingCount = 0;
	@Override
    public void started(Activity activity) {
		System.out.println("START: activity.id=" + activity.getTrackingId()
				+ ", activity.name=" + activity.getName()
				+ ", started=" + activity.getStartTime());
	}

	@Override
    public void stopped(Activity activity) {
		pingCount++;
		// post processing of activity: enrich activity with application metrics
		PropertySnapshot snapshot = new PropertySnapshot("Pinger", "Stats");
		snapshot.add("ping.count", pingCount);
		activity.add(snapshot); // add property snapshot to activity
		System.out.println("END: activity.id=" + activity.getTrackingId()
				+ ", activity.name=" + activity.getName()
				+ ", elapsed.usec=" + activity.getElapsedTimeUsec()
				+ ", snap.count=" + activity.getSnapshotCount()
				+ ", id.count=" + activity.getIdCount()
				);
    }
}
