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
package com.jkoolcloud.tnt4j.config;

import java.util.Properties;

import com.jkoolcloud.tnt4j.dump.DumpSinkFactory;
import com.jkoolcloud.tnt4j.format.Formatter;
import com.jkoolcloud.tnt4j.locator.DefaultGeoService;
import com.jkoolcloud.tnt4j.locator.GeoLocator;
import com.jkoolcloud.tnt4j.source.Source;
import com.jkoolcloud.tnt4j.source.SourceFactory;
import com.jkoolcloud.tnt4j.core.ActivityListener;
import com.jkoolcloud.tnt4j.dump.DefaultDumpSinkFactory;
import com.jkoolcloud.tnt4j.format.DefaultFormatter;
import com.jkoolcloud.tnt4j.format.EventFormatter;
import com.jkoolcloud.tnt4j.repository.TokenRepository;
import com.jkoolcloud.tnt4j.selector.DefaultTrackingSelector;
import com.jkoolcloud.tnt4j.selector.TrackingSelector;
import com.jkoolcloud.tnt4j.sink.DefaultEventSinkFactory;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.sink.EventSinkFactory;
import com.jkoolcloud.tnt4j.sink.SinkEventFilter;
import com.jkoolcloud.tnt4j.sink.SinkLogEventListener;
import com.jkoolcloud.tnt4j.source.DefaultSourceFactory;
import com.jkoolcloud.tnt4j.source.SourceType;
import com.jkoolcloud.tnt4j.tracker.DefaultTrackerFactory;
import com.jkoolcloud.tnt4j.tracker.Tracker;
import com.jkoolcloud.tnt4j.tracker.TrackerFactory;
import com.jkoolcloud.tnt4j.uuid.DefaultSignFactory;
import com.jkoolcloud.tnt4j.uuid.DefaultUUIDFactory;
import com.jkoolcloud.tnt4j.uuid.SignFactory;
import com.jkoolcloud.tnt4j.uuid.UUIDFactory;

/**
 * <p>
 * This class consolidates all configuration for creating {@link Tracker} instances. Developers should use this
 * class and override default configuration with user defined elements.
 * </p>
 *
 * <pre>
 * {@code
 * TrackerConfig config = DefaultConfigFactory.getInstance().getConfig(source);
 * TrackingLogger tracker = TrackingLogger.getInstance(config.build());
 * ...
 * }
 * </pre>
 *
 * @see TokenRepository
 * @see TrackingSelector
 * @see EventFormatter
 * @see EventSinkFactory
 *
 * @version $Revision: 9 $
 *
 */

public class TrackerConfig {
	String srcName;
	SourceType srcType = SourceType.APPL;
	Source sourceHandle;

	TrackerFactory trFactory;
	SourceFactory sourceFactory;
	UUIDFactory uuidFactory;
	SignFactory signFactory;
	GeoLocator geoLocator;
	
	EventSinkFactory defEvFactory;
	EventSinkFactory evFactory;
	DumpSinkFactory dpFactory;
	EventFormatter evFormatter;
	ActivityListener activityListener;
	SinkLogEventListener sinkLogEventListener;
	SinkEventFilter sinkFilter;

	TrackingSelector tSelector = null;

	Properties props = new Properties();
	boolean built = false;
	
	
	/**
	 * Create an empty configuration with a specific source name
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 */
	protected TrackerConfig(String source) {
		this(source, SourceType.APPL);
	}

	/**
	 * Create an empty configuration with a specific source name
	 *
	 * @param source
	 *            name of the source instance associated with the configuration
	 * @param type
	 *            source type associated with this configuration
	 */
	protected TrackerConfig(String source, SourceType type) {
		srcName = source;
		srcType = type;
	}

	/**
	 * Create an empty configuration with a specific source handle
	 *
	 * @param source
	 *            handle instance associated with the configuration
	 */
	protected TrackerConfig(Source source) {
		sourceHandle = source;
	}

	/**
	 * True of configuration was initialized and built using {{@link #build()}, false otherwise.
	 *
	 * @return True of configuration was initialized and built using {{@link #build()}, false otherwise
	 */
	public boolean isBuilt() {
		return built;
	}
	
	/**
	 * Get configuration source handle
	 *
	 * @return current source handle
	 */
	public Source getSource() {
		return sourceHandle;
	}

