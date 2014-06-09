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
package com.nastel.jkool.tnt4j.core;

import java.util.StringTokenizer;

import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * Implements an Source entity.
 * </p>
 * 
 * <p>
 * Source is an entity that generates events and activities. Sources can be nested. By default root source is defined by the
 * following property:
 * 
 * <pre>
 * {@code
 *  tnt4j.source.root.fqname=JVM=?#SERVER=?#ADDRESS=?
 * }
 * </pre>
 * Source above is interpreted as: JVM running on a SERVER, which in turn located at given ADDRESS (IP). "?" automatically maps to runtime
 * values within a given runtime environment. All new source instantiations use <code>Source.defaultRootSource()</code> as the parent.
 * 
 * </p>
 * 
 * 
 * @version $Revision: 3 $
 */
public class Source {
	private static Source systemRootSource = fromFQN(System.getProperty("tnt4j.source.root.fqname",
	        "JVM=?#SERVER=?#ADDRESS=?"));

	private Source parentSource;
	private String sname;
	private SourceType sourceType;
	private String user;
	private String url;
	private String os;

	/**
	 * Creates an Source object with the specified name, deriving other attributes from local environment.
	 * 
	 * @param name
	 *            Name used to identify the application
	 */
	public Source(String name) {
		this(name, SourceType.APPL);
	}

	/**
	 * Creates an Source object with the specified name, deriving other attributes from local environment.
	 * 
	 * @param name
	 *            Name used to identify the application
	 * @param type
	 *            source type
	 */
	public Source(String name, SourceType type) {
		this(name, type, defaultRootSource());
	}

	/**
	 * Creates an Source object with the specified properties.
	 * 
	 * @param name
	 *            Name used to identify the application
	 * @param root
	 *            parent source
	 */
	public Source(String name, SourceType type, Source root) {
		setName(getSourceName(name, type));
		setType(type);
		setSource(root);
		setUser(System.getProperty("user.name"));
		setDefaultInfo();
	}

	/**
	 * <p>
	 * Gets root source.
	 * </p>
	 * 
	 * @return root source
	 */
	public static Source defaultRootSource() {
		return systemRootSource;
	}

	/**
	 * <p>
	 * Obtains default name based on a given name/type pair ? name is converted into a runtime binding. Example: ?,
	 * SERVER will return localhost name of the location server.
	 * </p>
	 * 
	 * @return container type
	 */
	protected String getSourceName(String name, SourceType type) {
		if (name.equals("?") && type == SourceType.SERVER)
			name = Utils.getLocalHostName();
		else if (name.equals("?") && type == SourceType.ADDRESS)
			name = Utils.getLocalHostAddress();
		else if (name.equals("?") && type == SourceType.JVM)
			name = Utils.getVMName();
		else if (name.equals("?"))
			throw new RuntimeException("Unknown name for type=" + type);
		return name;
	}

	/**
	 * <p>
	 * Create source from a given fully qualified name.
	 * </p>
	 * 
	 * @return source name based on fqn
	 */
	public static Source fromFQN(String fq) {
		StringTokenizer tk = new StringTokenizer(fq, "#");
		Source child = null, root = null;
		while (tk.hasMoreTokens()) {
			String sName = tk.nextToken();
			String[] pair = sName.split("=");
			Source source = new Source(pair[1], SourceType.valueOf(pair[0]), null);
			if (child != null)
				child.setSource(source);
			if (root == null)
				root = source;
			child = source;
		}
		return root;
	}

	/**
	 * Gets the name used to identify the application.
	 * 
	 * @return name of application
	 */
	public String getName() {
		return sname;
	}

	/**
	 * Gets the fully qualified name used to identify this source type=source-name#parent-source
	 * 
	 * @return fully qualified name of this source
	 */
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

	/**
	 * Gets parent source
	 * 
	 * @return parent source
	 */
	public Source getSource() {
		return parentSource;
	}

	/**
	 * Sets the parent of this source indicating contains relationship
	 * 
	 * @param parent
	 *            source associated with his source
	 */
	public Source setSource(Source parent) {
		this.parentSource = parent;
		return this;
	}

	/**
	 * Gets the user name that the application is running under.
	 * 
	 * @return name of user running application
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the user name that the application is running under, truncating if necessary.
	 * 
	 * @param user
	 *            User name that application is running under
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets the URL that the application is running at.
	 * 
	 * @return URL that application is running at
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the URL that the application is running at, truncating if necessary.
	 * 
	 * @param url
	 *            URL that application is running at
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * <p>
	 * Gets source info string.
	 * </p>
	 * 
	 * <p>
	 * If this attribute was not explicitly set, it defaults to the concatenation of the system properties "os.name" and
	 * "os.version".
	 * </p>
	 * 
	 * @return source info
	 * @since Revision 27
	 */
	public String getInfo() {
		return os;
	}

	/**
	 * Sets source info associated with this source
	 * 
	 * @param inf
	 *            info associated with the source
	 * @since Revision 27
	 */
	public void setInfo(String inf) {
		this.os = inf;
	}

	/**
	 * <p>
	 * Gets source type.
	 * </p>
	 * 
	 * @return container type
	 */
	public SourceType getType() {
		return sourceType;
	}

	/**
	 * <p>
	 * Set source type associated with this source.
	 * </p>
	 * 
	 */
	public void setType(SourceType ct) {
		sourceType = ct;
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
			if (other.sname != null)
				return false;
		} else if (!sname.equals(other.sname)) {
			return false;
		}

		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user)) {
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
		        .append(getName()).append(",").append("User: ").append(getUser()).append(",").append("Type: ").append(
		                getType()).append(",").append("URL: ").append(getUrl()).append(",").append("OS: ").append(
		                Utils.quote(getInfo())).append("}");

		return str.toString();
	}

	private void setDefaultInfo() {
		setInfo(System.getProperty("os.name") + ", Version: " + System.getProperty("os.version") + ", Arch: "
		        + System.getProperty("os.arch"));
	}
}
