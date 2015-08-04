package com.aote.lodspider.matching;



public class Matchings extends Matching {
	Matching[] _matchings;
	public Matchings(Matching[] matchings) {
		_matchings = matchings;
	}

	public boolean ifmatch(String[] a, String[] b) {
		for (Matching matching : _matchings) {
			if (matching.ifmatch(a, b) == true){
				return true;
			}
		}
		return false;
	}

}
