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
package com.jkoolcloud.tnt4j.core;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.utils.Useconds;
import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Represents a timestamp that has microsecond accuracy. This timestamp also implements Lamport clock synchronization
 * algorithm see {@link UsecTimestamp#getLamportClock()} and {@link UsecTimestamp#UsecTimestamp(long, long, long)}.
 * </p>
 *
 * <p>
 * Stores timestamp as <i>mmmmmmmmmm.uuu</i>, where <i>mmmmmmmmmm</i> is the timestamp in milliseconds, and <i>uuu</i>
 * is the fractional microseconds.
 * </p>
 *
 * @version $Revision: 6 $
 */
public class UsecTimestamp extends Number implements Comparable<UsecTimestamp>, Cloneable, Serializable {
	private static final long serialVersionUID = 3658590467907047916L;

	protected static final int SECS_SCALE = 1000;
	protected static final int MAX_USEC = SECS_SCALE - 1;

	private static final String DFLT_JAVA_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String DEFAULT_FORMAT = DFLT_JAVA_FORMAT + "SSS z";
	private static final TimeZone DEFAULT_TZ = TimeZone.getDefault();// TimeZone.getTimeZone("UTC");

	protected static AtomicLong LamportCounter = new AtomicLong(System.currentTimeMillis());

	private long msecs;
	private long usecs;
	private long currentLamportClock = LamportCounter.incrementAndGet();

	/**
	 * Creates UsecTimestamp based on current time with microsecond precision/accuracy
	 *
	 * @see com.jkoolcloud.tnt4j.utils.Utils#currentTimeUsec
	 */
	public UsecTimestamp() {
		this(Useconds.CURRENT.get());
	}

	/**
	 * Creates UsecTimestamp based on specified microsecond timestamp.
	 *
	 * @param usecTime
	 *            timestamp, in microsecond
	 * @throws IllegalArgumentException
	 *             if usecTime is negative
	 */
	public UsecTimestamp(long usecTime) {
		setTimeUsec(usecTime);
	}

	/**
	 * Creates UsecTimestamp based on specified microsecond timestamp.
	 *
	 * @param usecTime
	 *            timestamp, in microsecond
	 * @throws IllegalArgumentException
	 *             if usecTime is negative
	 */
	public UsecTimestamp(Number usecTime) {
		setTimeUsec(usecTime.longValue());
	}

	/**
	 * Creates UsecTimestamp based on specified millisecond timestamp and fractional microsecond.
	 *
	 * @param msecs
	 *            timestamp, in milliseconds
	 * @param usecs
	 *            fraction microseconds
	 * @throws IllegalArgumentException
	 *             if any arguments are negative, or if usecs is greater than {@value #MAX_USEC}
	 */
	public UsecTimestamp(long msecs, long usecs) {
		setTimestampValues(msecs, usecs, 0);
	}

	/**
	 * Creates UsecTimestamp based on specified millisecond timestamp and fractional microsecond. This timestamp Lamport
	 * clock will be computed based on the specified sender's Lamport clock value.
	 *
	 * @param msecs
	 *            timestamp, in milliseconds
	 * @param usecs
	 *            fraction microseconds
	 * @param recvdLamportClock
	 *            Lamport clock of the received event (0 do not compute Lamport clock)
	 * @throws IllegalArgumentException
	 *             if any arguments are negative, or if usecs is greater than {@value #MAX_USEC}
	 */
	public UsecTimestamp(long msecs, long usecs, long recvdLamportClock) {
		setTimestampValues(msecs, usecs, recvdLamportClock);
	}

	/**
	 * Creates UsecTimestamp based on specified {@code timestamp}, providing time in milliseconds resolution, and
	 * fractional microsecond {@code usecs}.
	 *
	 * @param timestamp
	 *            database timestamp, milliseconds resolution
	 * @param usecs
	 *            fraction microseconds
	 * @throws NullPointerException
	 *             if timestamp is {@code null}
	 * @throws IllegalArgumentException
	 *             if usecs is greater than 999999
	 */
	public UsecTimestamp(Timestamp timestamp, long usecs) {
		initFromTimestamp(timestamp, usecs);
	}

	/**
	 * Creates UsecTimestamp based on specified {@code instant}, providing time in milliseconds resolution, and
	 * fractional microsecond {@code usecs}.
	 *
	 * @param instant
	 *            instant time, milliseconds resolution
	 * @param usecs
	 *            fraction microseconds
	 * @throws NullPointerException
	 *             if timestamp is {@code null}
	 * @throws IllegalArgumentException
	 *             if usecs is greater than 999999
	 */
	public UsecTimestamp(Instant instant, long usecs) {
		this(instant.toEpochMilli(), usecs);
	}

	/**
	 * <p>
	 * Creates UsecTimestamp from string representation of timestamp in the specified format.
	 * </p>
	 * <p>
	 * This is based on {@link SimpleDateFormat}, but extends its support to recognize microsecond fractional seconds.
	 * If number of fractional second characters is greater than 3, then it's assumed to be microseconds. Otherwise,
	 * it's assumed to be milliseconds (as this is the behavior of {@link SimpleDateFormat}.
	 *
	 * @param timeStampStr
	 *            timestamp string
	 * @throws NullPointerException
	 *             if timeStampStr is {@code null}
	 * @throws IllegalArgumentException
	 *             if timeStampStr is not in the correct format
	 * @throws ParseException
	 *             if failed to parse string based on specified format
	 */
	public UsecTimestamp(String timeStampStr) throws ParseException {
		this(timeStampStr, DEFAULT_FORMAT, DEFAULT_TZ);
	}

	/**
	 * <p>
	 * Creates UsecTimestamp from string representation of timestamp in the default format.
	 * </p>
	 * <p>
	 * This is based on {@link SimpleDateFormat}, but extends its support to recognize microsecond fractional seconds.
	 * If number of fractional second characters is greater than 3, then it's assumed to be microseconds. Otherwise,
	 * it's assumed to be milliseconds (as this is the behavior of {@link SimpleDateFormat}.
	 *
	 * @param timeStampStr
	 *            timestamp string
	 * @param formatStr
	 *            format specification for timestamp string
	 * @throws NullPointerException
	 *             if timeStampStr is {@code null}
	 * @throws IllegalArgumentException
	 *             if timeStampStr is not in the correct format
	 * @throws ParseException
	 *             if failed to parse string based on default format
	 */
	public UsecTimestamp(String timeStampStr, String formatStr) throws ParseException {
		this(timeStampStr, formatStr, DEFAULT_TZ);
	}

	/**
	 * <p>
	 * Creates UsecTimestamp from string representation of timestamp in the specified format.
	 * </p>
	 * <p>
	 * This is based on {@link SimpleDateFormat}, but extends its support to recognize microsecond fractional seconds.
	 * If number of fractional second characters is greater than 3, then it's assumed to be microseconds. Otherwise,
	 * it's assumed to be milliseconds (as this is the behavior of {@link SimpleDateFormat}.
	 *
	 * @param timeStampStr
	 *            timestamp string
	 * @param formatStr
	 *            format specification for timestamp string
	 * @param timeZoneId
	 *            time zone that timeStampStr represents. This is only needed when formatStr does not include time zone
	 *            specification and timeStampStr does not represent a string in local time zone.
	 * @throws NullPointerException
	 *             if timeStampStr is {@code null}
	 * @throws IllegalArgumentException
	 *             if timeStampStr is not in the correct format
	 * @throws ParseException
	 *             if failed to parse string based on specified format
	 * @see java.util.TimeZone
	 */
	public UsecTimestamp(String timeStampStr, String formatStr, String timeZoneId) throws ParseException {
		this(timeStampStr, formatStr, (StringUtils.isEmpty(timeZoneId) ? null : TimeZone.getTimeZone(timeZoneId)));
	}

	/**
	 * <p>
	 * Creates UsecTimestamp from string representation of timestamp in the specified format.
	 * </p>
	 * <p>
	 * This is based on {@link SimpleDateFormat}, but extends its support to recognize microsecond fractional seconds.
	 * If number of fractional second characters is greater than 3, then it's assumed to be microseconds. Otherwise,
	 * it's assumed to be milliseconds (as this is the behavior of {@link SimpleDateFormat}.
	 *
	 * @param timeStampStr
	 *            timestamp string
	 * @param formatStr
	 *            format specification for timestamp string
	 * @param timeZoneId
	 *            time zone that timeStampStr represents. This is only needed when formatStr does not include time zone
	 *            specification and timeStampStr does not represent a string in local time zone.
	 * @param locale
	 *            locale for date format to use.
	 * @throws NullPointerException
	 *             if timeStampStr is {@code null}
	 * @throws IllegalArgumentException
	 *             if timeStampStr is not in the correct format
	 * @throws ParseException
	 *             if failed to parse string based on specified format
	 * @see java.util.TimeZone
	 */
	public UsecTimestamp(String timeStampStr, String formatStr, String timeZoneId, String locale)
			throws ParseException {
		this(timeStampStr, formatStr, (StringUtils.isEmpty(timeZoneId) ? null : TimeZone.getTimeZone(timeZoneId)),
				locale);
	}

	/**
	 * <p>
	 * Creates UsecTimestamp from string representation of timestamp in the specified format.
	 * </p>
	 * <p>
	 * This is based on {@link SimpleDateFormat}, but extends its support to recognize microsecond fractional seconds.
	 * If number of fractional second characters is greater than 3, then it's assumed to be microseconds. Otherwise,
	 * it's assumed to be milliseconds (as this is the behavior of {@link SimpleDateFormat}.
	 *
	 * @param timeStampStr
	 *            timestamp string
	 * @param formatStr
	 *            format specification for timestamp string
	 * @param timeZone
	 *            time zone that timeStampStr represents. This is only needed when formatStr does not include time zone
	 *            specification and timeStampStr does not represent a string in local time zone.
	 * @throws NullPointerException
	 *             if timeStampStr is {@code null}
	 * @throws IllegalArgumentException
	 *             if timeStampStr is not in the correct format
	 * @throws ParseException
	 *             if failed to parse string based on specified format
	 * @see java.util.TimeZone
	 */
	public UsecTimestamp(String timeStampStr, String formatStr, TimeZone timeZone) throws ParseException {
		this(timeStampStr, formatStr, timeZone, null);
	}

	/**
	 * <p>
	 * Creates UsecTimestamp from string representation of timestamp in the specified format.
	 * </p>
	 * <p>
	 * This is based on {@link SimpleDateFormat}, but extends its support to recognize microsecond fractional seconds.
	 * If number of fractional second characters is greater than 3, then it's assumed to be microseconds. Otherwise,
	 * it's assumed to be milliseconds (as this is the behavior of {@link SimpleDateFormat}.
	 *
	 * @param timeStampStr
	 *            timestamp string
	 * @param formatStr
	 *            format specification for timestamp string
	 * @param timeZone
	 *            time zone that timeStampStr represents. This is only needed when formatStr does not include time zone
	 *            specification and timeStampStr does not represent a string in local time zone.
	 * @param locale
	 *            locale for date format to use.
	 * @throws NullPointerException
	 *             if timeStampStr is {@code null}
	 * @throws IllegalArgumentException
	 *             if timeStampStr is not in the correct format
	 * @throws ParseException
	 *             if failed to parse string based on specified format
	 * @see java.util.TimeZone
	 */
	public UsecTimestamp(String timeStampStr, String formatStr, TimeZone timeZone, String locale)
			throws ParseException {
		if (timeStampStr == null) {
			throw new NullPointerException("timeStampStr must be non-null");
		}

		int usecs = 0;
		SimpleDateFormat dateFormat;

		if (StringUtils.isEmpty(formatStr)) {
			dateFormat = new SimpleDateFormat();
		} else {
			// Java date formatter cannot deal with usecs, so we need to extract those ourselves
			int fFsecPos = formatStr.indexOf('S');
			if (fFsecPos > 0) {
				int fFsecEndPos = formatStr.lastIndexOf('S');
				int fFsecLen = fFsecEndPos - fFsecPos + 1;

				if (fFsecLen > 6) {
					throw new ParseException(
							"Date format containing more than 6 significant digits for fractional seconds is not supported",
							0);
				}

				int dFsecPos = adjustFsecPosition(fFsecPos, formatStr);
				if (dFsecPos > 2) {
					int dFsecEndPos;
					for (dFsecEndPos = dFsecPos; dFsecEndPos < timeStampStr.length(); dFsecEndPos++) {
						if (!StringUtils.containsAny("0123456789", timeStampStr.charAt(dFsecEndPos))) {
							break;
						}
					}

					StringBuilder sb = new StringBuilder();

					if (fFsecLen > 3) {
						// format specification represents more than milliseconds, assume microseconds
						try {
							String dUsecStr = timeStampStr.substring(dFsecPos, dFsecEndPos);

							if (dUsecStr.length() < fFsecLen) {
								dUsecStr = StringUtils.rightPad(dUsecStr, fFsecLen, '0');
							} else if (dUsecStr.length() > fFsecLen) {
								dUsecStr = dUsecStr.substring(0, fFsecLen);
							}
							usecs = Integer.parseInt(dUsecStr);

							// trim off fractional part < microseconds from both timestamp and format strings
							sb.append(timeStampStr);
							sb.delete(dFsecPos, dFsecEndPos);
							timeStampStr = sb.toString();
						} catch (IndexOutOfBoundsException exc) {
						}

						sb.setLength(0);
						sb.append(formatStr);
						sb.delete(fFsecPos, fFsecEndPos + 1);
						formatStr = sb.toString();
					} else if ((dFsecEndPos - dFsecPos) < 3 && (dFsecEndPos <= timeStampStr.length())) {
						// pad msec value in date string with 0's so that it is 3 digits long
						sb.append(timeStampStr);
						while ((dFsecEndPos - dFsecPos) < 3) {
							sb.insert(dFsecEndPos, '0');
							dFsecEndPos++;
						}
						timeStampStr = sb.toString();
					}
				}
			}

			dateFormat = StringUtils.isEmpty(locale) ? new SimpleDateFormat(formatStr)
					: new SimpleDateFormat(formatStr, Utils.getLocale(locale));
		}

		dateFormat.setLenient(true);
		if (timeZone != null) {
			dateFormat.setTimeZone(timeZone);
		}

		try {
			Date date = dateFormat.parse(timeStampStr);

			setTimestampValues(date.getTime(), 0, 0);
			add(usecs);
		} catch (ParseException pe) {
			throw new ParseException(pe.getMessage() + " using pattern '" + formatStr + "'", pe.getErrorOffset());
		}
	}

	/**
	 * Adjusts fractional seconds section start position in datetime string according to provided format pattern.
	 * <p>
	 * Pattern used quote symbols does not map to datetime string value 1:1, so position must be adjusted.
	 * 
	 * @param fFsecPos
	 *            format pattern string fractional seconds section start position, or negative value to find it
	 * @param formatStr
	 *            format pattern string
	 * @return adjusted fractional seconds section start position in datetime string
	 */
	protected static int adjustFsecPosition(int fFsecPos, String formatStr) {
		if (fFsecPos < 0) {
			fFsecPos = formatStr.indexOf('S');
		}
		String dtFormatStr = formatStr.substring(0, fFsecPos);
		int dqCount = StringUtils.countMatches(dtFormatStr, "''");
		int sqCount = StringUtils.countMatches(dtFormatStr, '\'');
		int qCount = sqCount - dqCount;

		return fFsecPos - qCount;
	}

	/**
	 * Creates UsecTimestamp based on specified UsecTimestamp.
	 *
	 * @param other
	 *            timestamp to copy
	 * @throws NullPointerException
	 *             if timestamp is {@code null}
	 */
	public UsecTimestamp(UsecTimestamp other) {
		this(other.msecs, other.usecs, other.getLamportClock());
	}

	/**
	 * Creates UsecTimestamp based on specified Date.
	 *
	 * @param date
	 *            timestamp to copy
	 * @throws NullPointerException
	 *             if date is {@code null}
	 */
	public UsecTimestamp(Date date) {
		setTimestampValues(date.getTime(), 0, 0);
	}

	/**
	 * Returns Lamport clock value of this time stamp (based on Lamport Clock algorithm)
	 *
	 * @return Lamport clock value
	 */
	public long getLamportClock() {
		return currentLamportClock;
	}

	/**
	 * Returns UsecTimestamp based on current time with microsecond precision/accuracy
	 *
	 * @return UsecTimestamp for current time
	 * @see com.jkoolcloud.tnt4j.utils.Utils#currentTimeUsec
	 */
	public static UsecTimestamp now() {
		return new UsecTimestamp();
	}

	/**
	 * Sets UsecTimestamp based on specified microsecond timestamp.
	 *
	 * @param usecTime
	 *            timestamp, in microsecond
	 * @return current UsecTimestamp instance
	 * @throws IllegalArgumentException
	 *             if usecTime is negative
	 */
	public UsecTimestamp setTimeUsec(long usecTime) {
		if (usecTime < 0) {
			throw new IllegalArgumentException("usecTime must be non-negative");
		}

		this.msecs = upscale(usecTime);
		this.usecs = usecTime - downscale(this.msecs);
		return this;
	}

	private static long upscale(long timeUnitValue) {
		return timeUnitValue / SECS_SCALE;
	}

	private static long downscale(long timeUnitValue) {
		return timeUnitValue * SECS_SCALE;
	}

	/**
	 * @see #UsecTimestamp(Timestamp, long)
	 */
	private void initFromTimestamp(Timestamp timestamp, long usecs) {
		if (timestamp == null) {
			throw new NullPointerException("timestamp must be non-null");
		}
		if (usecs < 0 || usecs > 999999) {
			throw new IllegalArgumentException("usecs must be in the range [0,999999], inclusive");
		}

		this.msecs = timestamp.getTime();
		if (usecs > MAX_USEC) {
			// extract milliseconds portion from usecs and add to msecs
			long msecs = upscale(usecs);
			this.msecs += msecs;
			usecs -= downscale(msecs);
		}
		this.usecs = usecs;
	}

	/**
	 * Creates UsecTimestamp based on specified millisecond timestamp and fractional microsecond.
	 *
	 * @param msecs
	 *            timestamp, in milliseconds
	 * @param usecs
	 *            fraction microseconds
	 * @param recvdLamportClock
	 *            received Lamport clock
	 * @throws IllegalArgumentException
	 *             if any arguments are negative, or if usecs is greater than {@value #MAX_USEC}
	 */
	protected void setTimestampValues(long msecs, long usecs, long recvdLamportClock) {
		if (msecs < 0) {
			throw new IllegalArgumentException("msecs must be non-negative");
		}
		if (usecs < 0 || usecs > MAX_USEC) {
			throw new IllegalArgumentException("usecs must be in the range [0,999], inclusive");
		}

		this.msecs = msecs;
		this.usecs = usecs;
		assignLamportClock(recvdLamportClock);
	}

	/**
	 * Assign local Lamport clock based on the value of the received Lamport clock.
	 *
	 * @param recvdLamportClock
	 *            received Lamport clock
	 */
	public void assignLamportClock(long recvdLamportClock) {
		while (recvdLamportClock > currentLamportClock) {
			if (LamportCounter.compareAndSet(currentLamportClock, recvdLamportClock + 1)) {
				currentLamportClock = recvdLamportClock + 1;
				break;
			} else {
				currentLamportClock = LamportCounter.incrementAndGet();
			}
		}
	}

	/**
	 * Gets current time stamp value to seconds resolution.
	 *
	 * @return timestamp, in seconds.
	 */
	public long getTimeSec() {
		return upscale(msecs);
	}

	/**
	 * Gets current time stamp value to milliseconds resolution.
	 *
	 * @return timestamp, in milliseconds.
	 */
	public long getTimeMillis() {
		return msecs;
	}

	/**
	 * Gets current time stamp value to microseconds resolution.
	 *
	 * @return timestamp, in microseconds.
	 */
	public long getTimeUsec() {
		return downscale(msecs) + usecs;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Returns {@link #longValue()} as an int, possibly truncated.
	 * </p>
	 */
	@Override
	public int intValue() {
		return (int) longValue();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Returns {@link #getTimeUsec()}.
	 * </p>
	 */
	@Override
	public long longValue() {
		return getTimeUsec();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Returns {@link #longValue()} as a float, possibly truncated.
	 * </p>
	 */
	@Override
	public float floatValue() {
		return longValue();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Returns {@link #longValue()} as a double.
	 * </p>
	 */
	@Override
	public double doubleValue() {
		return longValue();
	}

	/**
	 * Gets fractional microseconds portion of time stamp.
	 *
	 * @return fractional microseconds
	 */
	public long getUsecPart() {
		return usecs;
	}

	/**
	 * Gets fractional microseconds portion of time stamp after previous second. Converts mmm.uuu representation of
	 * timestamp (where mmm is millisecond timestamp and uuu is fractional microseconds) to sss.uuuuuu representation
	 * (where sss is seconds timestamp and uuuuuu is fractional milliseconds and microseconds added together as
	 * microseconds) and returns uuuuuu portion.
	 *
	 * @return fractional microseconds
	 */
	public long getSecUsecPart() {
		long msec = msecs - downscale(upscale(msecs));
		return downscale(msec) + usecs;
	}

	/**
	 * Adds/subtracts the specified time value {@code usecs} to this one.
	 * 
	 * @param usecs
	 *            time value in microseconds to add/subtract
	 */
	public void add(long usecs) {
		add(0L, usecs);
	}

	/**
	 * Adds the time value from specified UsecTimestamp {@code other} to this one.
	 *
	 * @param other
	 *            timestamp to add to current one
	 */
	public void add(UsecTimestamp other) {
		add(other.msecs, other.usecs);
	}

	/**
	 * Adds/subtracts the specified time values {@code msecs} and {@code usecs} to this one.
	 *
	 * @param msecs
	 *            milliseconds value to add/subtract
	 * @param usecs
	 *            microseconds value to add/subtracts
	 */
	public void add(long msecs, long usecs) {
		add_(this, msecs, usecs);
	}

	/**
	 * Creates new UsecTimestamp instance by adding/subtracting specified time value {@code usecs} to value of this
	 * UsecTimestamp.
	 *
	 * @param usecs
	 *            time value in microseconds to add/subtract
	 * @return new UsecTimestamp instance
	 */
	public UsecTimestamp addNew(long usecs) {
		return addNew(0L, usecs);
	}

	/**
	 * Creates new UsecTimestamp instance by adding time value from specified UsecTimestamp {@code other} to value of
	 * this one.
	 *
	 * @param other
	 *            timestamp to add
	 * @return new UsecTimestamp instance
	 */
	public UsecTimestamp addNew(UsecTimestamp other) {
		return addNew(other.msecs, other.usecs);
	}

	/**
	 * Creates new UsecTimestamp instance by adding/subtracting specified time values {@code msecs} and {@code usecs} to
	 * value of this one.
	 *
	 * @param msecs
	 *            milliseconds value to add/subtract
	 * @param usecs
	 *            microseconds value to add/subtract
	 * @return new UsecTimestamp instance
	 */
	public UsecTimestamp addNew(long msecs, long usecs) {
		UsecTimestamp newTS = new UsecTimestamp(this);
		add_(newTS, msecs, usecs);

		return newTS;
	}

	/**
	 * Adds/subtracts the specified time values values {@code msecs} and {@code usecs} to timestamp {@code ts}.
	 * 
	 * @param ts
	 *            timestamp instance to add/subtract value
	 * @param msecs
	 *            milliseconds value to add/subtract
	 * @param usecs
	 *            microseconds value to add/subtract
	 */
	protected static void add_(UsecTimestamp ts, long msecs, long usecs) {
		if (usecs > MAX_USEC || usecs < -MAX_USEC) {
			long ms = upscale(usecs);
			msecs += ms;
			usecs -= downscale(ms);
		}

		ts.usecs += usecs;

		if (ts.usecs > MAX_USEC) {
			long ms = upscale(ts.usecs);
			msecs += ms;
			ts.usecs -= downscale(ms);
		} else if (ts.usecs < 0) {
			msecs--;
			ts.usecs += SECS_SCALE;
		}

		ts.msecs += msecs;
	}

	/**
	 * Computes the difference between this timestamp and the specified one (as this - other). It relates to
	 * {@link Comparable#compareTo(Object)} such that if {@code x.compareTo(y)} returns a negative number implying that
	 * {@code x} comes before {@code y} (that is, {@code x < y}), then {@code x.difference(y)} returns a negative
	 * number.
	 *
	 * @param other
	 *            other UsecTimestamp instance
	 * @return difference, in microseconds, between two timestamps
	 */
	public long difference(UsecTimestamp other) {
		long thisMsecs = this.msecs;
		long thisUsecs = this.usecs;
		long otherMsecs = other.msecs;
		long otherUsecs = other.usecs;

		if (thisUsecs < otherUsecs) {
			thisMsecs--;
			thisUsecs += SECS_SCALE;
		}

		return downscale(thisMsecs - otherMsecs) + (thisUsecs - otherUsecs);
	}

	/**
	 * Returns the string representation of the current timestamp, with a given time zone.
	 *
	 * @param tz
	 *            format current time based on a given timezone.
	 * @return formatted date/time string based on default pattern and given timezone
	 */
	public static String getTimeStamp(TimeZone tz) {
		return getTimeStamp(tz, null);
	}

	/**
	 * Returns the string representation of the current timestamp, with a given time zone and locale.
	 *
	 * @param tz
	 *            format current time based on a given timezone.
	 * @param locale
	 *            locale
	 * @return formatted date/time string based on default pattern and given timezone
	 */
	public static String getTimeStamp(TimeZone tz, Locale locale) {
		return getTimeStamp(null, tz, locale, Useconds.CURRENT.get());
	}

	/**
	 * Returns the string representation of the timestamp based on the default format pattern, microseconds.
	 *
	 * @param usecs
	 *            timestamp in microseconds
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(long usecs) {
		return getTimeStamp(null, usecs);
	}

	/**
	 * Returns the string representation of the timestamp based on the default format pattern, milliseconds and
	 * microseconds.
	 *
	 * @param msecs
	 *            milliseconds fraction of timestamp
	 * @param usecs
	 *            microseconds fraction of timestamp
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(long msecs, long usecs) {
		return getTimeStamp(null, msecs, usecs);
	}

	/**
	 * Returns the string representation of the current timestamp.
	 *
	 * @return formatted date/time string based on default pattern
	 */
	public static String getTimeStamp() {
		return getTimeStamp((String) null);
	}

	/**
	 * Returns the string representation of the current timestamp based on the given format pattern.
	 *
	 * @param pattern
	 *            format pattern
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern) {
		return getTimeStamp(pattern, Useconds.CURRENT.get());
	}

	/**
	 * Returns the string representation of the timestamp based on the given format pattern, milliseconds.
	 *
	 * @param pattern
	 *            format pattern
	 * @param usecs
	 *            timestamp in microseconds
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern, long usecs) {
		return getTimeStamp(pattern, DEFAULT_TZ, null, usecs);
	}

	/**
	 * Returns the string representation of the timestamp based on the specified format pattern, milliseconds and
	 * microseconds, default timezone.
	 *
	 * @param pattern
	 *            format pattern
	 * @param msecs
	 *            milliseconds fraction of timestamp
	 * @param usecs
	 *            microseconds fraction of timestamp
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern, long msecs, long usecs) {
		return getTimeStamp(pattern, DEFAULT_TZ, msecs, usecs);
	}

	/**
	 * Returns the string representation of the timestamp based on the specified format pattern, and microseconds scaled
	 * timestamp value.
	 *
	 * @param pattern
	 *            format pattern
	 * @param tz
	 *            time zone
	 * @param usecs
	 *            timestamp in microseconds
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern, TimeZone tz, long usecs) {
		return getTimeStamp(pattern, tz, null, usecs);
	}

	/**
	 * Returns the string representation of the timestamp based on the specified format pattern, milliseconds and
	 * microseconds.
	 *
	 * @param pattern
	 *            format pattern
	 * @param tz
	 *            time zone
	 * @param msecs
	 *            milliseconds fraction of timestamp
	 * @param usecs
	 *            microseconds fraction of timestamp
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern, TimeZone tz, long msecs, long usecs) {
		return getTimeStamp(pattern, tz, null, msecs, usecs);
	}

	/**
	 * Returns the string representation of the timestamp based on the specified format pattern, and microseconds scaled
	 * timestamp value.
	 *
	 * @param pattern
	 *            format pattern
	 * @param tz
	 *            time zone
	 * @param locale
	 *            locale
	 * @param usecs
	 *            timestamp in microseconds
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern, TimeZone tz, Locale locale, long usecs) {
		long msecs = upscale(usecs);
		return getTimeStamp(pattern, tz, locale, msecs, usecs - downscale(msecs));
	}

	/**
	 * Returns the string representation of the timestamp based on the specified format pattern, milliseconds and
	 * microseconds.
	 *
	 * @param pattern
	 *            format pattern
	 * @param tz
	 *            time zone
	 * @param locale
	 *            locale
	 * @param msecs
	 *            milliseconds fraction of timestamp
	 * @param usecs
	 *            microseconds fraction of timestamp
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern, TimeZone tz, Locale locale, long msecs, long usecs) {
		String tsStr = null;

		if (pattern == null) {
			pattern = DFLT_JAVA_FORMAT + String.format("%03d", usecs) + " z";
		} else {
			int fracSecPos = pattern.indexOf('S');
			if (fracSecPos >= 0) {
				String usecStr = String.format("%03d", usecs);
				pattern = pattern.replaceFirst("SS*", "SSS" + usecStr);
			}
		}

		SimpleDateFormat df = (locale == null ? new SimpleDateFormat(pattern) : new SimpleDateFormat(pattern, locale));
		df.setTimeZone(tz == null ? DEFAULT_TZ : tz);
		tsStr = df.format(new Date(msecs));

		return tsStr.replace(" Z", " 00:00");
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public int compareTo(UsecTimestamp other) {
		if (msecs < other.msecs) {
			return -1;
		}
		if (msecs > other.msecs) {
			return 1;
		}

		return Long.compare(usecs, other.usecs);
	}

	@Override
	public int hashCode() {
		int prime = 31;
		long result = 1;

		result = prime * result + (int) (msecs ^ (msecs >>> 32));
		result = prime * result + usecs;

		return (int) result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UsecTimestamp)) {
			return false;
		}

		UsecTimestamp other = (UsecTimestamp) obj;

		if (msecs != other.msecs) {
			return false;
		}

		return usecs == other.usecs;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns the string representation of this timestamp in the default timezone.
	 * </p>
	 */
	@Override
	public String toString() {
		return getTimeStamp(msecs, usecs);
	}

	/**
	 * Returns the string representation of this timestamp in the specified timezone.
	 *
	 * @param tz
	 *            timezone
	 * @return formatted date/time string in specified timezone
	 */
	public String toString(TimeZone tz) {
		return getTimeStamp(null, tz, msecs, usecs);
	}

	/**
	 * Returns the string representation of this timestamp in the specified timezone and locale.
	 *
	 * @param tz
	 *            timezone
	 * @param locale
	 *            locale
	 * @return formatted date/time string in specified timezone and locale
	 */
	public String toString(TimeZone tz, Locale locale) {
		return getTimeStamp(null, tz, locale, msecs, usecs);
	}

	/**
	 * Returns the string representation of this timestamp based on the specified format pattern in the default
	 * timezone.
	 *
	 * @param pattern
	 *            format pattern
	 * @return formatted date/time string based on pattern
	 */
	public String toString(String pattern) {
		return getTimeStamp(pattern, msecs, usecs);
	}

	/**
	 * Returns the string representation of this timestamp based on the specified format pattern in the specified
	 * timezone.
	 *
	 * @param pattern
	 *            format pattern
	 * @param tz
	 *            timezone name
	 * @return formatted date/time string based on pattern
	 */
	public String toString(String pattern, String tz) {
		return getTimeStamp(pattern, StringUtils.isEmpty(tz) ? DEFAULT_TZ : TimeZone.getTimeZone(tz), msecs, usecs);
	}

	/**
	 * Returns the string representation of this timestamp based on the specified format pattern in the specified
	 * timezone.
	 *
	 * @param pattern
	 *            format pattern
	 * @param tz
	 *            timezone name
	 * @param locale
	 *            locale for date format to use.
	 * @return formatted date/time string based on pattern
	 */
	public String toString(String pattern, String tz, String locale) {
		return getTimeStamp(pattern, //
				StringUtils.isEmpty(tz) ? DEFAULT_TZ : TimeZone.getTimeZone(tz), //
				StringUtils.isEmpty(locale) ? null : Utils.getLocale(locale), //
				msecs, usecs);
	}

	/**
	 * Returns the string representation of this timestamp based on the specified format pattern in the specified
	 * timezone.
	 *
	 * @param pattern
	 *            format pattern
	 * @param tz
	 *            timezone
	 * @return formatted date/time string based on pattern
	 */
	public String toString(String pattern, TimeZone tz) {
		return getTimeStamp(pattern, tz, msecs, usecs);
	}

	/**
	 * Returns the string representation of this timestamp based on the specified format pattern in the specified
	 * timezone and locale.
	 *
	 * @param pattern
	 *            format pattern
	 * @param tz
	 *            timezone
	 * @param locale
	 *            locale
	 * @return formatted date/time string based on pattern
	 */
	public String toString(String pattern, TimeZone tz, Locale locale) {
		return getTimeStamp(pattern, tz, locale, msecs, usecs);
	}

	/**
	 * Purpose of this method is to make this class compatible with Groovy script standard for number operator "plus"
	 * {@code '+'} overloading. See <a href="http://groovy-lang.org/operators.html">Groovy operators spec</a> section
	 * "Operator overloading".
	 * <p>
	 * Performs same as {@link #add(UsecTimestamp)}.
	 *
	 * @param other
	 *            timestamp to add to current one
	 * @return current UsecTimestamp instance
	 *
	 * @see #add(UsecTimestamp)
	 */
	public UsecTimestamp plus(UsecTimestamp other) {
		add(other);
		return this;
	}

	/**
	 * Purpose of this method is to make this class compatible with Groovy script standard for number operator "plus"
	 * {@code '+'} overloading. See <a href="http://groovy-lang.org/operators.html">Groovy operators spec</a> section
	 * "Operator overloading".
	 * <p>
	 * Performs same as {@link #add(long)}.
	 *
	 * @param usecs
	 *            time value in microseconds to add
	 * @return current UsecTimestamp instance
	 *
	 * @see #add(long)
	 */
	public UsecTimestamp plus(long usecs) {
		add(usecs);
		return this;
	}

	/**
	 * Purpose of this method is to make this class compatible with Groovy script standard for number operator "minus"
	 * {@code '-'} overloading. See <a href="http://groovy-lang.org/operators.html">Groovy operators spec</a> section
	 * "Operator overloading".
	 * <p>
	 * Performs same as {@link #difference(UsecTimestamp)}.
	 *
	 * @param other
	 *            other UsecTimestamp instance
	 * @return difference, in microseconds, between two timestamps
	 *
	 * @see #difference(UsecTimestamp)
	 */
	public long minus(UsecTimestamp other) {
		return difference(other);
	}
}
