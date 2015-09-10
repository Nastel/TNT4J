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
package com.nastel.jkool.tnt4j.throttle;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Default throttle implementation (thread safe).
 *
 * @version $Revision: 1 $
 */
public class ThrottleImpl implements Throttle {
	
	boolean doThrottle = false;
	long maxMPS = UNLIMITED, maxBPS = UNLIMITED, start = 0;
	
	AtomicLong currentMPS = new AtomicLong(0);
	AtomicLong currentBPS = new AtomicLong(0);
	AtomicLong byteCount = new AtomicLong(0);
	AtomicLong msgCount = new AtomicLong(0);
	AtomicLong sleepCount = new AtomicLong(0);
	AtomicLong lastSleep = new AtomicLong(0);
	AtomicLong delayCount = new AtomicLong(0);
	
	public ThrottleImpl(int maxMps, int maxBps, boolean enabled) {
		setLimits(maxMps, maxBps);
		setEnabled(enabled);
	}
	
	@Override
    public long getMaxMPS() {
	    return maxMPS;
    }

	@Override
    public long getMaxBPS() {
	    return maxBPS;
    }

	@Override
    public Throttle setLimits(long maxMps, long maxBps) {
		maxMPS = maxMps;
		maxBPS = maxBps;
	    return this;
    }

	@Override
    public long getCurrentMPS() {
	    return currentMPS.get();
    }

	@Override
    public long getCurrentBPS() {
	    return currentBPS.get();
    }

	@Override
    public Throttle setEnabled(boolean flag) {
		doThrottle = flag;
		if (doThrottle) {
			start = System.currentTimeMillis();
		}
	    return this;
    }

	@Override
    public boolean isThrottled() {
	    return doThrottle;
    }

	@Override
    public Throttle throttle(long msgs, long bytes) throws InterruptedException {
		if (!doThrottle || ( msgs == 0 && bytes == 0)) {
			return this;
		}
		
		// Check the throttle.
		if (bytes > 0) {
			byteCount.addAndGet(bytes);
		}
		if (msgs > 0) {
			msgCount.addAndGet(msgs);
		}	
		long elapsed = Math.max(System.currentTimeMillis() - start, 1);

		long wakeElapsedByBps = 0, wakeElapsedByMps = 0;
		currentBPS.set(byteCount.get() * 1000L / elapsed);
		currentMPS.set(msgCount.get() * 1000L / elapsed);
		if (currentBPS.get() > maxBPS) {
			wakeElapsedByBps = (byteCount.get() * 1000L / maxBPS);
		}	
		if (currentMPS.get() > maxMPS) {
			wakeElapsedByMps = (msgCount.get() * 1000L / maxMPS);
		}	
		// sleep for the longest of the too
		long wakeElapsed = Math.max(wakeElapsedByBps, wakeElapsedByMps);
		long sleepTime = wakeElapsed - elapsed;
		if (sleepTime > 0) {
			sleepCount.addAndGet(sleepTime);
			delayCount.incrementAndGet();
			Thread.sleep(sleepTime);
		}
	    return this;
    }

	@Override
    public Throttle reset() {
		byteCount.set(0);
		msgCount.set(0);
		sleepCount.set(0);
		delayCount.set(0);
		currentBPS.set(0);
		currentMPS.set(0);
		start = System.currentTimeMillis();
		return this;
	}

	@Override
    public long getStartTime() {
	    return start;
    }

	@Override
    public long getTotalBytes() {
	    return byteCount.get();
    }

	@Override
    public long getTotalMsgs() {
	    return msgCount.get();
    }

	@Override
    public long getLastDelayTime() {
	    return lastSleep.get();
    }

	@Override
    public long getTotalDelayTime() {
	    return sleepCount.get();
    }
	
	@Override
    public long getDelayCount() {
	    return delayCount.get();
    }
}
