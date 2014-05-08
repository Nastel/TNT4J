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

/**
 * Classes that implement this interface support of parent/child relationship
 * of related items.
 * 
 * @version $Revision: 2 $
 */
public interface LinkedItem {
		
	/**
	 * Gets item tracking signature
	 *
	 * @return item tracking signature
	 */
	public String getTrackingId();
	
	/**
	 * Sets item tracking signature
	 *
	 * @param signature tracking signature
	 */
	public void setTrackingId(String signature);
	
	/**
	 * Sets the parent object for this object.
	 * 
	 * @param parentObject parent object
	 * @throws IllegalArgumentException if parentObject is not a valid type of parent
	 */
	public void setParentItem(LinkedItem parentObject);
	
	/**
	 * Gets the parent object for this object.
	 * 
	 * @return parent object
	 */
	public LinkedItem getParentItem();
	
	/**
	 * Removes all children objects.
	 */
	public void clearChildren();
}
