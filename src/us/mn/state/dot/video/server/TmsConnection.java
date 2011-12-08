/*
 * Video project
 * Copyright (C) 2011  Minnesota Department of Transportation
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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


public class TmsConnection extends DatabaseConnection {

	protected static final String CAMERA_ID = "name";
	protected static final String CAMERA_ENCODER = "encoder";
	protected static final String CAMERA_ENCODER_CHANNEL = "encoder_channel";
	protected static final String CAMERA_PUBLISH = "publish";
	protected static final String CAMERA_ENCODER_TYPE = "encoder_type";
	
	protected static final String TABLE_CAMERA = "camera_view";

	protected static final String F_CROSS_STREET = "cross_street";
	protected static final String F_CROSS_DIR = "cross_dir";
	protected static final String F_CROSS_MOD = "cross_mod";
	protected static final String F_ROADWAY = "roadway";
	protected static final String F_ROADWAY_DIR = "road_dir";
	protected static final String F_CAMERA_ID = "name";
	
	private static final Hashtable<String, TmsConnection> connections =
		new Hashtable<String, TmsConnection>();
	
	public static TmsConnection create(final Properties p){
		try{
			String key = p.getProperty("tms.db.user") +
				p.getProperty("tms.db.host") +
				p.getProperty("tms.db.name");
			TmsConnection c = connections.get(key);
			if(c != null) return c;
			c = new TmsConnection(p);
			connections.put(key, c);
			return c;
		}catch(Exception e){
			return null;
		}
	}
	protected TmsConnection(Properties p){
		super(
			DatabaseConnection.TYPE_POSTGRES,
			p.getProperty("tms.db.user"),
			p.getProperty("tms.db.pwd"),
			p.getProperty("tms.db.host"),
			Integer.parseInt(p.getProperty("tms.db.port")),
			p.getProperty("tms.db.name"));
	}

	protected String createId(int camNumber){
		String id = Integer.toString(camNumber);
		while(id.length()<4) id = "0" + id;
		return "C" + id;
	}

	public String getEncoderHost(String camId){
		String sql = "select " + CAMERA_ENCODER + " from " + TABLE_CAMERA +
			" where " + CAMERA_ID + " = '" + camId + "'";
		return getString(sql, CAMERA_ENCODER);
	}

	/**
	 * Get an array of encoder hostnames for all cameras.
	 */
	public ArrayList<String> getEncoderHosts(){
		String sql = "select distinct " + CAMERA_ENCODER + " from " + TABLE_CAMERA +
			" where " + CAMERA_ENCODER + " is not null";
		return getColumnList(sql, CAMERA_ENCODER);
	}
	
	/**
	 * Get an array of camera ids for the given encoder ip address.
	 * @param host The hostname of the encoder.
	 * @return An array camera ids.
	 */
	public ArrayList<String> getCameraIdsByEncoder(String ip){
		String sql = "select " + CAMERA_ID + " from " + TABLE_CAMERA +
			" where " + CAMERA_ENCODER + " like '" + ip + ":%'";
		return getColumnList(sql, CAMERA_ID);
	}

	public String getEncoderType(String name){
		String q = "select " + CAMERA_ENCODER_TYPE + " from " + TABLE_CAMERA +
			" where " + CAMERA_ID + " = '" + name + "'";
		try{
			ResultSet rs = query(q);
			if(rs != null && rs.next()) return rs.getString(CAMERA_ENCODER_TYPE);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public int getEncoderChannel(String camId){
		String q = "select " + CAMERA_ENCODER_CHANNEL + " from " + TABLE_CAMERA +
			" where " + CAMERA_ID + " = '" + camId + "'";
		try{
			ResultSet rs = query(q);
			if(rs != null && rs.next()) return rs.getInt(CAMERA_ENCODER_CHANNEL);
		}catch(Exception e){
			e.printStackTrace();
		}
		return -1;
	}

	public Hashtable<String,List> getEncoderInfo(){
		String q = "select " + CAMERA_ID + "," +
		CAMERA_ENCODER + "," + CAMERA_ENCODER_CHANNEL + "," + CAMERA_ENCODER_TYPE +
		" from " + TABLE_CAMERA +
		" where " + CAMERA_ENCODER + " is not null";
		Hashtable encoderData = new Hashtable<String,List>();
		try{
			ResultSet rs = query(q);
			while(rs != null && rs.next()){
				List l = new ArrayList();
				l.add(rs.getString(CAMERA_ENCODER));
				l.add(rs.getString(CAMERA_ENCODER_CHANNEL));
				l.add(rs.getString(CAMERA_ENCODER_TYPE));
				encoderData.put(rs.getString(CAMERA_ID), l);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return encoderData;
	}

	/** Get the publish attribute of the camera */
	public boolean isPublished(String camId){
		String q = "select " + CAMERA_PUBLISH + " from " + TABLE_CAMERA +
			" where " + CAMERA_ID + " = '" + camId + "'";
		try{
			ResultSet rs = query(q);
			if(rs != null && rs.next()){
				return rs.getBoolean(CAMERA_PUBLISH);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	/** Get the location of a camera */
	public String getLocation(String name){
		String q = "select " + F_ROADWAY + ", " + F_ROADWAY_DIR + ", " +
			F_CROSS_STREET + ", " + F_CROSS_DIR + ", " + F_CROSS_MOD +
			" from " + TABLE_CAMERA + " where " + F_CAMERA_ID + " = '" + name + "'";
		String loc = "";
		try{
			ResultSet rs = query(q);
			if(rs != null && rs.next()){
				loc = loc.concat(rs.getString(F_ROADWAY));
				loc = loc.concat(" " + rs.getString(F_ROADWAY_DIR));
				loc = loc.concat(" " + rs.getString(F_CROSS_MOD));
				loc = loc.concat(" " + rs.getString(F_CROSS_STREET));
				loc = loc.concat(" " + rs.getString(F_CROSS_DIR));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return loc;
	}

	/** Get a list of camera names. */
	public List<String> getCameraNames(){
		String sql = "select " + F_CAMERA_ID + " from " + TABLE_CAMERA +
			" order by " + F_CAMERA_ID;
		return getColumnList(sql, F_CAMERA_ID);
	}
}
