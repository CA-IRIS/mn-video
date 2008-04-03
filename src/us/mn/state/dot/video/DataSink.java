package us.mn.state.dot.video;

public interface DataSink {

	/** Connect a datasource */
	public void connect(DataSource source);
	
	/** Disconnect a datasource */
	public void disconnect(DataSource source);
	
	/** Flush the data down the sink */
	public void flush(byte[] data);
}
