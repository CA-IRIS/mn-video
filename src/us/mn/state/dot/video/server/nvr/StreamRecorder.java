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

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * StreamRecorder handles a single video stream and continually 
 * records it to the hard drive.
 * @author Timothy Johnson
 *
 */
public class StreamRecorder extends Thread {

	protected static final int bufSize = 1024 * 1024;
	protected static final long fileSize = bufSize * 1000 * 50;
	protected VideoFile videoFile = null;
	protected String streamURL = null;
	protected String encoderIp = null;
	protected String streamURI = "/mpeg4/1/media.amp";
	protected String streamProtocol = "rtsp";
	protected String cmd = null;
	protected String indexFile = null;
	
	/**
	 * Constructor for the StreamRecorder class.  Takes
	 * the the ip address of the camera's encoder and 
	 * a VideoFile object for storing the stream.
	 * @param ip IP address of the video encoder
	 * @param file The VideoFile in which to store the stream
	 */
	public StreamRecorder(String ip, VideoFile file){
		this.encoderIp = ip;
		this.videoFile = file;
		this.streamURL = streamProtocol + "://" + ip + streamURI;
		cmd = "vlc " + streamURL +
			" -I dummy --sout '#std{mux=" +
			VideoFile.CONTAINER_TYPE + ", access=file, dst=-}')";
	}
	
	protected InputStream getVideoStream(){
		try{
			Process p = Runtime.getRuntime().exec(cmd);
			return new BufferedInputStream(p.getInputStream());
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	/** Start recording the video stream. */
	public void run(){
		try{
			System.out.println("Recording from " + streamURL +
					" to " + videoFile.getName());
			InputStream in = getVideoStream();
			byte[] data = new byte[bufSize];
			int bytesRead = 0;
			while(true){
				bytesRead = in.read(data);
				videoFile.write(data, bytesRead);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}