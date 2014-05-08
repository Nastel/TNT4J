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

import java.lang.management.ManagementFactory;



/**
 * <p>
 * This class implements a default dump destination factory based on file based dump destination
 * backed by <code>FileDumpSink</code> implementation. By default dump destinations are 
 * name using this convention: ManagementFactory.getRuntimeMXBean().getName() + ".dump".
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 * @see FileDumpSink
 */
public class DefaultDumpSinkFactory implements DumpSinkFactory {

	@Override
    public DumpSink getInstance() {
	    return new FileDumpSink(ManagementFactory.getRuntimeMXBean().getName() + ".dump");
    }

	@Override
    public DumpSink getInstance(String url) {
	    return new FileDumpSink(url);
    }

	@Override
    public DumpSink getInstance(String url, boolean append) {
	    return new FileDumpSink(url, append);
    }

	@Override
    public DumpSink getInstance(String url, boolean append, DumpFormatter frm) {
	    return new FileDumpSink(url, append, frm);
    }
}
