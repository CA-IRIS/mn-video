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
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Hashtable;

import javax.imageio.stream.FileImageInputStream;

/**
 * 
 * @author Timothy Johnson
 *
 */
public abstract class AbstractEncoder implements Encoder {

	public String PROBE_URI = null;

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
	
	/** The ids of the cameras that are connected. */
	private Hashtable<String, Integer> ids =
		new Hashtable<String, Integer>();

	/** Set the number of available video channels. */
	public final void setChannels(int channels){ this.channels = channels; }

	/** Location of the no_video image */
	private static String noVideoFile = 
		"/usr/local/tomcat/current/webapps/@@NAME@@/images/novideo.jpg";

	private static byte[] noVideo = createNoVideoImage();
	
	public final String getHost(){ return host; }
	
	protected final String getIp() throws UnknownHostException{
		return InetAddress.getByName(host).getHostAddress();
	}

	public AbstractEncoder(String host){
		this.host = host;
	}
	
	public final String toString(){
		String ip = "";
		try{
			ip = getIp();
		}catch(Exception e){}
		return "Encoder: " + host + " (" + ip + ")";
	}

	/** Create a no-video image */
	protected static byte[] createNoVideoImage(){
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
	public final String getCamera(int channel) {
		for(String id : ids.keySet()){
			if(ids.get(id).intValue() == channel) return id;
		}
		return null;
	}

	/** Get the channel nummber for the given camera id. */
	public final int getChannel(String id){
		if(ids.get(id) != null){
			return ids.get(id);
		}
		return NO_CAMERA_CONNECTED;
	}

	/** Set the camera id for the given channel */
	public final void setCamera(String id, int channel) {
		ids.put(id, new Integer(channel));
	}

	/**
	 * Set the username for authentication.
	 */
	public final void setUsername(String user){
		username = user;
	}

	/**
	 * Set the password for authentication.
	 */
	public final void setPassword(String pwd){
		password = pwd;
	}
	
	/**
	 * Probe the host to determine manufacturer.
	 * @param host
	 * @return True if it's a model made by this manufacturer. Otherwise, false.
	 */
	public static final boolean probe(final URL url){
		HttpURLConnection con = null;
		try {
			if(url == null) return false;
			con = ConnectionFactory.createConnection(url);
			int response = con.getResponseCode();
			if(response == 200) return true;
			return false;
		}catch(Exception e){
			System.out.println(url.getHost() + ":" + e.getMessage());
			return false;
		}finally{
			try{
				con.disconnect();
			}catch(Exception e){}
		}
	}
}
