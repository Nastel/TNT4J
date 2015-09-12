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
package com.nastel.jkool.tnt4j.limiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.RateLimiter;

/**
 * Default rate limiter implementation (thread safe) based on Google Guava Library
 * {@code https://code.google.com/p/guava-libraries/}
 *
 * @version $Revision: 1 $
 */
public class LimiterImpl implements Limiter {
	
	boolean doLimit = false;
	double maxMPS = UNLIMITED, maxBPS = UNLIMITED;
	long start = System.currentTimeMillis();
	
	AtomicLong byteCount = new AtomicLong(0);
	AtomicLong msgCount = new AtomicLong(0);
	AtomicLong delayCount = new AtomicLong(0);
	AtomicLong denyCount = new AtomicLong(0);

	AtomicDouble sleepCount = new AtomicDouble(0);
	AtomicDouble lastSleep = new AtomicDouble(0);
	
	RateLimiter bpsLimiter, mpsLimiter;
	
	public LimiterImpl(double maxMps, double maxBps, boolean enabled) {
		setLimits(maxMps, maxBps);
		setEnabled(enabled);
	}
	
	@Override
    public double getMaxMPS() {
	    return maxMPS;
    }

	@Override
    public double getMaxBPS() {
	    return maxBPS;
    }

	@Override
    public Limiter setLimits(double maxMps, double maxBps) {
		maxMPS = maxMps;
		maxBPS = maxBps;
	    return this;
    }

	@Override
    public double getMPS() {
		return (double)(msgCount.get() * 1000.0 / (double)getAge());
    }

	@Override
    public double getBPS() {
		return (double)(byteCount.get() * 1000.0 / (double)getAge());
    }

	@Override
    public Limiter setEnabled(boolean flag) {
		doLimit = flag;
		if (doLimit) {
			start = System.currentTimeMillis();
			bpsLimiter = RateLimiter.create(maxBPS);
			mpsLimiter = RateLimiter.create(maxMPS);
		}
	    return this;
    }

	@Override
    public boolean isEnabled() {
	    return doLimit;
    }

	@Override
    public boolean tryObtain(int msgCount, int byteCount) {
	    return tryObtain(msgCount, byteCount, 0, TimeUnit.SECONDS);
    }

	@Override
    public boolean tryObtain(int msgs, int bytes, long timeout, TimeUnit unit) {
		count(msgs, bytes);
		if (!doLimit || (msgs == 0 && bytes == 0)) {
			return true;
		}

		boolean permit = false;
		if (maxBPS > UNLIMITED) {
			permit = bpsLimiter.tryAcquire(bytes, timeout, unit);
		}	
		if (maxMPS > UNLIMITED) {
			permit = permit && mpsLimiter.tryAcquire(msgs, timeout, unit);
		}	
		if (!permit) {
			denyCount.incrementAndGet();
		}
		return permit;
	}
	
	@Override
    public double obtain(int msgs, int bytes) {
		count(msgs, bytes);
		if (!doLimit || ( msgs == 0 && bytes == 0)) {
			return 0;
		}
		
		double elapsedSecByBps = 0;
		double elapsedSecByMps = 0;
		
		int delayCounter = 0;
		if (maxBPS > UNLIMITED) {
			elapsedSecByBps = bpsLimiter.acquire(bytes);
			if (elapsedSecByBps > 0) delayCounter++;
		}	
		if (maxMPS > UNLIMITED) {
			elapsedSecByMps = mpsLimiter.acquire(msgs);
			if (elapsedSecByMps > 0) delayCounter++;
		}	
		double sleepTime = elapsedSecByBps + elapsedSecByMps;
		if (sleepTime > 0) {
			lastSleep.set(sleepTime);
			sleepCount.addAndGet(sleepTime);
			delayCount.addAndGet(delayCounter);
		}
	    return sleepTime;
	}

	protected void count(int msgs, int bytes) {
		if (bytes > 0) {
			byteCount.addAndGet(bytes);
		}
		if (msgs > 0) {
			msgCount.addAndGet(msgs);
		}			
	}
	
	@Override
    public Limiter reset() {
		byteCount.set(0);
		msgCount.set(0);
		sleepCount.set(0);
		delayCount.set(0);
		start = System.currentTimeMillis();
		return this;
	}

	@Override
    public long getStartTime() {
	    return start;
    }

	@Override
	public long getAge() {
		return Math.max(System.currentTimeMillis() - start, 1);
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
    public double getLastDelayTime() {
	    return lastSleep.get();
    }

	@Override
    public double getTotalDelayTime() {
	    return sleepCount.get();
    }
	
	@Override
    public long getDelayCount() {
	    return delayCount.get();
    }

	@Override
    public long getDenyCount() {
	    return denyCount.get();
    }
}
