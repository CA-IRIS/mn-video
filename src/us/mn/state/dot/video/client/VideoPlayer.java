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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import us.mn.state.dot.log.TmsLogFactory;
import us.mn.state.dot.video.Constants;

/**
 * VideoPlayer is a specialized video viewer application. 
 *
 * @author    Timothy Johnson
 * @created   May 30, 2002
 */
public class VideoPlayer extends JFrame {

	private VideoMonitor monitor;
	Container contentPane = null;
	private Logger logger = null;
	
	private void initGui() {
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		Properties p = new Properties();
		InputStream is =
			this.getClass().getClassLoader().getResourceAsStream("client.properties");
		try{
			p.load(is);
		}catch(IOException ioe){
			logger.warning("Exception loading properties file.");
		}
		String uri = "http://" +
			p.getProperty("video.host") + ":" +
			p.getProperty("video.port") + "/video/imageserver";
		monitor = new VideoMonitor(uri);
		controls.add( new CameraIdControl(p, monitor, logger), c );
		c.gridx = 0;
		c.gridy = 1;
		controls.add( new IncidentControl(p, monitor, logger), c );
		contentPane = this.getContentPane();
		contentPane.setLayout( new GridBagLayout() );
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.BOTH;
		contentPane.add(controls, c2);
		c2.gridx = 1;
		c2.gridy = 0;
		contentPane.add(monitor, c2);
		addWindowListener(
			new WindowAdapter() {
				public void windowClosing( WindowEvent evt ) {
					System.exit( 0 );
				}
			} );
		this.setJMenuBar( createMenuBar() );
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
//				System.out.println("Resizing...");
				if(i.isSelected()){
//					System.out.println("  scaling up.");
					monitor.setVideoSize(Constants.SIF_4X);
				}else{
//					System.out.println("  scaling down.");
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
		logger = TmsLogFactory.createLogger("hyperstream", null, null);
		setVisible( true );
		initGui();
		setResizable(false);
	}

	public static void main(String[] args){
		Properties p = new Properties();
		p.setProperty("proxyHost", "proxy.dot.state.mn.us");
		p.setProperty("proxyPort", "3128");
		p.setProperty("noProxyHosts", "151.111.");
//		ProxySelector.setDefault(new HTTPProxySelector(p));
		new VideoPlayer();
	}

}
