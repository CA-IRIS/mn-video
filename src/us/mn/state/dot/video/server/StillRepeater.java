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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import us.mn.state.dot.video.AbstractImageFactory;
import us.mn.state.dot.video.AxisServer;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.VideoException;



/**
 * Java implementation of stills cache python script
 *
 * @author    john3tim
 * @created   December 20, 2005
 */
public final class StillRepeater extends VideoServlet{

	/** The time that an image should remain in cache */
	private static int cacheAge = 5000; // milliseconds

	private static Hashtable<String, CacheEntry> cache =
		new Hashtable<String, CacheEntry>();

	protected String[] backendUrls = null;
	
	/** Contructor for the redirector servlet */
    public void init(ServletConfig config) throws ServletException {
		super.init( config );
		ServletContext ctx = config.getServletContext();
		Properties p = (Properties)ctx.getAttribute("properties");
		backendUrls = AbstractImageFactory.createBackendUrls(p, 2);
		cacheAge = Integer.parseInt(p.getProperty("cacheAge"));
    }

    /** Process a request for a video image
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 */
    public void processRequest(HttpServletResponse response,
    		Client c)throws VideoException{
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
			logger.info("Unable to write image (" +c.getCameraNumber() + ") " +
					" to client (" + c.getHost() + ").");
		}
    }

    /** Get the image requested by the client
     * 
     * @return The image as a byte[].  Returns null if
     * the image cannot be obtained.
     */
    private byte[] getImage(Client c) throws VideoException{
    	if(c.getCameraNumber()==-1)return null;
    	byte[] image = null;
    	image = lookupImage(c);
    	if(image != null) return image;
		URL url = null;
		String s = "";
		try{
			s = backendUrls[c.getArea()] + "?id=" + c.getCameraNumber() +
				"&size=" + c.getSize();
			logger.fine("Fetching image: " + s);
			url = new URL(s);
		}catch(MalformedURLException mue){
			throw new VideoException("Malformed URL: " + s);
		}
		image = fetchImage(url);
		addEntry(c, image);
		return image;
    }
    
    /**
     * Get the number of milliseconds that have elapsed since start.
     * @param start
     * @return
     */
    private static float getAge(long start){
		return (Calendar.getInstance().getTimeInMillis() - start);
	}

    private static String createHashKey(Client c){
    	return c.getArea() + ":" + c.getCameraNumber() + ":" + c.getSize();
    }

    /* Lookup an image in the cache */
	private static byte[] lookupImage(Client c){
		String key = createHashKey(c); 
		CacheEntry entry = (CacheEntry)(cache.get(key));
		if(entry == null) return null;
		if(getAge(entry.imageTime) < cacheAge){
			return entry.imageData;
		}else{
			cache.remove(key);
		}
		return null;
	}
	
	/**
	 * Get the image at the given URL
	 * @param url
	 * @return An image as a byte[].  Returns null if
	 * the image cannot be obtained.
	 */
	private byte[] fetchImage(URL url) throws VideoException{
//    	fetchQueue.add(Calendar.getInstance());
    	URLConnection con = null;
    	try {
			con = ConnectionFactory.createConnection(url);
			InputStream is = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int bytesRead = 0;
			while(true){
				bytesRead = is.read(data);
				if(bytesRead==-1) break;
				bos.write(data, 0, bytesRead);
			}
			return bos.toByteArray();
		}catch(IOException ioe){
			throw new VideoException(ioe.getMessage() +
					": " + url.toString());
		}finally{
			try{
				con.getInputStream().close();
			}catch(IOException ioe2){
			}
		}
	}

	public synchronized void logRequest(Client c, byte[] imageData){
		String s = "[CACHE]";
		if(imageData == null) s = "[FETCH]";
		logger.info(s + " [EXP:" + cacheAge + "]" + c.getHost() + 
			" CAM: " + c.getCameraNumber());
	}
	
	/** Add an entry into the cache */
	private static void addEntry(Client c, byte[] image){
		if(image != null) cache.put(createHashKey(c), new CacheEntry(image));
	}

}