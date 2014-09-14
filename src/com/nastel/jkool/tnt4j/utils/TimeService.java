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
package com.nastel.jkool.tnt4j.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.sink.DefaultEventSinkFactory;
import com.nastel.jkool.tnt4j.sink.EventSink;


/**
 * This class implements a time service that delivers synchronized time using NTP.
 * Developers should use <code>TimeService.currentTimeMillis()</code> instead of calling
 * <code>System.currentTimeMillis()</code> to obtain synchronized and adjusted current time.
 * To enable NTP time synchronization set the following property: 
 * <code>tnt4j.time.server=ntp-server:port</code>,
 * otherwise <code>System.currentTimeMillis()</code> is returned.
 * 
 * @version $Revision: 1 $
 */
public class TimeService {
	private static EventSink logger = DefaultEventSinkFactory.defaultEventSink(TimeService.class);
	private static final String TIME_SERVER = System.getProperty("tnt4j.time.server");
	private static final int TIME_SERVER_TIMEOUT = Integer.getInteger("tnt4j.time.server.timeout", 10000);
	private static final int TIME_CLOCK_SYNC_INTERVAL = Integer.getInteger("tnt4j.time.server.sync.interval", 120000);

	static long timeOverheadNanos = 0;
	static long timeOverheadMillis = 0;
	static long adjustment = 0;
	static long updatedTime = 0;
	static ScheduledExecutorService scheduler;
	
	static NTPUDPClient timeServer = new NTPUDPClient();
	static TimeInfo timeInfo;
		
	static {
		try {
			timeOverheadNanos = calculateOverhead(1000000);
			timeOverheadMillis = (timeOverheadNanos/1000000);
			updateTime();
			if (timeInfo != null) {
				logger.log(OpLevel.INFO, "Time server={0}, timeout.ms={1}, offset.ms={2}, delay.ms={3}, clock.adjust.ms={4}, overhead.nsec={5}",
					TIME_SERVER, TIME_SERVER_TIMEOUT, 
					timeInfo.getOffset(), timeInfo.getDelay(),
					adjustment, timeOverheadNanos);
			}
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, 
					"Unable to obtain NTP time: time.server={0}, timeout={1}",
					TIME_SERVER, TIME_SERVER_TIMEOUT, e);
        } finally {
        	scheduleUpdates();
        }
	}
	
	/**
	 * Schedule automatic clock synchronization with NTP and internal clocks
	 * 
	 */
	private static void scheduleUpdates() {
		scheduler = Executors.newScheduledThreadPool(1, new TimeServiceThreadFactory("TimeService/task-"));
		scheduler.scheduleAtFixedRate(new TimeClockSync(logger), 
				TIME_CLOCK_SYNC_INTERVAL, TIME_CLOCK_SYNC_INTERVAL, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Obtain NTP connection host:port of the time server.
	 * 
	 */
	public static String getTimeServer() {
		return TIME_SERVER;
	}
	
	/**
	 * Obtain time stamp when the NTP time was synchronized
	 * 
	 */
	public static long getLastUpdatedMillis() {
		return updatedTime;
	}
	
	/**
	 * Obtain configured NTP server timeout
	 * 
	 */
	public static int getTimeServerTimeout() {
		return TIME_SERVER_TIMEOUT;
	}
	
	/**
	 * Obtain NTP time and synchronize with NTP server
	 * 
	 */
	public static void updateTime() throws IOException {
		if (TIME_SERVER != null) {
			timeServer.setDefaultTimeout(TIME_SERVER_TIMEOUT);		
			String [] pair = TIME_SERVER.split(":");
			InetAddress hostAddr = InetAddress.getByName(pair[0]);
			timeInfo = pair.length < 2? timeServer.getTime(hostAddr): timeServer.getTime(hostAddr, Integer.parseInt(pair[1]));
			timeInfo.computeDetails();     
			adjustment = timeInfo.getOffset() - timeOverheadMillis;
			updatedTime = currentTimeMillis();
		}
	}
	
	/**
	 * Obtain measured overhead of calling <code>TimeService.currentTimeMillis()</code> in nanoseconds.
	 * 
	 */
	public static long getOverheadNanos() {
		return  timeOverheadNanos;
	}
	
	/**
	 * Obtain NTP synchronized current time in milliseconds
	 * 
	 */
	public static long currentTimeMillis() {
		return System.currentTimeMillis() + adjustment;
	}
	
	/**
	 * Obtain NTP synchronized current time in microseconds
	 * 
	 */
	public static long currentTimeUsecs() {
		return (System.currentTimeMillis() + adjustment)*1000;
	}
	
	/**
	 * Calculate overhead of <code>TimeService.currentTimeMillis()</code> based on a given number of
	 * iterations.
	 * 
	 * @param runs number of iterations
	 * 
	 */
	public static long calculateOverhead(long runs) {
		long start = System.nanoTime();
		_calculateOverheadCost(runs);
		for (int i=0; i < runs; i++) {
			currentTimeMillis();
		}
		return ((System.nanoTime() - start)/runs);
	}
	
	private static long _calculateOverheadCost(long runs) {
		return runs;
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
		Thread task = new Thread(r, prefix + count++);
		task.setDaemon(true);
		return task;
    }	
}

class TimeClockSync implements Runnable {
	EventSink logger;
	
	TimeClockSync(EventSink lg) {
		logger = lg;
	}
	
	@Override
	public void run() {
		try {
			TimeService.updateTime();
			Useconds.CURRENT.sync();
		} catch (Throwable ex) {
			logger.log(OpLevel.ERROR, "Failed to update clocks: last.updated={0}, age.ms={1}", 
					new Date(TimeService.getLastUpdatedMillis()),
					(TimeService.currentTimeMillis() - TimeService.getLastUpdatedMillis()), ex);
		}
	}	
}