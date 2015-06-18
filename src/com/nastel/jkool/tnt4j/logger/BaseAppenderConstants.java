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
package com.nastel.jkool.tnt4j.logger;


/**
 * <p>
 * Common constants used by all logging appenders: tags & qualifiers.
 * </p>
 * 
 * @version $Revision: 11 $
 * 
 */
public interface BaseAppenderConstants {
	static final String PARAM_BEGIN_LABEL = "beg";
	static final String PARAM_END_LABEL = "end";

	static final String PARAM_APPL_LABEL = "app";
	static final String PARAM_USER_LABEL = "usr";
	static final String PARAM_CORRELATOR_LABEL = "cid";
	static final String PARAM_TAG_LABEL = "tag";
	static final String PARAM_LOCATION_LABEL = "loc";
	static final String PARAM_OP_NAME_LABEL = "opn";
	static final String PARAM_OP_TYPE_LABEL = "opt";
	static final String PARAM_RESOURCE_LABEL = "rsn";
	static final String PARAM_MSG_DATA_LABEL = "msg";
	static final String PARAM_SEVERITY_LABEL = "sev";
	static final String PARAM_COMP_CODE_LABEL = "ccd";
	static final String PARAM_REASON_CODE_LABEL = "rcd";
	static final String PARAM_START_TIME_LABEL = "stt";
	static final String PARAM_END_TIME_LABEL = "ent";
	static final String PARAM_ELAPSED_TIME_LABEL = "elt";
	static final String PARAM_AGE_TIME_LABEL = "age";

	static final String TAG_TYPE_QUALIFIER = "%";
	static final String TAG_TYPE_INTEGER = "%i";
	static final String TAG_TYPE_LONG = "%l";
	static final String TAG_TYPE_DOUBLE = "%d";
	static final String TAG_TYPE_FLOAT = "%f";
	static final String TAG_TYPE_NUMBER = "%n";
	static final String TAG_TYPE_BOOLEAN = "%b";
	static final String TAG_TYPE_STRING = "%s";
}
