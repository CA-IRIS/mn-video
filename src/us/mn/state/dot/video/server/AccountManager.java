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


import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * @author John3Tim
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AccountManager extends HttpServlet{

	private static final String ERROR_MSG =
		"The server experienced an error processing your request.<br/>" +
		"Please try again later or contact the site administrator.";

	/** Database connection driver */
	private String driver = "jdbc:postgresql";

	protected Properties props = new Properties();
	
	protected String appHome = null;

	protected int minPwdLength = 4;
	
	protected Logger logger = null;
	
	protected String appName = null;
	
	protected static MessageDigest SHA_DIGEST;

	public void init(ServletConfig cfg) throws ServletException{
		super.init(cfg);
		logger = (Logger)cfg.getServletContext().getAttribute("logger");
		props = (Properties)getServletContext().getAttribute("properties");
		if(props==null){
			logger.warning("Unable to load properties from servlet context.");
		}
		appHome = props.getProperty(PropertiesContext.PROP_APP_HOME);
		appName = props.getProperty(PropertiesContext.PROP_APP_NAME);
		props.setProperty("file.resource.loader.path", appHome);
		minPwdLength = Integer.parseInt(props.getProperty("minPwdLength"));
		try{
			MessageDigest.getInstance("SHA");
		}catch(Exception e){}
	}
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response)
	{
		String action = request.getServletPath().toLowerCase();
		if(action.indexOf("passwordform")>0){
			passwordForm(request, response);
		}else{
			processError(request, response, "Invalid servlet request.");
		}
	}

	private void passwordForm(HttpServletRequest request,
			HttpServletResponse response)
	{
		try{
			response.setHeader("text", "html");
			OutputStream out = response.getOutputStream();
			Velocity.init(props);
			Template template = Velocity.getTemplate(
					appName + "/client/changepass.vm");
			VelocityContext context = new VelocityContext();
	        String userName = request.getUserPrincipal().getName();
			context.put("userName", userName);
			context.put("minPwdLength", Integer.toString(minPwdLength));
			Writer writer = new OutputStreamWriter( out );
			if ( template != null ) {
				template.merge( context, writer );
				writer.flush();
				writer.close();
			}
		}catch(Exception e){
			logger.warning(e.toString());
			e.printStackTrace();
		}
	}
	
	protected void doPost( HttpServletRequest request,
			HttpServletResponse response )
	{
		String action = request.getServletPath().toLowerCase();
		if(action.indexOf("changepassword")>0){
			if(!validatePwd(request)){
				processError(request, response,
					"Passwords do not match or are shorter than " +
					minPwdLength + " characters.");
			}else{
				if(changePassword(request, response)){
					processSuccess(request, response,
						"Your password has been updated.");
				}else{
					processError(request, response, ERROR_MSG);
				}
			}
		}else if(action.indexOf("requestaccount")>0){
			if(requestAccount(request, response)){
				processSuccess(request, response,
						"Your request for an account has been processed.<br/>" +
						"Your will be notified when your account has been activated.");
			}else{
				processError(request, response, ERROR_MSG);
			}
		}else{
			processError(request, response,
				"Invalid servlet request.");
		}
	}

	private boolean validatePwd(HttpServletRequest request){
		String pwd = request.getParameter("pwd");
		if(pwd == null || pwd.length()<minPwdLength){
			return false;
		}
		String confirm = request.getParameter("confirm");
		if(pwd.equals(confirm)){
			return true;
		}
		return false;
	}

	private void processError(HttpServletRequest request,
			HttpServletResponse response, String msg)
	{
		try{
			response.setHeader("text", "html");
			OutputStream out = response.getOutputStream();
			Velocity.init(props);
			Template template = Velocity.getTemplate(
					appName + "/client/error.vm");
			VelocityContext context = new VelocityContext();
			context.put("msg", msg);
			Writer writer = new OutputStreamWriter( out );
			if ( template != null ) {
				template.merge( context, writer );
				writer.flush();
				writer.close();
			}
		}catch(Exception e){
			logger.warning(e.toString());
			e.printStackTrace();
		}
	}

	private void processSuccess(HttpServletRequest request,
			HttpServletResponse response, String msg)
	{
		try{
			response.setHeader("text", "html");
			OutputStream out = response.getOutputStream();
			Velocity.init(props);
			Template template = Velocity.getTemplate(
					appName + "/client/success.vm");
			VelocityContext context = new VelocityContext();
			context.put("msg", msg);
			Writer writer = new OutputStreamWriter(out);
			if(template != null){
				template.merge( context, writer );
				writer.flush();
				writer.close();
			}
		}catch(Exception e){
			logger.warning(e.toString());
			e.printStackTrace();
		}
	}
	
	private boolean changePassword(HttpServletRequest request,
			HttpServletResponse response)
	{
		Connection c = null;
		try{
			String pwdHash = getSHAHash(request.getParameter("pwd"));
			String id = request.getUserPrincipal().getName();
			c = getDBConnection();
			if(c != null){
				Statement statement = c.createStatement();
				int result = statement.executeUpdate(
						"update tomcat_user set password = '" +
						pwdHash + "' " + "where username = '" + id + "'");
				if(result>0){
					return true;
				}
			}
		}catch(Exception e){
			logger.warning(e.toString());
			e.printStackTrace();
		}finally{
			try{
				c.close();
			}catch(Exception e2){
			}
		}
		return false;
	}

	private String getSHAHash(String s){
		byte[] pwdBytes = s.getBytes();
		return SHA_DIGEST.digest(pwdBytes).toString();
	}
	
	private boolean requestAccount(HttpServletRequest request,
			HttpServletResponse response)
	{
		Connection c = null;
		try{
			String pwd = request.getParameter("password");
			pwd = clean(pwd);
			String pwdHash = getSHAHash(pwd);
			c = getDBConnection();
			Statement statement = c.createStatement();
			int result = statement.executeUpdate(
				"insert into tomcat_user " +
				"(username, password, f_name, l_name, email, phone, company, processed) " +
				"values " +
				"('" + clean(request.getParameter("userName")) + "', " +
				"'" + pwdHash + "', " +
				"'" + clean(request.getParameter("fName")) + "', " +
				"'" + clean(request.getParameter("lName")) + "', " +
				"'" + clean(request.getParameter("email")) + "', " +
				"'" + clean(request.getParameter("phone")) + "', " +
				"'" + clean(request.getParameter("company")) + "', 'f' )" );
			if(result>0){
				return true;
			}
		}catch(Exception e){
			logger.warning(e.toString());
			e.printStackTrace();
		}finally{
			try{
				c.close();
			}catch(Exception e2){
			}
		}
		return false;
	}

	/*
	 * Remove any characters from a string that could allow
	 * a user to inject SQL into their responses.
	 */
	private String clean(String s){
		s = s.replaceAll(";", "");
		s = s.replaceAll("'", "");
		return s;
	}
	
	private Connection getDBConnection(){
		try {
			Class.forName( "org.postgresql.Driver" );
			String db = "video";
			String user = "tomcat";;
			String pwd = "tomcat";
			return DriverManager.getConnection( driver + "://" +
				"localhost:5432/" + db, user, pwd );
		}catch(Exception e){
			logger.warning(e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
}
