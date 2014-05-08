/*
 * Copyright (c) 2008 Nastel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Nastel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with Nastel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 *
 */

package com.nastel.jkool.tnt4j.core;

import java.util.UUID;

/**
 * <p>Implements a Message entity .</p>
 *
 * <p>A <code>Message</code> represents a physical message being transported
 * between a set of entities.  It also will contain the operation(s) that have
 * directly acted upon it.</p>
 *
 * @see Activity
 * @see Operation
 * @see LinkedItem
 *
 * @version $Revision: 7 $
 */
public class Message implements LinkedItem {

	/**
	 * Maximum length of a Message Signature.
	 * @since Revision 20
	 */
	public static final int MAX_SIGNATURE_LENGTH = 36;

	/**
	 * Maximum length of Message Tag.
	 * @since Revision 22
	 */
	public static final int MAX_TAG_LENGTH = 256;


	/**
	 * Maximum length of Message Value.
	 * @since Revision 22
	 */
	public static final int MAX_VALUE_LENGTH = 256;

	private String            signature;
	private int               size;
	private String            tag;
	private String            strData;
	private byte[]            binData;

	private LinkedItem  parent;


	/**
	 * Creates a message object with generated, unique signature.
	 *
	 * @throws NullPointerException if any arguments are <code>null</code>
	 */
	public Message() {
		this(UUID.randomUUID().toString());
	}

	/**
	 * Creates a message object with the specified properties.
	 *
	 * @param signature unique signature identifying message
	 * @throws NullPointerException if any arguments are <code>null</code>
	 * @throws IllegalArgumentException if signature is empty or too long
	 */
	public Message(String signature) {
		setTrackingId(signature);
	}

	/**
	 * Creates a message object with the specified properties.
	 *
	 * @param signature unique signature identifying message
	 * @param msg actual string message associated with this instance
	 * @throws NullPointerException if any arguments are <code>null</code>
	 * @throws IllegalArgumentException if signature is empty or too long
	 */
	public Message(String signature, String msg) {
		setTrackingId(signature);
		setMessage(msg);
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
	 * Sets the message tracking signature, which is the unique identifier for the message.
	 * Could be any string that will uniquely identify this message.
	 *
	 * @param signature unique signature identifying message
	 * @throws NullPointerException if signature is <code>null</code>
	 * @throws IllegalArgumentException if signature is empty or is too long
	 * @see #MAX_SIGNATURE_LENGTH
	 */
	public void setTrackingId(String signature) {
		if (signature == null)
			throw new NullPointerException("signature must be a non-empty string");
		if (signature.length() == 0)
			throw new IllegalArgumentException("signature must be a non-empty string");
		if (signature.length() > MAX_SIGNATURE_LENGTH)
			throw new IllegalArgumentException("signature length must be <= " + MAX_SIGNATURE_LENGTH);
		this.signature = signature;
	}

	/**
	 * Gets the message tag, which is a user-defined value to associate with the message.
	 *
	 * @return user-defined message tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Sets the message tag, which is a user-defined value to associate with the message,
	 * truncating if necessary.
	 *
	 * @param tag user-defined message tag
	 * @see #MAX_TAG_LENGTH
	 */
	public void setTag(String tag) {
		if (tag != null) {
			if (tag.length() > MAX_TAG_LENGTH)
				tag = tag.substring(0, MAX_TAG_LENGTH);
			else if (tag.length() == 0)
				tag = null;
		}
		this.tag = tag;
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
	 * @param size message size, in bytes
	 * @throws IllegalArgumentException if size is negative
	 */
	public void setSize(int size) {
		if (size < 0)
			throw new IllegalArgumentException("size must be non-negative");
		this.size = size;
	}


	/**
	 * Returns an indication of whether the current message data is represented
	 * as a binary sequence of bytes.
	 *
	 * @return <code>true</code> if message data is binary,
	 *         <code>false</code> if message data is character
	 */
	public boolean isDataBinary() {
		return (binData != null);
	}

	/**
	 * Gets the current message data as a string, regardless of its internal
	 * representation.
	 *
	 * @return string message data, or <code>null</code> if there is no data
	 * @see #isDataBinary()
	 */
	public String getStringMessage() {
		if (strData != null)
			return strData;

		if (binData != null)
			return new String(binData);

		return null;
	}

	/**
	 * Gets the current message data as a binary sequence of bytes, regardless of
	 * its internal representation.
	 *
	 * @return binary message data, or <code>null</code> if there is no data
	 * @see #isDataBinary()
	 */
	public byte[] getBinaryMessage() {
		if (binData != null)
			return binData;

		if (strData != null)
			return strData.getBytes();

		return null;
	}

	/**
	 * Sets the data for the message.  This is usually the message body.
	 *
	 * @param msgData string message data
	 */
	public void setMessage(String msgData) {
		strData = msgData;
		binData = null;
		if (strData != null) {
			setSize(strData.length());
		} else setSize(0);
	}

	/**
	 * Sets the data for the message.  This is usually the message body.
	 *
	 * @param msgData binary message data
	 */
	public void setMessage(byte[] msgData) {
		binData = msgData;
		strData = null;
		if (binData != null) {
			setSize(binData.length);
		} else setSize(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParentItem(LinkedItem parentObject) {
		if (parentObject != null &&	!(parentObject instanceof Activity)) {
			throw new IllegalArgumentException("parent object (" + parentObject.getClass().getName() +
											   ") for " + getClass().getName() + " must be 'Activity'");
		}
		parent = parentObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkedItem getParentItem() {
		return parent;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearChildren() {
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Message))
			return false;

		final Message other = (Message) obj;

		if (signature == null) {
			if (other.signature != null)
				return false;
		}
		else if (!signature.equals(other.signature)) {
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
			.append("ParentId:").append(parent != null? parent.getTrackingId(): "root").append(",")
			.append("TrackId:").append(getTrackingId()).append(",")
			.append("Tag:").append(getTag()).append(",")
			.append("Size:").append(getSize()).append(")");

		return str.toString();
	}
}
