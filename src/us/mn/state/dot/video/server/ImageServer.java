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

import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.AbstractDataSource;
import us.mn.state.dot.video.AxisServer;
import us.mn.state.dot.video.Client;
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

	protected final long DEFAULT_CACHE_DURATION = 10000; //10 seconds
	
	protected long cacheDuration = DEFAULT_CACHE_DURATION;
	
	protected String[] backendUrls = null;
	
	protected ServerFactory serverFactory = null;
	
	/** Constructor for the ImageServer */
    public void init(ServletConfig config) throws ServletException {
		super.init( config );
		try{
			ServletContext ctx = config.getServletContext();
			Properties p = (Properties)ctx.getAttribute("properties");
			if(proxy){
				backendUrls = AbstractDataSource.createBackendUrls(p, 2);
			}else{
				serverFactory = new ServerFactory(p);
			}
			cacheDuration = Long.parseLong(
					p.getProperty("video.cache.duration",
					Long.toString(DEFAULT_CACHE_DURATION)));
			logger.info( "ImageServer initialized successfully." );
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
		if(proxy){
			processProxyRequest(response, c);
			return;
		}
		byte[] image = AxisServer.getNoVideoImage();
		int status = HttpServletResponse.SC_OK;
		String contentType = "image/jpeg\r\n";
		AxisServer server = serverFactory.getServer(c.getCameraId());
		if(server != null){
			try{
				image = server.getImage(c);
			}catch(VideoException ve){
				logger.info(c.getCameraId() + ": " + ve.getMessage());
			}
		}
		try{
			response.setStatus(status);
			response.setContentType(contentType);
			response.setContentLength(image.length);
			response.getOutputStream().write(image);
			response.flushBuffer();
		}catch(Throwable t){
			logger.warning("Error serving image " + c.getCameraId());
		}
	}
	

    /** Process a request for a video image
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 */
    public void processProxyRequest(HttpServletResponse response,
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
			entry = new CacheEntry(backendUrls, c, logger);
			entry.setExpiration(cacheDuration);
			cache.put(key, entry);
		}
		return entry.getImage();
    }
    
    private static String createCacheKey(Client c){
    	return c.getArea() + ":" + c.getCameraId() + ":" + c.getSize();
    }
}
