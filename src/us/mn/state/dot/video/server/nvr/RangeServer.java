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

import java.io.OutputStreamWriter;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.Constants;
import us.mn.state.dot.video.VideoException;

/**
 * Range returns a range of time for which video exists for the requested
 * camera.
 * @author Timothy Johnson
 *
 */
public class RangeServer extends NvrServer {

	public void processRequest(HttpServletResponse res, Client c)
			throws VideoException {
		Calendar start = nvrDb.getBegin(c.getCameraId());
		Calendar end = nvrDb.getEnd(c.getCameraId());
		if(start == null || end == null) return;
		try{
			OutputStreamWriter w = new OutputStreamWriter(res.getOutputStream());
			w.write("start=" + Constants.DATE_FORMAT.format(start.getTime()));
			w.write("\n");
			w.write("end=" + Constants.DATE_FORMAT.format(end.getTime()));
			w.write("\n");
			w.flush();
		}catch(Exception e){
			e.printStackTrace();
			throw new VideoException(e.getMessage());
		}
	}
}
