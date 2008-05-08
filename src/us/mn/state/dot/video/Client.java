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

	/** Constant for small sized images */
	public static final int SMALL = 1;

	/** Constant for medium sized images */
	public static final int MEDIUM = 2;

	/** Constant for large sized images */
	public static final int LARGE = 3;

	public static int maxImageSize = LARGE;
	
	/** The parameters for a video clip request */
	private VideoClip clip = new VideoClip();
	
	private int area = 0;
	
	private int rate = 30;

	private String host = "unknown";
	
	private String user = "unknown";
	
	private int duration = 60;
	
	private Camera camera = null;

	int size = 2;

	/** Value for the jpeg compression level */
	int compression = 50;
	
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
	public int getSize() {
		return Math.min(maxImageSize, size);
	}
	public int getArea() {
		return area;
	}
	public int getCompression() {
		return compression;
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
	public void setArea(int area) {
		this.area = area;
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
	public void setSize(int size) {
		size = Math.min(maxImageSize, size);
		if(size >= SMALL && size <= LARGE) this.size = size;
	}
	public void setCompression(int compression) {
		this.compression = compression;
	}
	public VideoClip getClip() {
		return clip;
	}
	public void setClip(VideoClip clip) {
		this.clip = clip;
	}
	public String getCameraId(){
		return camera.getId();
	}
	public void setCameraId(String id){
		if(id == null || id.length() > 10) return;
		if(camera == null) camera = new Camera();
		camera.setId(Camera.createStandardId(id));
	}
	
	public static void setMaxImageSize(int i){
		if(i >= SMALL && i <= LARGE) maxImageSize = i;
	}
}
