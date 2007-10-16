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
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Timothy Johnson</a>
 *
 */
public class CameraParser extends XmlParser {

	private static final String ATT_FREEWAY = "freeway";
	
	private static final String ATT_CROSS_STREET = "crossstreet";
	
	private Hashtable<String, Camera> cameras =
		new Hashtable<String, Camera>();

	public CameraParser(URL url) throws InstantiationException {
		super(url);
		loadCameras();
	}
	
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
			System.out.println("Camera " + c.getNumber() +
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
			c.setFreeway(e.getAttribute("freeway"));
			c.setCrossStreet(e.getAttribute("cross_street"));
			cameras.put(Integer.toString(c.getNumber()), c);
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
			File f = new File("/etc/tms/video.properties");
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
