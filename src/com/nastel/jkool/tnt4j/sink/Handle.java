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
package com.nastel.jkool.tnt4j.sink;

import java.io.IOException;

/**
 * <p>
 * This interface defines a handle interface, which can be opened, closed.
 * </p>
 * 
 * 
 * @version $Revision: 1 $
 * 
 */
public interface Handle extends java.io.Closeable {
	/**
	 * This method opens and prepares message destination for writing.
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException;

	/**
	 * This method determines of the message destination is in open state 
	 * and ready for writing.
	 * 
	 * @return true if open, false otherwise.
	 */
	public boolean isOpen();
}
