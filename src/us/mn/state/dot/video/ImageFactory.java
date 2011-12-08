/*
 * Project: Video
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package us.mn.state.dot.video;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;


/** The ImageFactory is a convenience class for retrieving images.
 * 
 * @author Timothy A. Johnson
 *
 */
abstract public class ImageFactory {

	static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

	public static HttpURLConnection createConnection(URL url, String user, String pwd)
			throws VideoException {
		if(url==null){
			throw new VideoException("URL is null");
		}
		HttpURLConnection c = createConnection(url);
		prepareConnection(c, user, pwd);
		return c;
	}
	
	public static HttpURLConnection createConnection(URL url)
			throws VideoException {
		try{
			HttpURLConnection c = (HttpURLConnection)url.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			c.setConnectTimeout(VideoThread.TIMEOUT_DIRECT);
			c.setReadTimeout(VideoThread.TIMEOUT_DIRECT);
			return c;
		}catch(Exception e){
			logger.info("CONNECT EXCEPT: " + url);
		}
		return null;
	}
	
	/**
	 * Get an image from the given url
	 * @param url The location of the image file
	 * @return A byte[] containing the image data.
	 * @throws VideoException
	 */
	public static byte[] getImage(URL url, String user, String pwd)
			throws VideoException{
		InputStream in = null;
		try{
			HttpURLConnection c = createConnection(url, user, pwd);
			int response = c.getResponseCode();
			if(response != 200){
				logger.info("RESPONSE " + response + ": " + url);
				return null;
			}
			in = c.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int bytesRead = 0;
			while(true){
				bytesRead = in.read(data);
				if(bytesRead==-1) break;
				bos.write(data, 0, bytesRead);
			}
			return bos.toByteArray();
		}catch(SocketTimeoutException ste){
			logger.info("TIMEOUT: " + url);
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}finally{
			try{
				in.close();
			}catch(Exception e2){
			}
		}
		return null;
	}

	/** Prepare a connection by setting necessary properties and timeouts */
	protected static void prepareConnection(URLConnection c, String user, String pwd) {
		if(user!=null && pwd!=null){
			String userPass = user + ":" + pwd;
			String encoded = Base64.encodeBytes(userPass.getBytes());
			c.addRequestProperty("Authorization", "Basic " + encoded.toString());
		}
	}
}