	/**
	 * Set configuration source handle
	 *
	 * @param app source handle
	 * @return current configuration instance
	 */
	public TrackerConfig setSource(Source app) {
		sourceHandle = app;
		return this;
	}

	/**
	 * Get configuration properties
	 *
	 * @return current configuration properties
	 */
	public Properties getProperties() {
		return props;
	}

	/**
	 * Set default UUID factory to generate UUIDs
	 *
	 * @param uuidf
	 *            UUID factory instance
	 * @see UUIDFactory
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setUUIDFactory(UUIDFactory uuidf) {
		uuidFactory = uuidf;
		return this;
	}

	/**
	 * Set default UUID factory instance
	 *
	 * @see UUIDFactory
	 * @return current UUID factory
	 */
	public UUIDFactory getUUIDFactory() {
		return uuidFactory;
	}

	/**
	 * Set default signature factory to generate message signatures
	 *
	 * @param sf
	 *            signature factory instance
	 * @see SignFactory
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setSignFactory(SignFactory sf) {
		signFactory = sf;
		return this;
	}

	/**
	 * Get default signature factory instance
	 *
	 * @see SignFactory
	 * @return current signature factory
	 */
	public SignFactory getSignFactory() {
		return signFactory;
	}

	/**
	 * Set default GEO locator
	 *
	 * @param gl
	 *            geo locator instance
	 * @see GeoLocator
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setGeoLocator(GeoLocator gl) {
		geoLocator = gl;
		return this;
	}

	/**
	 * Set default signature factory instance
	 *
	 * @see GeoLocator
	 * @return current geo locator instance
	 */
	public GeoLocator getGeoLocator() {
		return geoLocator;
	}

	/**
	 * Set default source factory to generate {@link Source} instances
	 *
	 * @param sfac
	 *            source factory instance
	 * @see SourceFactory
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setSourceFactory(SourceFactory sfac) {
		sourceFactory = sfac;
		return this;
	}

	/**
	 * Set default source factory instance
	 *
	 * @see SourceFactory
	 * @return current source factory
	 */
	public SourceFactory getSourceFactory() {
		return sourceFactory;
	}

	/**
	 * Set default tracker factory to generate {@link Tracker} instances
	 *
	 * @param tFactory
	 *            tracker factory instance
	 * @see TrackerFactory
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setTrackerFactory(TrackerFactory tFactory) {
		trFactory = tFactory;
		return this;
	}

	/**
	 * Set default tracker factory instance
	 *
	 * @see TrackerFactory
	 * @return current tracker factory
	 */
	public TrackerFactory getTrackerFactory() {
		return trFactory;
	}

	/**
	 * Set default dump sink factory
	 *
	 * @param dumpFactory
	 *            dump sink factory instance
	 * @see DumpSinkFactory
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setDumpSinkFactory(DumpSinkFactory dumpFactory) {
		dpFactory = dumpFactory;
		return this;
	}

	/**
	 * Set default dump sink factory instance
	 *
	 * @see DumpSinkFactory
	 * @return current dump sink factory instance
	 */
	public DumpSinkFactory getDumpSinkFactory() {
		return dpFactory;
	}

	/**
	 * Set default sink log listener which is triggered when logging activities occur
	 *
	 * @param snListener
	 *            activity listener instance
	 * @see SinkLogEventListener
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setSinkLogEventListener(SinkLogEventListener snListener) {
		sinkLogEventListener = snListener;
		return this;
	}

	/**
	 * Get default sink log listener which is triggered when logging activities occur
	 *
	 * @see SinkLogEventListener
	 * @return current activity listener
	 */
	public SinkLogEventListener getSinkLogEventListener() {
		return sinkLogEventListener;
	}

	/**
	 * Set default sink filter which is triggered when logging activities occur to filter
	 * out log events.
	 *
	 * @param filter
	 *            sink filter
	 * @see SinkEventFilter
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setSinkEventFilter(SinkEventFilter filter) {
		sinkFilter = filter;
		return this;
	}

	/**
	 * Get default sink filter which is triggered when logging activities occur to filter
	 * out log events.
	 *
	 * @see SinkEventFilter
	 * @return current sink filter
	 */
	public SinkEventFilter getSinkEventFilter() {
		return sinkFilter;
	}

