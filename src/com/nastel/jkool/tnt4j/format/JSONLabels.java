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
package com.nastel.jkool.tnt4j.format;

import com.nastel.jkool.tnt4j.utils.Utils;

/**
 * <p>
 * This interface defines all labels used for generating TNT4J JSON
 * messages.
 * </p>
 *
 * @version $Revision: 1 $
 *
 */

interface JSONLabels {
	// JSON fields
	static final String JSON_NAME_FIELD = "name";
	static final String JSON_CATEGORY_FIELD = "category";
	static final String JSON_STATUS_FIELD = "status";
	static final String JSON_COUNT_FIELD = "count";
	static final String JSON_TIME_USEC_FIELD = "time-usec";
	static final String JSON_PROPERTIES_FIELD = "properties";
	static final String JSON_TYPE_FIELD = "type";
	static final String JSON_TYPE_NO_FIELD = "type-no";
	static final String JSON_VALUE_FIELD = "value";
	static final String JSON_VALUE_TYPE_FIELD = "value-type";
	static final String JSON_CORR_ID_FIELD = "corrid";
	static final String JSON_TRACK_ID_FIELD = "tracking-id";
	static final String JSON_PARENT_TRACK_ID_FIELD = "parent-id";
	static final String JSON_SOURCE_FIELD = "source";
	static final String JSON_SOURCE_URL_FIELD = "source-url";
	static final String JSON_SOURCE_FQN_FIELD = "source-fqn";
	static final String JSON_SOURCE_SSN_FIELD = "source-ssn";	
	static final String JSON_RELATE_FQN_A_FIELD = "relate-afqn";
	static final String JSON_RELATE_FQN_B_FIELD = "relate-bfqn";
	static final String JSON_RELATE_TYPE_FIELD = "relate-type";
	static final String JSON_RESOURCE_FIELD = "resource";
	static final String JSON_OPERATION_FIELD = "operation";
	static final String JSON_LOCATION_FIELD = "location";
	static final String JSON_REASON_CODE_FIELD = "reason-code";
	static final String JSON_COMP_CODE_FIELD = "comp-code";
	static final String JSON_COMP_CODE_NO_FIELD = "comp-code-no";
	static final String JSON_SEVERITY_FIELD = "severity";
	static final String JSON_SEVERITY_NO_FIELD = "severity-no";
	static final String JSON_FQN_FIELD = "fqn";
	static final String JSON_PID_FIELD = "pid";
	static final String JSON_TID_FIELD = "tid";
	static final String JSON_USER_FIELD = "user";
	static final String JSON_START_TIME_USEC_FIELD = "start-time-usec";
	static final String JSON_END_TIME_USEC_FIELD = "end-time-usec";
	static final String JSON_ELAPSED_TIME_USEC_FIELD = "elapsed-time-usec";
	static final String JSON_WAIT_TIME_USEC_FIELD = "wait-time-usec";
	static final String JSON_MSG_AGE_USEC_FIELD = "msg-age-usec";
	static final String JSON_MSG_ENC_FIELD = "encoding";
	static final String JSON_MSG_CHARSET_FIELD = "charset";
	static final String JSON_MSG_MIME_FIELD = "mime-type";
	static final String JSON_MSG_SIZE_FIELD = "msg-size";
	static final String JSON_MSG_TAG_FIELD = "msg-tag";
	static final String JSON_MSG_TEXT_FIELD = "msg-text";
	static final String JSON_ID_COUNT_FIELD = "id-count";
	static final String JSON_SNAPSHOT_COUNT_FIELD = "snap-count";
	static final String JSON_PROPERTY_COUNT_FIELD = "prop-count";
	static final String JSON_EXCEPTION_FIELD = "exception";
	static final String JSON_SNAPSHOTS_FIELD = "snapshots";
	static final String JSON_ID_SET_FIELD = "id-set";
	static final String JSON_TTL_SEC_FIELD = "ttl-sec";

