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
package us.mn.state.dot.video.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.Encoder;
import us.mn.state.dot.video.ImageFactory;
import us.mn.state.dot.video.RequestType;
import us.mn.state.dot.video.VideoException;

/**
 * VideoServer is the main thread for still video server.
 *
 * @author    dinh1san
 * @author    Tim Johnson
 * @created   December 27, 2001
 */
public final class ImageServer extends VideoServlet{

	private static Hashtable<String, CacheEntry> cache =
		new Hashtable<String, CacheEntry>();

	private String encoderUser = null;
	
	private String encoderPass = null;
	
	protected final long DEFAULT_CACHE_DURATION = 10000; //10 seconds
	
	protected long cacheDuration = DEFAULT_CACHE_DURATION;
	
	/** Constructor for the ImageServer */
    public void init(ServletConfig config) throws ServletException {
		super.init( config );
		try{
			ServletContext ctx = config.getServletContext();
			Properties p = (Properties)ctx.getAttribute("properties");
			encoderUser = p.getProperty("video.encoder.user");
			encoderPass = p.getProperty("video.encoder.pwd");
			cacheDuration = Long.parseLong(
					p.getProperty("video.cache.duration",
					Long.toString(DEFAULT_CACHE_DURATION)));
		}catch(Exception e){
			logger.severe(e.getMessage() + " --see error log for details.");
			e.printStackTrace();
		}
    }

	/**
	 * @param request servlet request
	 * @param response servlet response
	 */
	public void processRequest(HttpServletResponse response, Client c)
		throws VideoException
	{
		long start = System.currentTimeMillis();
		byte[] image = getImage(c);
		try{
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("image/jpeg\r\n");
			response.setContentLength(image.length);
			response.getOutputStream().write(image);
			response.flushBuffer();
		}catch(Throwable t){
			logger.warning("Error serving image " + c.getCameraId() +
					" to client " + c.getHost());
		}finally{
			logger.fine("Request filled in " + (System.currentTimeMillis()-start) +
					" milliseconds");
		}
	}

	private byte[] getImage(Client c) {
		String key = createCacheKey(c);
		CacheEntry entry = cache.get(key);
		if(entry != null && !entry.isExpired()) return entry.getImage();
		byte[] image = fetchImage(c);
		if(image == null){
			return noVideo;
		}
		if(entry == null){
			entry = new CacheEntry(image, cacheDuration);
			cache.put(key, entry);
		}else{
			entry.setImage(image);
		}
		return image;
	}
	
	private byte[] fetchImage(Client c){
		URL url = null;
		try{
			if(proxy){
				url = getDistrictImageURL(c);
			}else{
				Encoder encoder = encoderFactory.getEncoder(c.getCameraId());
				url = encoder.getImageURL(c);
			}
			if(url==null){
				return null;
			}
			return ImageFactory.getImage(url, encoderUser, encoderPass);
		}catch(VideoException ve){
			logger.fine(ve.getMessage());
		}
		return null;
	}

	/** Get the URL used to retrieve a new image from a district server */
	private URL getDistrictImageURL(Client c) throws VideoException {
		String relativeURL = "";
		try{
			relativeURL = "/video/" +
				RequestType.IMAGE.name().toLowerCase() +
				"?id=" + c.getCameraId();
				//"&size=" + c.getSize();
			return new URL(districtVideoURLs.get(c.getDistrict()), relativeURL);
		}catch(MalformedURLException mue){
			throw new VideoException(mue.getMessage());
		}
	}

    private static String createCacheKey(Client c){
    	return c.getDistrict().name() + ":" + c.getCameraId() + ":" + c.getSize();
    }
}