	/**
	 * Set default activity listener which is triggered any time a given activity is started or stopped.
	 *
	 * @param acListener
	 *            activity listener instance
	 * @see ActivityListener
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setActivityListener(ActivityListener acListener) {
		activityListener = acListener;
		return this;
	}

	/**
	 * Get default activity listener which is triggered any time a given activity is started or stopped.
	 *
	 * @see ActivityListener
	 * @return current activity listener
	 */
	public ActivityListener getActivityListener() {
		return activityListener;
	}

	/**
	 * Set configuration event sink factory. Event sink factory is used to create {@link EventSink} instances,
	 * where all events, activities and messages are logged.
	 *
	 * @param evSinkFactory
	 *            event sink factory instance
	 * @see EventSinkFactory
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setEventSinkFactory(EventSinkFactory evSinkFactory) {
		evFactory = evSinkFactory;
		return this;
	}

	/**
	 * Set default event sink factory. Default Event sink factory is used to create
	 * {@link EventSink} instances for all logging activities.
	 *
	 * @param evSinkFactory
	 *            event sink factory instance
	 * @see EventSinkFactory
	 *
	 * @return current default event sink factory
	 */
	public TrackerConfig setDefaultEventSinkFactory(EventSinkFactory evSinkFactory) {
		defEvFactory = evSinkFactory;
		return this;
	}

	/**
	 * Set configuration event formatter. Event formatter is used to format event entries to text format.
	 *
	 * @param evformat
	 *            event formatter instance
	 * @see Formatter
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setEventFormatter(EventFormatter evformat) {
		evFormatter = evformat;
		return this;
	}


	/**
	 * Set configuration tracking selector. Tracking selectors allow loggers to test weather a specific sev/key/value is
	 * set. Tracking selectors use token repositories for look up.
	 *
	 * @param tselector
	 *            tracking selector instance
	 * @see TrackingSelector
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setTrackingSelector(TrackingSelector tselector) {
		tSelector = tselector;
		return this;
	}

	/**
	 * Set configuration user defined properties.
	 *
	 * @param pr
	 *            user defined properties
	 * @see TrackingSelector
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setProperties(Properties pr) {
		if (pr != null)
			props.putAll(pr);
		return this;
	}

	/**
	 * Obtain a property associated with the given key in this configuration instance
	 *
	 * @param key
	 *            property key
	 *
	 * @return value associated with the given key or null if non exist
	 */
	public String getProperty(String key) {
		return props.getProperty(key);
	}

	/**
	 * Obtain a boolean value associated with a given property
	 *
	 * @param key
	 *            property key
	 *
	 * @return boolean value associated with the given key or false if non exist
	 */
	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	/**
	 * Obtain a boolean value associated with a given property
	 *
	 * @param key
	 *            property key
	 *
	 * @param defValue
	 *            default value if key not found
	 *
	 * @return boolean value associated with the given key or defValue if non exist
	 */
	public boolean getBoolean(String key, boolean defValue) {
		Object value = props.getProperty(key);
		return value != null? Boolean.getBoolean(value.toString()): defValue;
	}

	/**
	 * Obtain a property associated with the given key in this configuration instance
	 *
	 * @param key
	 *            property key
	 * @param defValue
	 *            default value if key does not exist
	 *
	 * @return value associated with the given key or {@code defValue} if non exist
	 */
	public String getProperty(String key, String defValue) {
		return props.getProperty(key, defValue);
	}

	/**
	 * Sets a specific property in the current configuration
	 *
	 * @param key
	 *            property key
	 * @param value
	 *            value associated with the key
	 *
	 * @return current configuration instance
	 */
	public TrackerConfig setProperty(String key, String value) {
		props.setProperty(key, value);
		return this;
	}

	/**
	 * Get configuration event sink factory
	 *
	 * @see EventSinkFactory
	 * @return current event sink factory
	 */
	public EventSinkFactory getEventSinkFactory() {
		return evFactory;
	}

	/**
	 * Get configuration default event sink factory
	 *
	 * @see EventSinkFactory
	 * @return current default event sink factory
	 */
	public EventSinkFactory getDefaultEvenSinkFactory() {
		return defEvFactory;
	}

