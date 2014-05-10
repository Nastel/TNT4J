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
package com.nastel.jkool.tnt4j.utils;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * This class computes estimated memory footprint of a specific object. The implementation provides shallow size as well
 * as deep size which includes all reachable objects. This class must be included in the "javaagent:" command line
 * option to be able to compute object sizes.
 * 
 * @version $Revision: 2 $
 */
public class SizeOf {
	/**
	 * Instance of java.lang.instrument.Instrument injected by the Java VM
	 * 
	 * @see premain(String options, Instrumentation inst)
	 */
	private static Instrumentation inst;

	private static boolean SKIP_STATIC_FIELD = false;
	private static boolean SKIP_FINAL_FIELD = false;
	private static boolean SKIP_FLYWEIGHT_FIELD = false;

	/**
	 * Callback method used by the Java VM to inject the java.lang.instrument.Instrument instance
	 */
	public static void premain(String options, Instrumentation inst) {
		SizeOf.inst = inst;
	}

	/**
	 * Calls java.lang.instrument.Instrument.getObjectSize(object).
	 * 
	 * @param object
	 *            the object to size
	 * @return an implementation-specific approximation of the amount of storage consumed by the specified object.
	 * 
	 */
	public static long sizeOf(Object object) {
		if ((inst == null) || (SKIP_FLYWEIGHT_FIELD && isSharedFlyweight(object)))
			return 0;
		return inst.getObjectSize(object);
	}

	/**
	 * Compute an implementation-specific approximation of the amount of storage consumed by objectToSize and by all the
	 * objects reachable from it
	 * 
	 * @param objectToSize
	 * @return an implementation-specific approximation of the amount of storage consumed by objectToSize and by all the
	 *         objects reachable from it
	 */
	public static long deepSizeOf(Object objectToSize) {
		Map<Object, Long> doneObj = new IdentityHashMap<Object, Long>();
		return deepSizeOf(objectToSize, doneObj, null, 0);
	}

	/**
	 * Compute an implementation-specific approximation of the amount of storage consumed by objectToSize and by all the
	 * objects reachable from it
	 * 
	 * @param objectToSize
	 * @return an implementation-specific approximation of the amount of storage consumed by objectToSize and by all the
	 *         objects reachable from it
	 */
	public static long deepSizeOf(Object objectToSize, Map<Field, Long> doneFields) {
		Map<Object, Long> doneObj = new IdentityHashMap<Object, Long>();
		return deepSizeOf(objectToSize, doneObj, doneFields, 0);
	}

	private static long deepSizeOf(Object o, Map<Object, Long> doneObj, Map<Field, Long> doneFields, int depth) {
		if (o == null || (inst == null)) {
			return 0;
		}

		long size = 0;
		if (doneObj.containsKey(o)) {
			return 0;
		}
		
		size = sizeOf(o);
		doneObj.put(o, size); // record initial size

		Class<?> clazz = o.getClass();		
		if (clazz.isArray()) {
			int length = Array.getLength(o);
			for (int i = 0; i < length; i++) {
				size += deepSizeOf(Array.get(o, i), doneObj, doneFields, depth + 1);
			}
		} else {
			do {
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					field.setAccessible(true);
					Object obj;
					try {
						obj = field.get(o);
					} catch (Throwable e) {
						throw new RuntimeException(e);
					} 
					if (isComputable(field)) {
						long fSize = deepSizeOf(obj, doneObj, doneFields, depth + 1);
						size += fSize;
						if (doneFields != null) doneFields.put(field, fSize);
					}
				}
				clazz = clazz.getSuperclass();
			} while (clazz != null);
		}
		doneObj.put(o, size); // record final size
		return size;
	}


	/**
	 * Determines if the field is computable based on flags set for primitive, static and final fields.
	 * 
	 * @param field
	 * @return true if the field must be computed
	 */
	private static boolean isComputable(Field field) {
		int modificatori = field.getModifiers();

		if (field.getType().isPrimitive())
			return false;
		else if (SKIP_STATIC_FIELD && Modifier.isStatic(modificatori))
			return false;
		else if (SKIP_FINAL_FIELD && Modifier.isFinal(modificatori))
			return false;
		else
			return true;
	}

	/**
	 * Returns true if this is a well-known shared flyweight. For example, interned Strings, Booleans and Number
	 * objects.
	 * 
	 */
	private static boolean isSharedFlyweight(Object obj) {
		// optimization - all of our flyweights are Comparable
		if (obj instanceof Comparable) {
			if (obj instanceof Enum) {
				return true;
			} else if (obj instanceof String) {
				return (obj == ((String) obj).intern());
			} else if (obj instanceof Boolean) {
				return (obj == Boolean.TRUE || obj == Boolean.FALSE);
			} else if (obj instanceof Integer) {
				return (obj == Integer.valueOf((Integer) obj));
			} else if (obj instanceof Short) {
				return (obj == Short.valueOf((Short) obj));
			} else if (obj instanceof Byte) {
				return (obj == Byte.valueOf((Byte) obj));
			} else if (obj instanceof Long) {
				return (obj == Long.valueOf((Long) obj));
			} else if (obj instanceof Character) {
				return (obj == Character.valueOf((Character) obj));
			}
		}
		return false;
	}

	/**
	 * If true deepSizeOf() doesn't compute the final fields of an object. Default value is false.
	 */
	public static void skipFinal(boolean skip_final_field) {
		SKIP_FINAL_FIELD = skip_final_field;
	}

	/**
	 * If true deepSizeOf() doesn't compute the static fields of an object. Default value is false.
	 */
	public static void skipStatic(boolean skip_static_field) {
		SKIP_STATIC_FIELD = skip_static_field;
	}

	/**
	 * If true flyweight objects has a size of 0. Default value is false.
	 */
	public static void skipFlyweight(boolean skip) {
		SKIP_FLYWEIGHT_FIELD = skip;
	}
}
