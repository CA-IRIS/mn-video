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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;

public class DurationChooser extends JPanel {

	protected SpinnerModel durations = new SpinnerNumberModel(1,1,10,1);
	protected JSpinner spinner = new JSpinner(durations);
	
	public DurationChooser(){
		addWidgets();
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	}
	
	protected void addWidgets(){
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		this.add(new JLabel("Minutes of Video",JLabel.CENTER), c);
		this.add(spinner, c);
	}
	
	public int getDuration(){
		return ((Integer)(spinner.getValue())).intValue() * 60;
	}
}
