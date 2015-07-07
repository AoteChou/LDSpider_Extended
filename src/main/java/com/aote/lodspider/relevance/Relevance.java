package com.aote.lodspider.relevance;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.riot.RiotException;
import org.semanticweb.yars.nx.namespace.Namespace;
import org.semanticweb.yars.nx.parser.NxParser;

import com.aote.lodspider.corrections.Correction;
import com.aote.lodspider.corrections.CorrectionParser;
import com.aote.lodspider.util.XMLParser;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.util.FileManager;
import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.http.ConnectionManager;

public class Relevance {
	
	private final Logger _log = Logger.getLogger(this.getClass().getName());
	
	public static Map<URI, List<Correction>> _relevances; 
	public static Collection<Correction> _corrections;
	private double dividingScore;
	static{
		_relevances = new HashMap<URI, List<Correction>>();
		try {
			_corrections = CorrectionParser.parse();
		} catch (Exception e) {
			//deal with the exception when there is no corrections in triple store
			e.printStackTrace();
		}
	}
	
	
	public Relevance(){
		String configFilePath = "src/main/java/com/aote/lodspider/config/CrawlerConfig.xml";
		Hashtable<String, String> configMap = XMLParser.parse(configFilePath);

		this.dividingScore = Double.parseDouble(configMap.get("relevanceThreshold")); 
		//need to change: read from doc
	}
	public Relevance(double dividingScore){
		this.dividingScore = dividingScore;
	}
	

	/**
	 * 
	 * @param uri the resource Uri
	 */
	public void setRelevance(URI uri){
		if (_relevances.get(uri) == null){
			List<Correction> result = new ArrayList<Correction>();
			for (Correction correction : _corrections) {
				double score = calculateRelevance(uri, correction);
				if(  score >= dividingScore){
//					System.out.println(calculateRelevance(uri, correction));
					result.add(correction);
					_log.info("add correction to[score: "+score+"] :"+ uri.toString());
				}
			}
			_relevances.put(uri, result);
			
		}
	}
	
//	//set relevance for URI with redirects(303)
//	public void setRelevance(URI from, URI to){
//		//[URI from] would have been visited but have no correction because of redirection
//		if (_relevances.get(from).size() == 0 ){
//			List<Correction> result = _relevances.get(to);
//			if (result == null) {
//				result = new ArrayList<Correction>();
//				for (Correction correction : _corrections) {
//					double score = calculateRelevance(to, correction);
//					if(  score >= dividingScore){
////					System.out.println(calculateRelevance(uri, correction));
//						result.add(correction);
//						_log.info("add correction to[score: "+score+"] :\n"+from+"("+to+")");
//					}
//				}
//				
//				_relevances.put(to, result);
//			}
//			_relevances.put(from, result);
//			
//		}
//	}
	
	private double calculateRelevance(URI uri, Correction correction){
		//TODO:getnamespace and calculate
		//For corrections there are two options:
		//1.get namespaces from source uri
		//2.get namespaces from the result of SPARQL
//		return (Math.random())*2;
		List<String> nameSpaceList_URI = getNamespaces(uri.toString());
		List<String> nameSpaceList_Correction = getNamespaces(correction.getSourceURI().toString());
		
		int size_URI = nameSpaceList_URI.size();
		if (size_URI == 0) {
			return 0;
		}
		int size_Correction = nameSpaceList_Correction.size();
		int sameNum = 0;
		
		
		for (int i = 0; i < size_Correction; i++) {
			String nameSpaceItem = nameSpaceList_Correction.get(i);
			if (nameSpaceList_URI.contains(nameSpaceItem)) {
				sameNum ++;
			}
		}
		
		double result = 0;
		if (size_Correction != 0) {
			//calculate the ratio of same nameSpace In nameSpaces of URI
			result = (double)sameNum/(double)size_URI;
		}
		
		return result;
	}
	
	private List<String> getNamespaces(String uri){
		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(uri, uri, null);
		} catch (Exception e) {
			_log.warning("[unsupported input]:"+uri);
		} 

		NsIterator listNameSpaces = model.listNameSpaces();
//		while (listNameSpaces.hasNext())
//		{
//		  System.out.println("Namespace from iterator: " + listNameSpaces.next());
//		}
		return listNameSpaces.toList();
	}

	public static void main(String[] args) {
		
//		Relevance relevance = new Relevance();
//		try {
//			relevance.setRelevance(new URI("http://localhost:8080/marmotta/resource?uri=http://localhost/Corrections"));
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(Relevance.readFromURI("http://purl.uniprot.org/uniprot/P37231"));
		
		
	}

}
