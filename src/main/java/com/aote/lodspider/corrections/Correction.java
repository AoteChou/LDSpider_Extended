package com.aote.lodspider.corrections;

import java.net.URI;
import java.util.Date;

public class Correction {
	// protected Triple[] defectTriples;
	// protected Triple[] correctionTriples;
	// public int id;
	private Type type;
	private Date publishedDate;
	private URI sourceURI;
	private String accessionPath;
	private String oldValue;
	private String newValue;

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
		info += "oldValue:" + oldValue + "\n";
		info += "newValue:" + newValue + "\n";
		
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

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
