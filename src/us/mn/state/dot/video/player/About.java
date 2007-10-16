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

import java.awt.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Provides an about dialog for the VideoPlayer application.
 *
 * @author    Timothy Johnson
 * @created   March 24, 2005
 * @version   $Revision: 1.1 $ $Date: 2006/01/17 22:21:47 $
 */
public class About extends JDialog {

	/**
	 * Creates an About dialog with the specified <code>Frame</code> as it's owner.
	 *
	 * @param frame  The parent <code>Frame</code>.
	 */
	public About(Frame frame) {
		super( frame, "About", true );
		JLabel mndotLogo = new JLabel();
		URL url = this.getClass().getResource( "/logo.gif" );
		mndotLogo.setIcon( new ImageIcon( url ) );
		JLabel tmcLogo = new JLabel();
		url = this.getClass().getResource( "/tmc.gif" );
		tmcLogo.setIcon( new ImageIcon( url ) );
		JPanel topPanel = new JPanel();
		topPanel.setBackground( Color.white );
		topPanel.setLayout( new BoxLayout( topPanel, BoxLayout.X_AXIS ) );
		topPanel.add( Box.createHorizontalStrut( 10 ) );
		topPanel.add( mndotLogo );
		topPanel.add( Box.createHorizontalGlue() );
		topPanel.add( tmcLogo );
		topPanel.add( Box.createHorizontalStrut( 10 ) );
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout( new BoxLayout( centerPanel, BoxLayout.Y_AXIS ) );
		centerPanel.add( Box.createVerticalStrut( 10 ) );
		JLabel label = new JLabel("Application: " + frame.getTitle());
		centerPanel.add( label );
		centerPanel.add( Box.createVerticalStrut( 10 ) );
		label = new JLabel("Version: @@VERSION@@");
		centerPanel.add( label );
		centerPanel.add( Box.createHorizontalStrut( 10 ) );
		label = new JLabel("Build: @@BUILD.ID@@");
		centerPanel.add( label );
		centerPanel.add( Box.createHorizontalStrut( 10 ) );
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
		panel.add( topPanel );
		panel.add( centerPanel );
		panel.setBorder( BorderFactory.createBevelBorder(
				BevelBorder.LOWERED ) );
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( panel, BorderLayout.CENTER );
		pack();
		this.setSize( this.getPreferredSize() );
	}

}
