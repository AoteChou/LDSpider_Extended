package com.aote.lodspider.matching;

public class PartialMatchingForEachItem extends Matching {
	//use contains to judge if match

	public boolean ifmatch(String[] pattern, String[] text) {
		int arraySize = pattern.length;
		if (pattern.length != text.length)
			arraySize = Math.min(pattern.length, text.length);
		
		for(int i=0; i < arraySize; i++){
			if(text[i].contains(pattern[i])){
				return true;
			}
		}
		return false;
	}

}
