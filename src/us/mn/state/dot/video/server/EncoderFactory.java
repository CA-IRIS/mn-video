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
import java.util.logging.Logger;

import us.mn.state.dot.util.db.TmsConnection;
import us.mn.state.dot.video.Axis;
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
	
	protected Properties properties = null;

	protected Logger logger = null;
	
	/** Hashtable of all encoders indexed by host */
	protected final Hashtable<String, Encoder> encoders =
		new Hashtable<String, Encoder>();

	private static EncoderFactory factory = null;
	
	public synchronized static EncoderFactory getInstance(Properties p, Logger l){
		if( factory != null ) return factory;
		factory = new EncoderFactory(p, l);
		return factory;
	}
	
	private EncoderFactory(Properties props, Logger l){
		this.logger = l;
		this.properties = props;
		tms = TmsConnection.create(props);
		encoderUser = props.getProperty("video.encoder.user");
		encoderPass = props.getProperty("video.encoder.pwd");
//		updateEncoders();
	}

	public Encoder getEncoder(String cameraId){
		try{
			return createEncoder(cameraId);
		}catch(Throwable th){
			if(logger != null){
				logger.warning("Error creating encoder for camera " +
						cameraId + ": " + th.getMessage());
			}
			tms = TmsConnection.create(properties);
			return null;
		}
	}
	
	protected Encoder createEncoder(String name){
		if(name == null) return null;
		if(!tms.isPublished(name)){
			logger.fine("camera " + name + " is not published.");
			return new Axis(null);
		}
		String host_port = tms.getEncoderHost(name);
		String host = host_port;
		if(host_port.indexOf(":")>-1){
			host = host_port.substring(0,host_port.indexOf(":"));
		}
		Encoder e = encoders.get(host);
		logger.fine("Re-using encoder for camera " + name);
		if(e == null){
			logger.fine("Creating new encoder for camera " + name);
			String mfr = tms.getEncoderType(name);
			if(mfr != null && mfr.indexOf(INFINOVA) > -1){
				e = new Infinova(host);
			}else{
				e = new Axis(host);
			}
		}
		if(host_port.indexOf(":")>-1){
			try{
				int port = Integer.parseInt(host_port.substring(host_port.indexOf(":")+1));
				e.setPort(port);
			}catch(NumberFormatException ex){
				//host port parsing error... use default http port
			}
		}
		int ch = tms.getEncoderChannel(name);
		String standardId = Camera.createStandardId(name);
		e.setCamera(standardId, ch);
		e.setUsername(encoderUser);
		e.setPassword(encoderPass);
		encoders.put(host, e);
		logger.fine(name + " " + e);
		logger.fine(encoders.size() + " encoders.");
		return e;
	}
}
