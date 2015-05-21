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
package com.nastel.jkool.tnt4j.source;

import com.nastel.jkool.tnt4j.utils.Utils;

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
	private Source parentSource;
	private String sname;
	private SourceType sourceType;
	private String user;
	private String url;
	private String info;

	/**
	 * Creates an Source object with the specified properties.
	 * 
	 * @param name
	 *            Name used to identify the source
	 * @param type
	 *            source type
	 * @param root
	 *            parent source
	 * @param userName
	 *            user name associated with this source
	 */
	public DefaultSource(String name, SourceType type, Source root, String userName) {
		setName(name);
		setType(type);
		setSource(root);
		setUser(userName);
		setDefaultInfo();
	}

	@Override
	public String getName() {
		return sname;
	}

	@Override
	public String getFQName() {
		return (parentSource == null) ? sourceType + "=" + sname : sourceType + "=" + sname + "#"
		        + parentSource.getFQName();
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
		if (this.sourceType.equals(type))
			return this;
		return parentSource != null ? (parentSource.getType().equals(type) ? parentSource : parentSource
		        .getSource(type)) : null;
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
	public String getInfo() {
		return info;
	}

	@Override
	public void setInfo(String inf) {
		this.info = inf;
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
	 * @param type source type
	 */
	protected void setType(SourceType type) {
		sourceType = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + ((sname == null) ? 0 : sname.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());

		return result;
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
		if (!(obj instanceof Source))
			return false;

		final Source other = (Source) obj;

		if (sname == null) {
			if (other.getName() != null)
				return false;
		} else if (!sname.equals(other.getName())) {
			return false;
		}

		if (!sourceType.equals(other.getType())) {
			return false;
		}

		if (user == null) {
			if (other.getUser() != null)
				return false;
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
		StringBuilder str = new StringBuilder();

		str.append(super.toString()).append("{").append("FQName: ").append(getFQName()).append(",").append("Name: ")
		        .append(getName()).append(",").append("User: ").append(getUser()).append(",").append("Type: ")
		        .append(getType()).append(",").append("URL: ").append(getUrl()).append(",").append("OS: ")
		        .append(Utils.quote(getInfo())).append("}");

		return str.toString();
	}

	private void setDefaultInfo() {
		setInfo(System.getProperty("os.name") + ", Version: " + System.getProperty("os.version") + ", Arch: "
		        + System.getProperty("os.arch"));
	}
}
