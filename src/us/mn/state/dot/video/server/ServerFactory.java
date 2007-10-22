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

import java.util.Hashtable;

import us.mn.state.dot.util.db.TmsConnection;
import us.mn.state.dot.video.AxisServer;

/**
 * @author john3tim
 *
 * The ServerFactory is responsible for creating AxisServer objects and
 * making sure that they are in sync with the database.
 */
public class ServerFactory {

	protected TmsConnection tms = null;
	
	/** Hashtable of all axis servers indexed by camera id */
	protected final Hashtable<String, AxisServer> servers =
		new Hashtable<String, AxisServer>();

	public ServerFactory(TmsConnection tmsConn){
		tms = tmsConn;
		updateServers();
	}

	public AxisServer getServer(String cameraId){
		return servers.get(cameraId);
	}
	
	/** Update the hashtable of servers with information from the database */
	protected void updateServers() {
		servers.clear();
		for(String host_port : tms.getEncoderHosts()){
			createEncoder(host_port);
		}
	}
	
	protected void createEncoder(String host_port){
		String host = host_port;
		if(host_port.indexOf(":")>-1){
			host = host_port.substring(0,host_port.indexOf(":"));
		}
		AxisServer s = AxisServer.getServer(host);
		if(host_port.indexOf(":")>-1){
			try{
				int port = Integer.parseInt(host_port.substring(host_port.indexOf(":")+1));
				s.setPort(port);
			}catch(NumberFormatException e){
				//host port parsing error... use default http port
			}
		}
		for(String camId : tms.getCameraIdsByEncoder(host)){
			int ch = tms.getEncoderChannel(camId);
			s.setCamera(camId, ch);
			servers.put(camId, s);
		}
	}
}
