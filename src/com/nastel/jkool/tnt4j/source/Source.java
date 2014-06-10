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
package com.nastel.jkool.tnt4j.source;

/**
 * Source is a logical nested entity that expresses container of applications, servers, application servers, jvms, etc.
 * 
 * @see SourceType
 * @version $Revision: 1 $
 */

public interface Source {
	/**
	 * Gets short name used to identify this source
	 * 
	 * @return short name which identifies this source
	 */
	public String getName();

	/**
	 * Gets the fully qualified name used to identify this source type=source-name#parent-source
	 * 
	 * @return fully qualified name of this source
	 */
	public String getFQName();

	/**
	 * Gets parent source
	 * 
	 * @return parent source
	 */
	public Source getSource();

	/**
	 * Gets the user name that the application is running under.
	 * 
	 * @return name of user running application
	 */
	public String getUser();

	/**
	 * Sets the user name that the application is running under, truncating if necessary.
	 * 
	 * @param user
	 *            User name that application is running under
	 */
	public void setUser(String user);

	/**
	 * Gets the URL that the application is running at.
	 * 
	 * @return URL that application is running at
	 */
	public String getUrl();

	/**
	 * Sets the URL that the application is running at, truncating if necessary.
	 * 
	 * @param url
	 *            URL that application is running at
	 */
	public void setUrl(String url);

	/**
	 * 
	 * Gets source info string.
	 * 
	 * @return source info
	 */
	public String getInfo();

	/**
	 * Sets source info associated with this source
	 * 
	 * @param inf
	 *            info associated with the source
	 */
	public void setInfo(String inf);

	/**
	 * Gets source type.
	 * 
	 * @return container type
	 */
	public SourceType getType();
}
