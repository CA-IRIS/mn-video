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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.util.db.DatabaseConnection;
import us.mn.state.dot.video.Camera;
import us.mn.state.dot.video.ConnectionFactory;
import us.mn.state.dot.video.Constants;

public class TimeSelector extends JPanel
		implements ListSelectionListener, ChangeListener {

	protected final int MIN_RESOLUTION = 15 * 1000; // 15 seconds
	protected final JLabel label =
		new JLabel("No Video Available", SwingConstants.CENTER);
	protected static final int LEFT = 37;
	protected static final int RIGHT = 39;
	protected final JSlider slider = new JSlider();
	protected String rangeURI = null;
	protected Calendar begin, end;
	protected Camera camera = null;
	protected SimpleDateFormat labelFormatter =
		new SimpleDateFormat("M/d");
	protected String videoHost = null;
	protected String videoPort = null;
	
	public TimeSelector(String host, String port){
		videoHost = host;
		videoPort = port;
		rangeURI = "/video/range";
		initializeWidgets();
		addWidgets();
	}
	
	protected void initializeWidgets(){
		slider.setMinorTickSpacing(15 * 1000);
		slider.setSnapToTicks(true);
		slider.setMaximum(0);
		slider.addChangeListener(this);
		slider.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent ke){
				switch(ke.getKeyCode()){
					case(LEFT):
						slider.setValue(slider.getValue() - MIN_RESOLUTION);
						break;
					case(RIGHT):
						slider.setValue(slider.getValue() + MIN_RESOLUTION);
						break;
				}
			}
		});
	}
	
	protected void addWidgets(){
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		this.add(label, c);
		this.add(slider, c);
	}
	
	public void valueChanged(ListSelectionEvent evt){
		if(evt.getValueIsAdjusting()) return;
		CameraSelector s = (CameraSelector)evt.getSource();
		camera = (Camera)s.getSelectedValue();
		setCalendars();
		updateModel();
	}

	/** Set the begin and end calendars for the current camera. */
	protected void setCalendars(){
		if(camera == null) return;
    	URLConnection con = null;
		try{
			URL url = new URL(
					"http://" + videoHost + ":" + videoPort + rangeURI +
					"?id=" + camera.getNumber());
			con = ConnectionFactory.createConnection(url);
			InputStream is = con.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String s = reader.readLine();
			while(s != null){
				StringTokenizer t = new StringTokenizer(s, "=", false);
				t.nextToken(); //throw away the property token
				if(s.startsWith("start")){
					begin = DatabaseConnection.getCalendar(t.nextToken());
					begin.add(Calendar.MILLISECOND,
							-(int)(begin.getTimeInMillis() % MIN_RESOLUTION));
				}else if(s.startsWith("end")){
					end = DatabaseConnection.getCalendar(t.nextToken());
					end.add(Calendar.SECOND,
							-(int)(begin.getTimeInMillis() % MIN_RESOLUTION));
				}
				s = reader.readLine();
			}
		}catch(FileNotFoundException fnfe){
			System.out.println("No NVR defined for camera " + camera.getNumber());
			begin = end = null;
		}catch(IOException ioe){
			ioe.printStackTrace();
		}finally{
			try{
				con.getInputStream().close();
			}catch(IOException ioe2){
			}
		}
	}
	
	/** Update the model with video archive availability information
	 * for the selected camera.
	 *
	 */
	protected void updateModel(){
		if(begin == null || end == null){
			clearModel();
			return;
		}
		if(camera != null){
			int range = (int)(end.getTimeInMillis() - begin.getTimeInMillis());
			slider.setMaximum(range);
			slider.setVisible(true);
		}
	}

	public int getDuration(){
		return slider.getExtent();
	}

	public Calendar getStartTime(){
		if(begin == null || end == null) return null;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(begin.getTimeInMillis() + slider.getValue());
		return c;
	}

	protected void clearModel(){
		slider.setMinimum(0);
		slider.setMaximum(0);
		slider.setVisible(false);
	}

	public void stateChanged(ChangeEvent evt){
		Calendar c = getStartTime();
		if(c == null){
			label.setText("No Video Available");
		}else{
			label.setText(Constants.DATE_FORMAT.format(c.getTime()));
		}
	}
}
