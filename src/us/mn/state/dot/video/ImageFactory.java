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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.stream.FileImageInputStream;


/** The ImageFactory is a convenience class for retrieving images.
 * 
 * @author Timothy A. Johnson
 *
 */
abstract public class ImageFactory {

	/** Location of the no_video image */
	private static String noVideoFile = 
		"/usr/share/tomcat6/webapps/video/images/novideo.jpg";

	private static byte[] noVideo = createNoVideoImage();

	public static HttpURLConnection createConnection(URL url, String user, String pwd)
			throws VideoException {
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
			throw new VideoException(e.getMessage());
		}
	}
	
	/** Read data from the URL into a file.
	 * 
	 * @param url The URL of the source
	 * @param f The file in which to save the data.
	 * @throws IOException
	 */
	public static void readData(URL url, File f)
			throws VideoException{
		FileOutputStream out = null;
		try{
			out = new FileOutputStream(f);
			URLConnection c = createConnection(url);
			InputStream in = c.getInputStream();
			byte[] data = new byte[1024];
			int bytesRead = 0;
			while(true){
				bytesRead = in.read(data);
				if(bytesRead==-1) break;
				out.write(data, 0, bytesRead);
			}
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}finally{
			try{
				out.flush();
				out.close();
			}catch(Exception e){
			}
		}
	}

	/**
	 * Get an image from the given url
	 * @param url The location of the image file
	 * @return A byte[] containing the image data.
	 * @throws IOException
	 */
	public static byte[] getImage(URL url)
			throws VideoException{
		InputStream in = null;
		try{
			URLConnection c = createConnection(url);
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
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}finally{
			try{
				in.close();
			}catch(IOException ioe2){
				System.err.println(ioe2.getStackTrace());
			}catch(NullPointerException npe){
				System.err.println(npe.getStackTrace());
			}
		}
	}

	/** Prepare a connection by setting necessary properties and timeouts */
	protected static void prepareConnection(URLConnection c, String user, String pwd)
			throws VideoException {
		if(user!=null && pwd!=null){
			String userPass = user + ":" + pwd;
			String encoded = Base64.encodeBytes(userPass.getBytes());
			c.addRequestProperty("Authorization", "Basic " + encoded.toString());
		}
	}

	/** Create a no-video image */
	protected final static byte[] createNoVideoImage(){
		try{
			FileImageInputStream in = null;
			in = new FileImageInputStream(new File(noVideoFile));
			byte[] bytes = new byte[(int)in.length()];
			in.read(bytes, 0, bytes.length);
			return bytes;
		}catch(IOException ioe){
			return null;
		}
	}

	public static byte[] getNoVideoImage(){
		return noVideo;
	}

}
