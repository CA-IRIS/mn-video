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
package us.mn.state.dot.video;

import java.sql.ResultSet;
import java.util.Properties;


public class TmsConnection extends DatabaseConnection {

	protected static final String CAMERA_ID = "name";
	protected static final String CAMERA_ENCODER = "encoder";
	protected static final String CAMERA_ENCODER_CHANNEL = "encoder_channel";
	protected static final String CAMERA_PUBLISH = "publish";
	protected static final String CAMERA_ENCODER_TYPE = "encoder_type";

	protected static final String TABLE_CAMERA = "camera_view";

	private static TmsConnection connection = null;
	
	public static synchronized TmsConnection create(final Properties p){
		if(connection == null){
			try{
				connection = new TmsConnection(p);
			}catch(Exception e){
				return null;
			}
		}
		return connection;
	}
	protected TmsConnection(Properties p){
		super(
			p.getProperty("tms.db.user"),
			p.getProperty("tms.db.pwd"),
			p.getProperty("tms.db.host"),
			Integer.parseInt(p.getProperty("tms.db.port")),
			p.getProperty("tms.db.name"));
	}

	public String getEncoderHost(String camId){
		String sql = "select " + CAMERA_ENCODER + " from " + TABLE_CAMERA +
			" where " + CAMERA_ID + " = '" + camId + "'";
		return getString(sql, CAMERA_ENCODER);
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

}
