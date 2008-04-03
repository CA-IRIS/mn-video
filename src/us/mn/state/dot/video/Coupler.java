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

	/** Connect a datasource */
	public void connect(DataSource source){
		this.source = source;
	}
	
	/** Disconnect a datasource */
	public void disconnect(DataSource source){
		this.source = null;
	}
	
	/** Flush the data down the sink */
	public void flush(byte[] data){
		for(DataSink sink : sinks){
			sink.flush(data);
		}
	}

	/** Connect a datasink */
	public void connect(DataSink sink){
		sinks.add(sink);
	}
	
	/** Disconnect a datasink */
	public void disconnect(DataSink sink){
		sinks.remove(sink);
	}

}
