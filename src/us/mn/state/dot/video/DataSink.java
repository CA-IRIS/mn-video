package us.mn.state.dot.video;

public interface DataSink {

	/** Connect a DataSource */
	public void connectSource(DataSource source);
	
	/** Disconnect a DataSource */
	public void disconnectSource();
	
	/** Flush the data down the sink */
	public void flush(byte[] data);
}
