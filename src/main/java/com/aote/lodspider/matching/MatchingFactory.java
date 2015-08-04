package com.aote.lodspider.matching;

public class MatchingFactory {
	public static Matching getMatching(){
		Matching exactMatching = new ExactMatching();
		Matching partialMatching = new PartialMatchingForEachItem();
		Matching edMatching = new EditDistance(0.5);
		return new Matchings(new Matching[]{exactMatching,edMatching});
	}
}
