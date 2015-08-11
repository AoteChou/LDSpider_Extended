package com.aote.lodspider.matching;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.semanticweb.yars.nx.cli.Split;


//import info.debatty.java.stringsimilarity.Levenshtein;

public class EditDistance extends Matching {
	private double matchVal;
	private static OutputStream _out = null;
	Levenshtein l = new Levenshtein();
	
	static{
		try {
			_out = new FileOutputStream("EDandSim");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public EditDistance(double matchVal) {
		this.matchVal = matchVal;
		
	}
	


	public boolean ifmatch(String[] pattern, String[] text) {
		
		
		int arraySize = pattern.length;
		for(int i=0; i < arraySize; i++){
//			writeInfo(pattern[i], text[i]);
			//CHANGE: split text according to pattern
			String[] text_split =split(pattern[i], text[i]);
//			StringBuffer title = new StringBuffer("_____________\n");
//			title.append(pattern[i]+ "[And]" + text[i]+"\n");
//			title.append("------------------------\n");
//			try {
////				_out.write(title.toString().getBytes());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			for (String text_part : text_split) {
//				writeInfo(pattern[i], text_part);
				double similarity =  l.similarity(pattern[i], text_part);
				System.out.println(similarity+"");
				if( similarity >= getMatchVal())
					return true;
			}
	
		}
	
		return false;
	}
	
	private void writeInfo(String pattern, String text){
		
		String distanceAbsolute ="\""+pattern+"\"[and]\""+text+"\" [absoluteDistance:"+l.distanceAbsolute(pattern, text)+"]\n";
		String distance ="\""+pattern+"\"[and]\""+text+"\" [distance:"+l.distance(pattern, text)+"]\n";
		String similarity = "\""+pattern+"\"[and]\""+text+"\" [similarity:"+l.similarity(pattern, text)+"]\n";
		
		try {
			_out.write(distanceAbsolute.getBytes());
			_out.write(distance.getBytes());
			_out.write(similarity.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String[] splitText(String pattern, String text){
		ArrayList<String> textList = new ArrayList<String>();
		
		
		return textList.toArray(new String[textList.size()]);
	}
	
	private int spaceNumInaString(String str){
		int num = 0;
		String [] result = str.split("\\s+");
		
		return result.length - 1;
		
	}
	
	private String[] split(String pattern, String text){
		int numOfSpace = spaceNumInaString(pattern);
		return split(text, numOfSpace);
	}
	
	private String[] split(String text, int numOfSpace){
		
		String[] result = text.split(" ");
		ArrayList<String> result_combine = new ArrayList<String>();
	
		StringBuffer result_combine_item = new StringBuffer();
		for(int i=1; i<=result.length; i++){
			result_combine_item.append(result[i-1]);
			if( ( i%(numOfSpace+1) == 0) || i == result.length ){
				result_combine.add(result_combine_item.toString());
				result_combine_item  = new StringBuffer();
			}else {
				result_combine_item.append(" ");
			}
		}
		
		return  result_combine.toArray(new String[ result_combine.size()]);
	}


	private double getMatchVal() {
		return matchVal;
	}



	private void setMatchVal(double matchVal) {
		this.matchVal = matchVal;
	}
	
	public static void main(String[] args) {
		EditDistance ed = new EditDistance(0.5);
		
		System.out.println(ed.ifmatch("Scorpion", "Scorpion_toxin/defesin"));
		
//		System.out.println(ed.spaceNumInaString("dd   ss sa"));
//		
//		String[] resultStrings = ed.split( "ds sas sa","dsds dsd ddd ddd dsa ase ds dsd");
//		
//		for (String string : resultStrings) {
//			System.out.println(string);
//		}
	}

}
