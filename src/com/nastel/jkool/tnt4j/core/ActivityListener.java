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

/**
 * <p>A simple event listener interface for tracking activity timing events.
 * This interface can be implemented by classes which need to be notified when
 * activities are started and stopped.</p>
 *
 * @see Activity
 *
 * @version $Revision: 1 $
 *
 */
public interface ActivityListener {
	/**
	 * Notifies this listener when activity is started.
	 * 
	 * @param activity activity which is just started
	 * 
	 */
	public void started(Activity activity);
	
	/**
	 * Notifies this listener when activity is stopped.
	 * 
	 * @param activity activity which is just stopped
	 * 
	 */
	public void stopped(Activity activity);
}
