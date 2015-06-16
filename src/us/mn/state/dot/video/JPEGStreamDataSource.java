/*
 * Project: Video
 * Copyright (C) 2014-2015  AHMCT, University of California
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package us.mn.state.dot.video;

import java.io.InputStream;
import java.io.IOException;
import java.lang.InterruptedException;
import java.lang.NumberFormatException;
import java.lang.Thread;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The JPEGStreamDataSource is a customized version of Timothy Johnson's
 * MultiRequestDataSource.  It is a separate class for the time being, as
 * MnDOT may not want these customizations.  In addition, it is currently
 * written in a verbose style for testing/debugging purposes.  It could
 * perhaps use some refactoring.
 *
 * NOTE: If modifying this class, please note that CA usage currently requires
 * that this class disconnect after each image fetch.
 *
 * @author Travis Swanston
 */

public class JPEGStreamDataSource extends AbstractDataSource {

	/** Maximum allowed number of consecutive failures before stopping */
	private final static int MAX_CONS_FAILS = 8;

	/** Period of time (ms) between sink notifications */
	private final static int NOTIFY_PERIOD = 50;

	/** Default period of time (ms) to pause between image fetches */
	private final static int DEFAULT_FETCH_PERIOD = 100;

	/** Period of time (ms) to pause between image fetches */
	private final int fetch_period;

	/** The most recently read image */
	byte[] latest_img = null;

	/** Thread which handles reading images */
	private class Fetcher extends Thread {
		public void run() {
			HttpURLConnection conn = null;
			InputStream in = null;
			logger.info("Starting: " + this);
			int fail = 0;
			boolean first = true;
			while (true) {
				if (done)
					break;
				if (!(this.isAlive()))
					break;
				if (Thread.interrupted())
					break;
				if (fail >= MAX_CONS_FAILS)
					break;
				if (conn != null)
					conn.disconnect();
				try {
					if (!first)
						Thread.sleep(fetch_period);
				}
				catch (InterruptedException e) {
					halt();		// sets done
				}
				first = false;
				try {
					conn = ImageFactory.createConnection(
						url, user, password);
				}
				catch (VideoException e) {
					logger.warning(e.getMessage());
					++fail;
					continue;
				}
				if (conn == null) {
					logger.warning("Error creating " +
						"connection.");
					++fail;
					continue;
				}
				int response = -1;
				try {
					response = conn.getResponseCode();
				}
				catch (IOException e) {
					logger.warning("Error getting " +
						"response code");
					++fail;
					continue;
				}
				if (response != 200) {
					logger.warning("HTTP response: " +
						response);
					++fail;
					continue;
				}
				try {
					in = conn.getInputStream();
				}
				catch (IOException e) {
					logger.warning(e.getMessage());
					++fail;
					continue;
				}
				int length = -1;
				try {
					length = Integer.parseInt(
						conn.getHeaderField(
						"Content-Length"));
				}
				catch (NumberFormatException e) {
					logger.warning("Error parsing " +
						"Content-Length");
					++fail;
					continue;
				}
				byte[] img = null;
				try {
					img = AbstractEncoder.readImage(in, length);
				}
				catch (IOException e) {
					logger.warning(e.getMessage());
					++fail;
					continue;
				}
				try {
					in.close();
				}
				catch (IOException e) {
					// NOP
				}
				conn.disconnect();
				if (img == null || img.length < 1) {
					++fail;
					continue;
				}
				setLatestImage(img);
				fail = 0;
			}
			logger.info("Stopping: " + this);
			if (conn != null)
				conn.disconnect();
		}
	}

	/** Constructor */
	public JPEGStreamDataSource(Client c, URL url, String user,
		String pwd, Integer period)
	{
		super(c, null, null, url, user, pwd);
		int p = DEFAULT_FETCH_PERIOD;
		if (period != null)
			p = period.intValue();
		if (p < 0)
			p = DEFAULT_FETCH_PERIOD;
		this.fetch_period = p;
	}

	protected synchronized byte[] getLatestImage() {
		return latest_img;
	}

	protected synchronized void setLatestImage(byte[] img) {
		latest_img = img;
	}

	/** Start the image-fetcher thread then loop, notifying sinks */
	public void run() {
		if (url == null) {
			logger.warning("No encoder defined for this source.");
			return;
		}
		logger.info("Starting: " + this);

		// start image-fetcher thread
		Thread fetcher = new Fetcher();
		fetcher.start();

		// main loop
		while (true) {
			if (done)
				break;
			if (!(this.isAlive()))
				break;
			byte[] img = getLatestImage();
			if (img != null && img.length > 0)
				notifySinks(img);
			else
				notifySinks(LoadingImage.data);
			try {
				Thread.sleep(NOTIFY_PERIOD);
			}
			catch (InterruptedException e) {
				halt();		// sets done
			}
		}
		fetcher.interrupt();
		logger.info("Stopping: " + this);
		removeSinks();
	}
}