	/**
	 * Get configuration event formatter
	 *
	 * @see EventSinkFactory
	 * @return current event formatter instance
	 */
	public Formatter getEventFormatter() {
		return evFormatter;
	}

	/**
	 * Get configuration tracking selector instance
	 *
	 * @see TrackingSelector
	 * @return current tracking selector instance
	 */
	public TrackingSelector getTrackingSelector() {
		return tSelector;
	}

	/**
	 * Get event logger instance created by {@link EventSinkFactory}
	 *
	 * @see EventSink
	 * @return new event logger instance created by {@link EventSinkFactory}
	 */
	public EventSink getEventSink() {
		EventSink sink = evFactory.getEventSink(sourceHandle.getName(), props, evFormatter);
		sink.setSource(sourceHandle);
		return sink;
	}

	/**
	 * Get event logger instance created by {@link EventSinkFactory}
	 *
	 * @param frm
	 *            user defined event formatter used to format tracking events
	 * @see EventSink
	 * @see EventFormatter
	 * @return new event logger instance created by {@link EventSinkFactory}
	 */
	public EventSink getEventSink(EventFormatter frm) {
		EventSink sink = evFactory.getEventSink(sourceHandle.getName(), props, frm);
		sink.setSource(sourceHandle);
		return sink;
	}

	/**
	 * Clone current tracking configuration instance and return a new one
	 *
	 * @return new {@link TrackerConfig} instance with cloned values from the current instance
	 */
	public TrackerConfig cloneConfig() {
		TrackerConfig config = new TrackerConfig(sourceHandle);
		config.setProperties(this.props);
		config.uuidFactory = this.uuidFactory;
		config.signFactory = this.signFactory;
		config.geoLocator = this.geoLocator;
		config.sourceFactory = this.sourceFactory;
		config.trFactory = this.trFactory;
		config.evFactory = this.evFactory;
		config.defEvFactory = this.defEvFactory;
		config.dpFactory = this.dpFactory;
		config.evFormatter = this.evFormatter;
		config.tSelector = this.tSelector;
		config.activityListener = this.activityListener;
		config.sinkLogEventListener = this.sinkLogEventListener;
		config.sinkFilter= this.sinkFilter;
		config.built = this.built;
		return config;
	}

	/**
	 * Build configuration based on specified configuration elements. This method must be called before passing
	 * configuration to initialize other objects:
	 *
	 * @return {@link TrackerConfig} instance with initialized configuration elements
	 */
	public synchronized TrackerConfig build() {
		if (uuidFactory == null)
			uuidFactory = DefaultUUIDFactory.getInstance();
		if (signFactory == null)
			signFactory = DefaultSignFactory.getInstance();
		if (geoLocator == null)
			geoLocator = DefaultGeoService.getInstance();
		if (sourceFactory == null)
			sourceFactory = DefaultSourceFactory.getInstance();
		if (sourceHandle == null)
			sourceHandle = sourceFactory.newSource(srcName, srcType);		
		if (trFactory == null)
			trFactory = new DefaultTrackerFactory();
		if (evFactory == null)
			evFactory = DefaultEventSinkFactory.getInstance();
		if (defEvFactory == null)
			defEvFactory = DefaultEventSinkFactory.getInstance();
		if (dpFactory == null)
			dpFactory = new DefaultDumpSinkFactory();
		if (evFormatter == null)
			evFormatter = new DefaultFormatter();
		if (tSelector == null)
			tSelector = new DefaultTrackingSelector();
		built = true;
		return this;
	}

	@Override
	public String toString() {
		return super.toString()
			+ "{"
			+ "built: " + built
			+ ", source: " + sourceHandle
			+ ", event.factory: " + evFactory
			+ ", source.factory: " + sourceFactory
			+ ", uuid.factory: " + uuidFactory
			+ ", sign.factory: " + signFactory
			+ ", geo.locator: " + geoLocator
			+ ", default.event.factory: " + defEvFactory
			+ ", event.formatter: " + evFormatter
			+ ", tracker.factory: " + trFactory
			+ ", dump.factory: " + dpFactory
			+ ", selector: " + tSelector
			+ ", activity.listener: " + activityListener
			+ ", sink.log.listener: " + sinkLogEventListener
			+ ", sink.event.filter: " + sinkFilter
			+ "}";
	}
}
