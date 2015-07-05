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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.jena.riot.RiotException;
import org.semanticweb.yars.nx.namespace.Namespace;
import org.semanticweb.yars.nx.parser.NxParser;

import com.aote.lodspider.corrections.Correction;
import com.aote.lodspider.corrections.CorrectionParser;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.util.FileManager;

public class Relevance {
	
	private final Logger _log = Logger.getLogger(this.getClass().getName());
	
	public static Map<URI, List<Correction>> _relevances; 
	public static Collection<Correction> _corrections;
	private double dividingScore;
	static{
		_relevances = new HashMap<URI, List<Correction>>();
		_corrections = CorrectionParser.parse();
	}
	
	
	public Relevance(){
		this.dividingScore = 1.0;
		//need to change: read from doc
	}
	public Relevance(double dividingScore){
		this.dividingScore = dividingScore;
	}
	
	public static void readCorrections(){
//		Correction[] corrections = {new Correction(1, "defesin"),
//				new Correction(2,"extracellular exosome"),new Correction(3,"ISS:UiProtKB"),
//				new Correction(4,"GO_0070062"),new Correction(5,"GO:0070062")};
		try {
			_corrections = CorrectionParser.parse(new FileInputStream("Corrections/corrections"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param uri the resource Uri
	 */
	public void setRelevance(URI uri){
		if (_relevances.get(uri) == null){
			List<Correction> result = new ArrayList<Correction>();
			for (Correction correction : _corrections) {
				if( calculateRelevance(uri, correction) >= dividingScore)
//					System.out.println(calculateRelevance(uri, correction));
					result.add(correction);
			}
			_relevances.put(uri, result);
			
		}
	}

	private double calculateRelevance(URI uri, Correction correction){
		//TODO:getnamespace and calculate
		//For corrections there are two options:
		//1.get namespaces from source uri
		//2.get namespaces from the result of SPARQL
//		return (Math.random())*2;
		List<String> nameSpaceList_URI = getNamespaces(uri.toString());
		List<String> nameSpaceList_Correction = getNamespaces(correction.getSourceURI().toString());
		
		int size_URI = nameSpaceList_URI.size();
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
//		InputStream in = FileManager.get().open(uri);
		InputStream in = null;
		try {
			in = new URL(uri).openStream();
			model.read(in, "" );
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RiotException e) {
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
		
		Relevance relevance = new Relevance();
		try {
			relevance.setRelevance(new URI("http://localhost:8080/marmotta/resource?uri=http://localhost/Corrections"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
