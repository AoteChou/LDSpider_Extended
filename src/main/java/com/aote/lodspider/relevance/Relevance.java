package com.aote.lodspider.relevance;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.yars.nx.namespace.Namespace;
import org.semanticweb.yars.nx.parser.NxParser;

import com.aote.lodspider.corrections.Correction;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.util.FileManager;

public class Relevance {
	
	public static Map<URI, List<Correction>> _relevances = new HashMap<URI, List<Correction>>();
	private double dividingScore;
	
	
	public Relevance(){
		this.dividingScore = 1.0;
		//need to change: read from doc
	}
	public Relevance(double dividingScore){
		this.dividingScore = dividingScore;
	}
	
	
	public void setRelevance(URI uri){
		if (_relevances.get(uri) == null){
			List<Correction> result = new ArrayList<Correction>();
			//TODO: crawl corrections from Apache Mamortta
			Correction[] corrections = getCorrections();
			for (Correction correction : corrections) {
				if( calculateRelevance(uri, correction) >= dividingScore)
					result.add(correction);
			}
			_relevances.put(uri, result);
			
		}
	}
	
	
	public Correction[] getCorrections(){
		Correction[] corrections = {new Correction(1, "defesin"),
				new Correction(2,"extracellular exosome"),new Correction(3,"ISS:UiProtKB"),
				new Correction(4,"GO_0070062"),new Correction(5,"GO:0070062")};
		return corrections;
	}
	
	private double calculateRelevance(URI uri, Correction correction){
		//TODO:getnamespace and calculate
		//For corrections there are two options:
		//1.get namespaces from source uri
		//2.get namespaces from the result of SPARQL
		return (Math.random())*2;
	}
	
	public List<String> getNamespaces(String uri){
		Model model = ModelFactory.createDefaultModel();
		InputStream in = FileManager.get().open(uri);
		model.read(in, "" );

		NsIterator listNameSpaces = model.listNameSpaces();
//		while (listNameSpaces.hasNext())
//		{
//		  System.out.println("Namespace from iterator: " + listNameSpaces.next());
//		}
		return listNameSpaces.toList();
	}
	
	public static void main(String[] args) {
		
	}

}
