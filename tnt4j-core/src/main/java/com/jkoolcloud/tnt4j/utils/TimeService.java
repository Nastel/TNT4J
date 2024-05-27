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
package com.jkoolcloud.tnt4j.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;

/**
 * This class implements a time service that delivers synchronized time using NTP. Developers should use
 * {@link TimeService#currentTimeMillis()} instead of calling {@link System#currentTimeMillis()} to obtain synchronized
 * and adjusted current time. To enable NTP time synchronization set the following property:
 * {@code tnt4j.time.server=ntp-server:port}, otherwise {@link System#currentTimeMillis()} is returned.
 *
 * @version $Revision: 1 $
 */
public class TimeService {
	private static EventSink logger = DefaultEventSinkFactory.defaultEventSink(TimeService.class);

	protected static final int ONE_K = 1000;
	protected static final int ONE_M = 1000000;
	protected static boolean TIME_SERVER_VERBOSE = Boolean.getBoolean("tnt4j.time.server.verbose");

	private static final String TIME_SERVER = System.getProperty("tnt4j.time.server");
	private static final long TIME_SERVER_TIMEOUT = Long.getLong("tnt4j.time.server.timeout", 10000);

	static boolean verbose = TIME_SERVER_VERBOSE;
	static long timeOverheadNanos = 0;
	static long timeOverheadMillis = 0;
	static long adjustment = 0;
	static long updatedTime = 0;
	private static ScheduledExecutorService scheduler;
	static ClockDriftMonitorTask clockSyncTask = null;

	static NTPUDPClient timeServer = new NTPUDPClient();
	static TimeInfo timeInfo;

	static {
		initScheduleUpdates();
	}

	private TimeService() {
	}

	private static void initScheduleUpdates() {
		try {
			timeOverheadNanos = calculateOverhead(ONE_M);
			timeOverheadMillis = (timeOverheadNanos / ONE_M);
			updateTime();
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, "Unable to obtain NTP time: time.server={}, timeout={}", TIME_SERVER,
					TIME_SERVER_TIMEOUT, e);
		} finally {
			scheduleUpdates();
		}
	}

	/**
	 * Schedule automatic clock synchronization with NTP and internal clocks.
	 */
	private static synchronized void scheduleUpdates() {
		if (scheduler == null) {
			scheduler = Executors.newScheduledThreadPool(1, new TimeServiceThreadFactory("TimeService/clock-sync"));
			clockSyncTask = new ClockDriftMonitorTask(logger);
			scheduler.submit(clockSyncTask);
		}
	}

	/**
	 * Enable/disable verbose level.
	 * 
	 * @param flag
	 *            verbose flag
	 */
	public static void setVerbose(boolean flag) {
		verbose = flag;
	}

	/**
	 * Obtain NTP connection host:port of the time server.
	 *
	 * @return time server connection string
	 */
	public static String getTimeServer() {
		return TIME_SERVER;
	}

	/**
	 * Obtain time stamp when the NTP time was synchronized.
	 *
	 * @return time stamp when NTP was updated
	 */
	public static long getLastUpdatedMillis() {
		return updatedTime;
	}

	/**
	 * Obtain configured NTP server timeout.
	 *
	 * @return time server timeout in milliseconds
	 */
	public static long getTimeServerTimeout() {
		return TIME_SERVER_TIMEOUT;
	}

	/**
	 * Obtain NTP time and synchronize with NTP server.
	 *
	 * @throws IOException
	 *             if error accessing time-server
	 */
	public static void updateTime() throws IOException {
		if (TIME_SERVER != null) {
			timeServer.setDefaultTimeout(Duration.of(TIME_SERVER_TIMEOUT, ChronoUnit.MILLIS));
			String[] pair = TIME_SERVER.split(":");
			InetAddress hostAddr = InetAddress.getByName(pair[0]);
			timeInfo = pair.length < 2 ? timeServer.getTime(hostAddr)
					: timeServer.getTime(hostAddr, Integer.parseInt(pair[1]));
			timeInfo.computeDetails();
			adjustment = timeInfo.getOffset() - timeOverheadMillis;
			updatedTime = currentTimeMillis();
			if (verbose) {
				logger.log(OpLevel.DEBUG,
						"Time server={}, timeout.ms={}, offset.ms={}, delay.ms={}, clock.adjust.ms={}, overhead.nsec={}",
						TIME_SERVER, TIME_SERVER_TIMEOUT, timeInfo.getOffset(), timeInfo.getDelay(), adjustment,
						timeOverheadNanos);
			}
		}
	}

	/**
	 * Obtain measured overhead of calling {@link com.jkoolcloud.tnt4j.utils.TimeService#currentTimeMillis()} in
	 * nanoseconds.
	 *
	 * @return total measured overhead in nanoseconds
	 */
	public static long getOverheadNanos() {
		return timeOverheadNanos;
	}

	/**
	 * Obtain number of milliseconds since NTP time was synchronized.
	 *
	 * @return time (ms) since last NTP synchronization
	 */
	public static long getUpdateAgeMillis() {
		return getLastUpdatedMillis() > 0 ? currentTimeMillis() - getLastUpdatedMillis() : -1;
	}

	/**
	 * Obtain NTP synchronized current time in milliseconds.
	 *
	 * @return current NTP synchronized time in milliseconds
	 */
	public static long currentTimeMillis() {
		return System.currentTimeMillis() + adjustment;
	}

	/**
	 * Obtain NTP synchronized current time in microseconds precision (but necessarily accuracy).
	 *
	 * @return current NTP synchronized time in microseconds
	 */
	public static long currentTimeUsecs() {
		return (System.currentTimeMillis() + adjustment) * ONE_K;
	}

	/**
	 * Obtain currently measured clock drift in milliseconds.
	 *
	 * @return clock drift in milliseconds
	 */
	public static long getDriftMillis() {
		return clockSyncTask.getDriftMillis();
	}

	/**
	 * Obtain measured total clock drift in milliseconds since start up.
	 *
	 * @return total clock drift since start up
	 */
	public static long getTotalDriftMillis() {
		return clockSyncTask.getTotalDriftMillis();
	}

	/**
	 * Obtain total number of times clocks have been updated to adjust for drift.
	 *
	 * @return number of times updated to adjust for cock drift
	 */
	public static long getDriftUpdateCount() {
		return clockSyncTask.getDriftUpdateCount();
	}

	/**
	 * Obtain currently measured clock drift interval in milliseconds.
	 *
	 * @return clock drift interval in milliseconds
	 */
	public static long getDriftIntervalMillis() {
		return clockSyncTask.getIntervalMillis();
	}

	/**
	 * Calculate overhead of {@link com.jkoolcloud.tnt4j.utils.TimeService#currentTimeMillis()} based on a given number
	 * of iterations.
	 *
	 * @param runs
	 *            number of iterations
	 * @return calculated overhead of getting timestamp
	 */
	public static long calculateOverhead(long runs) {
		long start = System.nanoTime();
		for (int i = 0; i < runs; i++) {
			currentTimeMillis();
		}
		return ((System.nanoTime() - start) / runs);
	}

	public static void main(String[] args) throws IOException, NumberFormatException, InterruptedException {
		setVerbose(true);
		Thread.sleep(Long.parseLong(args[0]));
	}
}

