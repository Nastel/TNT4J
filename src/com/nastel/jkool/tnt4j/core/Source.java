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

import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>Implements an Source entity.</p>
 *
 * <p>This is the root level object.  An instance of this object represents a single
 * instance of an application running within a specific container for a specific user.</p>
 *
 *
 * @version $Revision: 3 $
 */
public class Source  {

	/**
	 * Maximum length of Source Name.
	 * @since Revision 15
	 */
	public static final int MAX_NAME_LENGTH = 64;

	/**
	 * Maximum length of Container Name.
	 * @since Revision 15
	 */
	public static final int MAX_CONTAINER_NAME_LENGTH = 256;

	/**
	 * Maximum length of Source User Name.
	 * @since Revision 15
	 */
	public static final int MAX_USER_NAME_LENGTH = 64;

	/**
	 * Maximum length of Source URL.
	 * @since Revision 26
	 */
	public static final int MAX_URL_LENGTH = 512;

	/**
	 * Maximum length of Container Address.
	 * @since Revision 27
	 */
	public static final int MAX_CONTAINER_ADDRESS_LENGTH = 64;

	/**
	 * Maximum length of Container OS.
	 * @since Revision 27
	 */
	public static final int MAX_CONTAINER_TYPE_LENGTH = 256;

	private String name;
	private String container;
	private String user;
	private int    cpuCount;
	private int    mipsCount;
	private String url;
	private String ip;
	private String os;

	/**
	 * Creates an Source object with the specified name, deriving other attributes
	 * from local environment.
	 *
	 * @param name Name used to identify the application
	 * @throws NullPointerException if name is <code>null</code>
	 * @throws IllegalArgumentException if name is empty
	 */
	public Source(String name) {
		this(name, Utils.getLocalHostName(), Runtime.getRuntime().availableProcessors());
	}


	/**
	 * Creates an Source object with the specified name, deriving other attributes
	 * from local environment.
	 *
	 * @param name Name used to identify the application
	 * @param container name that application is running on
	 * @throws NullPointerException if name is <code>null</code>
	 * @throws IllegalArgumentException if name is empty
	 */
	public Source(String name, String container) {
		this(name, container, Runtime.getRuntime().availableProcessors());
	}


	/**
	 * Creates an Source object with the specified properties.
	 *
	 * @param name Name used to identify the application
	 * @param container name that application is running on
	 * @param cpuCount number of CPUs available to application
	 * @throws NullPointerException if any arguments are <code>null</code>
	 * @throws IllegalArgumentException if any arguments are empty
	 *  or if cpuCount less than or equal to zero
	 */
	public Source(String name, String container, int cpuCount) {
		setName(name);
		setContainer(container);
		setCpuCount(cpuCount);
		setUser(System.getProperty("user.name"));
		setDefaultOsInfo();
		setContainerAddress(Utils.getLocalHostAddress());
	}

	/**
	 * Gets the name used to identify the application.
	 *
	 * @return name of application
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name used to identify the application, truncating if necessary
	 *
	 * @param name Name used to identify the application
	 * @throws NullPointerException if name is <code>null</code>
	 * @throws IllegalArgumentException if name is empty
	 * @see #MAX_NAME_LENGTH
	 */
	public void setName(String name) {
		if (name == null)
			throw new NullPointerException("application name must be a non-empty string");
		if (name.length() == 0)
			throw new IllegalArgumentException("application name must be a non-empty string");
		if (name.length() > MAX_NAME_LENGTH)
			name = name.substring(0, MAX_NAME_LENGTH);
		this.name = name;
	}

	/**
	 * Gets the container name that the application is running on.
	 *
	 * @return container name that application is running on
	 */
	public String getContainer() {
		return container;
	}

