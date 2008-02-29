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

import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import us.mn.state.dot.video.AxisServer;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.Constants;
import us.mn.state.dot.video.VideoClip;
import us.mn.state.dot.video.VideoException;


/**
 * The VideoServlet is the base class for all servlets in the video package. 
 * @author Timothy Johnson
 *
 */
public abstract class VideoServlet extends HttpServlet {
	
	/** The logger used to log all output for the application */
	protected static Logger logger;

	protected String servletName = "VideoServlet";
	
	protected String requestURI = null;

	protected String queryString = null;

	protected int requestPort = 80;
	
	protected Calendar start = Calendar.getInstance();
	protected Calendar end = Calendar.getInstance();
	protected VelocityContext context= null;
	protected HttpServletRequest request = null;
	
	/** Contructor for the redirector servlet */
	public void init(ServletConfig config) throws ServletException {
		super.init( config );
		servletName = this.getClass().getSimpleName();
		ServletContext ctx = config.getServletContext();
		if(logger==null){
			logger = (Logger)ctx.getAttribute(PropertiesContext.PROP_LOGGER);
		}
	}

	/** Get an integer paramter request */
	protected int getIntRequest(HttpServletRequest req, String param) {
		return Integer.parseInt(req.getParameter(param));
	}

	/** Configure a client from an HTTP request */
	protected void configureClient(Client c, HttpServletRequest req) {
		try {
			c.setUser(req.getUserPrincipal().getName());
		}
		catch(NullPointerException npe) {
			if(req.getParameter("user") != null)
				c.setUser(req.getParameter("user"));
		}
		if(req.getParameter("area") != null)
			c.setArea(getIntRequest(req, "area"));
		if(req.getParameter("id") != null)
			c.setCameraId(req.getParameter("id"));
		if(req.getParameter("size") != null)
			c.setSize(getIntRequest(req, "size"));
		if(req.getParameter("rate") != null)
			c.setRate(getIntRequest(req, "rate"));
		if(req.getParameter("duration") != null)
			c.setDuration(getIntRequest(req, "duration"));
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
		VideoClip clip = new VideoClip();
		c.setClip(clip);
		this.request = request; 
		requestURI = request.getRequestURI();
		queryString = request.getQueryString();
		requestPort = request.getLocalPort();
		try {
			configureClient(c, request);
			configureClip(clip, request);
			t.setName("VIDEO " + servletName + " " +
				Constants.DATE_FORMAT.format(cal.getTime()) +
				" Camera " + c.getCameraId());
			if(isPublic(c.getCameraId())){
				processRequest(response, c);
			}else{
				sendNoVideo(response, c);
			}
		}
		catch(Throwable th) {
			th.printStackTrace();
			logger.warning(th.getMessage());
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

	protected void configureClip(VideoClip clip, HttpServletRequest req)
			throws ParseException {
		String start = req.getParameter("start");
		if(start != null){
			Calendar c = Calendar.getInstance();
			c.setTime(Constants.DATE_FORMAT.parse(start));
			clip.setStart(c);
		}
		if(req.getParameter("id") != null)
			clip.setCameraId(req.getParameter("id"));
		if(req.getParameter("duration") != null)
			clip.setDuration(getIntRequest(req, "duration"));
	}

	public abstract void processRequest(HttpServletResponse response,
		Client c) throws Exception;

	/** Check to see if a camera is viewable by the public. */
	protected boolean isPublic(String camId){
		//FIXME: temporary implementation, use Sonar when cameras
		// have been migrated from RMI.
		File f = new File("/tmp/" + camId);
		return f.exists();
	}

	private final void sendNoVideo(HttpServletResponse response, Client c){
		byte[] image = AxisServer.getNoVideoImage();
		try{
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("image/jpeg");
			response.setContentLength(image.length);
			response.getOutputStream().write(image);
			response.flushBuffer();
		}catch(Throwable t){
			logger.warning("Error serving image " + c.getCameraId());
		}
	}
}
