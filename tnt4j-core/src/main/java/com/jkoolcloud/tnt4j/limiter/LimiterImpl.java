/*
 * Copyright 2014-2021 JKOOL, LLC.
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
	long idleReset = 0L; // time between limiter accesses before resetting (0 implies no idle reset)

	AtomicLong totalByteCount = new AtomicLong(0);
	AtomicLong totalMsgCount = new AtomicLong(0);
	AtomicLong totalDelayCount = new AtomicLong(0);
	AtomicLong totalDenyCount = new AtomicLong(0);

	AtomicDouble totalDelayTimeSec = new AtomicDouble(0);
	AtomicDouble lastDelaySec = new AtomicDouble(0);
	AtomicLong lastAccessTime = new AtomicLong(System.nanoTime());

	RateLimiter bpsLimiter = RateLimiter.create(MAX_RATE);
	RateLimiter mpsLimiter = RateLimiter.create(MAX_RATE);

	/**
	 * Create a a limiter with specified rate limits
	 *
	 * @param maxMps
	 *            max messages per second (0 -- no limit)
	 * @param maxBps
	 *            max bytes per second (0 -- no limit)
	 * @param enabled
	 *            true to enable limits, false otherwise
	 */
	public LimiterImpl(double maxMps, double maxBps, boolean enabled) {
		setLimits(maxMps, maxBps);
		setEnabled(enabled);
	}

	/**
	 * Access limiter and reset counters if needed
	 *
	 */
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
		if (accessTime > prev) {
			lastAccessTime.compareAndSet(prev, accessTime);
		}
	}

	/**
	 * Count the number of messages and bytes
	 *
	 * @param msgs
	 *            message count
	 * @param bytes
	 *            byte count
	 */
	protected void count(int msgs, int bytes) {
		accessLimiter();
		if (bytes > 0) {
			totalByteCount.addAndGet(bytes);
		}
		if (msgs > 0) {
			totalMsgCount.addAndGet(msgs);
		}
	}

	/**
	 * Count time since last limiter access in ms
	 *
	 * @param accessTimeNanos
	 *            time counter in nanoseconds
	 * @return number of ms since last access
	 */
	protected long timeSinceLastReset(long accessTimeNanos) {
		return TimeUnit.NANOSECONDS.toMillis(accessTimeNanos - lastAccessTime.get());
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
		return mpsLimiter.getRate() == MAX_RATE ? 0 : mpsLimiter.getRate();
	}

	@Override
	public double getMaxBPS() {
		return bpsLimiter.getRate() == MAX_RATE ? 0 : bpsLimiter.getRate();
	}

	@Override
	public Limiter setLimits(double maxMps, double maxBps) {
		mpsLimiter.setRate(maxMps <= UNLIMITED_RATE ? MAX_RATE : maxMps);
		bpsLimiter.setRate(maxBps <= UNLIMITED_RATE ? MAX_RATE : maxBps);
		return this;
	}

	@Override
	public double getMPS() {
		return (totalMsgCount.get() * 1000.0) / getAge();
	}

	@Override
	public double getBPS() {
		return (totalByteCount.get() * 1000.0) / getAge();
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
		count(msgs, bytes);
		boolean permit = true;
		try {
			if (isEnabled()) {
				permit = bytes <= 0 || bpsLimiter.tryAcquire(bytes, timeout, unit);
				permit = msgs <= 0
						|| (!permit ? mpsLimiter.tryAcquire(msgs, timeout, unit) : mpsLimiter.tryAcquire(msgs));
			}
		} finally {
			if (!permit) {
				totalDenyCount.incrementAndGet();
			}
		}
		return permit;
	}

	@Override
	public double obtain(int msgs, int bytes) {
		count(msgs, bytes);
		double delayTimeSec = 0;
		try {
			if (isEnabled()) {
				delayTimeSec = bytes > 0 ? bpsLimiter.acquire(bytes) : 0;
				delayTimeSec += msgs > 0 ? mpsLimiter.acquire(msgs) : 0;
			}
		} finally {
			if (delayTimeSec > 0) {
				lastDelaySec.set(delayTimeSec);
				totalDelayTimeSec.addAndGet(delayTimeSec);
				totalDelayCount.incrementAndGet();
			}
		}
		return delayTimeSec;
	}

	@Override
	public Limiter reset() {
		totalByteCount.set(0);
		totalMsgCount.set(0);
		totalDelayTimeSec.set(0);
		totalDelayCount.set(0);
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
		return totalByteCount.get();
	}

	@Override
	public long getTotalMsgs() {
		return totalMsgCount.get();
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
		return totalDelayCount.get();
	}

	@Override
	public long getDenyCount() {
		return totalDenyCount.get();
	}

	@Override
	public long getTimeSinceLastAccess() {
		return timeSinceLastReset(System.nanoTime());
	}
}
