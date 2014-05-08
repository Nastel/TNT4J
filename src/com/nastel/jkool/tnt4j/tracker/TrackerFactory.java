package com.nastel.jkool.tnt4j.tracker;

import com.nastel.jkool.tnt4j.config.TrackerConfig;


/**
 * <p><code>TrackerFactory</code> interface allows creation of <code>Tracker</code> logger instances.
 * Developers should implement this interface when creating custom <code>Tracker</code> logger factories.</p>
 *
 * @see DefaultTrackerFactory
 *
 * @version $Revision: 4 $
 *
 */
public interface TrackerFactory {
	/**
	 * Obtain an instance to a <code>Tracker</code> logger. Each thread must obtain a logger instance.
	 * <code>Tracker</code> logger is not thread safe.
	 * 
	 * @param tconfig tracking configuration associated with the tracking instance
	 * @return <code>Tracker</code> logger instance associated with this thread
	 */
	public Tracker getInstance(TrackerConfig tconfig); 
		
	/**
	 * Close and release resources associated with <code>Tracker</code> instance
	 * 
	 */
	public void close(Tracker tr);
}
