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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

import us.mn.state.dot.util.db.NvrConnection;
import us.mn.state.dot.video.Constants;

public class VideoFile extends File {

	protected Calendar lastIndexTime = null;
	/** The max time (in seconds) between video file indices. */
	protected long indexInterval = 3;
	public static final String CONTAINER_TYPE = "ts";

	protected long lastOffset = -1;
	protected Calendar wrapTime = null;
	protected String fileName = null;
	protected String cameraId = null;
	protected NvrConnection nvrDb = null;
	protected OutputStream out = null;
	
	public VideoFile(NvrConnection nvrDb, String camId, String location){
		super(location, camId + "." + CONTAINER_TYPE);
		cameraId = camId;
		this.nvrDb = nvrDb;
		System.out.println("Camera = " + cameraId);
		lastIndexTime = nvrDb.getEnd(cameraId);
		if(lastIndexTime != null){
			lastOffset = nvrDb.getOffset(lastIndexTime, cameraId);
		}
	}

	public long getStart(){
		Calendar c = nvrDb.getEnd(cameraId);
		if(c != null) return getOffset(c);
		return -1;
	}

	public long getOffset(Calendar c){
		return nvrDb.getOffset(c, cameraId);
	}

	protected void setOffset(Calendar c, long offset){
	}

	/**
	 * Write some data to the file.
	 * @param data The data to write
	 * @param len The number of bytes to write
	 */
	public void write(byte[] data, int len) throws Exception {
		writeIndex();
		if(out == null){
			createOutput();
		}
		out.write(data, 0, len);
	}

	protected void createOutput() throws FileNotFoundException {
		out = new FileOutputStream(this);
	}
	
	protected void writeIndex(){
		System.out.println("File position: "); //% (v_file.tell() / 1024)
		Calendar now = Calendar.getInstance();
		String nowS = Constants.DATE_FORMAT.format(now.getTime());
		System.out.println(nowS);
		if(lastIndexTime == null) lastIndexTime = now;
		if((now.getTimeInMillis() - lastIndexTime.getTimeInMillis()) > indexInterval){
			System.out.println("Index created at:"); //% (now, v_file.tell())
//			indexFile.setOffset(now, vf.tell());
//			self.last_index_time = now
		}
	}

}
