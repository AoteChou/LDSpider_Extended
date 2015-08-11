package com.aote.lodspider.corrections;

import java.net.URI;
import java.util.Date;

import com.hp.hpl.jena.rdf.model.Model;

public class Correction {
	// protected Triple[] defectTriples;
	// protected Triple[] correctionTriples;
	// public int id;
	protected Type type;
	protected Date publishedDate;
	protected URI sourceURI;
	protected String accessionPath;
	protected Model oldValue;
	protected String oldValuePath;
	protected Model newValue;
	protected String newValuePath;
	public Correction(){
		type =Type.SUBSTITUTION;
		publishedDate = new Date();
		accessionPath = "";
		oldValuePath = "";
		newValuePath = "";
	}

	// private String keyword;

	// public Correction(int id, String keyword) {
	// this.id = id;
	// this.keyword = keyword;
	// }

	// Triple[] getCorrectionTriples() {
	// return correctionTriples;
	// }
	//
	// void setCorrectionTriples(Triple[] correctionTriples) {
	// this.correctionTriples = correctionTriples;
	// }
	//
	// Triple[] getDefectTriples() {
	// return defectTriples;
	// }
	//
	// void setDefectTriples(Triple[] defectTriples) {
	// this.defectTriples = defectTriples;
	// }
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String info = "";
		info += "type:" + type + "\n";
		info += "publishedDate:" + publishedDate + "\n";
		info += "sourceURI:" + sourceURI + "\n";
		info += "accessionPath:" + accessionPath + "\n";
		info += "oldValue:" + oldValuePath + "\n";
		info += "newValue:" + newValuePath + "\n";
		
		return info;
	}

	//
	// public String getKeyword() {
	// return keyword;
	// }
	//
	// public void setKeyword(String keyword) {
	// this.keyword = keyword;
	// }

	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}

	public URI getSourceURI() {
		return sourceURI;
	}

	public void setSourceURI(URI sourceURI) {
		this.sourceURI = sourceURI;
	}

	public String getAccessionPath() {
		return accessionPath;
	}

	public void setAccessionPath(String accessionPath) {
		this.accessionPath = accessionPath;
	}

	public Model getOldValue() {
		return oldValue;
	}

	public void setOldValue(Model oldValue) {
		this.oldValue = oldValue;
	}

	public Model getNewValue() {
		return newValue;
	}

	public void setNewValue(Model newValue) {
		this.newValue = newValue;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getOldValuePath() {
		return oldValuePath;
	}

	public void setOldValuePath(String oldValuePath) {
		this.oldValuePath = oldValuePath;
	}

	public String getNewValuePath() {
		return newValuePath;
	}

	public void setNewValuePath(String newValuePath) {
		this.newValuePath = newValuePath;
	}
}
