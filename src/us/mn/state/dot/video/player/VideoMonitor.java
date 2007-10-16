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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.video.AbstractImageFactory;
import us.mn.state.dot.video.Camera;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.Constants;
import us.mn.state.dot.video.ImageFactoryListener;
import us.mn.state.dot.video.client.nvr.CameraSelector;

/**
 * A JPanel that can display an RTMC video stream.
 *
 * @author    Timothy Johnson
 * @created   May 30, 2002
 */
public class VideoMonitor extends JPanel
		implements ImageFactoryListener, ListSelectionListener {

	private Camera camera = null;
	private AbstractImageFactory factory = null;
	private int imagesRendered = 0;
	private final DecimalFormat formatter = new DecimalFormat("000");
	Image image = null;
	String imageName = null;
	private final JLabel screen = new JLabel();
	private final JLabel description = new JLabel(null,null,JLabel.CENTER);
	private JProgressBar progress = new JProgressBar(0, 100);
	private LinkedList images = new LinkedList();
	private final JLabel status = new JLabel();
	public static final String CONNECT_ERROR = "Unable to connect to stream.";
	public static final String CONNECTING = "Connecting...";
	public static final String STREAMING = "Streaming...";
	public static final String STREAM_ERROR = "Stream unavailable.";
	public static final String STREAM_COMPLETE = "Stream finished.";
	public static final String WAIT_ON_USER = "Waiting for user...";
	private int imagesRequested = 0;
	private Dimension imageSize = new Dimension(Constants.SIF_FULL);
	protected String imageURI = null;
	
	/**
	 * Constructor for the VideoMonitor
	 */
	public VideoMonitor(String imageURI) {
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
		this.imageURI = imageURI;
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

	public void setImageFactory(AbstractImageFactory f, int totalFrames){
		imagesRendered = 0;
		this.imagesRequested = totalFrames;
		if(f != null){
			f.addImageFactoryListener(this);
			images.clear();
			status.setText(CONNECTING);
		}else{
			status.setText(WAIT_ON_USER);
		}
		if(factory != null){
			factory.removeImageFactoryListener(this);
		}
		factory = f;
		progress.setMaximum(imagesRequested);
		progress.setValue(0);
	}
	
	public void imageCreated(byte[] i){
		status.setText(STREAMING);
		ImageIcon icon = new ImageIcon(i);
		setImage(icon);
		progress.setValue(imagesRendered);
		imagesRendered++;
		if(imagesRendered >= imagesRequested){
			factory.removeImageFactoryListener(this);
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

	/** Update the video screen with the latest camera image. */
	protected void updateScreen(){
		if(camera == null) setImage(null);
    	URLConnection con = null;
		try{
			URL url = new URL(imageURI + "?id=" + camera.getNumber());
			con = ConnectionFactory.createConnection(url);
			InputStream is = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int bytesRead = 0;
			while(true){
				bytesRead = is.read(data);
				if(bytesRead==-1) break;
				bos.write(data, 0, bytesRead);
			}
			setImage(new ImageIcon(bos.toByteArray()));
		}catch(IOException ioe){
			ioe.printStackTrace();
		}finally{
			try{
				con.getInputStream().close();
			}catch(IOException ioe2){
			}
		}
	}
}