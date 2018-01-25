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

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Implements a Message entity.
 * </p>
 *
 * <p>
 * A {@code Message} represents a logical entity represented by: payload, mime type, encoding (e.g. base64) and
 * character set see{@code Charset}. Messages are exchanged between a set of software entities (applications). Message
 * has tracking id, size, tag and content with a set of arguments and age. Content is always represented as text and
 * binary data encoded with base64.
 * </p>
 *
 * @see Activity
 * @see Operation
 * @see Trackable
 *
 * @version $Revision: 7 $
 */
public class Message {
	public static final String ENCODING_BASE64 = "base64";
	public static final String ENCODING_NONE = "none";
	public static final String CHARSET_DEFAULT = Charset.defaultCharset().displayName();

	public static final String MIME_TYPE_BINARY = "application/octet-stream";
	public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";

	private String signature;
	private int size;
	private String strData;
	private Object[] argList;
	private long messageAge;
	private String mimeType = MIME_TYPE_TEXT_PLAIN;
	private String encoding = ENCODING_NONE;
	private String charset = CHARSET_DEFAULT;
	private HashSet<String> tags = new HashSet<String>(89);

	/**
	 * Creates a message object with empty signature.
	 *
	 */
	public Message() {
	}

	/**
	 * Creates a message object with the specified properties.
	 *
	 * @param signature
	 *            unique signature identifying message
	 * @throws NullPointerException
	 *             if any arguments are {@code null}
	 * @throws IllegalArgumentException
	 *             if signature is empty or too long
	 */
	public Message(String signature) {
		setTrackingId(signature);
	}

	/**
	 * Creates a message object with the specified properties.
	 *
	 * @param signature
	 *            unique signature identifying message
	 * @param msg
	 *            actual string message associated with this instance
	 * @param args
	 *            argument list passed along the message
	 * @throws NullPointerException
	 *             if any arguments are {@code null}
	 * @throws IllegalArgumentException
	 *             if signature is empty or too long
	 */
	public Message(String signature, String msg, Object... args) {
		setTrackingId(signature);
		setMessage(msg, args);
	}

	/**
	 * Creates a message object with the specified properties.
	 *
	 * @param signature
	 *            unique signature identifying message
	 * @param msg
	 *            actual byte message associated with this instance
	 * @param args
	 *            argument list passed along the message
	 * @throws NullPointerException
	 *             if any arguments are {@code null}
	 * @throws IllegalArgumentException
	 *             if signature is empty or too long
	 */
	public Message(String signature, byte[] msg, Object... args) {
		setTrackingId(signature);
		setMessage(msg, args);
	}

	/**
	 * Gets message encoding
	 *
	 * @return message encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Sets message encoding e.g. "base64"
	 *
	 * @param encoding
	 *            message content encoding type such as "base64" see {@code Message.ENCODING_BASE64}
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Gets message character as defined by {@code Charset}
	 *
	 * @return message encoding
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * Sets message character set see {@code Charset}
	 *
	 * @param charset
	 *            character set of the body of the message see {@code Charset}
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * Gets message mime type
	 *
	 * @return message encoding
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Sets message mime type
	 *
	 * @param mimeType
	 *            mime type of the message (default {@code text/plain})
	 *
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Gets the message signature, which is the unique identifier for the message.
	 *
	 * @return message signature
	 */
	public String getTrackingId() {
		return signature;
	}

	/**
	 * Sets the message tracking signature, which is the unique identifier for the message. Could be any string that
	 * will uniquely identify this message.
	 *
	 * @param signature
	 *            unique signature identifying message
	 */
	public void setTrackingId(String signature) {
		this.signature = signature;
	}

	/**
	 * Gets the age of the message that the operation applies to. This value represents the time between when the
	 * message was sent/created and time is was consumed.
	 *
	 * @return age of message, in microseconds
	 */
	public long getMessageAge() {
		return messageAge;
	}

	/**
	 * Sets the age of the message in microseconds. Age represents the relative time that the message was idle.
	 * Typically this applies to messages that are sent or received.
	 *
	 * @param messageAge
	 *            age of message, in microseconds
	 * @throws IllegalArgumentException
	 *             if messageAge is negative
	 */
	public void setMessageAge(long messageAge) {
		if (messageAge < 0) {
			throw new IllegalArgumentException("messageAge must be non-negative");
		}
		this.messageAge = messageAge;
	}

	/**
	 * Gets message tags, which are user-defined values associated with the message.
	 *
	 * @return user-defined set of message tags
	 */
	public Set<String> getTag() {
		return tags;
	}

	/**
	 * Sets message tags, which are user-defined values associated with the message
	 *
	 * @param tlist
	 *            user-defined list of message tags
	 */
	public void setTag(String... tlist) {
		for (int i = 0; (tlist != null) && (i < tlist.length); i++) {
			if (tlist[i] != null) {
				this.tags.add(tlist[i]);
			}
		}
	}

	/**
	 * Sets message tags, which are user-defined values associated with the message.
	 *
	 * @param tlist
	 *            user-defined list of message tags
	 */
	public void setTag(Collection<String> tlist) {
		if (tlist != null) {
			this.tags.addAll(tlist);
		}
	}

	/**
	 * Remove all tags from this message
	 *
	 */
	public void clearTags() {
		this.tags.clear();
	}

	/**
	 * Get the size of the message.
	 *
	 * @return message size, in bytes
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Set the size of the message.
	 *
	 * @param size
	 *            message size, in bytes
	 * @throws IllegalArgumentException
	 *             if size is negative
	 */
	public void setSize(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("size must be non-negative");
		}
		this.size = size;
	}

	/**
	 * Gets the current formatted message with formatted arguments
	 *
	 * @return string message data, or {@code null} if there is no data
	 */
	public String getMessage() {
		return Utils.format(strData, argList);
	}

	/**
	 * Gets the current non formatted message
	 *
	 * @return string non formatted message, or {@code null} if there is no data
	 */
	public String getMessagePattern() {
		return strData;
	}

	/**
	 * Gets the current message argument list
	 *
	 * @return message argument list
	 */
	public Object[] getMessageArgs() {
		return argList;
	}

	/**
	 * Sets the data for the message. This is usually the message body.
	 *
	 * @param pattern
	 *            message pattern
	 * @param args
	 *            list of arguments
	 */
	public void setMessage(String pattern, Object... args) {
		strData = pattern;
		if (strData != null) {
			setSize(strData.length());
		} else {
			setSize(0);
		}
		argList = args;
	}

	/**
	 * Sets binary data for the message. Binary message will be base64 encoded and message encoding set to "base64".
	 *
	 * @param bytes
	 *            binary message content
	 * @param args
	 *            list of arguments associated with this message
	 */
	public void setMessage(byte[] bytes, Object... args) {
		strData = new String(Base64.encodeBase64(bytes));
		setEncoding(ENCODING_BASE64);
		setMimeType(MIME_TYPE_BINARY);
		if (strData != null) {
			setSize(strData.length());
		} else {
			setSize(0);
		}
		argList = args;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 31 + ((signature == null) ? 0 : signature.hashCode());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Message)) {
			return false;
		}

		Message other = (Message) obj;

		if (signature == null) {
			if (other.signature != null) {
				return false;
			}
		} else if (!signature.equals(other.signature)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		str.append(getClass().getSimpleName()).append("(")
			.append("TrackId:").append(getTrackingId()).append(",")
			.append("Tag:").append(getTag()).append(",")
			.append("Encoding:").append(getEncoding()).append(",")
			.append("MimeType:").append(getMimeType()).append(",")
			.append("Size:").append(getSize()).append(")");

		return str.toString();
	}

}
