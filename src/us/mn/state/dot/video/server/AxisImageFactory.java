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


import java.util.logging.Logger;

import us.mn.state.dot.video.AbstractImageFactory;
import us.mn.state.dot.video.AxisServer;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.ThreadMonitor;
import us.mn.state.dot.video.VideoException;
import us.mn.state.dot.video.VideoStream;
/**
 * The ImageFactory interacts with an axis server to continually produce images
 * until there are no more requests for images.
 *
 * @author Timothy Johnson
 */
public class AxisImageFactory extends AbstractImageFactory{

	protected final AxisServer server;

	/** Constructor for the AxisImageFactory. */
	public AxisImageFactory(Client c, AxisServer s) {
		super(c, null, null);
		server = s;
		start();
	}

	/** Constructor for the AxisImageFactory. */
	public AxisImageFactory(Client c, Logger l, ThreadMonitor m, AxisServer s) {
		super(c, l, m);
		server = s;
		start();
	}

	/** Start the stream. */
	public void run() {
		if(server != null){
			try{
				VideoStream stream = server.getStream(getClient());
				logger.info("Opened factory " + this);
				byte[] img;
				while(!done && this.isAlive()){
					if(stream==null) break;
					img = stream.getImage();
					if(img != null){
						imageCreated(img);
					}else{
						break;
					}
				}
			}catch(VideoException ve){
				logger.info(ve.getMessage());
			}finally{
				removeListeners();
			}
		}
	}

}
