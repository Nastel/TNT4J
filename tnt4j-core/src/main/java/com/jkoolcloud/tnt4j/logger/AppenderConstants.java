/*
 * Copyright 2014-2023 JKOOL, LLC.
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
package com.jkoolcloud.tnt4j.logger;

/**
 * <p>
 * Common constants used by all logging appenders: tags &amp; qualifiers.
 * </p>
 * 
 * @version $Revision: 11 $
 * 
 */
public interface AppenderConstants {
	String PARAM_BEGIN_LABEL = "beg";
	String PARAM_END_LABEL = "end";

	String PARAM_APPL_LABEL = "app";
	String PARAM_USER_LABEL = "usr";
	String PARAM_CORRELATOR_LABEL = "cid";
	String PARAM_TAG_LABEL = "tag";
	String PARAM_LOCATION_LABEL = "loc";
	String PARAM_OP_NAME_LABEL = "opn";
	String PARAM_OP_TYPE_LABEL = "opt";
	String PARAM_RESOURCE_LABEL = "rsn";
	String PARAM_MSG_DATA_LABEL = "msg";
	String PARAM_EXCEPTION_LABEL = "exc";
	String PARAM_SEVERITY_LABEL = "sev";
	String PARAM_COMP_CODE_LABEL = "ccd";
	String PARAM_REASON_CODE_LABEL = "rcd";
	String PARAM_START_TIME_LABEL = "stt";
	String PARAM_END_TIME_LABEL = "ent";
	String PARAM_ELAPSED_TIME_LABEL = "elt";
	String PARAM_AGE_TIME_LABEL = "age";

	String TAG_TYPE_QUALIFIER = "%";
	String TAG_TYPE_INTEGER = "%i";
	String TAG_TYPE_LONG = "%l";
	String TAG_TYPE_DOUBLE = "%d";
	String TAG_TYPE_FLOAT = "%f";
	String TAG_TYPE_NUMBER = "%n";
	String TAG_TYPE_BOOLEAN = "%b";
	String TAG_TYPE_STRING = "%s";
}
