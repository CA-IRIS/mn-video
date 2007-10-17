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
package us.mn.state.dot.video.client.nvr;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URL;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.log.TmsLogFactory;
import us.mn.state.dot.util.HTTPProxySelector;
import us.mn.state.dot.video.Camera;
import us.mn.state.dot.video.CameraParser;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.Constants;
import us.mn.state.dot.video.VideoClip;
import us.mn.state.dot.video.player.VideoMonitor;

public class NvrClient extends JFrame implements ListSelectionListener {

	protected static final int STATUS_OK = 1;
	protected static final int STATUS_ERROR = 2;
	protected JTextField statusField = new JTextField();
	protected String propFile = "nvr-client.properties";
	protected VideoMonitor monitor = null;
	protected CameraSelector cameras = new CameraSelector();
	protected TimeSelector times = null;
	protected DurationChooser durations = new DurationChooser();
	protected JButton save = new JButton("Save Video");
	protected String videoHost = null;
	protected String clipURI = "/video/clip";
	protected String videoPort = null;
	protected Logger logger = null;
	
	public NvrClient(){
		try{
			String workingDir = System.getProperty("user.home");
			Properties p = new Properties();
			InputStream in = null;
			in = this.getClass().getClassLoader().getResourceAsStream(
						propFile);
			if(in == null){
				File f = new File(workingDir, propFile);
				in = new FileInputStream(f);
			}
			p.load(in);
			logger = TmsLogFactory.createLogger(
					"nvrclient", Level.FINE, new File(workingDir));
			ProxySelector.setDefault(new HTTPProxySelector(p));
			videoHost = p.getProperty("video.host");
			videoPort = p.getProperty("video.port");
			times = new TimeSelector(videoHost, videoPort);
			monitor = new VideoMonitor(createImageURL());
			URL url = new URL(p.getProperty("cameras.xml"));
			CameraParser parser = new CameraParser(url);
			cameras.setCameras(parser.getCameras().values());
			save.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
					saveClip();
				}
			});
		}catch(Exception e){
			e.printStackTrace();
		}
		this.addWidgets();
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				System.exit(0);
			}
		});
		statusField.setBackground(Color.DARK_GRAY);
		cameras.addListSelectionListener(times);
		cameras.addListSelectionListener(monitor);
		cameras.addListSelectionListener(this);
		this.pack();
		this.setVisible(true);
	}

	protected String createImageURL(){
		return "http://" + videoHost + ":" + videoPort + "/video/imageserver";
	}
	
	protected String createClipURL(){
		return "http://" + videoHost + ":" + videoPort + "/video/clip";
	}

	protected void addWidgets(){
		Container p = this.getContentPane();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		p.add(monitor, c);

		c.gridy = 1;
		p.add(times, c);
		
		c.gridy = 2;
		c.gridwidth = 1;
		p.add(new JScrollPane(cameras), c);

		c.gridx = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.NONE;
		p.add(durations, c);
		p.add(save, c);
		
		c.gridx = 0;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 3;
		p.add(statusField, c);
	}
	
	protected void saveClip(){
		setStatus(STATUS_OK, "Saving video clip...");
		VideoClip clip = new VideoClip();
		Calendar startCal = times.getStartTime();
		if(startCal == null){
			setStatus(STATUS_ERROR, "Start time of clip cannot be determined.");
			return;
		}
		clip.setStart(startCal);
		clip.setDuration(durations.getDuration());
		Camera c = (Camera)cameras.getSelectedValue();
		clip.setCameraId(c.getId());
    	try{
			String home = System.getProperty("user.home");
			File file = new File(home, clip.getName());
			String start = Constants.DATE_FORMAT.format(startCal.getTime());
			start = start.replaceAll(" ", "");
			String s = createClipURL() +
					"?id=" + clip.getCameraId() +
					"&start=" + start +
					"&duration=" + clip.getDuration();
			System.out.println("Clip URL: " + s);
			URL url = new URL(s);
			ConnectionFactory.readData(url, file);
			setStatus(STATUS_OK, "Video saved as " + file.getAbsolutePath());
		}catch(FileNotFoundException fnfe){
			fnfe.printStackTrace();
			setStatus(STATUS_ERROR, "Video unavailable at " +
					startCal.getTime());
		}catch(IOException ioe){
			ioe.printStackTrace();
			setStatus(STATUS_ERROR, ioe.getMessage());
		}
	}
	
	private void setStatus(int status, String message){
		switch(status){
			case(STATUS_OK):
				statusField.setForeground(Color.GREEN);
				break;
			case(STATUS_ERROR):
				statusField.setForeground(Color.RED);
				break;
		}
		statusField.setText(message);
	}
	
	public void valueChanged(ListSelectionEvent evt){
		if(evt.getValueIsAdjusting()) return;
		setStatus(STATUS_OK, "");
	}

	public static void main(String[] args) {
		NvrClient c = new NvrClient();
	}
}