	/**
	 * Sets the container name for the application, indicating where the application is running,
	 * truncating if necessary.
	 *
	 * @param containerName name that application is running on
	 * @throws NullPointerException if container is <code>null</code>
	 * @throws IllegalArgumentException if container is empty
	 * @see #MAX_CONTAINER_NAME_LENGTH
	 */
	public void setContainer(String containerName) {
		if (containerName == null)
			throw new NullPointerException("container must be a non-empty string");
		if (containerName.length() == 0)
			throw new IllegalArgumentException("container must be a non-empty string");
		if (containerName.length() > MAX_CONTAINER_NAME_LENGTH)
			containerName = containerName.substring(0, MAX_CONTAINER_NAME_LENGTH);
		this.container = containerName;
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
	 * @param user User name that application is running under
	 * @see #MAX_USER_NAME_LENGTH
	 */
	public void setUser(String user) {
		if (user != null) {
			if (user.length() > MAX_USER_NAME_LENGTH)
				user = user.substring(0, MAX_USER_NAME_LENGTH);
			else if (user.length() == 0)
				user = null;
		}
		this.user = user;
	}

	/**
	 * Gets the number of CPUs reported as being available to the application.
	 *
	 * @return number of CPUs
	 */
	public int getCpuCount() {
		return cpuCount;
	}

	/**
	 * Sets the number of CPUs which are available to the application.
	 *
	 * @param cpuCount number of CPUs available to application
	 * @throws IllegalArgumentException if cpuCount is less than or equal to zero
	 */
	public void setCpuCount(int cpuCount) {
		if (cpuCount <= 0)
			throw new IllegalArgumentException("cpuCount must be a > 0");
		this.cpuCount = cpuCount;
	}

	/**
	 * Gets the number of MIPS reported as being available to the application.
	 *
	 * @return number of MIPS
	 */
	public int getMipsCount() {
		return mipsCount;
	}

	/**
	 * Sets the number of MIPS which are available to the application.
	 *
	 * @param mipsCount number of MIPS available to application
	 * @throws IllegalArgumentException if mipsCount is less than or equal to zero
	 */
	public void setMipsCount(int mipsCount) {
		if (mipsCount <= 0)
			throw new IllegalArgumentException("mipsCount must be a > 0");
		this.mipsCount = mipsCount;
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
	 * @param url URL that application is running at
	 * @see #MAX_URL_LENGTH
	 */
	public void setUrl(String url) {
		if (url != null) {
			if (url.length() > MAX_URL_LENGTH)
				url = url.substring(0, MAX_URL_LENGTH);
			else if (url.length() == 0)
				url = null;
		}
		this.url = url;
	}

	/**
	 * <p>Gets the IP Address of container that application is running on.</p>
	 *
	 * <p>If this attribute was not explicitly set, it defaults to the
	 * IP address of the application's container name, or current host if
	 * container name cannot be resolved to an IP Address.</p>
	 *
	 * @return IP Address of container that application is running on
	 * @since Revision 27
	 */
	public String getContainerAddress() {
		return ip;
	}

	/**
	 * Sets the IP Address of container that application is running on, truncating if necessary.
	 *
	 * @param ip Address of container that application is running on
	 * @see #MAX_CONTAINER_ADDRESS_LENGTH
	 * @since Revision 27
	 */
	public void setContainerAddress(String ip) {
		if (ip != null) {
			if (ip.length() > MAX_CONTAINER_ADDRESS_LENGTH)
				ip = ip.substring(0, MAX_CONTAINER_ADDRESS_LENGTH);
			else if (ip.length() == 0)
				ip = null;
		}

		if (!Utils.isEmpty(ip))
			this.ip = ip;
	}

	/**
	 * <p>Gets the operation system type for container that application is running on.</p>
	 *
	 * <p>If this attribute was not explicitly set, it defaults to the
	 * concatenation of the system properties "os.name" and "os.version".</p>
	 *
	 * @return operation system type for container that application is running on
	 * @since Revision 27
	 */
	public String getContainerType() {
		return os;
	}

	/**
	 * Sets the operation system type for container that application is running on.
	 *
	 * @param os operation system type for container that application is running on
	 * @see #MAX_CONTAINER_TYPE_LENGTH
	 * @since Revision 27
	 */
	public void setContainerType(String os) {
		if (os != null) {
			if (os.length() > MAX_CONTAINER_TYPE_LENGTH)
				os = os.substring(0, MAX_CONTAINER_TYPE_LENGTH);
			else if (os.length() == 0)
				os = null;
		}

		this.os = os;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + ((name == null)   ? 0 : name.hashCode());
		result = prime * result + ((container == null) ? 0 : container.hashCode());
		result = prime * result + ((user == null)   ? 0 : user.hashCode());

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

		if (container == null) {
			if (other.container != null)
				return false;
		}
		else if (!container.equals(other.container)) {
			return false;
		}

		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name)) {
			return false;
		}

		if (user == null) {
			if (other.user != null)
				return false;
		}
		else if (!user.equals(other.user)) {
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

		str.append(super.toString()).append("{")
			.append("Name: ").append(getName()).append(",")
			.append("User: ").append(getUser()).append(",")
			.append("Container: ").append(getContainer()).append(",")
			.append("Address: ").append(getContainerAddress()).append(",")
			.append("CPUS: ").append(getCpuCount()).append(",")
			.append("MIPS: ").append(getMipsCount()).append(",")
			.append("URL: ").append(getUrl()).append(",")
			.append("Type: ").append(Utils.quote(getContainerType())).append("}");

		return str.toString();
	}

	private void setDefaultOsInfo() {
		setContainerType(System.getProperty("os.name") + ", Version: " + System.getProperty("os.version") + ", Arch: " + System.getProperty("os.arch"));
	}
}
