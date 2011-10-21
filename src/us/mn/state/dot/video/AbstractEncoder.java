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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Hashtable;

import javax.imageio.stream.FileImageInputStream;

/**
 * 
 * @author Timothy Johnson
 *
 */
public abstract class AbstractEncoder implements Encoder {

	/** The HttpURLConnection used for getting stills */
	private HttpURLConnection stillsCon;
	
	/** The username used to connect to this server.  Only required when
	 * the encoder does not allow anonymous connections.
	 */
	protected String username = null;
	
	/** The password used to connect to this server.  Only required when
	 * the encoder does not allow anonymous connections.
	 */
	protected String password = null;

	/** Constant string for no camera connected */
	public static final int NO_CAMERA_CONNECTED = -1;

	/** The Encoder host name (or IP). */
	protected final String host;
	
	/** The tcp port of this encoder. */
	protected int port = 80;

	/** The number of video channels. */
	protected int channels = 1;
	
	/** Get the tcp port of the encoder */
	public final int getPort() { return  port; }

	/** Set the tcp port of the encoder */
	public final void setPort(int p){ this.port = p; }

	/** Get the number of video channels. */
	public final int getChannels(){ return channels; }

	protected abstract URL getImageURL(Client c);
	
	/** The ids of the cameras that are connected. */
	private Hashtable<String, Integer> ids =
		new Hashtable<String, Integer>();

	/** Set the number of available video channels. */
	public final void setChannels(int channels){ this.channels = channels; }

	/** Location of the no_video image */
	private static String noVideoFile = 
		"/usr/share/tomcat6/webapps/video/images/novideo.jpg";

	private static byte[] noVideo = createNoVideoImage();
	
	public final String getHost(){ return host; }
	
	protected String getIp() throws UnknownHostException{
		return InetAddress.getByName(host).getHostAddress();
	}

	public AbstractEncoder(String host){
		this.host = host;
	}
	
	public String toString(){
		String ip = "";
		try{
			ip = getIp();
		}catch(Exception e){}
		return "Encoder: " + host + " (" + ip + ")";
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

	/** Get the id of the camera connected to the given channel */
	public String getCamera(int channel) {
		for(String id : ids.keySet()){
			if(ids.get(id).intValue() == channel) return id;
		}
		return null;
	}

	/** Get the channel nummber for the given camera id. */
	public int getChannel(String id){
		if(ids.get(id) != null){
			return ids.get(id);
		}
		return NO_CAMERA_CONNECTED;
	}

	/** Set the camera id for the given channel */
	public void setCamera(String id, int channel) {
		ids.put(id, new Integer(channel));
	}

	/**
	 * Set the username for authentication.
	 */
	public void setUsername(String user){
		username = user;
	}

	/**
	 * Set the password for authentication.
	 */
	public void setPassword(String pwd){
		password = pwd;
	}
	
	/** Get the next image in the mjpeg stream 
	 *  in which the Content-Length header is present
	 * @return
	 */
	protected byte[] readImage(InputStream in, int imageSize)
			throws IOException{
		byte[] image = new byte[imageSize];
		int bytesRead = 0;
		int currentRead = 0;
		while(bytesRead < imageSize){
			currentRead = in.read(image, bytesRead,
					imageSize - bytesRead);
			if(currentRead==-1){
				break;
			}else{
				bytesRead = bytesRead + currentRead;
			}
		}
		return image;
	}

	protected synchronized final byte[] fetchImage(Client c, URL url) throws VideoException{
		InputStream in = null;
		try {
			stillsCon = ConnectionFactory.createConnection(url);
			prepareConnection(stillsCon);
			int response = stillsCon.getResponseCode();
			if(response == 503){
				throw new Exception("HTTP 503");
			}
			in = stillsCon.getInputStream();
			int length = Integer.parseInt(
					stillsCon.getHeaderField("Content-Length"));
			return readImage(in, length);
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}finally{
			try{
				stillsCon.disconnect();
			}catch(Exception e){}
		}
	}

	/** Prepare a connection by setting necessary properties and timeouts */
	protected void prepareConnection(URLConnection c) throws VideoException {
		if(username!=null && password!=null){
			String userPass = username + ":" + password;
			String encoded = Base64.encodeBytes(userPass.getBytes());
			c.addRequestProperty("Authorization", "Basic " + encoded.toString());
		}
	}
	
	public byte[] getImage(Client c) throws VideoException{
		URL url = getImageURL(c);
		if(url == null){
			throw new VideoException("No URL for camera " + c.getCameraId());
		}
		byte[] image = fetchImage(c, url);
		if(image != null) return image;
		return getNoVideoImage();
	}
}
