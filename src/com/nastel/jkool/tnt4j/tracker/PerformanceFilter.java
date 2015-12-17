/*
 * Copyright 2014-2015 JKOOL, LLC.
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

package com.nastel.jkool.tnt4j.tracker;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * A servlet filter that will intercept all servlets and set the correlator id on the 
 * {@link ContextTracker}. The correlator id will be obtained from the session object. 
 * It was originally set via the performance monitoring plugin that is utilized on the 
 * client side in their jsp pages.  This correlator id will connect user response times to
 * server side processing times.  The ContextTracker's get method will be used to 
 * obtain the correlator id when reporting server side processing times. 
 * 
 */
public class PerformanceFilter implements Filter {

	public static String JK_CORR_ID = "JK_CORR_ID";

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpSession session = request.getSession();
		String corrId = (String)session.getAttribute(ContextTracker.JK_CORR_ID);
		if (corrId != null)
			ContextTracker.set(corrId);
		chain.doFilter(req, res);
	}

	public void init(FilterConfig config) throws ServletException {

	}

	public void destroy() {

	}

}
