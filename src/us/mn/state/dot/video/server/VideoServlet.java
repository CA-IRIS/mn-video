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
package us.mn.state.dot.video.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.AbstractEncoder;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.Constants;
import us.mn.state.dot.video.VideoThread;


/**
 * The VideoServlet is the base class for all servlets in the video package. 
 * @author Timothy Johnson
 *
 */
public abstract class VideoServlet extends HttpServlet {
	
	protected URL ssidURL = null;

	/**Flag that controls whether this instance is acting as a proxy 
	 * or a direct video server */
	protected boolean proxy = false;
	
	/** The logger used to log all output for the application */
	protected static Logger logger;

	protected String servletName = "VideoServlet";
	
	protected String requestURI = null;

	protected String queryString = null;

	protected int requestPort = 80;
	
	protected Calendar start = Calendar.getInstance();
	protected Calendar end = Calendar.getInstance();
	protected HttpServletRequest request = null;
	
	/** The request parameter name for the SONAR session ID */
	public static final String PARAM_SSID = "ssid";
	
	/** The request parameter name for the video area (sub-system) */
	public static final String PARAM_AREA = "area";

	/** The request parameter name for the frame rate of MJPEG stream */
	public static final String PARAM_RATE = "rate";

	/** The request parameter name for the size of video images */
	public static final String PARAM_SIZE = "size";

	/** The request parameter name for the duration of MJPEG streams */
	public static final String PARAM_DURATION = "duration";

	/** The request parameter name for the compression of JPEG images */
	public static final String PARAM_COMPRESSION = "compression";

	/** The request parameter name for the user making the request */
	public static final String PARAM_USER = "user";

	/** Initialize the VideoServlet */
	public void init(ServletConfig config) throws ServletException {
		super.init( config );
		servletName = this.getClass().getSimpleName();
		ServletContext ctx = config.getServletContext();
		Properties props =(Properties)ctx.getAttribute("properties");
		proxy = new Boolean(props.getProperty("proxy", "false")).booleanValue();
		if(proxy){
			try{
				ssidURL = new URL(props.getProperty("ssid.url"));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		int max = Integer.parseInt(props.getProperty("max.imagesize", "2"));
		Client.setMaxImageSize(max);
		if(logger==null) logger = Logger.getLogger(Constants.LOGGER_NAME);
	}

	/** Get an integer parameter request */
	protected int getIntRequest(HttpServletRequest req, String param) {
		return Integer.parseInt(req.getParameter(param));
	}

	/** Get a 'long' parameter request */
	protected long getLongRequest(HttpServletRequest req, String param) {
		return Long.parseLong(req.getParameter(param));
	}

	/** Configure a client from an HTTP request */
	protected void configureClient(Client c, HttpServletRequest req) {
		if(req.getParameter(PARAM_USER) != null)
			c.setUser(req.getParameter(PARAM_USER));
		if(req.getParameter(PARAM_AREA) != null)
			c.setArea(getIntRequest(req, PARAM_AREA));
		if(req.getParameter("id") != null)
			c.setCameraId(req.getParameter("id"));
		if(req.getParameter(PARAM_SIZE) != null)
			c.setSize(getIntRequest(req, PARAM_SIZE));
		if(req.getParameter(PARAM_RATE) != null)
			c.setRate(getIntRequest(req, PARAM_RATE));
		if(req.getParameter(PARAM_COMPRESSION) != null)
			c.setCompression(getIntRequest(req, PARAM_COMPRESSION));
		if(req.getParameter(PARAM_DURATION) != null)
			c.setDuration(getIntRequest(req, PARAM_DURATION));
		if(req.getParameter(PARAM_SSID) != null)
			c.setSonarSessionId(getLongRequest(req, PARAM_SSID));
		String host = req.getHeader("x-forwarded-for");
		if(host == null)
			host = req.getRemoteHost();
		if(req.getHeader("via") != null)
			host = host + " via proxy";
		c.setHost(host);
	}
    
	/**
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected final void doGet(HttpServletRequest request,
		HttpServletResponse response)
	{
		Thread t = Thread.currentThread();
		Calendar cal = Calendar.getInstance();
		Client c = new Client();
		this.request = request; 
		requestURI = request.getRequestURI();
		queryString = request.getQueryString();
		requestPort = request.getLocalPort();
		try {
			configureClient(c, request);
			t.setName("VIDEO " + servletName + " " +
				Constants.DATE_FORMAT.format(cal.getTime()) +
				" Camera " + c.getCameraId());
			processRequest(response, c);
			File f = new File("/tmp/video_profile.txt");
			PrintStream ps = new PrintStream(f);
			Profile.printMemory(ps);
			Profile.printThreads(ps);
		}
		catch(Throwable th) {
			logger.warning(c.getCameraId() + ": " + th.getMessage());
			//sendNoVideo(response, c);
		}
		finally {
			try {
				t.setName(t.getName() + " done");
				response.getOutputStream().close();
			}
			catch(Exception e) {
			}
		}
	}

	public abstract void processRequest(HttpServletResponse response,
		Client c) throws Exception;

	protected final void sendNoVideo(HttpServletResponse response, Client c)
			throws IOException {
		byte[] image = AbstractEncoder.getNoVideoImage();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("image/jpeg");
		response.setContentLength(image.length);
		response.getOutputStream().write(image);
		response.flushBuffer();
	}
	
	/** Check to see if the client is authenticated through SONAR */
	protected final boolean isAuthenticated(Client c){
		if(!proxy) return true;
		return isValidSSID(c.getSonarSessionId());
	}

	/** Validate the Sonar Session ID */
	protected final boolean isValidSSID(long ssid){
		logger.fine("Validating client " + ssid + "...");
		try{
			HttpURLConnection conn = ConnectionFactory.createConnection(ssidURL);
			conn.setConnectTimeout(VideoThread.TIMEOUT_DIRECT);
			conn.setReadTimeout(VideoThread.TIMEOUT_DIRECT);
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader reader = new BufferedReader(in);
			String l = reader.readLine();
			while(l != null){
				logger.fine("\tchecking against " + l);
				try{
					long validId = Long.parseLong(l);
					if(ssid == validId) return true;
				}catch(NumberFormatException nfe){
					//invalid ssid... ignore it!
				}
				l = reader.readLine();
			}
		}catch(Exception e){
			logger.warning("VideoServlet.isValidSSID: " + e.getMessage());
		}
		return false;
	}
}
