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
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

/**
 * The ImageFactory connects to a video stream from the stream server.
 * It notifies each of it's listeners whenever there is a new image.
 *
 * @author Timothy Johnson
 */
public class RepeaterImageFactory extends AbstractImageFactory {

	/** URL of the images to get */
	protected URL url;

	/** Constructor for the ImageFactory. */
	public RepeaterImageFactory(Client c, String baseUrl)
			throws VideoException {
		this(c, baseUrl, null, null);
	}

	/** Constructor for the ImageFactory. */
	public RepeaterImageFactory(Client c, String baseUrl,
			Logger l, ThreadMonitor m) throws VideoException {
		super(c, l, m);
		logger.fine("Creating RepeaterImageFactory for client: " + c.toString());
		String s = "";
		try{
			s = baseUrl +
				"?id=" + c.getCameraId() +
				"&size=" + c.getSize() +
				"&rate=" + c.getRate() +
				"&duration=" + c.getDuration() +
				"&user=" + c.getUser() +
				"&area=" + c.getArea();
			url = new URL(s);
		}catch(MalformedURLException mue){
			throw new VideoException("Malformed URL: " + s);
		}
		start();
	}

	public void run() {
		URLConnection con = null;
		try {
			con = ConnectionFactory.createConnection(url);
			MJPEGStream stream = new MJPEGStream(con.getInputStream());
			if(stream != null){
				logger.info("Opened factory " + this);
				byte[] img;
				while(!done && this.isAlive()){
					img = stream.getImage();
					if(img != null){
						imageCreated(img);
					}else{
						break;
					}
				}
			}else{
				logger.info("Unable to create " + this);
			}
		}catch(SocketTimeoutException ste){
			logger.info(this + " timed out connecting to stream server.");
		}catch(IOException ioe){
			logger.info(this + ": exception reading stream.");
		}catch(InstantiationException ie){
			logger.info(this + " unable to create MJPEGStream.");
		}finally{
			logger.info("Closing ImageFactory: " + this);
			try {
				con.getInputStream().close();
			}catch(Exception e){
			}
		}
	}

}
