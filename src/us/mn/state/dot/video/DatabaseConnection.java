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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


/**
 * DatabaseConnection is a convenience class for making a connection to
 * a database.  It handles all of the queries and sql exceptions as
 * well as re-establishing the connection if it is lost.
 * 
 * @author Timothy Johnson
 *
 */
public class DatabaseConnection {

	protected static final String ASCENDING = "asc";
	
	protected static final String DESCENDING = "desc";
	
	/** Username for authentication to the db server */
	private String user = null;

	/** The name of the database to connect to */
	private String dbName = null;

	/** Password for authentication to the db server */
	private String password = null;

	/** Database URL */
	private String url = null;

	/** The connection object used for executing queries */
	protected Connection connection = null;
	
	protected Statement statement = null;
	
	/** Constructor for the DatabaseConnection class.
	 * 
	 * @param user The username for connections.
	 * @param pwd The user password.
	 * @param host Host name or ip.
	 * @param port Port on which to connect.
	 * @param dbName The name of the database.
	 */
	public DatabaseConnection(
			String user, String pwd, String host, int port, String dbName) {
		this.user = user;
		this.dbName = dbName;
		this.password = pwd;
		String port_name_separator = "/";
		url = "jdbc:postgresql://" + host + ":" + port + port_name_separator + dbName;
	}

	private void connect(){
		try {
			Class.forName( "org.postgresql.Driver" );
			System.out.println( "Openning connection to " + dbName + " database." );
			connection = DriverManager.getConnection( url, user, password );
			DatabaseMetaData md = connection.getMetaData();
			String dbVersion = md.getDatabaseProductName() + ":" + md.getDatabaseProductVersion();
			System.out.println("DB: " + dbVersion);
			statement = connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			System.out.println( "Opened connection to " + dbName + " database." );
		} catch ( Exception e ) {
			System.err.println("Error connecting to DB: " + url + " USER: " + user + " PWD: " + password );
		}
	}

	private boolean connected(){
		if(connection == null) connect();
		try{
			connection.isClosed();
			return true;
		}catch(Exception e){
			try{
				connect();
				connection.isClosed();
				return true;
			}catch(Exception e2){
				System.err.println("Unable to connect to DB.");
				return false;
			}
		}
		
	}
	
	public ResultSet query( String sql ){
		try{
			return statement.executeQuery(sql);
		}catch(Exception e){
			try{
				System.err.println("Unable to execute DB query. Reconnecting...");
				connect();
				return statement.executeQuery(sql);
			}catch(Exception e2){
				System.err.println("Reconnection to DB failed: " + e2.getMessage());
			}
		}
		return null;
	}

	protected final String getString(String sql, String column){
		try{
			ResultSet rs = query(sql);
			if(rs == null) throw new Exception("Null resultset");
			if(rs.next()) return rs.getString(column);
		}catch(Exception e){
			System.err.println("Error retrieving DB column " + column +
					": " + e.getMessage());
		}
		return null; 
	}

	protected ArrayList<String> getColumnList(String sql, String column){
		ArrayList<String> list = new ArrayList<String>();
		try{
			ResultSet rs = query(sql);
			if(rs == null) throw new Exception("Null resultset");
			while(rs != null && rs.next()){
				list.add(rs.getString(column));
			}
		}catch(Exception e){
			System.err.println("Error retrieving DB column " + column +
					": " + e.getMessage());
		}
		return list;
	}
}