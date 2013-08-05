/*
* VideoServer
* Copyright (C) 2003-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.video;

import java.net.URL;


/**
 * The AxisEncoder class encapsulates information about an axis video
 * capture device
 *
 * @author    Timothy Johnson
 * @created   July 2, 2003
 */

public final class Axis extends AbstractEncoder {

	/** The base URI for a request for an image */
	private final String BASE_IMAGE_URI = "/axis-cgi/jpg/image.cgi?" +
		"showlength=1&";
	
	private final String BASE_STREAM_URI = "/axis-cgi/mjpg/video.cgi?" +
	"showlength=1&";
	
	/** The camera request parameter */
	private static final String PARAM_CAMERA = "camera";
	
	/** Constructor for the axis encoder object */
	public Axis(String host, String user, String pass) {
		super(host, user, pass);
	}
	
	/**
	 * Get a URL for connecting to the MJPEG stream of an Axis Server.
	 * @param c The client object containing request parameters.
	 * @return
	 */
	public URL getStreamURL(Client c){
		int channel = getChannel(c.getCameraName());
		if(channel == NO_CAMERA_CONNECTED) return null;
		try{
			return new URL( "http://" + host + ":" +
					getPort() + BASE_STREAM_URI +
					createCameraParam(c) +
					"&" + createSizeParam(c.getSize()));
		}catch(Exception e){
		}
		return null;
	}

	private String createCameraParam(Client c){
		return PARAM_CAMERA + "=" + getChannel(c.getCameraName());	
	}

	public URL getImageURL(Client c) {
		int channel = getChannel(c.getCameraName());
		if(channel == NO_CAMERA_CONNECTED) return null;
		try{
			String url = 
				"http://" + host + ":" +
				getPort() + BASE_IMAGE_URI +
				createCameraParam(c) +
				"&" + createSizeParam(c.getSize()) +
				"&compression=30";
			return new URL(url);
		}catch(Exception e){
			return null;
		}
	}

	public DataSource getDataSource(Client c) throws VideoException{
		URL url = getStreamURL(c);
		if(url == null) return null;
		try{
			return new HttpDataSource(c, url, username, password);
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}
	}
}
