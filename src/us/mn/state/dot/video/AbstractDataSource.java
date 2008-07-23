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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import us.mn.state.dot.log.TmsLogFactory;

/**
 * An abstract implementation of a DataSource.
 *
 * @author Timothy Johnson
 */
public abstract class AbstractDataSource extends VideoThread implements DataSource {

	/** List of DataSinks for this stream. */
	private ArrayList<DataSink> sinks =
		new ArrayList<DataSink>();

	protected final Logger logger;
	
	protected final Client client;
	
	/** Constructor for the ImageFactory. */
	protected AbstractDataSource(Client c,
			Logger l, ThreadMonitor m) {
		super(m);
		client = c;
		logger = l==null ? TmsLogFactory.createLogger("video"): l;
	}

	/** Get the string representation of this factory */
	public final String toString() {
		if(client==null){
			return "Uninitialized DataSource";
		}
		return "DataSource for" +
			" " + client.getCameraId() + " " +
			"size " + client.getSize();
	}

	public final String getStatus(){
		return sinks.size() + " listeners.";
	}

	public final synchronized DataSink[] getListeners(){
		return (DataSink[])sinks.toArray(new DataSink[0]);
	}
	
	/** Notify listeners that an image was created */
	protected final synchronized void notifySinks(byte[] data) {
		//take a copy of the sinks to avoid concurrent modification exception
		// if the sink tries to remove itself during the flush call
		DataSink[] tempSinks = sinks.toArray(new DataSink[0]);
		for(DataSink sink : tempSinks) {
			logger.fine(this.getClass().getSimpleName() +
					" is Notifying " + sink.toString() +
					": image size is " + data.length);
			sink.flush(data);
		}
	}

	/** Add a DataSink to this Image Factory. */
	public synchronized final void connectSink(DataSink sink) {
		if(sink != null){
			logger.info("Adding DataSink: " + sink.toString());
			sinks.add(sink);
		}
	}

	/** Remove a DataSink from this DataSource. */
	public synchronized final void disconnectSink(DataSink sink) {
		logger.info("Removing DataSink: " + sink.getClass().getSimpleName());
		sinks.remove(sink);
		if(sinks.size()==0){
			logger.info(this.toString() + " has no sinks, stopping now.");
			halt();
		}
	}

	protected synchronized void removeSinks(){
		for(DataSink sink : sinks){
			disconnectSink(sink);
		}
	}
	
	public final Client getClient() {
		return client;
	}

	/** Create an array of baseUrls for connecting to the backend
     *  server.
     * @param p
     * @param type Stream (1) or Still (2)
     * @return
     */
    public static String[] createBackendUrls(Properties p, int type){
	    ArrayList<String> baseUrls = new ArrayList<String>();
	    int id = 0;
	    while(true){
	    	String ip = p.getProperty("video.backend.host" + id);
	    	if(ip==null) break;
    		try{
    			ip = InetAddress.getByName(ip).getHostAddress();
    		}catch(UnknownHostException uhe){
    			System.out.println("Invalid backend server " + id +
    					" " + uhe.getMessage());
    			break;
    		}
    		String port = p.getProperty("video.backend.port" + id,
    				p.getProperty("video.backend.port" + 0));
    		String servletName = "";
    		if(type==1) servletName = "stream";
    		if(type==2) servletName = "image";
    		baseUrls.add(
				"http://" + ip + ":" + port +
				"/@@NAME@@/" + servletName);
    		id++;
	    }
	    System.out.println("Video server backend URLs:");
	    for(int i=0; i<baseUrls.size(); i++){
	    	System.out.println("\t" + baseUrls.get(i));
	    }
	    return (String[])baseUrls.toArray(new String[0]);
    }
}
