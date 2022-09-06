/*
 * Copyright 2014-2022 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jkoolcloud.tnt4j.source;

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
	String getName();

	/**
	 * Gets the fully qualified name used to identify this source type=source-name#parent-source
	 * 
	 * @return fully qualified name of this source
	 */
	String getFQName();

	/**
	 * Gets the fully qualified name used to identify this source type=source-name#parent-source and append it to
	 * {@code StringBuilder}
	 *
	 * @param buff
	 *            string builder to append
	 * @return returns {@code StringBuilder} instance containing appended source name
	 */
	StringBuilder getFQName(StringBuilder buff);

	/**
	 * Gets parent source
	 * 
	 * @return parent source
	 */
	Source getSource();

	/**
	 * Gets parent source associated with the given type
	 * 
	 * @param type
	 *            source type
	 * @return parent source
	 */
	Source getSource(SourceType type);

	/**
	 * Gets the user name that the application is running under.
	 * 
	 * @return name of user running application
	 */
	String getUser();

	/**
	 * Sets the user name that the application is running under, truncating if necessary.
	 * 
	 * @param user
	 *            User name that application is running under
	 */
	void setUser(String user);

	/**
	 * Gets the URL that the application is running at.
	 * 
	 * @return URL that application is running at
	 */
	String getUrl();

	/**
	 * Sets the URL that the application is running at, truncating if necessary.
	 * 
	 * @param url
	 *            URL that application is running at
	 */
	void setUrl(String url);

	/**
	 * Gets streaming source name (sender name)
	 * 
	 * @return streaming source name (sender name)
	 */
	String getSSN();

	/**
	 * Sets streaming source name (sender name)
	 * 
	 * @param ssn
	 *            streaming source name (sender name)
	 */
	void setSSN(String ssn);

	/**
	 * Gets source type.
	 * 
	 * @return container type
	 */
	SourceType getType();

	/**
	 * Gets source factory.
	 * 
	 * @return source factory
	 */
	SourceFactory getSourceFactory();

	/**
	 * Returns value of {@code fieldName} defined field for this source.
	 *
	 * @param fieldName
	 *            source field name
	 * @return field contained value
	 */
	Object getFieldValue(String fieldName);
}
