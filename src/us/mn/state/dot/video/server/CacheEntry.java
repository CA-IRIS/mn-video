/*
 * Project: Video
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.video.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.Encoder;
import us.mn.state.dot.video.VideoException;

/**
 * An object that can be placed in the stills cache.  This class
 * also makes sure that the image data is not expired before returning
 * it to calling classes.
 *
 */
public class CacheEntry {

	protected long imageTime = System.currentTimeMillis();
	protected Client client = null;
	protected String[] backendUrls = null;
	protected byte[] imageData = null;
	protected Logger logger = null;
	protected Encoder encoder = null;
	
	/** Length of time that an image should be cached */
	protected long expirationAge = 10000; // 10 seconds
	
	public CacheEntry(String[] backendUrls, Client c, Logger l){
		this.backendUrls = backendUrls;
		this.client = c;
		this.logger = l;
	}

	public CacheEntry(Encoder e, Client c, Logger l){
		this.encoder = e;
		this.client = c;
		this.logger = l;
	}

	/** Set the expiration time for the cache */
	public void setExpiration(long ex){
		this.expirationAge = ex;
	}
	
    /**
     * Get the age of the image data in milliseconds.
     * @param start
     * @return
     */
    private long getAge(){
		return (System.currentTimeMillis() - imageTime);
	}

    public synchronized byte[] getImage() throws VideoException {
    	if(getAge() > expirationAge || imageData == null){
    		logger.fine(client.getCameraId() + " fetching image.");
	    	imageData = retrieveImage();
	    	imageTime = System.currentTimeMillis();
	    }else{
	    	logger.fine(client.getCameraId() + " using cache.");
	    }
	    return imageData;
    }

    protected byte[] retrieveImage() throws VideoException {
    	try{
	    	if(encoder != null){
	    		return encoder.getImage(client);
	    	}else{
	    		return ConnectionFactory.getImage(getImageURL());
	    	}
    	}catch(IOException ioe){
    		throw new VideoException(ioe.getMessage());
    	}
    }
    
    /** Get the URL used to retrieve a new image */
    protected URL getImageURL() throws VideoException {
		String s = "";
    	try{
			s = backendUrls[client.getArea()] + "?id=" + client.getCameraId() +
				"&size=" + client.getSize();
			return new URL(s);
		}catch(MalformedURLException mue){
			throw new VideoException("Malformed URL: " + s);
		}
    }

}

