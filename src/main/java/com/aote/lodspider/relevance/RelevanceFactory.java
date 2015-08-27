package com.aote.lodspider.relevance;

public class RelevanceFactory {
	private static class RelevanceHolder{
		private static final Relevance r = new Relevance_Domain();
	}
	private RelevanceFactory(){
		
	}
	public static final Relevance getRelevance(){
		return RelevanceHolder.r;
	}
}
