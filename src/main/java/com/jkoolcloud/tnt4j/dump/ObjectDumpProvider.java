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
package com.jkoolcloud.tnt4j.dump;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import com.jkoolcloud.tnt4j.utils.SizeOf;

/**
 * <p>
 * This class dumps the contents of a given object using reflection. All fields and their values are reported as part of
 * the {@link com.jkoolcloud.tnt4j.dump.DumpCollection} collection.
 * </p>
 * 
 * @see DumpCollection
 * 
 * @version $Revision: 6 $
 * 
 */
public class ObjectDumpProvider extends DefaultDumpProvider {
	WeakReference<Object> ref = null;

	private int max_size = 1000;
	private boolean shallowSizeOf = true, deepSizeOf = true;

	/**
	 * Create a new java object dump provider with a given name and user specified object
	 * 
	 * @param name
	 *            provider name
	 * @param obj
	 *            object to be dumped
	 */
	public ObjectDumpProvider(String name, Object obj) {
		this(name, "Objects", obj);
	}

	/**
	 * Create a new java object dump provider with a given name and user specified object
	 * 
	 * @param name
	 *            provider name
	 * @param cat
	 *            provider category
	 * @param obj
	 *            object to be dumped
	 */
	public ObjectDumpProvider(String name, String cat, Object obj) {
		super(name, cat);
		ref = new WeakReference<Object>(obj);
	}

	/**
	 * Enable/disable shallow, deep memory size of the object associated with this dump provider.
	 * 
	 * @param shallow
	 *            enable shallow sizeOf object reporting
	 * @param deep
	 *            enable deep sizeOf object reporting
	 */
	public void setMemorySizeOf(boolean shallow, boolean deep) {
		shallowSizeOf = shallow;
		deepSizeOf = deep;
	}

	/**
	 * Set the maximum collection, map size limit for which to report collection value. This is done to avoid dumping of
	 * large collections. Collections, maps at or below the max will be dumped as a collection value otherwise just
	 * collection size is reported
	 * 
	 * @param max
	 *            maximum collection, map size limit
	 */
	public void setMaxCollectionLimit(int max) {
		max_size = max;
	}

	/**
	 * Obtain maximum collection, map size limit for which to report collection value. This is done to avoid dumping of
	 * large collections. Collections, maps at or below the max will be dumped as a collection value otherwise just
	 * collection size is reported
	 * 
	 * @return max maximum collection, map size limit
	 */
	public int getMaxCollectionLimit() {
		return max_size;
	}

	private Dump getFields(Class<?> clazz, Object dObj, Dump dump) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field fld : fields) {
			fld.setAccessible(true);
			try {
				dump.add(clazz.getName() + "." + fld.getName() + ".$type", fld.getType().getName());
				dump.add(clazz.getName() + "." + fld.getName() + ".$modifiers", Modifier.toString(fld.getModifiers()));
				Object fHandle = fld.get(dObj);

				boolean dumpValue = true;
				if (fHandle instanceof Collection<?>) {
					int size = ((Collection<?>) fHandle).size();
					dumpValue = size <= max_size;
					dump.add(clazz.getName() + "." + fld.getName() + ".$size", size);
				} else if (fHandle instanceof Map<?, ?>) {
					int size = ((Map<?, ?>) fHandle).size();
					dumpValue = size <= max_size;
					dump.add(clazz.getName() + "." + fld.getName() + ".$size", size);
				} else if (clazz.isArray()) {
					int size = Array.getLength(dObj);
					dumpValue = size <= max_size;
					dump.add(clazz.getName() + "." + fld.getName() + ".$size", size);
				}
				if (dumpValue) {
					dump.add(clazz.getName() + "." + fld.getName() + ".$value", String.valueOf(fHandle));
				}
				if (fHandle != null) {
					dump.add(clazz.getName() + "." + fld.getName() + ".$class", fHandle.getClass().getName());
					long sizeOf = SizeOf.sizeOf(fHandle);
					if (sizeOf > 0) {
						dump.add(clazz.getName() + "." + fld.getName() + ".$sizeOf", sizeOf);
					}
					sizeOf = fld.getType().isPrimitive() ? 0 : SizeOf.deepSizeOf(fHandle);
					if (sizeOf > 0) {
						dump.add(clazz.getName() + "." + fld.getName() + ".$deepSizeOf", sizeOf);
					}
				}
			} catch (Throwable e) {
				dump.add(clazz.getName() + "." + fld.getName() + ".$exception", e);
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if (superClass != null) {
			return getFields(superClass, dObj, dump);
		}
		return dump;
	}

	@Override
	public DumpCollection getDump() {
		Dump dump = null;
		Object dObj = ref.get();
		if (dObj != null) {
			dump = new Dump(dObj.toString(), this);
			Class<?> clazz = dObj.getClass();
			if (shallowSizeOf) {
				long size = SizeOf.sizeOf(dObj);
				if (size > 0) {
					dump.add(clazz.getName() + ".$sizeOf", size);
				}
			}

			if (deepSizeOf) {
				long size = SizeOf.deepSizeOf(dObj);
				if (size > 0) {
					dump.add(clazz.getName() + ".$deepSizeOf", size);
				}
			}
			dump.add(clazz.getName() + ".$classloader", String.valueOf(clazz.getClassLoader()));
			getFields(clazz, dObj, dump);
		}
		return dump;
	}
}
