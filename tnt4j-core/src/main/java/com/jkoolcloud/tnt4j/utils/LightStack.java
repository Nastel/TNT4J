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
package com.jkoolcloud.tnt4j.utils;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * This class implements a non synchronized, non thread safe, fast stack implementation backed by an ArrayList.
 *
 * @version $Revision: 1 $
 * @param <T>
 *            type of elements in stack
 */

public class LightStack<T> extends ArrayList<T> {
	private static final long serialVersionUID = 4249091055001865102L;

	int cursor = 0;

	/**
	 * Create a new stack with a default initial capacity of 10
	 *
	 */
	public LightStack() {
		super(10);
	}

	/**
	 * Create a new stack with a given initial size
	 *
	 * @param size
	 *            stack initial capacity
	 */
	public LightStack(int size) {
		super(size);
	}

	/**
	 * Pushes an item onto the top of this stack.
	 *
	 * @param item
	 *            the item to be pushed onto this stack.
	 * @return the {@code item} argument.
	 */
	public T push(T item) {
		if (cursor >= super.size()) {
			add(item);
		} else {
			set(cursor, item);
		}
		cursor++;
		return item;
	}

	/**
	 * Removes the object at the top of this stack and returns that object as the value of this function.
	 *
	 * @return The object at the top of this stack.
	 * @exception EmptyStackException
	 *                if this stack is empty.
	 */
	public T pop() {
		T item = peek();
		cursor--;
		set(cursor, null);
		return item;
	}

	/**
	 * Removes the object at the top of this stack and returns that object as the value of this function.
	 *
	 * @param obj
	 *            to be popped, top of the stack must match the object being passed
	 *
	 * @return The object at the top of this stack.
	 * @exception EmptyStackException
	 *                if this stack is empty.
	 * @exception IllegalStateException
	 *                if the top of the stack is not the specified object
	 */
	public T pop(T obj) {
		T item = peek();
		if (item != obj) {
			throw new IllegalStateException("Item not on stop of the stack, stack.size=" + size());
		}
		cursor--;
		set(cursor, null);
		return item;
	}

	/**
	 * Looks at the object at the top of this stack without removing it from the stack.
	 *
	 * @return the object at the top of this stack (the last item of the <tt>ArrayList</tt> object).
	 * @exception EmptyStackException
	 *                if this stack is empty.
	 */
	public T peek() {
		if (cursor == 0) {
			throw new EmptyStackException();
		}
		return get(cursor - 1);
	}

	/**
	 * Looks at the object at the top of this stack without removing it from the stack.
	 *
	 * @param defValue
	 *            value to return if stack is empty
	 * @return the object at the top of this stack or defValue if empty
	 */
	public T peek(T defValue) {
		if (cursor == 0) {
			return defValue;
		}
		return get(cursor - 1);
	}

	/**
	 * Tests if this stack is empty.
	 *
	 * @return {@code true} if and only if this stack contains no items; {@code false} otherwise.
	 */
	public boolean empty() {
		return cursor == 0;
	}

	@Override
	public int size() {
		return cursor;
	}
}
