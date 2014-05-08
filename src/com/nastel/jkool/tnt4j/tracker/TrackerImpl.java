/*
 * Copyright (c) 2013 Nastel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Nastel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with Nastel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *
 */
package com.nastel.jkool.tnt4j.tracker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import com.nastel.jkool.tnt4j.config.TrackerConfig;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.core.Source;
import com.nastel.jkool.tnt4j.selector.TrackingSelector;
import com.nastel.jkool.tnt4j.sink.DefaultEventSinkFactory;
import com.nastel.jkool.tnt4j.sink.EventSink;
import com.nastel.jkool.tnt4j.sink.Handle;
import com.nastel.jkool.tnt4j.sink.SinkError;
import com.nastel.jkool.tnt4j.sink.SinkErrorListener;
import com.nastel.jkool.tnt4j.utils.Utils;


/**
 * <p>
 * Concrete class that implements <code>Tracker</code> interface. This class implements integration with
 * <code>EventSink</code>. Do not use this class directly. This class is instantiated by the 
 * <code>DefaultTrackerFactory.getInstance(...)</code> or <code>TrackingLogger.register(...)</code> calls. 
 * Access to this class is not thread safe. <code>TrackingLogger.tnt(...)</code> method will trigger 
 * logging to <code>EventSink</code> configured in <code>TrackingConfig</code>.
 * </p>
 * 
 * @see TrackerConfig
 * @see TrackingEvent
 * @see TrackingActivity
 * @see EventSink
 * @see OpLevel
 * @see Source
 * 
 * @version $Revision: 21 $
 * 
 */
public class TrackerImpl implements Tracker, SinkErrorListener {	
	private static EventSink logger = DefaultEventSinkFactory.defaultEventSink(TrackerImpl.class.getName());
		
	private EventSink eventSink;
	private TrackerConfig tConfig;
	private TrackingSelector selector;
	
	protected TrackerImpl(TrackerConfig config) {
		tConfig = config;
		selector = tConfig.getTrackingSelector();
		eventSink = tConfig.getEventSink();
		open();
	}

