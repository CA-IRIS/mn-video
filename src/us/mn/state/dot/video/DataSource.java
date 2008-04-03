package us.mn.state.dot.video;

public interface DataSource {

	/** Connect a datasink */
	public void connect(DataSink sink);
	
	/** Disconnect a datasink */
	public void disconnect(DataSink sink);

}
