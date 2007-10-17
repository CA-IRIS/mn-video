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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.VideoClip;
import us.mn.state.dot.video.VideoException;

/** The ClipServer returns a video file.  It takes a request
 * with 3 parameters; id, duration and start.  Id is the ID
 * of the camera in the form Cxxx.  Duration is the length of the requested clip
 * in seconds.  Start is the start time of the clip.
 * e.g. Friday, Sept. 7, 2007 at 4:03:15 pm would be expressed as:
 * '2007-09-07 16:03:15'
 * @author Timothy Johnson
 *
 */
public class ClipServer extends NvrServer {

	protected static final String CONTENT_TYPE = "video/mpeg4-generic";
	
	public void processRequest(HttpServletResponse res, Client c)
			throws VideoException {
		try{
			VideoClip clip = c.getClip();
			String fileName = nvrDb.getFilename(c.getCameraId());
			File f = new File(fileName);
			long fileSize = f.length();
			long startOff = nvrDb.getOffset(clip.getStart(), c.getCameraId());
			long endOff = nvrDb.getOffset(clip.getEnd(), c.getCameraId());
			logger.fine("Start of clip at " + startOff);
			logger.fine("End of clip at " + endOff);
			BufferedOutputStream bOut = new BufferedOutputStream(
					res.getOutputStream());
			long bytesWritten = 0;
			int clipSize = 0;
//			res.setContentType(CONTENT_TYPE);
			if(endOff == startOff){
				// There is no video available for the request.
				logger.fine("No video available for the request.");
				res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}else if(endOff > startOff){
				logger.fine("End offset > begin.");
				clipSize = (int)(endOff - startOff);
				res.setContentLength(clipSize);
				bytesWritten += processFile(fileName, bOut, startOff, endOff);
			}else{
				logger.fine("End offset < begin.");
				clipSize = (int)(fileSize - startOff) + (int)(endOff);
				res.setContentLength(clipSize);
				bytesWritten += processFile(fileName, bOut, startOff, fileSize);
				bytesWritten += processFile(fileName, bOut, 0, endOff);
			}
			logger.fine("Processed " + bytesWritten + " bytes.");
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}
	}

	protected long processFile(String fn, BufferedOutputStream out, long start, long end)
			throws IOException {
		logger.fine("\tprocessing from " + start + " to " + end);
		File f = new File(fn);
		FileInputStream in = new FileInputStream(f);
		long totalBytes = end - start;
		long bytesWritten = 0;
		in.skip(start);
		byte[] buf = new byte[1024 * 1024];
		int bytesRead = in.read(buf);
		while(bytesRead>0 && bytesWritten < totalBytes){
			out.write(buf, 0, bytesRead);
			bytesWritten += bytesRead;
			bytesRead = in.read(buf);
		}
		logger.fine("\tprocessed " + bytesWritten + " bytes.");
		return bytesWritten;
	}
}
