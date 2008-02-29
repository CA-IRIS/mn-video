/*
* VideoServer
* Copyright (C) 2003-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.video.server;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.AxisServer;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.VideoException;

/**
 * VideoServer is the main thread for still video server.
 *
 * @author    dinh1san
 * @author    Tim Johnson
 * @created   December 27, 2001
 */
public final class ImageServer extends VideoServlet{

	protected ServerFactory serverFactory = null;
	
	/** Constructor for the VideoServer */
    public void init( ServletConfig config ) throws ServletException {
		super.init( config );
		Properties p = (Properties)config.getServletContext().getAttribute("properties");
		serverFactory = new ServerFactory(p);
		logger.info( "ImageServer initialized successfully." );
	}


	/**
	 * @param request servlet request
	 * @param response servlet response
	 */
	public void processRequest(HttpServletResponse response, Client c){
		byte[] image = AxisServer.getNoVideoImage();
		int status = HttpServletResponse.SC_OK;
		String contentType = "image/jpeg";
		AxisServer server = serverFactory.getServer(c.getCameraId());
		if(server != null){
			try{
				image = server.getImage(c);
			}catch(VideoException ve){
				logger.info(c.getCameraId() + ": " + ve.getMessage());
			}
		}
		try{
			response.setStatus(status);
			response.setContentType(contentType);
			response.setContentLength(image.length);
			response.getOutputStream().write(image);
			response.flushBuffer();
		}catch(Throwable t){
			logger.warning("Error serving image " + c.getCameraId());
		}
	}
}
