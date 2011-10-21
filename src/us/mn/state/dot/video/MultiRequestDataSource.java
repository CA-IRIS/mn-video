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


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
/**
 * The MultiRequestDataSource gets it's data via the HTTP protocol
 * and a series of requests for single images over a single HTTP connection.
 *
 * @author Timothy Johnson
 */
public class MultiRequestDataSource extends AbstractDataSource {

	protected final HttpURLConnection connection;

	/** Constructor for the MultiRequestDataSource. */
	public MultiRequestDataSource(Client c, HttpURLConnection conn) {
		this(c, null, null, conn);
	}

	/** Constructor for the MultiRequestDataSource. */
	public MultiRequestDataSource(Client c, Logger l, ThreadMonitor m, HttpURLConnection conn) {
		super(c, l, m);
		this.connection = conn;
	}
	
	private byte[] readImage(InputStream in, int imageSize)
			throws IOException{
		byte[] image = new byte[imageSize];
		int bytesRead = 0;
		int currentRead = 0;
		while(bytesRead < imageSize){
			currentRead = in.read(image, bytesRead, imageSize - bytesRead);
			if(currentRead==-1){
				break;
			}else{
				bytesRead = bytesRead + currentRead;
			}
		}
		return image;
	}

	/** Start the stream. */
	public void run() {
		InputStream in = null;
		if(connection != null){
			try{
				while(!done && this.isAlive()){
					int response = connection.getResponseCode();
					if(response == 503){
						logger.info("503 response.");
						break;
					}
					in = connection.getInputStream();
					int length = Integer.parseInt(
							connection.getHeaderField("Content-Length"));
					logger.fine("Starting: " + this);
					byte[] img = readImage(in, length);
					if(img != null && img.length > 0){
						notifySinks(img);
					}else{
						//FIXME: Continue trying to get images even if null or empty.
						//Pehaps a counter can keep track of contiguous failures and
						//then break.

						//break;
					}
				}
			}catch(IOException ioe){
				logger.info(ioe.getMessage());
			}finally{
				logger.fine("Stopping: " + this);
				try{
					connection.disconnect();
				}catch(Exception e2){
				}
				removeSinks();
			}
		}else{
			logger.fine("No encoder defined for this source.");
		}
	}
}
