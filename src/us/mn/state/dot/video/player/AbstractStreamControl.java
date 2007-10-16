/*
 * Project: Video
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.video.player;

import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JPanel;

import us.mn.state.dot.video.AbstractImageFactory;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.RepeaterImageFactory;
import us.mn.state.dot.video.ThreadMonitor;
import us.mn.state.dot.video.VideoException;

/**
 * @author john3tim
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AbstractStreamControl extends JPanel{

	protected Logger logger = null;
	private ThreadMonitor threadMonitor = null;
	private AbstractImageFactory factory = null;
	private int camera = -1;
	private int rate = 30;
	private int duration = 60;
	private int size = 2;
	private VideoMonitor monitor = null;
	private String baseUrl = null;
	
	public AbstractStreamControl(Properties p, VideoMonitor monitor, Logger logger){
		this.logger = logger;
		this.monitor = monitor;
		String server = null;
		String port = null;
		String servlet = null;
		server = p.getProperty("serverIp");
		port = p.getProperty("serverPort");
		servlet = p.getProperty("streamServlet") + "repeater";
		baseUrl = "http://" +
			server + ":" + port + "/" +
			p.getProperty("appName") + "/" +
			servlet;
		threadMonitor = new ThreadMonitor("IncidentControl", 10000, logger);
	}

    public final void stop(){
		monitor.setImageFactory(null, 0);
	}
	
	public final void start() {
		logger.info("Starting stream...");
		stop();
		//FIXME make the size selectable
		logger.info("Creating imagefactory for " + camera);
		Client c = new Client();
		c.setCameraNumber(camera);
		c.setSize(size);
		c.setDuration(duration);
		c.setRate(rate);
		try{
			factory = new RepeaterImageFactory(
				c, baseUrl, logger, threadMonitor);
		}catch(VideoException ve){
			logger.warning(ve.getMessage());
		}
		monitor.setImageFactory(factory, duration * rate);
	}

	public int getCamera() {
		return camera;
	}
	public void setCamera(int camera) {
		this.camera = camera;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public int getImageSize() {
		return size;
	}
	public void setImageSize(int size) {
		this.size = size;
	}
}
