package com.aote.lodspider.corrections;

import com.hp.hpl.jena.rdf.model.ModelFactory;



public class Addition extends Correction{

	public Addition() {
		super();
		oldValue = ModelFactory.createDefaultModel();
		// TODO Auto-generated constructor stub
	}
	
	public Addition(Correction correction) {
		type = correction.getType();
		publishedDate = correction.getPublishedDate();
		sourceURI = correction.getSourceURI();
		accessionPath = correction.getAccessionPath();
		newValue = correction.getNewValue();
		newValuePath = correction.getNewValuePath();
	}
	
	@Override
	public String toString() {
		String info = "";
		info += "type:" + type + "\n";
		info += "publishedDate:" + publishedDate + "\n";
		info += "sourceURI:" + sourceURI + "\n";
		info += "accessionPath:" + accessionPath + "\n";
		info += "newValue:" + newValuePath + "\n";
		
		return info;
	}
//	private String entryID;
//	
//	public Insertion(String entryID, Triple[] correctionTriples){
//		this.defectTriples = null;
//		this.entryID = entryID;
//		this.correctionTriples = correctionTriples;
//	}
//
//	private String getEntryID() {
//		return entryID;
//	}
//
//	private void setEntryID(String entryID) {
//		entryID = entryID;
//	}

}
