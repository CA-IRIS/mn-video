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

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import us.mn.state.dot.video.Axis;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.Constants;
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

	/** The expiration time of database information, in milliseconds */
	protected long dbExpire = 10 * 1000;

	/** The time, in milliseconds, of the last database update */
	protected long dbTime = 0;
	
	protected Properties properties = null;

	protected Logger logger = null;
	
	/** Hashtable of encoders indexed by camera name */
	protected final Hashtable<String, Encoder> encoders =
		new Hashtable<String, Encoder>();

	private static EncoderFactory factory = null;
	
	public synchronized static EncoderFactory getInstance(Properties p){
		if( factory != null ) return factory;
		factory = new EncoderFactory(p);
		return factory;
	}
	
	public String getUser(){
		return encoderUser;
	}
	
	public String getPassword(){
		return encoderPass;
	}
	
	private EncoderFactory(Properties props){
		this.logger = Logger.getLogger(Constants.LOGGER_NAME);
		this.properties = props;
		tms = TmsConnection.create(props);
		encoderUser = props.getProperty("video.encoder.user");
		encoderPass = props.getProperty("video.encoder.pwd");
		try{
			dbExpire = Long.parseLong(props.getProperty("db.expire"));
		}catch(Exception e){
			//do nothing, use the default database expiration
		}
	}

	private boolean dbExpired(){
		logger.info("Checking if database is expired.");
		long now = Calendar.getInstance().getTimeInMillis();
		return (now - dbTime) > dbExpire;
	}

	private synchronized void updateEncoders(){
		logger.info("Updating encoder information.");
		for(String key : encoders.keySet()){
			logger.info("Updating encoder for " + key);
			createEncoder(key);
		}
		dbTime = Calendar.getInstance().getTimeInMillis();
	}
	
	public Encoder getEncoder(String cameraId){
		if(dbExpired()) updateEncoders();
		Encoder e = encoders.get(cameraId);
		if(e != null) return e;
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
		String mfr = tms.getEncoderType(name);
		String host = tms.getEncoderHost(name);
		if(mfr == null || host == null) return null;
		logger.info("Creating new encoder for camera " + name);
		Encoder e = null;
		if(mfr.indexOf(INFINOVA) > -1){
			e = new Infinova(host);
		}else{
			e = new Axis(host);
		}
		int ch = tms.getEncoderChannel(name);
		String standardId = Client.createStandardId(name);
		e.setCamera(standardId, ch);
		e.setUsername(encoderUser);
		e.setPassword(encoderPass);
		encoders.put(name, e);
		logger.info(name + " " + e);
		logger.info(encoders.size() + " encoders.");
		return e;
	}
	
	public boolean isPublished(String cameraId){
		return tms.isPublished(cameraId);
	}
}
