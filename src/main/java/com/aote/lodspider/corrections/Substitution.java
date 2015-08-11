package com.aote.lodspider.corrections;


public class Substitution extends Correction {

	public Substitution() {
		super();
	}
	public Substitution(Correction correction) {
		type = correction.getType();
		publishedDate = correction.getPublishedDate();
		sourceURI = correction.getSourceURI();
		accessionPath = correction.getAccessionPath();
		oldValue = correction.getOldValue();
		oldValuePath = correction.getOldValuePath();
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
		info += "oldValue:" + oldValuePath + "\n";
		info += "newValue:" + newValuePath + "\n";
		
		return info;
	}
//	Subsititution(Triple[] defectionTriples, Triple[] correctionTriples){
//		this.defectTriples = defectionTriples;
//		this.correctionTriples = correctionTriples;
//		
//	}

}
