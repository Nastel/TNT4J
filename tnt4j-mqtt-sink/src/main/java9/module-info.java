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

/**
 * JPMS definition for {@code "com.jkoolcloud.tnt4j.sink.mqtt"} module.
 */
module com.jkoolcloud.tnt4j.sink.mqtt {
    requires java.base;
    requires com.jkoolcloud.tnt4j.core;
    requires org.eclipse.paho.client.mqttv3;

    exports com.jkoolcloud.tnt4j.sink.impl.mqtt;
}