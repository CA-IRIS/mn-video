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


/**
 * An object that can be placed in the stills cache.  This class
 * also makes sure that the image data is not expired before returning
 * it to calling classes.
 *
 */
public class CacheEntry {

	private long imageTime = System.currentTimeMillis();
	private byte[] image = null;
	
	/** Length of time that an image should be cached */
	protected final long expirationAge;
	
	public CacheEntry(byte[] data, long age){
		this.image = data;
		this.expirationAge = age;
	}

    /**
     * Get the age of the image data in milliseconds.
     * @param start
     * @return
     */
	private long getAge(){
		return (System.currentTimeMillis() - imageTime);
	}

	public boolean isExpired(){
		return (getAge() > expirationAge || image == null);
	}

	public synchronized void setImage(byte[] i){
		imageTime = System.currentTimeMillis();
		image = i;
	}
	
	public synchronized byte[] getImage() {
		if(!isExpired()){
			return image;
		}else{
			return null;
		}
	}
}

