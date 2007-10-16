/*
* Video
* Copyright (C) 2007  Minnesota Department of Transportation
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
* Foundation, Inc., 59 temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package us.mn.state.dot.video.dev;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Properties;

import us.mn.state.dot.util.HTTPProxySelector;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.Constants;
import us.mn.state.dot.video.VideoClip;

public class TestDownload {

	public TestDownload(){
		Properties p = new Properties();
		ProxySelector.setDefault(new HTTPProxySelector(p));
		String cal = "2007-09-27_15:00:00";
		int dur = 20;
		int cam = 630;
		saveClip(cal, cam, dur);
	}
	
	protected void saveClip(String cal, int cam, int dur){
		VideoClip clip = new VideoClip();
    	URLConnection con = null;
    	FileOutputStream out = null;
    	try{
    		Calendar start = Calendar.getInstance();
    		start.setTime(Constants.DATE_FORMAT.parse(cal));
    		clip.setStart(start);
    		clip.setDuration(dur);
    		clip.setCamera(cam);
			String home = System.getProperty("user.home");
			File f = new File(home, clip.getName());
			out = new FileOutputStream(f);
			String loc = "http://tms-nms:8080/video/clip" +
					"?id=" + cam +
					"&start=" + cal +
					"&duration=" + dur;
			System.out.println("Clip URL: " + loc);
			URL url = new URL(loc);
			con = ConnectionFactory.createConnection(url);
			InputStream in = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int bytesRead = 0;
			while(true){
				bytesRead = in.read(data);
				if(bytesRead==-1) break;
				out.write(data, 0, bytesRead);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				out.flush();
				out.close();
			}catch(Exception e2){
			}
		}
	}
	
	public static void main(String[] args) {
		TestDownload td = new TestDownload();
	}
}
