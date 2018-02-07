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
package com.jkoolcloud.tnt4j.format;

import com.jkoolcloud.tnt4j.utils.Utils;

/**
 * <p>
 * This interface defines all labels used for generating TNT4J JSON messages.
 * </p>
 *
 * @version $Revision: 1 $
 *
 */

interface JSONLabels {
	// JSON fields
	String JSON_NAME_FIELD = "name";
	String JSON_CATEGORY_FIELD = "category";
	String JSON_STATUS_FIELD = "status";
	String JSON_COUNT_FIELD = "count";
	String JSON_TIME_USEC_FIELD = "time-usec";
	String JSON_PROPERTIES_FIELD = "properties";
	String JSON_TYPE_FIELD = "type";
	String JSON_TYPE_NO_FIELD = "type-no";
	String JSON_VALUE_FIELD = "value";
	String JSON_VALUE_TYPE_FIELD = "value-type";
	String JSON_CORR_ID_FIELD = "corrid";
	String JSON_TRACK_ID_FIELD = "tracking-id";
	String JSON_TRACK_SIGN_FIELD = "tracking-sign";
	String JSON_PARENT_TRACK_ID_FIELD = "parent-id";
	String JSON_SOURCE_FIELD = "source";
	String JSON_SOURCE_URL_FIELD = "source-url";
	String JSON_SOURCE_FQN_FIELD = "source-fqn";
	String JSON_SOURCE_SSN_FIELD = "source-ssn";
	String JSON_RELATE_FQN_A_FIELD = "relate-afqn";
	String JSON_RELATE_FQN_B_FIELD = "relate-bfqn";
	String JSON_RELATE_TYPE_FIELD = "relate-type";
	String JSON_RESOURCE_FIELD = "resource";
	String JSON_OPERATION_FIELD = "operation";
	String JSON_LOCATION_FIELD = "location";
	String JSON_REASON_CODE_FIELD = "reason-code";
	String JSON_COMP_CODE_FIELD = "comp-code";
	String JSON_COMP_CODE_NO_FIELD = "comp-code-no";
	String JSON_SEVERITY_FIELD = "severity";
	String JSON_SEVERITY_NO_FIELD = "severity-no";
	String JSON_FQN_FIELD = "fqn";
	String JSON_PID_FIELD = "pid";
	String JSON_TID_FIELD = "tid";
	String JSON_USER_FIELD = "user";
	String JSON_START_TIME_USEC_FIELD = "start-time-usec";
	String JSON_END_TIME_USEC_FIELD = "end-time-usec";
	String JSON_ELAPSED_TIME_USEC_FIELD = "elapsed-time-usec";
	String JSON_WAIT_TIME_USEC_FIELD = "wait-time-usec";
	String JSON_MSG_AGE_USEC_FIELD = "msg-age-usec";
	String JSON_MSG_ENC_FIELD = "encoding";
	String JSON_MSG_CHARSET_FIELD = "charset";
	String JSON_MSG_MIME_FIELD = "mime-type";
	String JSON_MSG_SIZE_FIELD = "msg-size";
	String JSON_MSG_TAG_FIELD = "msg-tag";
	String JSON_MSG_TEXT_FIELD = "msg-text";
	String JSON_ID_COUNT_FIELD = "id-count";
	String JSON_SNAPSHOT_COUNT_FIELD = "snap-count";
	String JSON_PROPERTY_COUNT_FIELD = "prop-count";
	String JSON_EXCEPTION_FIELD = "exception";
	String JSON_SNAPSHOTS_FIELD = "snapshots";
	String JSON_ID_SET_FIELD = "id-set";
	String JSON_TTL_SEC_FIELD = "ttl-sec";

