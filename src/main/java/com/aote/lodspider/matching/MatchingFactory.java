package com.aote.lodspider.matching;

public class MatchingFactory {
	public static Matching getExactMatching(){
		Matching exactMatching = new ExactMatching();
		return new Matchings(new Matching[]{exactMatching});
	}
	public static Matching getApproachingMatching(){
		return getApproachingMatching(0.8);
	}
	public static Matching getApproachingMatching(double threshold){
		if(threshold< 0 || threshold > 1)
			threshold = 1;
		Matching exactMatching = new ExactMatching();
		Matching edMatching = new EditDistance(threshold);
		return new Matchings(new Matching[]{exactMatching,edMatching});
	}
}