	// JSON quoted fields
	static final String JSON_NAME_LABEL = Utils.quote(JSON_NAME_FIELD);
	static final String JSON_CATEGORY_LABEL = Utils.quote(JSON_CATEGORY_FIELD);
	static final String JSON_STATUS_LABEL = Utils.quote(JSON_STATUS_FIELD);
	static final String JSON_COUNT_LABEL = Utils.quote(JSON_COUNT_FIELD);
	static final String JSON_TIME_USEC_LABEL = Utils.quote(JSON_TIME_USEC_FIELD);
	static final String JSON_PROPERTIES_LABEL = Utils.quote(JSON_PROPERTIES_FIELD);
	static final String JSON_TYPE_LABEL = Utils.quote(JSON_TYPE_FIELD);
	static final String JSON_TYPE_NO_LABEL = Utils.quote(JSON_TYPE_NO_FIELD);
	static final String JSON_VALUE_LABEL = Utils.quote(JSON_VALUE_FIELD);
	static final String JSON_VALUE_TYPE_LABEL = Utils.quote(JSON_VALUE_TYPE_FIELD);
	static final String JSON_CORR_ID_LABEL = Utils.quote(JSON_CORR_ID_FIELD);
	static final String JSON_TRACK_ID_LABEL = Utils.quote(JSON_TRACK_ID_FIELD);
	static final String JSON_PARENT_TRACK_ID_LABEL = Utils.quote(JSON_PARENT_TRACK_ID_FIELD);
	static final String JSON_SOURCE_LABEL = Utils.quote(JSON_SOURCE_FIELD);
	static final String JSON_SOURCE_URL_LABEL = Utils.quote(JSON_SOURCE_URL_FIELD);
	static final String JSON_SOURCE_FQN_LABEL = Utils.quote(JSON_SOURCE_FQN_FIELD);
	static final String JSON_RELATE_FQN_A_LABEL = Utils.quote(JSON_RELATE_FQN_A_FIELD);
	static final String JSON_RELATE_FQN_B_LABEL = Utils.quote(JSON_RELATE_FQN_B_FIELD);
	static final String JSON_RELATE_TYPE_LABEL = Utils.quote(JSON_RELATE_TYPE_FIELD);	
	static final String JSON_SOURCE_SSN_LABEL = Utils.quote(JSON_SOURCE_SSN_FIELD);
	static final String JSON_RESOURCE_LABEL = Utils.quote(JSON_RESOURCE_FIELD);
	static final String JSON_OPERATION_LABEL = Utils.quote(JSON_OPERATION_FIELD);
	static final String JSON_LOCATION_LABEL = Utils.quote(JSON_LOCATION_FIELD);
	static final String JSON_REASON_CODE_LABEL = Utils.quote(JSON_REASON_CODE_FIELD);
	static final String JSON_COMP_CODE_LABEL = Utils.quote(JSON_COMP_CODE_FIELD);
	static final String JSON_COMP_CODE_NO_LABEL = Utils.quote(JSON_COMP_CODE_NO_FIELD);
	static final String JSON_SEVERITY_LABEL = Utils.quote(JSON_SEVERITY_FIELD);
	static final String JSON_SEVERITY_NO_LABEL = Utils.quote(JSON_SEVERITY_NO_FIELD);
	static final String JSON_FQN_LABEL = Utils.quote(JSON_FQN_FIELD);
	static final String JSON_PID_LABEL = Utils.quote(JSON_PID_FIELD);
	static final String JSON_TID_LABEL = Utils.quote(JSON_TID_FIELD);
	static final String JSON_USER_LABEL = Utils.quote(JSON_USER_FIELD);
	static final String JSON_START_TIME_USEC_LABEL = Utils.quote(JSON_START_TIME_USEC_FIELD);
	static final String JSON_END_TIME_USEC_LABEL = Utils.quote(JSON_END_TIME_USEC_FIELD);
	static final String JSON_ELAPSED_TIME_USEC_LABEL = Utils.quote(JSON_ELAPSED_TIME_USEC_FIELD);
	static final String JSON_WAIT_TIME_USEC_LABEL = Utils.quote(JSON_WAIT_TIME_USEC_FIELD);
	static final String JSON_MSG_AGE_USEC_LABEL = Utils.quote(JSON_MSG_AGE_USEC_FIELD);
	static final String JSON_MSG_ENC_LABEL = Utils.quote(JSON_MSG_ENC_FIELD);
	static final String JSON_MSG_CHARSET_LABEL = Utils.quote(JSON_MSG_CHARSET_FIELD);
	static final String JSON_MSG_MIME_LABEL = Utils.quote(JSON_MSG_MIME_FIELD);
	static final String JSON_MSG_SIZE_LABEL = Utils.quote(JSON_MSG_SIZE_FIELD);
	static final String JSON_MSG_TAG_LABEL = Utils.quote(JSON_MSG_TAG_FIELD);
	static final String JSON_MSG_TEXT_LABEL = Utils.quote(JSON_MSG_TEXT_FIELD);
	static final String JSON_ID_COUNT_LABEL = Utils.quote(JSON_ID_COUNT_FIELD);
	static final String JSON_SNAPSHOT_COUNT_LABEL = Utils.quote(JSON_SNAPSHOT_COUNT_FIELD);
	static final String JSON_PROPERTY_COUNT_LABEL = Utils.quote(JSON_PROPERTY_COUNT_FIELD);
	static final String JSON_EXCEPTION_LABEL = Utils.quote(JSON_EXCEPTION_FIELD);
	static final String JSON_SNAPSHOTS_LABEL = Utils.quote(JSON_SNAPSHOTS_FIELD);
	static final String JSON_ID_SET_LABEL = Utils.quote(JSON_ID_SET_FIELD);
	static final String JSON_TTL_SEC_LABEL = Utils.quote(JSON_TTL_SEC_FIELD);
}
