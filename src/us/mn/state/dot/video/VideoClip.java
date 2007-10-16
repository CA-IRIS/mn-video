/*
 * Project: Video
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package us.mn.state.dot.video;

import java.util.Calendar;

/** The VideoClip class represents an archive video clip from the NVR system. */ 
public class VideoClip {

	protected Calendar start = Calendar.getInstance();
	protected int duration = 0;
	protected int camera = -1;
	
	public VideoClip(){
		
	}

	public int getCamera() {
		return camera;
	}

	public void setCamera(int camera) {
		this.camera = camera;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Calendar getStart() {
		return start;
	}

	public void setStart(Calendar start) {
		this.start = start;
	}

	public String getName(){
		String s = "C" + camera + "_" +
			Constants.DATE_FORMAT.format(start.getTime()) +
			".mpg";
		s = s.replace("-", "");
		s = s.replace(":", "");
		return s;
	}

	public Calendar getEnd(){
		Calendar c = (Calendar)start.clone();
		c.add(Calendar.SECOND, duration);
		return c;
	}
}
