package us.mn.state.dot.video;

import java.util.Collection;
import java.util.Vector;

/** A coupler is used to couple a DataSource with multiple DataSinks
 * @author Timothy A. Johnson
 *
 */
public class Coupler implements DataSink, DataSource {

	/** The collection of sinks which receive the data */
	private final Collection<DataSink> sinks = new Vector<DataSink>();
	
	/** The sources which supplies the data */
	private DataSource source;

	/** Connect a DataSource */
	public final void connectSource(DataSource source){
		this.source = source;
	}
	
	/** Disconnect the DataSource */
	public final void disconnectSource(){
		this.source = null;
	}
	
	/** Flush the data down the sink */
	public final void flush(byte[] data){
		for(DataSink sink : sinks){
			sink.flush(data);
		}
	}

	/** Connect a DataSink */
	public final void connectSink(DataSink sink){
		sinks.add(sink);
	}
	
	/** Disconnect a datasink */
	public final void disconnectSink(DataSink sink){
		sinks.remove(sink);
	}

}
