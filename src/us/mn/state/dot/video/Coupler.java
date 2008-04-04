package us.mn.state.dot.video;

import java.util.Collection;
import java.util.Vector;

/** A coupler is used to couple a DataSource with multiple DataSinks
 * @author Timothy A. Johnson
 *
 */
public class Coupler implements DataSink, DataSource {

	/** The data to flush down the sink */
	protected byte[] data = null;
	
	/** The collection of sinks which receive the data */
	private final Collection<DataSink> sinks = new Vector<DataSink>();
	
	/** The source which supplies the data */
	private DataSource source;

	/** Notify the connected sinks that there is new data */
	protected final void notifySinks(byte[] data){
		for(DataSink sink : sinks){
			sink.flush(data);
		}
	}

	/** Flush the data down the sink */
	public void flush(byte[] data){
		this.data = data;
		notifySinks(data);
	}

	/** Connect a DataSink */
	public final void connectSink(DataSink sink){
		if(sink == this) return;
		sinks.add(sink);
	}
	
	/** Disconnect a DataSink */
	public final void disconnectSink(DataSink sink){
		sinks.remove(sink);
	}
}
