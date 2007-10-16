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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;


/**
 *
 * @author    Timothy Johnson
 * @created   May 30, 2002
 */
public class CameraIdControl extends AbstractStreamControl{

	JButton start, stop;
	TextField cameraField = null;

	/** Create a control panel for the video client */
	public CameraIdControl(Properties p, VideoMonitor monitor, Logger l) {
		super(p, monitor, l);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		setLayout( new GridBagLayout() );
		cameraField = new TextField( 5 );
		start = new JButton( "Start" );
		start.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				try{
					setCamera(Integer.parseInt(cameraField.getText()));
					start();
				}catch(NumberFormatException nfe){
					return;
				}
			}
		});
		stop = new JButton( "Stop" );
		stop.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				stop();
			}
		});
		GridBagConstraints c = new GridBagConstraints();
		Label label;
		label = new Label( "Camera" );
		add( label, c );
		c.gridx = GridBagConstraints.RELATIVE;
		add( cameraField, c );
		add( start, c );
		add( stop, c );
	}

}
