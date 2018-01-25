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
package com.jkoolcloud.tnt4j.core;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

import com.jkoolcloud.tnt4j.utils.TimeService;
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

	private static final String DFLT_JAVA_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String DEFAULT_FORMAT = DFLT_JAVA_FORMAT + "SSS z";

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
	 *             if any arguments are negative, or if usecs is greater than 999
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
	 *             if any arguments are negative, or if usecs is greater than 999
	 */
	public UsecTimestamp(long msecs, long usecs, long recvdLamportClock) {
		setTimestampValues(msecs, usecs, recvdLamportClock);
	}

	/**
	 * Creates UsecTimestamp based on specified Timestamp, providing time in seconds resolution, and fractional
	 * microsecond.
	 *
	 * @param timestamp
	 *            database timestamp, seconds resolution
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
		this(timeStampStr, DEFAULT_FORMAT, TimeZone.getDefault());
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
		this(timeStampStr, formatStr, TimeZone.getDefault());
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
			int fmtPos = formatStr.indexOf('S');
			if (fmtPos > 0) {
				int endFmtPos = formatStr.lastIndexOf('S');
				int fmtFracSecLen = endFmtPos - fmtPos + 1;

				if (fmtFracSecLen > 6) {
					throw new ParseException(
							"Date format containing more than 6 significant digits for fractional seconds is not supported",
							0);
				}

				StringBuilder sb = new StringBuilder();
				int usecPos = timeStampStr.lastIndexOf('.') + 1;
				int usecEndPos;
				if (usecPos > 2) {
					for (usecEndPos = usecPos; usecEndPos < timeStampStr.length(); usecEndPos++) {
						if (!StringUtils.containsAny("0123456789", timeStampStr.charAt(usecEndPos))) {
							break;
						}
					}

					if (fmtFracSecLen > 3) {
						// format specification represents more than milliseconds, assume microseconds
						String usecStr = String.format("%s", timeStampStr.substring(usecPos, usecEndPos));
						if (usecStr.length() < fmtFracSecLen) {
							usecStr = StringUtils.rightPad(usecStr, fmtFracSecLen, '0');
						} else if (usecStr.length() > fmtFracSecLen) {
							usecStr = usecStr.substring(0, fmtFracSecLen);
						}
						usecs = Integer.parseInt(usecStr);

						// trim off fractional part < microseconds from both timestamp and format strings
						sb.append(timeStampStr);
						sb.delete(usecPos - 1, usecEndPos);
						timeStampStr = sb.toString();

						sb.setLength(0);
						sb.append(formatStr);
						sb.delete(fmtPos - 1, endFmtPos + 1);
						formatStr = sb.toString();
					} else if ((usecEndPos - usecPos) < 3) {
						// pad msec value in date string with 0's so that it is 3 digits long
						sb.append(timeStampStr);
						while ((usecEndPos - usecPos) < 3) {
							sb.insert(usecEndPos, '0');
							usecEndPos++;
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

		Date date = dateFormat.parse(timeStampStr);

		setTimestampValues(date.getTime(), 0, 0);
		add(0, usecs);
	}

	/**
	 * Creates UsecTimestamp based on specified UsecTimestamp.
	 *
	 * @param other timestamp to copy
	 * @throws NullPointerException if timestamp is {@code null}
	 */
	public UsecTimestamp(UsecTimestamp other) {
		this(other.msecs, other.usecs, other.getLamportClock());
	}

	/**
	 * Creates UsecTimestamp based on specified Date.
	 *
	 * @param date timestamp to copy
	 * @throws NullPointerException if date is {@code null}
	 */
	public UsecTimestamp(Date date) {
		setTimestampValues(date.getTime(), 0, 0);
	}

	/**
	 * Returns Lamport clock value of this time stamp
	 * (based on Lamport Clock algorithm)
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
	 * @param usecTime timestamp, in microsecond
	 * @return current UsecTimestamp instance
	 * @throws IllegalArgumentException if usecTime is negative
	 */
	public UsecTimestamp setTimeUsec(long usecTime) {
		if (usecTime < 0) {
			throw new IllegalArgumentException("usecTime must be non-negative");
                }

		this.msecs = usecTime / 1000L;
		this.usecs = (int) (usecTime - (this.msecs * 1000));
		return this;
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
		if (usecs > 999) {
			// extract milliseconds portion from usecs and add to msecs
			long msecs = usecs / 1000;
			this.msecs += msecs;
			usecs -= msecs * 1000;
		}
		this.usecs = usecs;
	}

	/**
	 * Creates UsecTimestamp based on specified millisecond timestamp
	 * and fractional microsecond.
	 *
	 * @param msecs timestamp, in milliseconds
	 * @param usecs fraction microseconds
	 * @param recvdLamportClock received Lamport clock
	 * @throws IllegalArgumentException if any arguments are negative,
	 *  or if usecs is greater than 999
	 */
	protected void setTimestampValues(long msecs, long usecs, long recvdLamportClock) {
		if (msecs < 0) {
			throw new IllegalArgumentException("msecs must be non-negative");
                }
		if (usecs < 0 || usecs > 999) {
			throw new IllegalArgumentException("usecs must be in the range [0,999], inclusive");
		}

		this.msecs = msecs;
		this.usecs = usecs;
		assignLamportClock(recvdLamportClock);
	}

	/**
	 * Assign local Lamport clock based on the value of the received
	 * Lamport clock.
	 *
	 * @param recvdLamportClock received Lamport clock
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
		return msecs / 1000;
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
		return msecs * 1000 + usecs;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Returns {@link #getTimeUsec()} as an int, possibly truncated.</p>
	 */
	@Override
	public int intValue() {
		return (int) getTimeUsec();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Returns {@link #getTimeUsec()}.</p>
	 */
	@Override
	public long longValue() {
		return getTimeUsec();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Returns {@link #getTimeUsec()} as a float, possibly truncated.</p>
	 */
	@Override
	public float floatValue() {
		return getTimeUsec();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Returns {@link #getTimeUsec()} as a double.</p>
	 */
	@Override
	public double doubleValue() {
		return getTimeUsec();
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
	 * Gets fractional microseconds portion of time stamp after previous second.
	 * Converts mmm.uuu representation of timestamp (where mmm is millisecond
	 * timestamp and uuu is fractional microseconds) to sss.uuuuuu representation
	 * (where sss is seconds timestamp and uuuuuu is fractional milliseconds and
	 * microseconds added together as microseconds) and returns uuuuuu portion.
	 *
	 * @return fractional microseconds
	 */
	public long getSecUsecPart() {
		int msec = (int) (msecs - (msecs / 1000) * 1000);
		return (msec * 1000) + usecs;
	}

	/**
	 * Adds the specified UsecTimestamp to this one.
	 *
	 * @param other timestamp to add to current one
	 */
	public void add(UsecTimestamp other) {
		add(other.msecs, other.usecs);
	}

	/**
	 * Adds the specified time values to this UsecTimestamp.
	 *
	 * @param msecs milliseconds value to add
	 * @param usecs microseconds value to add
	 * @throws IllegalArgumentException if any arguments are negative
	 */
	public void add(long msecs, long usecs) {
		if (msecs < 0) {
			throw new IllegalArgumentException("msecs must be non-negative");
                }
		if (usecs < 0) {
			throw new IllegalArgumentException("usecs must be non-negative");
                }

		if (usecs > 999) {
			long ms = usecs / 1000;
			msecs += ms;
			usecs -= ms * 1000;
		}

		this.msecs += msecs;
		this.usecs += usecs;

		if (this.usecs > 999) {
			long ms = (this.usecs / 1000);

			this.msecs += ms;
			this.usecs -= ms * 1000;
		}
	}

	/**
	 * Subtracts the specified UsecTimestamp from this one (e.g. {@code x.subtract(y)} means {@code x - y}).
	 *
	 * @param other timestamp to subtract from current one
	 */
	public void subtract(UsecTimestamp other) {
		subtract(other.msecs, other.usecs);
	}

	/**
	 * Subtracts the specified time values from this UsecTimestamp.
	 *
	 * @param msecs milliseconds value to subtract
	 * @param usecs microseconds value to subtract
	 * @throws IllegalArgumentException if any arguments are negative
	 */
	public void subtract(long msecs, long usecs) {
		if (msecs < 0) {
			throw new IllegalArgumentException("msecs must be non-negative");
                }
		if (usecs < 0) {
			throw new IllegalArgumentException("usecs must be non-negative");
                }

		if (usecs > 999) {
			long ms = usecs / 1000;
			msecs += ms;
			usecs -= ms * 1000;
		}

		long thisMsecs = this.msecs;
		long thisUsecs = this.usecs;

		if (thisUsecs < usecs) {
			thisMsecs--;
			thisUsecs += 1000;
		}

		this.msecs = thisMsecs - msecs;
		this.usecs = thisUsecs - (int) usecs;
	}

	/**
	 * Computes the difference between this timestamp and the specified one
	 * (as this - other).  It relates to {@link Comparable#compareTo(Object)}
	 * such that if {@code x.compareTo(y)} returns a negative number implying that
	 * {@code x} comes before {@code y} (that is, {@code x < y}),
	 * then {@code x.difference(y)} returns a negative number.
	 *
	 * @param other other UsecTimestamp instance
	 * @return difference, in microseconds, between two timestamps
	 */
	public long difference(UsecTimestamp other) {

		long thisMsecs = this.msecs;
		long thisUsecs = this.usecs;
		long otherMsecs = other.msecs;
		long otherUsecs = other.usecs;

		if (thisUsecs < otherUsecs) {
			thisMsecs--;
			thisUsecs += 1000;
		}

		return ((thisMsecs - otherMsecs) * 1000) + (thisUsecs - otherUsecs);
	}

	/**
	 * Returns the string representation of the current timestamp, with a given time zone.
	 *
	 * @param tz format current time based on a given timezone.
	 * @return formatted date/time string based on default pattern and given timezone
	 */
	public static String getTimeStamp(TimeZone tz) {
		return getTimeStamp(null, tz, TimeService.currentTimeMillis(), 0);
	}

	/**
	 * Returns the string representation of the current timestamp.
	 *
	 * @return formatted date/time string based on default pattern
	 */
	public static String getTimeStamp() {
		return getTimeStamp(null, TimeService.currentTimeMillis(), 0);
	}

	/**
	 * Returns the string representation of the current timestamp based on the given
	 * format pattern.
	 *
	 * @param pattern format pattern
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern) {
		return getTimeStamp(pattern, TimeService.currentTimeMillis(), 0);
	}

	/**
	 * Returns the string representation of the timestamp based on the given
	 * format pattern, milliseconds.
	 *
	 * @param pattern format pattern
	 * @param usecs milliseconds
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern, long usecs) {
		long msecs = usecs / 1000L;
		return getTimeStamp(pattern, msecs, (usecs - msecs * 1000));
	}

	/**
	 * Returns the string representation of the timestamp based on the default
	 * format pattern, microseconds.
	 *
	 * @param usecs microseconds
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(long usecs) {
		long msecs = usecs / 1000L;
		return getTimeStamp(null, msecs, (usecs - msecs * 1000));
	}

	/**
	 * Returns the string representation of the timestamp based on the default
	 * format pattern, milliseconds and microseconds.
	 *
	 * @param msecs milliseconds
	 * @param usecs microseconds
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(long msecs, long usecs) {
		return getTimeStamp(null, msecs, usecs);
	}

	/**
	 * Returns the string representation of the timestamp based on the specified
	 * format pattern, milliseconds and microseconds, default timezone.
	 *
	 * @param pattern format pattern
	 * @param msecs milliseconds
	 * @param usecs microseconds
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern, long msecs, long usecs) {
		return getTimeStamp(pattern, TimeZone.getDefault(), msecs, usecs);
	}

	/**
	 * Returns the string representation of the timestamp based on the specified
	 * format pattern, milliseconds and microseconds.
	 *
	 * @param pattern format pattern
	 * @param tz time zone
	 * @param msecs milliseconds
	 * @param usecs microseconds
	 * @return formatted date/time string based on pattern
	 */
	public static String getTimeStamp(String pattern, TimeZone tz, long msecs, long usecs) {
		String tsStr = null;

		if (pattern == null) {
			SimpleDateFormat df = new SimpleDateFormat(DFLT_JAVA_FORMAT + String.format("%03d", usecs) + " z");
			df.setTimeZone(tz);
			tsStr = df.format(new Date(msecs));
		}

		if (tsStr == null) {
			int fracSecPos = pattern == null ? -1 : pattern.indexOf('S');
			if (fracSecPos < 0) {
				SimpleDateFormat df = new SimpleDateFormat(pattern);
				df.setTimeZone(tz);
				tsStr = df.format(new Date(msecs));
			}
		}

		if (tsStr == null) {
			String usecStr = String.format("%03d", usecs);
			pattern = pattern.replaceFirst("SS*", "SSS" + usecStr);
			SimpleDateFormat df = new SimpleDateFormat(pattern);
			df.setTimeZone(tz);
			tsStr = df.format(new Date(msecs));
		}

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
		if (usecs < other.usecs) {
			return -1;
		}
		if (usecs > other.usecs) {
			return 1;
		}

		return 0;
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

		if (usecs != other.usecs) {
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 * <p>Returns the string representation of this timestamp in the default timezone.</p>
	 */
	@Override
	public String toString() {
		return getTimeStamp(msecs, usecs);
	}

	/**
	 * Returns the string representation of this timestamp in the specified timezone.
	 *
	 * @param tz timezone
	 * @return formatted date/time string in specified timezone
	 */
	public String toString(TimeZone tz) {
		return getTimeStamp(null, tz, msecs, usecs);
	}

	/**
	 * Returns the string representation of this timestamp based on the specified
	 * format pattern in the default timezone.
	 *
	 * @param pattern format pattern
	 * @return formatted date/time string based on pattern
	 */
	public String toString(String pattern) {
		return getTimeStamp(pattern, msecs, usecs);
	}

	/**
	 * Returns the string representation of this timestamp based on the specified
	 * format pattern in the specified timezone.
	 *
	 * @param pattern format pattern
	 * @param tz timezone
	 * @return formatted date/time string based on pattern
	 */
	public String toString(String pattern, TimeZone tz) {
		return getTimeStamp(pattern, tz, msecs, usecs);
	}
}
