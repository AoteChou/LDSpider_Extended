package com.aote.lodspider.corrections;


public class Correction {
//	protected Triple[] defectTriples;
//	protected Triple[] correctionTriples;
	public int id;
	private String keyword;
	
	public Correction(int id, String keyword) {
		this.id = id;
		this.keyword = keyword;
	}

//	Triple[] getCorrectionTriples() {
//		return correctionTriples;
//	}
//
//	void setCorrectionTriples(Triple[] correctionTriples) {
//		this.correctionTriples = correctionTriples;
//	}
//
//	Triple[] getDefectTriples() {
//		return defectTriples;
//	}
//
//	void setDefectTriples(Triple[] defectTriples) {
//		this.defectTriples = defectTriples;
//	}
	@Override
		public String toString() {
			// TODO Auto-generated method stub
			return id+":"+keyword;
		}

	private String getKeyword() {
		return keyword;
	}

	private void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
