/*
 * Copyright (c) 2014 Nastel Technologies, Inc. All Rights Reserved.
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
 *
 */
package com.nastel.jkool.tnt4j.dump;


/**
 * <p>
 * A simple event listener interface for observers of dump generation.
 * This interface can be implemented by classes that are interested in "raw" events caused by <code>TrackerLogger.dump()</code> method. 
 * Each dump generation will generate such an event before, after, complete and error invocations into the listener instance.
 * </p>
 *
 * @see DumpEvent
 * @see DumpProvider
 *
 * @version $Revision: 1 $
 *
 */

public interface DumpListener {
	/**
	 * Notifies when a dump event is generated. Dump events are generated
	 * on before, after, complete and error defined in <code>DumpProvider</code>
	 * 
	 * @param event dump event instance
	 * 
	 * @see DumpEvent
	 * @see DumpProvider
	 */
	public void onDumpEvent(DumpEvent event);
}
