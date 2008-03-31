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
import java.util.Calendar;

import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.VideoException;

/**
 * An object that can be placed in the stills cache.  This class
 * also makes sure that the image data is not expired before returning
 * it to calling classes.
 *
 */
public class CacheEntry {

	//Image cache entry
	protected long imageTime = System.currentTimeMillis();
	protected Client client = null;
	protected String[] backendUrls = null;
	protected byte[] imageData = null;
	protected long expirationAge = 20000; // 20 seconds
	
	public CacheEntry(String[] backendUrls, Client c){
		this.backendUrls = backendUrls;
		this.client = c;
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
    	try{
	    	if(getAge() > expirationAge){
	    		System.out.println(client.getCameraId() + " fetching image.");
	    		imageData = ConnectionFactory.getImage(getImageURL());
	    		imageTime = System.currentTimeMillis();
	    	}else{
	    		System.out.println(client.getCameraId() + " using cache.");
	    	}
	    	return imageData;
    	}catch(IOException ioe){
    		throw new VideoException(ioe.getMessage());
    	}
    }

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

