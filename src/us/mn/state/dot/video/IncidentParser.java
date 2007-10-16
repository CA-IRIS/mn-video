package us.mn.state.dot.video;

import java.net.URL;

import us.mn.state.dot.util.xml.XmlParser;

public class IncidentParser extends XmlParser {

	public IncidentParser(URL url) throws InstantiationException {
		super(url);
	}

/*	private Document getIncidentDocument(String location){
		SAXBuilder builder = new SAXBuilder(false);
		InputStream stream = null;
		try{
			URL url = new URL(location);
			stream = url.openStream();
			return builder.build(stream);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}*/

}
