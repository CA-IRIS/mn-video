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
	
	public static final int TYPE_CONTROLLER = 1;
	public static final int TYPE_COMM_LINK = 2;
	public static final int TYPE_CAMERA = 3;
	public static final int TYPE_DETECTOR = 4;
	public static final int TYPE_LCS = 5;
	public static final int TYPE_DMS = 6;
	public static final int TYPE_METER = 7;
	public static final int TYPE_ROADWAY = 8;
	
	protected static final String TABLE_CAMERA = "camera_view";
	protected static final String TABLE_DMS = "dms_view";
	protected static final String TABLE_METER = "ramp_meter_view";
	protected static final String TABLE_DETECTOR = "detector_view";
	protected static final String TABLE_COMMLINK = "comm_link_view";
	protected static final String TABLE_CONTROLLER = "controller_loc_view";

	protected static final String F_CROSS_STREET = "cross_street";
	protected static final String F_CROSS_DIR = "cross_dir";
	protected static final String F_CROSS_MOD = "cross_mod";
	protected static final String F_ROADWAY = "roadway";
	protected static final String F_ROADWAY_DIR = "road_dir";

	protected static final String F_DMS_ID = "name";
	protected static final String F_CAMERA_ID = "name";
	protected static final String F_METER_ID = "name";
	protected static final String F_DETECTOR_ID = "det_id";
	protected static final String F_COMMLINK_ID = "name";
	protected static final String F_COMMLINK_URL = "url";
	protected static final String F_CONTROLLER_ID = "name";
	
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

	/** Get the location of a device */
	public String getLocation(int type, String deviceName){
		String table = null;
		String idField = null;
		switch (type) {
			case TYPE_DMS:
				table = TABLE_DMS;
				idField = F_DMS_ID;
				break;
			case TYPE_CAMERA:
				table = TABLE_CAMERA;
				idField = F_CAMERA_ID;
				break;
			case TYPE_DETECTOR:
				table = TABLE_DETECTOR;
				idField = F_DETECTOR_ID;
				break;
			case TYPE_METER:
				table = TABLE_METER;
				idField = F_METER_ID;
				break;
			case TYPE_CONTROLLER:
				table = TABLE_CONTROLLER;
				idField = F_CONTROLLER_ID;
				break;
			default:
				break;
		}
		if(table == null || idField == null) return "";
		String q = "select " + F_ROADWAY + ", " + F_ROADWAY_DIR + ", " +
			F_CROSS_STREET + ", " + F_CROSS_DIR + ", " + F_CROSS_MOD +
			" from " + table + " where " + idField + " = '" + deviceName + "'";
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

	/** Get a list of names for a given device type
	 */
	public ArrayList<String> getNames(int type){
		switch(type){
			case(TYPE_CONTROLLER):
				return getControllerNames();
			case(TYPE_COMM_LINK):
				return getCommLinkNames();
			case(TYPE_CAMERA):
				return getCameraNames();
			case(TYPE_DETECTOR):
				return getDetectorNames();
			case(TYPE_DMS):
				return getDMSNames();
			case(TYPE_LCS):
				return getLCSNames();
			case(TYPE_METER):
				return getMeterNames();
			case(TYPE_ROADWAY):
				return getRoadwayNames();
		}
		return new ArrayList<String>();
	}
	
	private ArrayList<String> getControllerNames(){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select comm_link, drop_id " +
				"from controller_loc_view ";
		ResultSet set = query(sql);
		if(set == null) return list;
		try{
			set.beforeFirst();
			while(set.next()){
				list.add(set.getString("comm_link") + "D" + set.getString("drop_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return list;
	}
	
	/** Get a list of comm_link names. */
	private ArrayList<String> getCommLinkNames(){
		String sql = "select name from comm_link";
		return getColumnList(sql, "name");
	}

	/** Get a list of meter names. */
	private ArrayList<String> getMeterNames(){
		String sql = "select id from ramp_meter_view";
		return getColumnList(sql, "id");
	}

	/** Get a list of detector names. */
	private ArrayList<String> getDetectorNames(){
		String sql = "select det_id from detector_view";
		return getColumnList(sql, "det_id");
	}

	/** Get a list of DMS names. */
	private ArrayList<String> getDMSNames(){
		String sql = "select id from dms_view";
		return getColumnList(sql, "id");
	}

	/** Get a list of camera names. */
	private ArrayList<String> getCameraNames(){
		String sql = "select name from camera_view order by name";
		return getColumnList(sql, "name");
	}

	/** Get a list of LCS names. */
	private ArrayList<String> getLCSNames(){
		String sql = "select id from lcs";
		return getColumnList(sql, "id");
	}

	/** Get a list of Roadway names. */
	private ArrayList<String> getRoadwayNames(){
		String sql = "select name from road_view order by name";
		return getColumnList(sql, "name");
	}

	/** Get the comm_link name for the given URL */
	public String getCommLink(String url){
		String sql = "select " + F_COMMLINK_ID + " from " + TABLE_COMMLINK +
			" where " + F_COMMLINK_URL + " = '" + url + "'";
		return getString(sql, F_COMMLINK_ID);
	}

	/** Get the url for the given comm_link name */
	public String getURL(String name){
		String sql = "select " + F_COMMLINK_URL + " from " + TABLE_COMMLINK +
			" where " + F_COMMLINK_ID + " = '" + name + "'";
		return getString(sql, F_COMMLINK_URL);
	}
}
