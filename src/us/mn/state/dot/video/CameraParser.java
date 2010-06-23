package us.mn.state.dot.video;

import java.io.File;
import java.io.FileInputStream;
import java.net.ProxySelector;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import us.mn.state.dot.util.HTTPProxySelector;
import us.mn.state.dot.util.xml.XmlParser;

/** 
 * This class creates <code>Camera</code> objects from an xml file. 
 * @author Timothy Johnson
 *
 */
public class CameraParser extends XmlParser {

	private static final String FREEWAY = "freeway";
	
	private static final String CROSS_STREET = "cross_street";
	
	private Hashtable<String, Camera> cameras =
		new Hashtable<String, Camera>();

	public CameraParser(URL url) throws InstantiationException {
		super(url);
		loadCameras();
	}
	
	/**
	 * Get a hash of cameras.
	 * @return A hashtable of cameras indexed by camera id.
	 */
	public Hashtable getCameras(){
		return cameras;
	}
	
	private void loadCameras(){
		if(document == null) return;
		Element root = document.getDocumentElement();
		loadCameras( root.getChildNodes() );
//		printCameras();
	}

	protected void printCameras(){
		Enumeration camList = cameras.elements();
		while(camList.hasMoreElements()){
			Camera c = (Camera)camList.nextElement();
			System.out.println(c.getId() +
				"\t" + c.getFreeway() + " @ " + c.getCrossStreet());
		}
	}
	
	private void loadCameras(NodeList nodes){
		for(int i=0; i<nodes.getLength(); i++){
			Node n = nodes.item(i);
			if(!n.getNodeName().equals("camera")) continue;
			Camera c = new Camera();
			Element e = (Element)n;
			String id = e.getAttribute("id");
			c.setId(id);
			c.setFreeway(e.getAttribute(FREEWAY));
			c.setCrossStreet(e.getAttribute(CROSS_STREET));
			cameras.put(c.getId(), c);
		}
	}
	
	protected void print(Node n){
		if(!n.getNodeName().equals("camera")) return;
		NamedNodeMap map = n.getAttributes();
		if(map == null) return;
		Node att = map.getNamedItem("id");
		if(att != null){
			System.out.println("ID " + att.getNodeValue());
		}
	}

	public static void main(String[] args){
		try{
			File f = new File("/etc/iris/video.properties");
			Properties p = new Properties();
			p.load(new FileInputStream(f));
			ProxySelector.setDefault(new HTTPProxySelector(p)); 
			URL url = new URL(p.getProperty("cameras.xml"));
			new CameraParser(url);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
