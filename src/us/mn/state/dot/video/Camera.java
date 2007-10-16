package us.mn.state.dot.video;

import java.awt.Point;

public class Camera {

	protected String id = null;
	
	protected String freeway = null;
	
	protected String crossStreet = null;
	
	protected Point location = new Point(0,0);

	public Camera(){
	}

	public void setCrossStreet(String crossStreet) {
		this.crossStreet = crossStreet;
	}

	public String getCrossStreet() { return crossStreet; }

	public String getFreeway() { return freeway; }

	public String getId() { return id; }

	public int getEasting(){ return location.x; }

	public int getNorthing(){ return location.y; }

	public void setFreeway(String freeway) {
		this.freeway = freeway;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getNumber(){
		return Integer.parseInt(id.substring(1));
	}
	
	public void setEasting(int e){
		location.x = e;
	}

	public void setNorthing(int n){
		location.y = n;
	}
	
	public String toString(){
		return id + ": (" + getFreeway() + " @ " + getCrossStreet() + ")";
	}
}
