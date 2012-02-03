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
package us.mn.state.dot.video;

import java.net.URL;
import java.util.logging.Logger;

import javax.xml.ws.http.HTTPException;

import us.mn.state.dot.video.ImageFactory;
import us.mn.state.dot.video.VideoException;


/**
 * An object that can be placed in the stills cache.  This class
 * also makes sure that the image data is not expired before returning
 * it to calling classes.
 *
 */
public class CacheEntry {

	private long imageTime = System.currentTimeMillis();
	private byte[] image = null;
	private final URL imageURL;
	private final String user;
	private final String pass;
	private int statusCode = -1;
	
	/** Length of time that an image should be cached */
	protected final long expirationAge;
	
	public CacheEntry(URL url, String user, String pass, long age){
		this.imageURL = url;
		this.user = user;
		this.pass = pass;
		this.expirationAge = age;
		imageTime = imageTime - (2 * expirationAge); //initially expired
	}

    /**
     * Get the age of the image data in milliseconds.
     * @param start
     * @return
     */
	private long getAge(){
		return (System.currentTimeMillis() - imageTime);
	}

	private boolean isExpired(){
		return (getAge() > expirationAge);
	}

	private void setImage(byte[] i){
		imageTime = System.currentTimeMillis();
		image = i;
	}
	
	public synchronized byte[] getImage() throws HTTPException, VideoException{
		if(isExpired()){
			fetchImage();
		}
		if(statusCode > 0 && statusCode != 200){
			throw new HTTPException(statusCode);
		}
		return image;
	}

	private void fetchImage() throws HTTPException, VideoException {
		try{
			setImage(ImageFactory.getImage(imageURL, user, pass));
			statusCode = 200;
		}catch(HTTPException httpE){
			statusCode = httpE.getStatusCode();
			imageTime = System.currentTimeMillis();
			throw httpE;
		}catch(VideoException ve){
			statusCode = -1;
			imageTime = System.currentTimeMillis();
			throw ve;
		}
	}
}

