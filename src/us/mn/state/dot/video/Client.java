/*
* VideoServer
* Copyright (C) 2003-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.video;

/**
 * 
 * @author Timothy Johnson
 *
 */
public class Client {

	/** Sonar session identifier for authenticating to the video system */
	private long sonarSessionId = -1;
	
	private District district = District.METRO;
	
	private int rate = 30;

	private String host = "unknown";
	
	private String user = "unknown";
	
	private int duration = 60;
	
	private Camera camera = null;

	protected ImageSize size = ImageSize.MEDIUM;

	public Client(){
	}

	public int getDuration() {
		return duration;
	}
	public String getHost() {
		return host;
	}
	public int getRate() {
		return rate;
	}
	public String getUser() {
		return user;
	}
	public int getFramesRequested() {
		return duration * rate;
	}
	public ImageSize getSize() {
		return size;
	}
	public District getDistrict() {
		return district;
	}
	public String toString(){
		return user + "@" + host + ": C=" + getCameraId() +
			" S=" + size + " R=" + rate + " D=" +
			duration;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public void setDistrict(District d) {
		this.district = d;
	}
	public void setCamera(Camera c) {
		this.camera = c;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public void setSize(ImageSize size) {
		this.size = size;
	}
	public String getCameraId(){
		return camera.getId();
	}
	public void setCameraId(String id){
		if(id == null || id.length() > 10) return;
		if(camera == null) camera = new Camera();
		camera.setId(Camera.createStandardId(id));
	}
	/** Get the SONAR session ID */
	public long getSonarSessionId() {
		return sonarSessionId;
	}
	/** Set the SONAR session ID */
	public void setSonarSessionId(long sonarSessionId) {
		this.sonarSessionId = sonarSessionId;
	}
}
