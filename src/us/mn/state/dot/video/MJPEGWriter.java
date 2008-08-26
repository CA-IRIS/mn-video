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

package us.mn.state.dot.video;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 * The ClientStream is a stream that sends images to the client
 *
 * @author Timothy Johnson
 */
public class MJPEGWriter implements DataSink {

	/** The maximum time a stream can run (in seconds) */
	private static final long MAX_DURATION = 10 * 1000 * 1; // 5 minutes
	
	/** The maximum time to wait for more data before terminating (in seconds) */
	private static final long DATA_TIMEOUT = 5 * 1000 ; // 5 seconds

	protected boolean done = false;
	
	private static final String CONTENT_TYPE =
		"Content-Type: image/jpeg";

	private static final String CONTENT_LENGTH = "Content-Length: ";
	
	/** The output stream to write the video to. */
	private DataOutputStream out;

	private Client client = null;
	
	private long startTime = Calendar.getInstance().getTimeInMillis();
	
	/** The time (in milliseconds) of the last data packet */
	private long lastPacket = startTime;
	
	private Logger logger = null;
	
	/** The time to sleep, in milliseconds, between sending images to the client */
	private final int sleepDuration;

	private DataSource source = null;
	
	private byte[] data = null;
	
	/** Constructor for the MJPEGWriter. */
	public MJPEGWriter (Client c, OutputStream out,
			DataSource source, Logger l, int maxRate){
		logger = l;
		client = c;
		sleepDuration = 1000 / Math.min(maxRate, client.getRate());
		this.out = new DataOutputStream(out);
		this.source = source;
		source.connectSink(this);
	}

	/** Flush the data down the sink */
	public synchronized void flush(byte[] data){
		this.data = data;
	}

	public synchronized byte[] getData(){
		return data;
	}
	
	public String toString(){
		if(client==null){
			return "Uninitialized " + this.getClass().getSimpleName();
		}
		return this.getClass().getSimpleName() + " " + client.toString();
	}

	/** Sends images to the client until all the requested
	 * images have been sent. */
	public void sendImages() {
		String termReason = "completed";
		try{
			while(!isDone()) {
				writeBodyPart();
				Thread.sleep(sleepDuration);
			}
		}catch(IOException ioe){
			termReason = "IOE:";
			logger.info("IOE: " + this.toString() + " is closing.");
		}catch(Exception e){
			termReason = e.getClass().getSimpleName();
			logger.info("Error sending images to " + client.getUser());
		}finally{
			source.disconnectSink(this);
			try{
				halt(termReason);
				out.close();
			}catch(Exception e2){
			}
		}
	}

	public boolean isDone(){
		long now = Calendar.getInstance().getTimeInMillis();
		if((now-startTime) > MAX_DURATION){
			halt("Stream is too old.");
		}
		if((now-lastPacket) > DATA_TIMEOUT){
			halt("Time out receiving data.");
		}
		return done;
	}
	
	public void halt(String reason){
		logger.info(this.toString() + " terminated: " + reason);
		done = true;
	}
	
	/** Write a body part (a piece of a multipart response) */
	private synchronized void writeBodyPart()throws IOException{
		if(data==null || data.length == 0) return;
		writeBoundary();
		writeHeaderArea();
		out.write('\r');
		out.write('\n');
		writeBodyArea();
		out.flush();
		data = null;
		lastPacket = Calendar.getInstance().getTimeInMillis();
	}
	
	private void writeBoundary() throws IOException {
		out.write(MJPEG.BOUNDARY.getBytes());
	}

	private void writeHeaderArea() throws IOException {
		out.write(CONTENT_TYPE.getBytes());
		out.write('\r');
		out.write('\n');
		out.write(CONTENT_LENGTH.getBytes());
		out.write(Integer.toString(data.length).getBytes());
		out.write('\r');
		out.write('\n');
	}

	private void writeBodyArea() throws IOException {
		out.write(data);
		out.write('\r');
		out.write('\n');
	}
}
