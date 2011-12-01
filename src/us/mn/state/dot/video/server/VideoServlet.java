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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
import us.mn.state.dot.video.District;
import us.mn.state.dot.video.ImageSize;
import us.mn.state.dot.video.VideoThread;


/**
 * The VideoServlet is the base class for all servlets in the video package. 
 * @author Timothy Johnson
 *
 */
public abstract class VideoServlet extends HttpServlet {
	
	protected ImageSize maxImageSize = ImageSize.MEDIUM;
	
	protected ImageSize defaultImageSize = ImageSize.MEDIUM;

	protected URL ssidURL = null;

	/**Flag that controls whether this instance is acting as a proxy 
	 * or a direct video server */
	protected boolean proxy = false;
	
	/** The logger used to log all output for the application */
	protected static Logger logger;

	protected String servletName = "VideoServlet";
	
	protected HttpServletRequest request = null;
	
	/** The request parameter name for the SONAR session ID */
	public static final String PARAM_SSID = "ssid";
	
	/** The request parameter name for the IRIS district */
	public static final String PARAM_DISTRICT = "area";

	/** The request parameter name for the frame rate of MJPEG stream */
	public static final String PARAM_RATE = "rate";

	/** The request parameter name for the size of video images */
	public static final String PARAM_SIZE = "size";

	/** The request parameter name for the duration of MJPEG streams */
	public static final String PARAM_DURATION = "duration";

	/** The request parameter name for the user making the request */
	public static final String PARAM_USER = "user";

	protected static District defaultDistrict = District.METRO;

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
		String max = props.getProperty("max.imagesize", "MEDIUM");
		for(ImageSize size : ImageSize.values()){
			if(max.equalsIgnoreCase(size.name())){
				maxImageSize = size;
				break;
			}
		}
		if(logger==null) logger = Logger.getLogger(Constants.LOGGER_NAME);
	}

	/** Get an integer parameter request */
	protected int getIntRequest(HttpServletRequest req, String param) {
		return Integer.parseInt(req.getParameter(param));
	}

	/** Get the requested district. */
	protected District getRequestedDistrict(HttpServletRequest req) {
		String value = req.getParameter(PARAM_DISTRICT);
		if(value == null)
			return defaultDistrict;
		for(District d : District.values()){
			if(d.name().equalsIgnoreCase(value)){
				return d;
			}
		}
		//for backward compatibility, support area parameter
		value = req.getParameter("area");
		if(value == null){
			return defaultDistrict;
		}
		if(value.equals("0")) return District.METRO;
		if(value.equals("1")) return District.D6;
		if(value.equals("2")) return District.D1;
		return defaultDistrict;
	}
	
	/** Get the requested image size.
	 * Valid request values are d1,d2,3 or s,m,l
	 */
	protected ImageSize getRequestedSize(HttpServletRequest req) {
		String value = req.getParameter(PARAM_SIZE);
		if(value == null)
			return defaultImageSize;
		if(value.length()!=1)
			return defaultImageSize;
		value = value.toUpperCase();
		if(Character.isDigit(value.charAt(0))){
			//for backward compatibility, subtract 1 from size
			int i = Integer.parseInt(value) - 1;
			for(ImageSize size : ImageSize.values()){
				if(size.ordinal() == i)
					return size;
			}
		}else{
			for(ImageSize size : ImageSize.values()){
				if(size.name().startsWith(value)){
					return size;
				}
			}
		}
		return defaultImageSize;
	}

	/** Get a 'long' parameter request */
	protected long getLongRequest(HttpServletRequest req, String param) {
		return Long.parseLong(req.getParameter(param));
	}

	/** Configure a client from an HTTP request */
	protected void configureClient(Client c, HttpServletRequest req) {
		if(req.getParameter(PARAM_USER) != null)
			c.setUser(req.getParameter(PARAM_USER));
		c.setDistrict(getRequestedDistrict(req));
		if(req.getParameter("id") != null)
			c.setCameraId(req.getParameter("id"));
		if(req.getParameter(PARAM_SIZE) != null)
			c.setSize(getRequestedSize(req));
		if(req.getParameter(PARAM_RATE) != null)
			c.setRate(getIntRequest(req, PARAM_RATE));
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
		Client c = new Client();
		this.request = request; 
		try {
			configureClient(c, request);
			processRequest(response, c);
		}
		catch(Throwable th) {
			logger.warning(c.getCameraId() + ": " + th.getMessage());
			sendNoVideo(response, c);
		}
		finally {
			try {
				response.getOutputStream().close();
			}
			catch(Exception e) {
			}
		}
	}

	public abstract void processRequest(HttpServletResponse response,
		Client c) throws Exception;

	protected final void sendNoVideo(HttpServletResponse response, Client c){
		try{
			byte[] image = AbstractEncoder.getNoVideoImage();
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("image/jpeg");
			response.setContentLength(image.length);
			response.getOutputStream().write(image);
			response.flushBuffer();
		}catch(Exception e){
			logger.warning(e.getMessage());
		}
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
