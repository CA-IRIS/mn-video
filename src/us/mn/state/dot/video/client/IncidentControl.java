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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author John3Tim
 *
 * This class displays a list of current incidents and allows the user
 * to change the video stream by selecting an incident.
 */
public class IncidentControl extends AbstractStreamControl{

	private JList incidentList = new JList();

	private String incidentsLocation = null;
	
	public IncidentControl(Properties props, VideoMonitor monitor, Logger logger){
		super(props, monitor, logger);
		this.setLayout(new GridBagLayout());
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		incidentsLocation = props.getProperty("incidents.url");
		updateIncidents(incidentsLocation);
		JScrollPane scrollPane = new JScrollPane(incidentList);
		scrollPane.setPreferredSize(new Dimension(300, 100));
		scrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		incidentList.addListSelectionListener(new IncidentListListener());
		//add the incidents ScrollPane
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 1;
		scrollPane.setPreferredSize(new Dimension(300, 200));
		this.add(scrollPane,c);
		//add the control buttons
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = GridBagConstraints.RELATIVE;
		c2.gridy = 0;
		JButton b = new JButton("Update Incident List");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				updateIncidents(incidentsLocation);
			}
		});
		p.add(b, c2);
		b = new JButton("Start");
		b.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Incident i = (Incident)incidentList.getSelectedValue();
				if(i == null){
					return;
				}
				setCameraId(i.getCameraId());
				start();
			}
		});
		p.add(b, c2);
		b = new JButton("Stop");
		b.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				stop();
			}
		});
		p.add(b, c2);
		c.gridy = 1;
		c.weighty = 0;
		this.add(p,c);
	}
	
	public class IncidentListListener implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent evt){
			if(!evt.getValueIsAdjusting()){
				Incident i = (Incident)incidentList.getSelectedValue();
				if(i == null){
					return;
				}
				setCameraId(i.getCameraId());
				start();
			}
		}
	}
	
	public class Incident{
		private String cameraId;
		private String description;
		
		public Incident(String desc, String camId){
			cameraId = camId;
			description = desc;
		}
		
		public String getDescription(){
			return description;
		}
		
		public String getCameraId(){
			return cameraId;
		}
		
		public String toString(){
			return description;
		}
	}
	
	private void updateIncidents(String location){
		DefaultListModel model = new DefaultListModel();
		Collection c = getIncidents(location);
		Iterator it = c.iterator();
		while(it.hasNext()){
			model.addElement(it.next());
		}
		incidentList.setModel(model);
	}

	private Collection getIncidents(String location){
		Collection<Incident> c = new Vector<Incident>();
/*		try{
			Document doc = getIncidentDocument(location);
			if(doc == null) return c;
			List incidents = doc.getRootElement().getChildren("incident");
			Iterator it = incidents.iterator();
			while(it.hasNext()){
				Element xmlIncident = (Element)it.next();
				String cameraId = xmlIncident.getAttribute("camera").getValue();
				int camNumber = Integer.parseInt(cameraId.substring(1));
				String desc = xmlIncident.getAttributeValue("message");
				desc = desc + ": " +xmlIncident.getAttributeValue("road");
				desc = desc + xmlIncident.getAttributeValue("direction");
				desc = desc + " @ " + xmlIncident.getAttributeValue("location");
				desc = desc + "   " + xmlIncident.getAttributeValue("date");
				desc = desc + " " + xmlIncident.getAttributeValue("time");
				c.add(new Incident(desc, camNumber));
			}
		}catch(Exception e){
			e.printStackTrace();
		}*/
		return c;
	}
}
