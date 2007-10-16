/*
* Video
* Copyright (C) 2007  Minnesota Department of Transportation
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
* Foundation, Inc., 59 temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package us.mn.state.dot.video.server.nvr;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.ConnectionFactory;

/** The NvrInitializer sets up the video recording processes but
 * does not interact with the user requests for video.
 * @author Timothy Johnson
 *
 */
public class NvrInitializer extends NvrServer {

	protected static final String[] drives =
		{"/drive1", "/drive2", "/drive3", "/drive4"};

	protected HashMap<String, StreamRecorder> recorders =
		new HashMap<String, StreamRecorder>();
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext ctx = config.getServletContext();
		Properties p =(Properties)ctx.getAttribute("properties");
		String videoHost = p.getProperty("video.host");
		String videoPort = p.getProperty("video.port");
		String loc = "http://" + videoHost + ":" + videoPort + "/video/nvrconfig";
		System.out.println(loc);
		updateRecorders(loc);
	}

	protected void updateRecorders(String loc) throws ServletException {
		logger.fine("Starting video recording processes...");
		clearRecorders();
		try{
			URL url = new URL(loc);
			URLConnection c = ConnectionFactory.createConnection(url);
			InputStreamReader in = new InputStreamReader(c.getInputStream());
			BufferedReader reader = new BufferedReader(in);
			String l = reader.readLine();
			int driveIndex = 0;
			while(l != null){
				StringTokenizer tok = new StringTokenizer(l, "=", false);
				if(driveIndex >= drives.length) driveIndex = 0;
				String camId = tok.nextToken();
				String encoderIp = tok.nextToken();
				VideoFile f = new VideoFile(nvrDb, camId, drives[driveIndex]);
				StreamRecorder r = new StreamRecorder(encoderIp, f);
				recorders.put(camId, r);
				r.start();
				l = reader.readLine();
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new ServletException(e);
		}
	}

	/**
	 * Purge all existing StreamRecorders.
	 *
	 */
	protected void clearRecorders(){
		for(StreamRecorder r : recorders.values()){
			r.stop();
		}
		recorders.clear();
	}
	
	public void processRequest(HttpServletResponse res, Client c){
		//do nothing.
	}

}
