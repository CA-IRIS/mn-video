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
import java.util.List;
import java.util.Properties;
import java.util.Set;

import us.mn.state.dot.util.db.TmsConnection;
import us.mn.state.dot.video.AxisServer;
import us.mn.state.dot.video.Camera;
import us.mn.state.dot.video.Encoder;
import us.mn.state.dot.video.Infinova;

/**
 * @author john3tim
 *
 * The EncoderFactory is responsible for creating Encoder objects and
 * making sure that they are in sync with the database.
 */
public class EncoderFactory {

	protected static final String INFINOVA = "Infinova";

	protected static final String AXIS     = "Axis";
	
	protected TmsConnection tms = null;

	protected String encoderUser = null;
	
	protected String encoderPass = null;

	/** Hashtable of all encoders indexed by camera id */
	protected final Hashtable<String, Encoder> encoders =
		new Hashtable<String, Encoder>();

	private static EncoderFactory factory = null;
	
	public synchronized static EncoderFactory getInstance(Properties p){
		if( factory != null ) return factory;
		factory = new EncoderFactory(p);
		return factory;
	}
	
	private EncoderFactory(Properties props){
		tms = TmsConnection.create(props);
		encoderUser = props.getProperty("video.encoder.user");
		encoderPass = props.getProperty("video.encoder.pwd");
		updateEncoders();
	}

	public Encoder getEncoder(String cameraId){
		return encoders.get(cameraId);
	}
	
	public int getEncoderCount(){
		return encoders.size();
	}

	/** Update the hashtable of encoders with information from the database */
	protected void updateEncoders() {
		encoders.clear();
		Hashtable<String,List> info = tms.getEncoderInfo();
		for(String name : info.keySet()){
			List l = info.get(name);
			createEncoder(name, l);
		}
	}
	
	
	protected void createEncoder(String name, List l){
		String host_port = (String)l.get(0);
		String host = host_port;
		if(host_port.indexOf(":")>-1){
			host = host_port.substring(0,host_port.indexOf(":"));
		}
		Encoder e = null;
		String mfr = (String)l.get(2);
		if(mfr != null && mfr.equalsIgnoreCase(INFINOVA)) e = Infinova.getServer(host);
		else e = AxisServer.getServer(host);
		if(host_port.indexOf(":")>-1){
			try{
				int port = Integer.parseInt(host_port.substring(host_port.indexOf(":")+1));
				e.setPort(port);
			}catch(NumberFormatException ex){
				//host port parsing error... use default http port
			}
		}
		for(String camId : tms.getCameraIdsByEncoder(host)){
			int ch = Integer.parseInt((String)l.get(1));
			String standardId = Camera.createStandardId(camId);
			e.setCamera(standardId, ch);
			e.setUsername(encoderUser);
			e.setPassword(encoderPass);
			encoders.put(standardId, e);
		}
	}
}
