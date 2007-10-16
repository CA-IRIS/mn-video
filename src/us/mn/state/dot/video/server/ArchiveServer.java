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
package us.mn.state.dot.video.server;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.util.db.TmsConnection;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.VideoException;

/**
 * The ArchiveServer handles range and clip
 * requests.  It redirects the client to the appropriate
 * nvr which ultimately fills the request.
 * @author Timothy Johnson
 *
 */
public class ArchiveServer extends VideoServlet {

	protected static final String PROP_TMS_DB_USER = "tms.db.user"; 
	protected static final String PROP_TMS_DB_PWD = "tms.db.pwd";
	protected static final String PROP_TMS_DB_HOST = "tms.db.host";
	protected static final String PROP_TMS_DB_PORT = "tms.db.port";
	protected static final String PROP_TMS_DB_NAME = "tms.db.name";

	/** The tms_db is the master camera database. */
	public static TmsConnection tms;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext ctx = config.getServletContext();
		Properties p =(Properties)ctx.getAttribute("properties");
		try{
			tms = new TmsConnection(p);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void processRequest(HttpServletResponse res, Client c)
			throws VideoException {
		try{
			String s = requestURI + "?" + queryString;
			System.out.println("Processing request for: " + s);
			String nvrHost = tms.getNvrHost(c.getCameraId());
			if(nvrHost==null || nvrHost.length()==0) {
				res.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			String host = nvrHost;
			int port = 80;
			if(nvrHost.indexOf(":")>-1){
				host = nvrHost.substring(0, nvrHost.indexOf(":"));
				try{
					port = Integer.parseInt(nvrHost.substring(nvrHost.indexOf(":")+1));
				}catch(NumberFormatException nfe){
					//use default port due to port parsing error
				}
			}
			s = "http://" + host + ":" + port + s;
			System.out.println("Redirecting to: " + s);
			res.sendRedirect(s);
		}catch(Exception e){
			e.printStackTrace();
			throw new VideoException(e.getMessage());
		}
	}

}
