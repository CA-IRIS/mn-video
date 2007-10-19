package us.mn.state.dot.video;

import java.awt.Point;

public class Camera {

	protected String id = null;
	
	protected String freeway = "";
	
	protected String crossStreet = "";
	
	protected Point location = new Point(0,0);

	public Camera(){
	}

	public void setCrossStreet(String xStreet) {
		if(xStreet==null) return;
		this.crossStreet = xStreet;
	}

	public String getCrossStreet() { return crossStreet; }

	public String getFreeway() { return freeway; }

	public String getId() { return id; }

	public int getEasting(){ return location.x; }

	public int getNorthing(){ return location.y; }

	public void setFreeway(String fwy) {
		if(fwy==null) return;
		this.freeway = fwy;
	}

	public void setId(String id) {
		this.id = id;
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
