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

import java.io.File;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Handler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.AbstractDataSource;
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
	
	protected EncoderFactory encoderFactory = null;
	
	/** Constructor for the ImageServer */
    public void init(ServletConfig config) throws ServletException {
		super.init( config );
		Calendar begin = Calendar.getInstance();
		try{
			ServletContext ctx = config.getServletContext();
			Properties p = (Properties)ctx.getAttribute("properties");
			if(proxy){
				backendUrls = AbstractDataSource.createBackendUrls(p, 2);
			}else{
				encoderFactory = EncoderFactory.getInstance(p);
			}
			cacheDuration = Long.parseLong(
					p.getProperty("video.cache.duration",
					Long.toString(DEFAULT_CACHE_DURATION)));
			Calendar end = Calendar.getInstance();
			float seconds = (end.getTimeInMillis()-begin.getTimeInMillis())/1000.0f;
			logger.info("ImageServer initialization took " + seconds + " seconds.");
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
		CacheEntry entry = getCacheEntry(c);
		byte[] image = entry.getImage();
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
			Handler[] handlers = logger.getHandlers();
			System.out.println("Printing handlers...");
			for(Handler h : handlers){
				System.out.println("VIDEO HANDLER: " + h.getClass());
			}
			logger.fine("Request filled in " + (System.currentTimeMillis()-start) +
					" milliseconds");
		}
	}
	
    private CacheEntry getCacheEntry(Client c) {
   		String key = createCacheKey(c);
    	CacheEntry entry = cache.get(key);
    	if(entry != null && !entry.isExpired()) return entry;
    	if(proxy){
			entry = new CacheEntry(backendUrls, c, logger);
		}else{
			entry = new CacheEntry(encoderFactory.getEncoder(c.getCameraId()),
					c, logger);
		}
		entry.setExpiration(cacheDuration);
		cache.put(key, entry);
		return entry;
    }
    
    private static String createCacheKey(Client c){
    	return c.getArea() + ":" + c.getCameraId() + ":" + c.getSize();
    }
}
