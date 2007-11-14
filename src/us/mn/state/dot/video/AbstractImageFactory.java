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
 * The ImageFactory connects to a video stream from the stream server.
 * It notifies each of it's listeners whenever there is a new image.
 *
 * @author Timothy Johnson
 */
public abstract class AbstractImageFactory extends VideoThread {

	/** List of registered listeners to this stream. */
	private ArrayList<ImageFactoryListener> listeners =
		new ArrayList<ImageFactoryListener>();

	/** A byte array used to store the image.*/
	private byte[] image;

	protected final Logger logger;
	
	protected final Client client;
	
	/** Constructor for the ImageFactory. */
	protected AbstractImageFactory(Client c,
			Logger l, ThreadMonitor m) {
		super(m);
		client = c;
		logger = l==null ? TmsLogFactory.createLogger("video"): l;
	}

	/** Get the string representation of this factory */
	public final String toString() {
		if(client==null){
			return "Uninitialized image factory.";
		}
		return "ImageFactory: " +
			Constants.DATE_FORMAT.format(getStartTime().getTime()) + 
			" " + client.getCameraId() + " " +
			"size " + client.getSize();
	}

	public final String getStatus(){
		return listeners.size() + " listeners.";
	}

	public final ImageFactoryListener[] getListeners(){
		return (ImageFactoryListener[])listeners.toArray(new ImageFactoryListener[0]);
	}
	
	/** Notify listeners that an image was created */
	protected final void imageCreated(byte[] img) {
		image = img;
		for(int i=0; i<listeners.size(); i++) {
			ImageFactoryListener l = (ImageFactoryListener)listeners.get(i);
			logger.fine(this.getClass().getSimpleName() +
					" is Notifying " + l.toString() +
					": image size is " + image.length);
			l.imageCreated(img);
		}
	}

	/** Add a listener to this Image Factory. */
	public synchronized final void addImageFactoryListener(ImageFactoryListener l) {
		if(l != null){
			logger.info("Adding ImageFactoryListener: " + l.toString());
			listeners.add(l);
		}
	}

	/** Remove a listener from this Image Factory. */
	public synchronized final void removeImageFactoryListener(ImageFactoryListener l) {
		logger.info("Removing ImageFactoryListener: " + l.toString());
		listeners.remove(l);
		if(listeners.size()==0){
			logger.info(this.toString() + " has no listeners, stopping now.");
			halt();
		}
	}

	protected synchronized void removeListeners(){
		for(ImageFactoryListener l : listeners){
			removeImageFactoryListener(l);
		}
	}
	
	public final Client getClient() {
		return client;
	}

	public byte[] getImage(){
		return image;
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
