/*
 * Project: Video
 * Copyright (C) 2012  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Properties;

import javax.xml.ws.http.HTTPException;

import us.mn.state.dot.video.server.CacheEntry;

public class ImageCache {

	private String user = null;
	
	private String pass = null;
	
	private static HashMap<String, CacheEntry> cacheMap =
		new HashMap<String, CacheEntry>();

	protected final long DEFAULT_CACHE_DURATION = 10000; //10 seconds
	
	protected long cacheDuration = DEFAULT_CACHE_DURATION;
	
	private static ImageCache imageCache = null;
	
	protected ImageCache(Properties p){
		user = p.getProperty("video.encoder.user");
		pass = p.getProperty("video.encoder.pwd");
		cacheDuration = Long.parseLong(
				p.getProperty("video.cache.duration",
				Long.toString(DEFAULT_CACHE_DURATION)));
	}
	public static synchronized ImageCache create(Properties p){
		if(imageCache == null){
			imageCache = new ImageCache(p);
		}
		return imageCache;
	}
	
	private synchronized CacheEntry getEntry(String key, URL imageURL){
		CacheEntry entry = cacheMap.get(key);
		if(entry != null){
			return entry;
		}
		entry = new CacheEntry(imageURL, user, pass, cacheDuration);
		cacheMap.put(key, entry);
		return entry;
	}
	
	public byte[] getImage(String key, URL imageURL) throws HTTPException, VideoException {
		CacheEntry entry = getEntry(key, imageURL);
		return entry.getImage();
	}
}
