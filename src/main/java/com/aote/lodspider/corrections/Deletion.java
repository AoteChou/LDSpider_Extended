package com.aote.lodspider.corrections;

public class Deletion extends Correction {

	public Deletion() {
		super();
		newValue = "";
		// TODO Auto-generated constructor stub
	}
	public Deletion(Correction correction) {
		type = correction.getType();
		publishedDate = correction.getPublishedDate();
		sourceURI = correction.getSourceURI();
		accessionPath = correction.getAccessionPath();
		oldValue = correction.getOldValue();
	}
	@Override
	public String toString() {
		String info = "";
		info += "type:" + type + "\n";
		info += "publishedDate:" + publishedDate + "\n";
		info += "sourceURI:" + sourceURI + "\n";
		info += "accessionPath:" + accessionPath + "\n";
		info += "oldValue:" + oldValue + "\n";
		
		return info;
	}
	
//	public Deletion(String entryID, Triple[] defectTriples){
//		this.correctionTriples = null;
//		this.defectTriples = defectTriples;
//
//	}

}
