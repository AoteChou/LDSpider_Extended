package com.aote.lodspider.corrections;

public class Triple {
	private String object;
	private String predicate;
	private String subject;
	Triple(String obj, String pre,String sub){
		this.object = obj;
		this.predicate = pre;
		this.subject = sub;
	}
	private String getSubject() {
		return subject;
	}
	private void setSubject(String subject) {
		this.subject = subject;
	}
	private String getPredicate() {
		return predicate;
	}
	private void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	private String getObject() {
		return object;
	}
	private void setObject(String object) {
		this.object = object;
	}

}
