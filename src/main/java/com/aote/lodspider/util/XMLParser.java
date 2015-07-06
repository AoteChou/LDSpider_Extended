package com.aote.lodspider.util;


import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {

	static Document dom;
	
	private static void parseXMLFile(String filePath) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();

			dom = db.parse(filePath);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static Hashtable<String, String> parse(String path) {
		
		parseXMLFile(path);
		
		Hashtable<String, String> configMap = new Hashtable<String, String>();
		
		NodeList nl = dom.getElementsByTagName("config");
		
		
		NodeList nl_ChildNodes = nl.item(0).getChildNodes();
		
		for (int i=1; i < nl_ChildNodes.getLength(); i+=2){
			String key = nl_ChildNodes.item(i).getNodeName();
			String value = nl_ChildNodes.item(i).getTextContent();
			configMap.put(key, value);
		}


		return configMap;
		
		
		
	}
	
	public static void main(String[] args) {
		Hashtable<String, String> configMap = XMLParser.parse("src/main/java/com/aote/lodspider/config/CrawlerConfig.xml");
		
				
	}
}