class TimeServiceThreadFactory implements ThreadFactory {
	int count = 0;
	String prefix;

	TimeServiceThreadFactory(String pfix) {
		prefix = pfix;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread task = new Thread(r, prefix + "-" + count++);
		task.setDaemon(true);
		return task;
	}
}

class ClockDriftMonitorTask implements Runnable {
	private static final long TIME_CLOCK_DRIFT_SAMPLE = Integer.getInteger("tnt4j.time.server.drift.sample.ms", 10000);
	private static final long TIME_CLOCK_DRIFT_LIMIT = Integer.getInteger("tnt4j.time.server.drift.limit.ms", 1);

	long interval, drift, updateCount = 0, totalDrift;
	EventSink logger;

	ClockDriftMonitorTask(EventSink lg) {
		logger = lg;
	}

	public long getIntervalMillis() {
		return interval;
	}

	public long getDriftMillis() {
		return drift;
	}

	public long getTotalDriftMillis() {
		return totalDrift;
	}

	public long getDriftUpdateCount() {
		return updateCount;
	}

	private void syncClocks() {
		try {
			TimeService.updateTime();
			Useconds.CURRENT.sync();
			updateCount++;
			if (TimeService.verbose) {
				logger.log(OpLevel.DEBUG, "Updated clocks: drift.ms={}, interval.ms={}, total.drift.ms={}, updates={}",
						drift, interval, totalDrift, updateCount);
			}
		} catch (Throwable ex) {
			logger.log(OpLevel.ERROR, "Failed to update clocks: last.updated={}, age.ms={}",
					new Date(TimeService.getLastUpdatedMillis()), TimeService.getUpdateAgeMillis(), ex);
		}
	}

	@Override
	public void run() {
		long start = System.nanoTime();
		long base = System.currentTimeMillis() - (start / TimeService.ONE_M);

		while (true) {
			try {
				Thread.sleep(TIME_CLOCK_DRIFT_SAMPLE);
			} catch (InterruptedException e) {
			}
			long now = System.nanoTime();
			drift = System.currentTimeMillis() - (now / TimeService.ONE_M) - base;
			totalDrift += Math.abs(drift);
			interval = (now - start) / TimeService.ONE_M;
			if (Math.abs(drift) >= TIME_CLOCK_DRIFT_LIMIT) {
				syncClocks();
				start = System.nanoTime();
				base = System.currentTimeMillis() - (start / TimeService.ONE_M);
			}
		}
	}
}
