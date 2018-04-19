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
package com.jkoolcloud.tnt4j.source;

import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * Implements Source entity -- container of other sources. Each one identifies a specific entity such as an application,
 * server, device etc.
 * </p>
 * 
 * 
 * @version $Revision: 3 $
 */
public class DefaultSource implements Source {
	private String sname;
	private String user;
	private String url;
	private String ssname;
	private Source parentSource;
	private SourceType sourceType;
	private SourceFactory factory;

	/**
	 * Creates an Source object with the specified properties.
	 *
	 * @param fac
	 *            source factory instance
	 * @param name
	 *            Name used to identify the source
	 * @param type
	 *            source type
	 * @param parent
	 *            parent source
	 * @param userName
	 *            user name associated with this source
	 */
	public DefaultSource(SourceFactory fac, String name, SourceType type, Source parent, String userName) {
		factory = fac;
		setName(name);
		setType(type);
		setSource(parent);
		setUser(userName);
	}

	@Override
	public String getName() {
		return sname;
	}

	@Override
	public String getFQName() {
		StringBuilder buff = new StringBuilder(128);
		return getFQName(buff).toString();
	}

	@Override
	public StringBuilder getFQName(StringBuilder buff) {
		buff.append(sourceType).append("=").append(sname);
		if (parentSource != null) {
			buff.append("#");
			parentSource.getFQName(buff);
		}
		return buff;
	}

	/**
	 * Sets the name used to identify the application, truncating if necessary
	 * 
	 * @param name
	 *            Name used to identify the application
	 */
	public void setName(String name) {
		this.sname = name;
	}

	@Override
	public Source getSource() {
		return parentSource;
	}

	@Override
	public Source getSource(SourceType type) {
		if (this.sourceType.equals(type)) {
			return this;
		}
		return parentSource != null
				? (parentSource.getType().equals(type) ? parentSource : parentSource.getSource(type)) : null;
	}

	/**
	 * Sets the parent of this source indicating contains relationship
	 * 
	 * @param parent
	 *            source associated with his source
	 * @return same source instance
	 */
	public Source setSource(Source parent) {
		this.parentSource = parent;
		return this;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getSSN() {
		return !Utils.isEmpty(ssname) ? ssname : (parentSource != null ? parentSource.getSSN() : ssname);
	}

	@Override
	public void setSSN(String ssn) {
		this.ssname = ssn;
	}

	@Override
	public SourceType getType() {
		return sourceType;
	}

	/**
	 * <p>
	 * Set source type associated with this source.
	 * </p>
	 * 
	 * @param type
	 *            source type
	 */
	protected void setType(SourceType type) {
		sourceType = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;

		result = prime * result + ((sname == null) ? 0 : sname.hashCode());
		result = prime * result + ((ssname == null) ? 0 : ssname.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());

		return result;
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
		if (!(obj instanceof Source)) {
			return false;
		}

		Source other = (Source) obj;

		if (sname == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!sname.equals(other.getName())) {
			return false;
		}

		if (!sourceType.equals(other.getType())) {
			return false;
		}

		if (user == null) {
			if (other.getUser() != null) {
				return false;
			}
		} else if (!user.equals(other.getUser())) {
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(256);

		str.append(super.toString()).append("{").append("FQName: ").append(getFQName()).append(",").append("Name: ")
				.append(getName()).append(",").append("User: ").append(getUser()).append(",").append("Type: ")
				.append(getType()).append(",").append("URL: ");
		Utils.quote(getUrl(), str).append(",").append("SSN: ");
		Utils.quote(getSSN(), str).append("}");

		return str.toString();
	}

	@Override
	public SourceFactory getSourceFactory() {
		return factory;
	}

	/**
	 * Returns value of {@code fieldName} defined field for this source.
	 * <p>
	 * List of supported field names :
	 * <ul>
	 * <li>SourceFQN</li>
	 * <li>SourceUser</li>
	 * <li>SourceName</li>
	 * <li>SourceURL</li>
	 * <li>SourceSSN</li>
	 * <li>SourceType</li>
	 * </ul>
	 *
	 * @param fieldName
	 *            source field or property name
	 * @return field contained value
	 *
	 * @see com.jkoolcloud.tnt4j.core.Trackable#getFieldValue(String)
	 */
	@Override
	public Object getFieldValue(String fieldName) {
		if ("SourceFQN".equalsIgnoreCase(fieldName)) {
			return getFQName();
		}
		if ("SourceUser".equalsIgnoreCase(fieldName)) {
			return user;
		}
		if ("SourceName".equalsIgnoreCase(fieldName)) {
			return sname;
		}
		if ("SourceURL".equalsIgnoreCase(fieldName)) {
			return url;
		}
		if ("SourceSSN".equalsIgnoreCase(fieldName)) {
			return getSSN();
		}
		if ("SourceType".equalsIgnoreCase(fieldName)) {
			return sourceType;
		}

		return null;
	}
}