	private void openIOHandle(Handle handle) {
		try {
			handle.open();
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, 
					"Failed to open {handle: " + handle 
					+ ", vm.pid: " + Utils.getVMPID() 
					+ ", source: " + getSource()
					+ "}", e);
		} 
	}
	
	private void openEventSink() {
		try {
			if (tConfig.getSinkLogEventListener() != null) {
				eventSink.addSinkLogEventListener(tConfig.getSinkLogEventListener());
			}
			if (tConfig.getSinkEventFilter() != null) {
				eventSink.addSinkEventFilter(tConfig.getSinkEventFilter());
			}
			eventSink.addSinkErrorListener(this);
			eventSink.open();
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, 
					"Failed to open sink {event.sink: " + eventSink 
					+ ", vm.pid: " + Utils.getVMPID() 
					+ ", source: " + getSource()
					+ "}", e);
		} 
	}
	
	private void closeEventSink() {
		try {
			if (eventSink != null) {
				if (tConfig.getSinkLogEventListener() != null) {
					eventSink.removeSinkLogEventListener(tConfig.getSinkLogEventListener());
				}
				if (tConfig.getSinkEventFilter() != null) {
					eventSink.removeSinkEventFilter(tConfig.getSinkEventFilter());
				}
				eventSink.removeSinkErrorListener(this);
				eventSink.close();
			}	
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, 
					"Failed to close event sink {vm.name: " + Utils.getVMName() 
					+ ", vm.pid: " + Utils.getVMPID() 
					+ ", event.sink: " + eventSink
					+ ", source: " + getSource()
					+ "}", e);
		}
	}
	
	private void reportActivity(TrackingActivity activity) throws IOException, URISyntaxException {
		try {
			if (!eventSink.isOpen()) {
				eventSink.open();
			}
		} finally {
			eventSink.log(activity);						
		}
	}

	@Override
	public Source getSource() {
		return tConfig.getSource();
	}

	@Override
    public EventSink getEventSink() {
	    return eventSink;
    }

	
	@Override
	public TrackingActivity newActivity() {
		return newActivity(UUID.randomUUID().toString());
	}

	@Override
	public TrackingActivity newActivity(String signature) {
		TrackingActivity luw = new TrackingActivity(signature, this);
		luw.setPID(Utils.getVMPID());
		if (tConfig.getActivityListener() != null) {
			luw.addActivityListener(tConfig.getActivityListener());
		}
		return luw;
	}

	@Override
    public void tnt(TrackingActivity activity) {
		try  { reportActivity(activity); }
		catch (Throwable ex) {
			logger.log(OpLevel.ERROR, 
					"Failed to report activity {signature: " + activity.getTrackingId() 
					+ ", vm.pid: " + Utils.getVMPID() 
					+ ", event.sink: " + eventSink 
					+ ", source: " + getSource()
					+ "}", ex);
		}
	}

	@Override
	public void tnt(TrackingEvent event) {
		TrackingActivity activity = newActivity();
		activity.start(event.getOperation().getStartTime());
		activity.stop(event.getOperation().getEndTime());
		activity.tnt(event);
		tnt(activity);
	}

	@Override
    public TrackingEvent newEvent(OpLevel severity, String msg, String opName) {
		TrackingEvent event = new TrackingEvent(severity, msg, opName);
		event.getOperation().setUser(tConfig.getSource().getUser());
		return event;
    }

	@Override
    public TrackingEvent newEvent(OpLevel severity, String correlator, String msg, String opName) {
		TrackingEvent event = new TrackingEvent(severity, correlator, msg, opName);
		event.getOperation().setUser(tConfig.getSource().getUser());
		return event;
   }

	@Override
    public TrackingEvent newEvent(OpLevel severity, OpType opType, String msg, String opName) {
		TrackingEvent event = new TrackingEvent(severity, opType, msg, opName);
		return event;
   }

	@Override
    public TrackingEvent newEvent(OpLevel severity, OpType opType, String correlator, String msg,
            String opName) {
		TrackingEvent event = new TrackingEvent(severity, opType, correlator, msg, opName);
		event.getOperation().setUser(tConfig.getSource().getUser());
		return event;
   }
	
	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}
	
	@Override
    public TrackingSelector getTrackingSelector() {
	    return selector;
    }

	@Override
    public TrackerConfig getConfiguration() {
	    return tConfig;
    }

	@Override
    public boolean isOpen() {
	    return eventSink != null? eventSink.isOpen(): false;
    }

	@Override
    public void open() {
		openIOHandle(selector);
		openEventSink();		
		logger.log(OpLevel.DEBUG, 
				"Tracker opened {vm.name: " + Utils.getVMName() 
				+ ", vm.pid: " + Utils.getVMPID() 
				+ ", event.sink: " + eventSink
				+ ", source: " + getSource() 
				+ "}"
				);
    }

	@Override
	public void close() {
		try {
			closeEventSink();
			Utils.close(selector);
			logger.log(OpLevel.DEBUG, 
					"Tracker closed {vm.name: " + Utils.getVMName() 
					+ ", vm.pid: " + Utils.getVMPID() 
					+ ", event.sink: " + eventSink
					+ ", source: " + getSource() 
					+ "}"
					);
		} catch (Throwable e) {
			logger.log(OpLevel.ERROR, 
					"Failed to close tracker {vm.name: " + Utils.getVMName() 
					+ ", vm.pid: " + Utils.getVMPID() 
					+ ", event.sink: " + eventSink
					+ ", source: " + getSource()
					+ "}"
					, e);
		}
	}

	@Override
    public void sinkError(SinkError ev) {
		logger.log(OpLevel.ERROR, 
			"Sink write error {vm.name: " + Utils.getVMName() 
			+ ", vm.pid: " + Utils.getVMPID() 
			+ ", event.sink: " + eventSink
			+ ", source: " + getSource() 
			+ "}", ev.getCause());
		closeEventSink();
	}
}
