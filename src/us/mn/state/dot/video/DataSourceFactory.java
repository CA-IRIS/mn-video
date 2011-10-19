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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import us.mn.state.dot.video.server.ServerFactory;

/**
 * The DataSourceFactory creates and maintains DataSources.
 * It is responsible for making sure that only one DataSource
 * object is created for each stream regardless of the number 
 * of clients requesting the stream.
 *
 * @author Timothy Johnson
 */
public class DataSourceFactory {

	private ThreadMonitor monitor = null;
	
	/** Table of video streams that are active. */
	static final protected Hashtable<String, HttpDataSource>
		sourceTable = new Hashtable<String, HttpDataSource>();

	private final Logger logger;
	
	/**Flag that controls whether this instance is acting as a proxy 
	 * or a direct video server */
	private boolean proxy = false;

	protected String[] backendUrls = null;
	
	protected ServerFactory serverFactory;
	
	/** Constructor for the DataSourceFactory. */
	public DataSourceFactory(Properties p,
			Logger l, ThreadMonitor m) {
		logger = l;
		monitor = m;
		proxy = new Boolean(p.getProperty("proxy", "false")).booleanValue();
		if(proxy) {
			backendUrls = AbstractDataSource.createBackendUrls(p, 1);
		}else{
			serverFactory = ServerFactory.getInstance(p);
		}
		Thread t = new Thread(){
			public void run(){
				while(true){
					Enumeration<HttpDataSource> e = sourceTable.elements();
					while(e.hasMoreElements()){
						HttpDataSource src = e.nextElement();
						if(!src.isAlive()){
							Client c = src.getClient();
							logger.info("Purging " + src);
							sourceTable.remove(c.getCameraId() + ":" + c.getSize());
						}
					}
					try{
						Thread.sleep(60 * 1000);
					}catch(InterruptedException ie){
					}
				}
			}
		};
		t.start();
	}
	
	private DataSource createDataSource(Client c)
			throws VideoException {
		URL url = null;
		try{
			if(proxy){
				url = createURL(c, backendUrls[c.getArea()]);
			}else{
				AxisServer server = serverFactory.getServer(c.getCameraId());
				if(server == null){
					throw new VideoException("No encoder for " + c.getCameraId());
				}else{
					url = server.getStreamURL(c);
				}
			}
			HttpDataSource src = new HttpDataSource(c, logger, monitor, url);
			return src;
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}
	}

	public static URL createURL(Client c, String baseUrl) throws VideoException {
		try{
			String s = 
				baseUrl +
				"?id=" + c.getCameraId() +
				"&size=" + c.getSize() +
				"&rate=" + c.getRate() +
				"&duration=" + c.getDuration() +
				"&user=" + c.getUser() +
				"&area=" + c.getArea() +
				"&ssid=" + c.getSonarSessionId();
			return new URL(s);
		}catch(Exception e){
			throw new VideoException(e.getMessage());
		}
	}

	public synchronized DataSource getDataSource(Client c)
			throws VideoException {
		if(c.getCameraId()==null) throw new VideoException(
				"Invalid camera: " + c.getCameraId());
		String name = c.getCameraId() + ":" + c.getSize();
		logger.fine("There are currently " + sourceTable.size() + " datasources.");
		HttpDataSource src = sourceTable.get(name);
		if(src != null){
			if(src.isAlive()){
				return src;
			}else{
				sourceTable.remove(name);
			}
		}
		src = (HttpDataSource)createDataSource(c);
		sourceTable.put(name, src);
		return src;
	}
}
