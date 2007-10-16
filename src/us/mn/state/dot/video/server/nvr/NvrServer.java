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

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import us.mn.state.dot.util.db.NvrConnection;
import us.mn.state.dot.video.server.VideoServlet;

/** The NvrServer is the abstract superclass of all servlets
 * running on the NVR servers.
 * @author Timothy Johnson
 *
 */
public abstract class NvrServer extends VideoServlet {

	/** The nvr_db contains information about the status of the local
	 * video stream recordings.
	 */
	public static NvrConnection nvrDb;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext ctx = config.getServletContext();
		Properties p =(Properties)ctx.getAttribute("properties");
		try{
			nvrDb = new NvrConnection(p);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
