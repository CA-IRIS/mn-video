/*
 * Project: Video
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
 * Copyright (C) 2015  AHMCT, University of California
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package us.mn.state.dot.video;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * The HttpDataSource gets its data via the HTTP protocol
 *
 * @author Timothy Johnson
 * @author Travis Swanston
 */
public class HttpDataSource extends AbstractDataSource {

	/** Approximate minimum time (ms) between sink notifications */
	private final static int NOTIFY_PERIOD = 50;

	/** Most recent frame received */
	private byte[] latest_img = LoadingImage.data;

	/** Timestamp of most recent sink notification */
	private long last_notify = -1;

	/** Thread which handles sink notification */
	private class Notifier extends Thread {
		public void run() {
			logger.info("Starting: " + this);
			while (true) {
				if (done)
					break;
				if (!(this.isAlive()))
					break;
				if (Thread.interrupted())
					break;
				long delta = timeSinceLastNotify();
				long sleep = 0;
				if (delta >= NOTIFY_PERIOD) {
					// frames seem to be coming in slowly;
					// re-notify w/ current frame to avoid
					// stream timeout
					notifyWithLatest();
					sleep = NOTIFY_PERIOD;
				}
				else {
					sleep = delta;
				}
				try {
					Thread.sleep(sleep);
				}
				catch (InterruptedException e) {
					break;
				}
			}
			logger.info("Stopping: " + this);
		}
	}


	/** Constructor for the HttpDataSource. */
	public HttpDataSource(Client c, URL url, String user, String pwd) {
		super(c, null, null, url, user, pwd);
	}

	/** Constructor for the HttpDataSource. */
	public HttpDataSource(Client c, Logger l, ThreadMonitor m, URL url, String user, String pwd) {
		super(c, l, m, url, user, pwd);
	}

	/** Start the stream. */
	public void run() {
		HttpURLConnection conn = null;
		if (url == null) {
			logger.fine("No encoder defined for this source.");
			return;
		}
		// start notifier thread
		Thread notifier = new Notifier();
		notifier.start();
		try{
			conn = ImageFactory.createConnection(url, user, password);
			final MJPEGReader stream = new MJPEGReader(conn.getInputStream());
			logger.fine("Starting: " + this);
			byte[] img;
			while (!done && this.isAlive()) {
				if (stream == null) {
					break;
				}
				img = stream.getImage();
				if (img != null && img.length > 0) {
					updateImage(img);
					notifyWithLatest();	// notify immediately
				}
				else {
					// FIXME: Continue trying to get images even if null or empty.
					// Perhaps a counter can keep track of contiguous failures and
					// then break.

					//break;
				}
			}
		}
		catch(Exception e) {
			logger.info(e.getMessage());
		}
		finally {
			notifier.interrupt();
			logger.fine("Stopping: " + this);
			try {
				conn.disconnect();
			}
			catch(Exception e2) {
			}
			removeSinks();
		}
	}

	/** Update the latest-image variable */
	private synchronized void updateImage(byte[] img) {
		latest_img = img;
	}

	/** Notify sinks with latest image */
	private synchronized void notifyWithLatest() {
		notifySinks(latest_img);
		last_notify = System.currentTimeMillis();
	}

	/** Time (ms) since last notifyWithLatest() */
	private synchronized long timeSinceLastNotify() {
		long delta = System.currentTimeMillis() - last_notify;
		if (delta < 0)		// sanity
			return 0;
		return delta;
	}

}
