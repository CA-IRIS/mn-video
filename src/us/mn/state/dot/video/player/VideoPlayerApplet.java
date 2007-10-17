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
package us.mn.state.dot.video.player;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JButton;

import us.mn.state.dot.log.TmsLogFactory;

/**
 * VideoPlayer is a specialized video viewer application. 
 *
 * @author    Timothy Johnson
 * @created   May 30, 2002
 */
public class VideoPlayerApplet extends JApplet {

	private final VideoMonitor monitor = new VideoMonitor("");
	Container contentPane = null;
	private Logger logger = null;
	String cameraId = null;
	CameraIdControl control = null;
	JButton restart = new JButton("Restart");
	
	private void initGui() {
		monitor.setLabelVisible(false);
		monitor.setProgressVisible(false);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		Properties p = new Properties();
		InputStream is =
			this.getClass().getClassLoader().getResourceAsStream("client.properties");
		try{
			p.load(is);
		}catch(IOException ioe){
			System.out.println("Exception loading properties file.");
		}
		restart.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				startStream();
			}
		});
		restart.setEnabled(false);
		control = new CameraIdControl(p, monitor, logger);
		contentPane = this.getContentPane();
		contentPane.setLayout( new GridBagLayout() );
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		contentPane.add(monitor, c);
		c.fill=GridBagConstraints.NONE;
		contentPane.add(restart, c);
	}

	/** Initialize the video player */
	public void init() {
		logger = TmsLogFactory.createLogger("hyperstream", null, null);
		initGui();
		try{
			cameraId = getParameter("camera");
			restart.setEnabled(true);
			startStream();
		}catch(Exception e){
			
		}
	}

	public void startStream(){
		try{
			control.setCameraId(cameraId);
			control.start();
		}catch(Exception e){
			logger.warning("There was a problem with the URL...");
			e.printStackTrace();
		}
	}
	
}
