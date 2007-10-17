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

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.AbstractImageFactory;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.ClientStream;
import us.mn.state.dot.video.ThreadMonitor;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

/**
 * The <code>StreamServer</code> class is a servlet that responds to client requests for
 * a MN/Dot video stream.  The response stream consists of repeatedly sending an
 * int (image size in bytes) followed by the image data.
 *
 * @author Timothy Johnson
 */
public class StreamServer extends VideoServlet {

	protected static final Hashtable clientStreams = new Hashtable();
	
	/** The ImageFactoryDispatcher that maintains the ImageFactories. */
	private static ImageFactoryDispatcher dispatcher;

	private ThreadMonitor monitor = null;
	
	private int maxFrameRate = 3;
	
	/** Initializes the servlet. */
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );
		monitor = new ThreadMonitor("ThreadMonitor", 10000, logger);
		ServletContext ctx = config.getServletContext();
		Properties props =(Properties)ctx.getAttribute("properties");
		dispatcher = new ImageFactoryDispatcher(props, logger, monitor);
		try{
			maxFrameRate = Integer.parseInt(props.getProperty("maxFrameRate"));
		}catch(Exception e){
			logger.info("Max frame rate not defined, using default...");
		}
		logger.info("StreamServer initialized successfully.");
	}

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	public void processRequest(HttpServletResponse response,
			Client c) throws Exception{
		AbstractImageFactory f = dispatcher.getFactory(c);
		if(f==null || c.getCameraId() == null){
			byte[] data = ("No image for camera " + c.getCameraId()).getBytes();
			try{
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.setContentType("text/html");
				response.setContentLength(data.length);
				response.getOutputStream().write(data);
				response.flushBuffer();
			}catch(Exception e){
				logger.info("Error streaming camera " + c.getCameraId() +
						" to " + c.getUser());
			}
		}else{
			ClientStream cs =
				new ClientStream(c, response.getOutputStream(),
						f, logger, maxFrameRate);
			registerStream(c, cs);
			cs.sendImages();
		}
	}

	protected synchronized static final void registerStream(
			Client c, ClientStream cs){
		ClientStream oldStream = (ClientStream)clientStreams.get(c.getUser());
		if(oldStream != null){
			oldStream.halt("New stream requested.");
		}
		clientStreams.put(c.getUser(), cs);
	}
}
