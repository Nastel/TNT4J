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
package com.nastel.jkool.tnt4j.throttle;

/**
 * Default throttle factory implementation
 *
 * @version $Revision: 1 $
 */
public class ThrottleFactoryImpl implements ThrottleFactory {

	@Override
    public Throttle newThrottle(int maxMps, int maxBps, boolean enabled) {
	    return new ThrottleImpl(maxMps, maxBps, enabled);
    }
}
