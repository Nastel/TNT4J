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
 * JPMS definition for {@code "com.jkoolcloud.tnt4j.core"} module.
 */
module com.jkoolcloud.tnt4j.core {
    requires java.base;
    requires java.instrument;
    requires java.management;
    requires java.sql;
    requires org.apache.commons.lang3;
    requires org.apache.commons.text;
    requires org.apache.commons.codec;
    requires org.apache.commons.configuration2;
    requires org.apache.commons.net;
    requires commons.beanutils;
    requires com.fasterxml.uuid;
    requires com.google.common;
    requires org.slf4j;

    exports com.jkoolcloud.tnt4j;
    exports com.jkoolcloud.tnt4j.config;
    exports com.jkoolcloud.tnt4j.core;
    exports com.jkoolcloud.tnt4j.core.fields;
    exports com.jkoolcloud.tnt4j.dump;
    exports com.jkoolcloud.tnt4j.filters;
    exports com.jkoolcloud.tnt4j.format;
    exports com.jkoolcloud.tnt4j.limiter;
    exports com.jkoolcloud.tnt4j.locator;
    exports com.jkoolcloud.tnt4j.logger;
    exports com.jkoolcloud.tnt4j.repository;
    exports com.jkoolcloud.tnt4j.selector;
    exports com.jkoolcloud.tnt4j.sink;
    exports com.jkoolcloud.tnt4j.sink.impl;
    exports com.jkoolcloud.tnt4j.sink.impl.jul;
    exports com.jkoolcloud.tnt4j.sink.impl.slf4j;
    exports com.jkoolcloud.tnt4j.source;
    exports com.jkoolcloud.tnt4j.tracker;
    exports com.jkoolcloud.tnt4j.utils;
    exports com.jkoolcloud.tnt4j.uuid;
}