	// JSON quoted fields
	String JSON_NAME_LABEL = Utils.quote(JSON_NAME_FIELD);
	String JSON_CATEGORY_LABEL = Utils.quote(JSON_CATEGORY_FIELD);
	String JSON_STATUS_LABEL = Utils.quote(JSON_STATUS_FIELD);
	String JSON_COUNT_LABEL = Utils.quote(JSON_COUNT_FIELD);
	String JSON_TIME_USEC_LABEL = Utils.quote(JSON_TIME_USEC_FIELD);
	String JSON_PROPERTIES_LABEL = Utils.quote(JSON_PROPERTIES_FIELD);
	String JSON_TYPE_LABEL = Utils.quote(JSON_TYPE_FIELD);
	String JSON_TYPE_NO_LABEL = Utils.quote(JSON_TYPE_NO_FIELD);
	String JSON_VALUE_LABEL = Utils.quote(JSON_VALUE_FIELD);
	String JSON_VALUE_TYPE_LABEL = Utils.quote(JSON_VALUE_TYPE_FIELD);
	String JSON_CORR_ID_LABEL = Utils.quote(JSON_CORR_ID_FIELD);
	String JSON_TRACK_ID_LABEL = Utils.quote(JSON_TRACK_ID_FIELD);
	String JSON_TRACK_SIGN_LABEL = Utils.quote(JSON_TRACK_SIGN_FIELD);
	String JSON_PARENT_TRACK_ID_LABEL = Utils.quote(JSON_PARENT_TRACK_ID_FIELD);
	String JSON_SOURCE_LABEL = Utils.quote(JSON_SOURCE_FIELD);
	String JSON_SOURCE_URL_LABEL = Utils.quote(JSON_SOURCE_URL_FIELD);
	String JSON_SOURCE_FQN_LABEL = Utils.quote(JSON_SOURCE_FQN_FIELD);
	String JSON_RELATE_FQN_A_LABEL = Utils.quote(JSON_RELATE_FQN_A_FIELD);
	String JSON_RELATE_FQN_B_LABEL = Utils.quote(JSON_RELATE_FQN_B_FIELD);
	String JSON_RELATE_TYPE_LABEL = Utils.quote(JSON_RELATE_TYPE_FIELD);
	String JSON_SOURCE_SSN_LABEL = Utils.quote(JSON_SOURCE_SSN_FIELD);
	String JSON_RESOURCE_LABEL = Utils.quote(JSON_RESOURCE_FIELD);
	String JSON_OPERATION_LABEL = Utils.quote(JSON_OPERATION_FIELD);
	String JSON_LOCATION_LABEL = Utils.quote(JSON_LOCATION_FIELD);
	String JSON_REASON_CODE_LABEL = Utils.quote(JSON_REASON_CODE_FIELD);
	String JSON_COMP_CODE_LABEL = Utils.quote(JSON_COMP_CODE_FIELD);
	String JSON_COMP_CODE_NO_LABEL = Utils.quote(JSON_COMP_CODE_NO_FIELD);
	String JSON_SEVERITY_LABEL = Utils.quote(JSON_SEVERITY_FIELD);
	String JSON_SEVERITY_NO_LABEL = Utils.quote(JSON_SEVERITY_NO_FIELD);
	String JSON_FQN_LABEL = Utils.quote(JSON_FQN_FIELD);
	String JSON_PID_LABEL = Utils.quote(JSON_PID_FIELD);
	String JSON_TID_LABEL = Utils.quote(JSON_TID_FIELD);
	String JSON_USER_LABEL = Utils.quote(JSON_USER_FIELD);
	String JSON_START_TIME_USEC_LABEL = Utils.quote(JSON_START_TIME_USEC_FIELD);
	String JSON_END_TIME_USEC_LABEL = Utils.quote(JSON_END_TIME_USEC_FIELD);
	String JSON_ELAPSED_TIME_USEC_LABEL = Utils.quote(JSON_ELAPSED_TIME_USEC_FIELD);
	String JSON_WAIT_TIME_USEC_LABEL = Utils.quote(JSON_WAIT_TIME_USEC_FIELD);
	String JSON_MSG_AGE_USEC_LABEL = Utils.quote(JSON_MSG_AGE_USEC_FIELD);
	String JSON_MSG_ENC_LABEL = Utils.quote(JSON_MSG_ENC_FIELD);
	String JSON_MSG_CHARSET_LABEL = Utils.quote(JSON_MSG_CHARSET_FIELD);
	String JSON_MSG_MIME_LABEL = Utils.quote(JSON_MSG_MIME_FIELD);
	String JSON_MSG_SIZE_LABEL = Utils.quote(JSON_MSG_SIZE_FIELD);
	String JSON_MSG_TAG_LABEL = Utils.quote(JSON_MSG_TAG_FIELD);
	String JSON_MSG_TEXT_LABEL = Utils.quote(JSON_MSG_TEXT_FIELD);
	String JSON_ID_COUNT_LABEL = Utils.quote(JSON_ID_COUNT_FIELD);
	String JSON_SNAPSHOT_COUNT_LABEL = Utils.quote(JSON_SNAPSHOT_COUNT_FIELD);
	String JSON_PROPERTY_COUNT_LABEL = Utils.quote(JSON_PROPERTY_COUNT_FIELD);
	String JSON_EXCEPTION_LABEL = Utils.quote(JSON_EXCEPTION_FIELD);
	String JSON_SNAPSHOTS_LABEL = Utils.quote(JSON_SNAPSHOTS_FIELD);
	String JSON_ID_SET_LABEL = Utils.quote(JSON_ID_SET_FIELD);
	String JSON_TTL_SEC_LABEL = Utils.quote(JSON_TTL_SEC_FIELD);
}
