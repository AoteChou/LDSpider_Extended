package com.aote.lodspider.matching;

public abstract class Matching {
	
	public abstract boolean ifmatch(String[] a, String[] b);
	public  boolean ifmatch(String a, String b){
		return ifmatch(new String[]{a}, new String[]{b});
	}
	

}
