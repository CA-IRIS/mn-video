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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import us.mn.state.dot.log.TmsLogFactory;
import us.mn.state.dot.util.HTTPProxySelector;
import us.mn.state.dot.video.Constants;

/**
 * VideoPlayer is a specialized video viewer application. 
 *
 * @author    Timothy Johnson
 * @created   May 30, 2002
 */
public class VideoPlayer extends JFrame {

	private final VideoMonitor monitor = new VideoMonitor();
	Container contentPane = null;
	private Logger logger = null;
	
	private void initGui(Properties p) {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		contentPane = this.getContentPane();
		contentPane.setLayout( new GridBagLayout() );
		contentPane.add(monitor, c);
		contentPane.add(new CameraIdControl(p, monitor, logger), c);
		addWindowListener(
			new WindowAdapter() {
				public void windowClosing( WindowEvent evt ) {
					System.exit( 0 );
				}
			} );
		this.setJMenuBar( createMenuBar() );
		this.setPreferredSize(new Dimension(400, 600));
		setVisible( true );
		setResizable(false);
		this.pack();
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add( createFileMenu() );
//		menuBar.add( createViewMenu() );
		menuBar.add( createHelpMenu() );
		return menuBar;
	}

	private JMenu createFileMenu() {
		JMenu m = new JMenu( "File" );
		JMenuItem exit = new JMenuItem( "Exit" );
		exit.setMnemonic( 'E' );
		exit.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				System.exit( 0 );
			}
		} );
		m.add( exit );
		return m;
	}

	private JMenu createViewMenu() {
		JMenu m = new JMenu("View");
		JMenuItem item = new JCheckBoxMenuItem("BigScreen");
		item.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				JCheckBoxMenuItem i = (JCheckBoxMenuItem)ae.getSource();
				if(i.isSelected()){
					monitor.setVideoSize(Constants.SIF_4X);
				}else{
					monitor.setVideoSize(Constants.SIF_FULL);
				}
				VideoPlayer.this.pack();
			}
		} );
		m.add(item);
		return m;
	}

	private JMenu createHelpMenu() {
		JMenu m = new JMenu( "Help" );
		JMenuItem about = new JMenuItem( "About" );
		about.setMnemonic( 'A' );
		about.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				new About(VideoPlayer.this).setVisible( true );
			}
		} );
		m.add( about );
		return m;
	}

	/** Initialize the video player */
	public VideoPlayer() {
		super("HyperStream");
		Properties p = new Properties();
		InputStream is =
			this.getClass().getClassLoader().getResourceAsStream("video-client.properties");
		try{
			p.load(is);
			ProxySelector.setDefault(new HTTPProxySelector(p));
		}catch(IOException ioe){
			logger.warning("Exception loading properties file.");
		}
		logger = TmsLogFactory.createLogger("hyperstream", null, null);
		initGui(p);
	}

	public static void main(String[] args){
		new VideoPlayer();
	}

}
