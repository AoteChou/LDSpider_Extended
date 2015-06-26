package com.aote.lodspider.relevance;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aote.lodspider.corrections.Correction;

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
			Correction[] corrections = {new Correction(1, "defesin"),
					new Correction(2,"extracellular exosome"),new Correction(3,"ISS:UiProtKB"),
					new Correction(4,"GO_0070062"),new Correction(5,"GO:0070062")};
			for (Correction correction : corrections) {
				if( calculateRelevance(uri, correction) >= dividingScore)
					result.add(correction);
			}
			_relevances.put(uri, result);
			
		}
	}
	private double calculateRelevance(URI uri, Correction correction){
		//TODO: add relevance algorithm
		return (Math.random())*2;
	}
	

}
