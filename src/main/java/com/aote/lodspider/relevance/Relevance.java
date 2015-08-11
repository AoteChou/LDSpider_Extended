package com.aote.lodspider.relevance;

import java.net.URI;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.aote.lodspider.corrections.Correction;
import com.aote.lodspider.corrections.CorrectionParser;
import com.aote.lodspider.util.XMLParser;

public abstract class Relevance {
	
	public static Map<String, List<Correction>> _relevances; 
//	public static Map<String, Boolean> _flags; 
	public static Map<URI, List<String>> _nameSpaces; 
	public static Collection<Correction> _corrections;
	protected double dividingScore;
	
	public Relevance(){
		String configFilePath = "src/main/java/com/aote/lodspider/config/CrawlerConfig.xml";
		Hashtable<String, String> configMap = XMLParser.parse(configFilePath);

		this.dividingScore = Double.parseDouble(configMap.get("relevanceThreshold")); 
	}
	public Relevance(double dividingScore){
		this.dividingScore = dividingScore;
	}
	
	static{
		_relevances = new Hashtable<String, List<Correction>>();
//		_flags = new Hashtable<String, Boolean>();
		_nameSpaces = new Hashtable<URI, List<String>>();
		try {
			_corrections = CorrectionParser.parse();
		} catch (Exception e) {
			//deal with the exception when there is no corrections in triple store
			e.printStackTrace();
		}
	}

	public abstract List<Correction> getRelatedCorrections(URI uri);
	public abstract void setRelatedCorrections(URI uri);


}
