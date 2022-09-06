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

package com.jkoolcloud.tnt4j.core.fields;

import com.jkoolcloud.tnt4j.core.Property;
import com.jkoolcloud.tnt4j.core.ValueTypes;

/**
 * This class implements event or transaction cost property
 * 
 * @see Property
 * @see ValueTypes
 *
 * @version $Revision: 1 $
 *
 */
public class CostField extends Property {
	public static final String COST_PROP_NAME = "cost";

	/**
	 * Create a cost field with value type {@link ValueTypes}
	 * 
	 * @param value
	 *            associated with the property
	 */
	public CostField(Object value) {
		super(COST_PROP_NAME, value, ValueTypes.VALUE_TYPE_CURRENCY_USD);
	}

	/**
	 * Create a cost field
	 * 
	 * @param value
	 *            associated with the property
	 * @param valType
	 *            value type {@link ValueTypes}
	 * @see ValueTypes
	 */
	public CostField(Object value, String valType) {
		super(COST_PROP_NAME, value, valType);
	}

	/**
	 * Create a cost field
	 * 
	 * @param value
	 *            value associated with the property
	 * @param valType
	 *            value type {@link ValueTypes}
	 * @param trans
	 *            flag to mark field as transient
	 */
	public CostField(Object value, String valType, boolean trans) {
		super(COST_PROP_NAME, value, valType, trans);
	}

	/**
	 * Create a cost field, with value type {@link ValueTypes}
	 * 
	 * @param value
	 *            associated with the property with {@link ValueTypes}
	 * @param trans
	 *            flag to mark field as transient
	 */
	public CostField(Object value, boolean trans) {
		super(COST_PROP_NAME, value, ValueTypes.VALUE_TYPE_CURRENCY_USD, trans);
	}
}
