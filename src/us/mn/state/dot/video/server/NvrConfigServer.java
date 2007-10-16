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

import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.VideoException;

/**
 * The NvrConfigServer handles requests from NVR's
 * for configuration files.  The configuration is built from
 * the tms database which allows the NVR's to keep up to date
 * with which cameras they are responsible for recording but
 * without having to maintain a configuration file on each
 * individual server.
 * @author Timothy Johnson
 *
 */
public class NvrConfigServer extends ArchiveServer {

	/**
	 * Process the request for a nvr configuration.
	 * No parameters are necessary since the NVR's
	 * ip / hostname can be obtained from the request.
	 */
	public void processRequest(HttpServletResponse res, Client c)
			throws VideoException {
		try{
			String remote = request.getRemoteAddr();
			logger.fine("Processing NvrConfig request for " + remote);
			OutputStreamWriter w = new OutputStreamWriter(res.getOutputStream());
			String[] cams = tms.getCameraIdsByNvr(remote);
			logger.fine("\tfound " + cams.length + " cameras for " + remote);
			for(int i=0; i<cams.length; i++){
				String encoder = tms.getEncoderHost(cams[i]);
				if(encoder != null){
					w.write(cams[i] + "=" + encoder);
					w.write("\n");
				}
			}
			w.flush();
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}
	}

}
