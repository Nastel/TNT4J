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
	// JSON elements
	static final String JSON_NAME_LABEL = Utils.quote("name");
	static final String JSON_CATEGORY_LABEL =  Utils.quote("category");
	static final String JSON_STATUS_LABEL =  Utils.quote("status");
	static final String JSON_COUNT_LABEL =  Utils.quote("count");
	static final String JSON_TIME_USEC_LABEL =  Utils.quote("time-usec");
	static final String JSON_PROPERTIES_LABEL =  Utils.quote("properties");
	static final String JSON_TYPE_LABEL =  Utils.quote("type");
	static final String JSON_TYPE_NO_LABEL =  Utils.quote("type-no");
	static final String JSON_VALUE_LABEL =  Utils.quote("value");
	static final String JSON_VALUE_TYPE_LABEL =  Utils.quote("value-type");
	static final String JSON_CORR_ID_LABEL =  Utils.quote("corrid");
	static final String JSON_TRACK_ID_LABEL =  Utils.quote("tracking-id");
	static final String JSON_PARENT_TRACK_ID_LABEL =  Utils.quote("parent-id");
	static final String JSON_SOURCE_LABEL =  Utils.quote("source");
	static final String JSON_SOURCE_URL_LABEL =  Utils.quote("source-url");
	static final String JSON_SOURCE_FQN_LABEL =  Utils.quote("source-fqn");
	static final String JSON_SOURCE_INFO_LABEL =  Utils.quote("source-info");
	static final String JSON_RESOURCE_LABEL =  Utils.quote("resource");
	static final String JSON_OPERATION_LABEL =  Utils.quote("operation");
	static final String JSON_LOCATION_LABEL =  Utils.quote("location");
	static final String JSON_REASON_CODE_LABEL =  Utils.quote("reason-code");
	static final String JSON_COMP_CODE_LABEL =  Utils.quote("comp-code");
	static final String JSON_COMP_CODE_NO_LABEL =  Utils.quote("comp-code-no");
	static final String JSON_SEVERITY_LABEL =  Utils.quote("severity");
	static final String JSON_SEVERITY_NO_LABEL =  Utils.quote("severity-no");
	static final String JSON_FQN_LABEL =  Utils.quote("fqn");
	static final String JSON_PID_LABEL = Utils.quote("pid");
	static final String JSON_TID_LABEL =  Utils.quote("tid");
	static final String JSON_USER_LABEL =  Utils.quote("user");
	static final String JSON_START_TIME_USEC_LABEL =  Utils.quote("start-time-usec");
	static final String JSON_END_TIME_USEC_LABEL =  Utils.quote("end-time-usec");
	static final String JSON_ELAPSED_TIME_USEC_LABEL =  Utils.quote("elapsed-time-usec");
	static final String JSON_WAIT_TIME_USEC_LABEL =  Utils.quote("wait-time-usec");
	static final String JSON_MSG_AGE_USEC_LABEL =  Utils.quote("msg-age-usec");
	static final String JSON_MSG_ENC_LABEL =  Utils.quote("encoding");
	static final String JSON_MSG_CHARSET_LABEL =  Utils.quote("charset");
	static final String JSON_MSG_MIME_LABEL =  Utils.quote("mime-type");
	static final String JSON_MSG_SIZE_LABEL =  Utils.quote("msg-size");
	static final String JSON_MSG_TAG_LABEL =  Utils.quote("msg-tag");
	static final String JSON_MSG_TEXT_LABEL =  Utils.quote("msg-text");
	static final String JSON_ID_COUNT_LABEL =  Utils.quote("id-count");
	static final String JSON_SNAPSHOT_COUNT_LABEL =  Utils.quote("snap-count");
	static final String JSON_EXCEPTION_LABEL =  Utils.quote("exception");
	static final String JSON_SNAPSHOTS_LABEL =  Utils.quote("snapshots");
	static final String JSON_ID_SET_LABEL =  Utils.quote("id-set");
}
