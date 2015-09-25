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
package com.nastel.jkool.tnt4j.core;

/**
 * This interface defines a way to relate 2 objects together.
 *
 *
 * @version $Revision: 1 $
 */
public interface Relate2<T> {
	public static final int OBJ_ONE = 0;
	public static final int OBJ_TWO = 1;
	
	/**
	 * Relate object A to given object B
	 * {@code objA->ObjB}
	 *
	 * @param objA first object to relate
	 * @param objB object to relate to
	 * @param type of relationship between objA and objB {@code objA->ObjB}
	 * @return same relation object
	 */
	Relate2<T> relate2(T objA, T objB, OpType type);

	/**
	 * Relate current object to given object B
	 *
	 * @param objB object to relate to
	 * @param type of relationship
	 * @return same relation object
	 */
	Relate2<T> relate2(T objB, OpType type);

	/**
	 * Clear current relation if any
	 *
	 * @return same relation object
	 */
	Relate2<T> clear2();
	
	/**
	 * Obtain relation type
	 *
	 * @return relation type.
	 */
	OpType get2Type();
	
	/**
	 * Obtain relation binding which consists of 2 elements.
	 *
	 * @return relation binding
	 */
	T get2(int index);
}
