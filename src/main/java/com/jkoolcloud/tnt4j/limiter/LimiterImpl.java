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
package com.jkoolcloud.tnt4j.limiter;

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
	long start = System.currentTimeMillis();
	long idleReset = 0L;	// time between limiter accesses before resetting (0 implies no idle reset)

	AtomicLong byteCount = new AtomicLong(0);
	AtomicLong msgCount = new AtomicLong(0);
	AtomicLong delayCount = new AtomicLong(0);
	AtomicLong denyCount = new AtomicLong(0);

	AtomicDouble totalDelayTimeSec = new AtomicDouble(0);
	AtomicDouble lastDelaySec = new AtomicDouble(0);
	AtomicLong lastAccessTime = new AtomicLong(System.nanoTime());

	RateLimiter bpsLimiter =  RateLimiter.create(MAX_RATE);
	RateLimiter mpsLimiter =  RateLimiter.create(MAX_RATE);

	public LimiterImpl(double maxMps, double maxBps, boolean enabled) {
		setLimits(maxMps, maxBps);
		setEnabled(enabled);
	}

	@Override
	public long getIdleReset() {
		return idleReset;
	}

	@Override
	public Limiter setIdleReset(long idleResetMs) {
		this.idleReset = idleResetMs;
		return this;
	}

	@Override
    public double getMaxMPS() {
	    return mpsLimiter.getRate();
    }

	@Override
    public double getMaxBPS() {
	    return bpsLimiter.getRate();
    }

	@Override
    public Limiter setLimits(double maxMps, double maxBps) {
		mpsLimiter.setRate(maxMps <= 0.0D ? MAX_RATE : maxMps);
		bpsLimiter.setRate(maxBps <= 0.0D ? MAX_RATE : maxBps);
		return this;
    }

	@Override
    public double getMPS() {
		return msgCount.get() * 1000.0 / getAge();
    }

	@Override
    public double getBPS() {
		return byteCount.get() * 1000.0 / getAge();
    }

	@Override
    public Limiter setEnabled(boolean flag) {
		doLimit = flag;
		if (doLimit) {
			reset();
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
		accessLimiter();
		count(msgs, bytes);
		if (!doLimit || (msgs == 0 && bytes == 0)) {
			return true;
		}

		boolean permit = true;
		if (bytes > 0) {
			permit = bpsLimiter.tryAcquire(bytes, timeout, unit);
		}
		if (msgs > 0 && !permit) {
			permit = permit && mpsLimiter.tryAcquire(msgs, timeout, unit);
		}
		if (!permit) {
			denyCount.incrementAndGet();
		}
		return permit;
	}

	@Override
	public double obtain(int msgs, int bytes) {
		accessLimiter();
		double delayTimeSec = 0;
		try {
			if (!isEnabled() || (msgs == 0 && bytes == 0)) {
				return 0;
			}
			delayTimeSec = bytes > 0 ? bpsLimiter.acquire(bytes) : 0;
			if (delayTimeSec == 0) {
				delayTimeSec += (msgs > 0 ? mpsLimiter.acquire(msgs) : 0);
			} else {
				mpsLimiter.tryAcquire(msgs, 0, TimeUnit.MILLISECONDS);
			}
		} finally {
			count(msgs, bytes);
			if (delayTimeSec > 0) {
				lastDelaySec.set(delayTimeSec);
				totalDelayTimeSec.addAndGet(delayTimeSec);
				delayCount.incrementAndGet();
			}
		}
		return delayTimeSec;
	}

	protected void count(int msgs, int bytes) {
		if (bytes > 0) {
			byteCount.addAndGet(bytes);
		}
		if (msgs > 0) {
			msgCount.addAndGet(msgs);
		}
	}

	protected long timeSinceLastReset(long accessTime) {
		return TimeUnit.NANOSECONDS.toMillis(accessTime - lastAccessTime.get());		
	}
	
	protected void accessLimiter() {
		long accessTime = System.nanoTime();
		if (isEnabled() && (timeSinceLastReset(accessTime) > idleReset)) {
			synchronized (this) {
				if (timeSinceLastReset(accessTime) > idleReset) {
					reset();				
				}
			}
		}
		long prev = lastAccessTime.get();
		if (accessTime > prev)
			lastAccessTime.compareAndSet(prev, accessTime);
	}

	@Override
    public Limiter reset() {
		byteCount.set(0);
		msgCount.set(0);
		totalDelayTimeSec.set(0);
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
	    return lastDelaySec.get();
    }

	@Override
    public double getTotalDelayTime() {
	    return totalDelayTimeSec.get();
    }

	@Override
    public long getDelayCount() {
	    return delayCount.get();
    }

	@Override
    public long getDenyCount() {
	    return denyCount.get();
    }

	@Override
	public long getTimeSinceLastAccess() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastAccessTime.get());		
	}
}
