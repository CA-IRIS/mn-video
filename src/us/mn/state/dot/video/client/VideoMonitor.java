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

package us.mn.state.dot.video.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.video.Camera;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.Constants;
import us.mn.state.dot.video.DataSink;
import us.mn.state.dot.video.DataSource;

/**
 * A JPanel that can display an RTMC video stream.
 *
 * @author    Timothy Johnson
 * @created   May 30, 2002
 */
public class VideoMonitor extends JPanel
		implements DataSink, ListSelectionListener {

	private Camera camera = null;
	private DataSource source = null;
	private int imagesRendered = 0;
	Image image = null;
	String imageName = null;
	private final JLabel screen = new JLabel();
	private final JLabel description = new JLabel(null,null,JLabel.CENTER);
	private JProgressBar progress = new JProgressBar(0, 100);
	private LinkedList<byte[]> images = new LinkedList<byte[]>();
	private final JLabel status = new JLabel();
	public static final String CONNECT_ERROR = "Unable to connect to stream.";
	public static final String CONNECTING = "Connecting...";
	public static final String STREAMING = "Streaming...";
	public static final String STREAM_ERROR = "Stream unavailable.";
	public static final String STREAM_COMPLETE = "Stream finished.";
	public static final String WAIT_ON_USER = "Waiting for user...";
	private int imagesRequested = 0;
	private Dimension imageSize = new Dimension(Constants.SIF_FULL);
	protected URI imageURI = null;
	
	/**
	 * Constructor for the VideoMonitor
	 */
	public VideoMonitor() {
		super(new BorderLayout());
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		p.add(description, c);
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		p.add(screen, c);
		p.add(progress, c);
		c.gridwidth = 1;
		p.add(status, c);
		this.add(p);
		setVideoSize(imageSize);
		screen.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public void setImageUri(URI uri){
		imageURI = uri;
	}
	
	public synchronized void setVideoSize(Dimension d){
		imageSize = d;
		status.setPreferredSize(new Dimension(d.width, 20));
		description.setPreferredSize(new Dimension(d.width, 20));
		screen.setPreferredSize(d);
		screen.setMinimumSize(d);
	}

	/**
	 * Set the image to be displayed on the panel
	 *
	 * @param image  The image to display.
	 */
	public synchronized void setImage(ImageIcon icon){
		Image i = icon.getImage().getScaledInstance(
			imageSize.width, imageSize.height,
				Image.SCALE_FAST);
		screen.setIcon(new ImageIcon(i));
		repaint();
	}

	public void setDataSource(DataSource src, int totalFrames){
		if(source != null ){
			source.disconnectSink(this);
		}
		imagesRendered = 0;
		this.imagesRequested = totalFrames;
		if(src != null){
			source.connectSink(this);
			images.clear();
			status.setText(CONNECTING);
		}else{
			status.setText(WAIT_ON_USER);
		}
		source = src;
		progress.setMaximum(imagesRequested);
		progress.setValue(0);
	}
	
	public void flush(byte[] i){
		status.setText(STREAMING);
		ImageIcon icon = new ImageIcon(i);
		setImage(icon);
		progress.setValue(imagesRendered);
		imagesRendered++;
		if(imagesRendered >= imagesRequested){
			source.disconnectSink(this);
			clear();
		}
	}
	
	private void clear(){
		progress.setMaximum(imagesRequested);
		progress.setValue(0);
		status.setText(WAIT_ON_USER);
	}
	
	public void setProgressVisible(boolean m){
		progress.setVisible(m);
	}

	public void setStatusVisible(boolean m){
		status.setVisible(m);
	}

	public void setLabelVisible(boolean m){
		description.setVisible(m);
	}

	public void valueChanged(ListSelectionEvent evt){
		if(evt.getValueIsAdjusting()) return;
		CameraSelector s = (CameraSelector)evt.getSource();
		this.setCamera((Camera)s.getSelectedValue());
	}

	public void setCamera(Camera c){
		camera = c;
		if(camera != null){
			description.setText(camera.toString());
			updateScreen();
		}
	}

	public String toString(){
		String id = "";
		try{
			id = camera.getId();
		}catch(Exception e){
		}
		return id + " video monitor";
	}
	
	/** Update the video screen with the latest camera image. */
	protected void updateScreen(){
		if(camera == null) setImage(null);
		try{
			URL url = new URL(imageURI + "?id=" + camera.getId());
			byte[] image = ConnectionFactory.getImage(url);
			setImage(new ImageIcon(image));
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
}