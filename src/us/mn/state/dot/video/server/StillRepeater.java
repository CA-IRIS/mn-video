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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.AbstractImageFactory;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.VideoException;

/**
 * Java implementation of stills cache python script
 *
 * @author    Timothy Johnson
 * @created   December 20, 2005
 */
public final class StillRepeater extends VideoServlet{

	private static Hashtable<String, CacheEntry> cache =
		new Hashtable<String, CacheEntry>();

	protected final long DEFAULT_CACHE_DURATION = 10000; //10 seconds
	
	protected long cacheDuration = DEFAULT_CACHE_DURATION;
	
	protected String[] backendUrls = null;
	
	/** Constructor for the redirector servlet */
    public void init(ServletConfig config) throws ServletException {
		super.init( config );
		try{
			ServletContext ctx = config.getServletContext();
			Properties p = (Properties)ctx.getAttribute("properties");
			backendUrls = AbstractImageFactory.createBackendUrls(p, 2);
			cacheDuration = Long.parseLong(
					p.getProperty("video.cache.duration",
					Long.toString(DEFAULT_CACHE_DURATION)));
			System.out.println("StillRepeater initialized.");
		}catch(Exception e){
			logger.severe(e.getMessage() + " --see error log for details.");
			e.printStackTrace();
		}
    }

    /** Process a request for a video image
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 */
    public void processRequest(HttpServletResponse response,
    		Client c)throws VideoException{
    	long start = System.currentTimeMillis();
    	byte[] image = getImage(c);
		try{
			if(image != null){
				response.setContentType("image/jpeg");
				response.getOutputStream().write(image);
			}else{
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			response.flushBuffer();
		}catch(Throwable th){
			logger.info("Unable to write image (" +c.getCameraId() + ") " +
					" to client (" + c.getHost() + ").");
		}finally{
			logger.fine("Request filled in " + (System.currentTimeMillis()-start) +
					" milliseconds");
		}
    }

    /** Get the image requested by the client
     * 
     * @return The image as a byte[].  Returns null if
     * the image cannot be obtained.
     */
    private byte[] getImage(Client c) throws VideoException{
    	if(c.getCameraId()==null)return null;
    	String key = createCacheKey(c);
		CacheEntry entry = cache.get(key);
		if(entry == null){
			entry = new CacheEntry(backendUrls, c);
			entry.setExpiration(cacheDuration);
			cache.put(key, entry);
		}
		return entry.getImage();
    }
    
    private static String createCacheKey(Client c){
    	return c.getArea() + ":" + c.getCameraId() + ":" + c.getSize();
    }
}