package comp437.footchat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {

	
	public Object[] get2DReasult(InputStream in,String nodes,String[] strA, int length) throws ParserConfigurationException, SAXException, IOException{
		Object[] returnMsg = new Object[length];
		
		for(int i=0;i<length;i++){
			returnMsg[i]=new ArrayList<String>();
		}
		
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbfactory.newDocumentBuilder();
		Document dom = db.parse(in);
		Element docEle = dom.getDocumentElement();
		
		NodeList nl = docEle.getElementsByTagName(nodes);
		for (int i = 0 ; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			for(int j=0;j<length;j++){
				ArrayList<String> als =(ArrayList<String>)returnMsg[j];
				als.add(ele.getElementsByTagName(strA[j]).item(0).getTextContent());
			}
		}
		
		return returnMsg;
	}
}
