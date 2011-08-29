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
import java.util.Properties;

import us.mn.state.dot.util.db.TmsConnection;
import us.mn.state.dot.video.AxisServer;
import us.mn.state.dot.video.Camera;
import us.mn.state.dot.video.Encoder;

/**
 * @author john3tim
 *
 * The ServerFactory is responsible for creating Encoder objects and
 * making sure that they are in sync with the database.
 */
public class EncoderFactory {

	protected TmsConnection tms = null;

	protected String encoderUser = null;
	
	protected String encoderPass = null;

	/** Hashtable of all encoders indexed by camera id */
	protected final Hashtable<String, Encoder> encoders =
		new Hashtable<String, Encoder>();

	public EncoderFactory(Properties props){
		tms = new TmsConnection(props);
		encoderUser = props.getProperty("video.encoder.user");
		encoderPass = props.getProperty("video.encoder.pwd");
		updateEncoders();
	}

	public Encoder getEncoder(String cameraId){
		return encoders.get(cameraId);
	}
	
	/** Update the hashtable of encoders with information from the database */
	protected void updateEncoders() {
		encoders.clear();
		//Fixme: this is just to get things to build, need to get protocol from db. 
		String protocol = "Axis MJPG";
		for(String host_port : tms.getEncoderHosts()){
			createEncoder(host_port, protocol);
		}
	}

	protected void createEncoder(String host_port, String protocol){
		String host = host_port;
		if(host_port.indexOf(":")>-1){
			host = host_port.substring(0,host_port.indexOf(":"));
		}
		Encoder enc = null;
		if(protocol.toLowerCase().startsWith("Axis")){
			enc = new AxisEncoder(host);
		}else if(protocol.toLowerCase().startsWith("Infinova")){
			enc = new InfinovaEncoder(host);
		}
		if(host_port.indexOf(":")>-1){
			try{
				int port = Integer.parseInt(host_port.substring(host_port.indexOf(":")+1));
				enc.setPort(port);
			}catch(NumberFormatException nfe){
				//host port parsing error... use default http port
			}
		}
		for(String camId : tms.getCameraIdsByEncoder(host)){
			int ch = tms.getEncoderChannel(camId);
			String standardId = Camera.createStandardId(camId);
			enc.setCamera(standardId, ch);
			enc.setUsername(encoderUser);
			enc.setPassword(encoderPass);
			encoders.put(standardId, enc);
		}
	}
}
