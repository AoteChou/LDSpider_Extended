package com.aote.lodspider.patch;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;

public class PatchProducer {
	
	private OutputStream _out;
	public StringBuffer pre;
	public StringBuffer suffix;
	private StringBuffer result;
	
	String _filename;
	public PatchProducer(){
		_filename = "Patch"+new Date().toString();
		pre = new StringBuffer();
		suffix = new StringBuffer();
		result = new StringBuffer();
		
	}
	public PatchProducer(String filename){
		_filename = filename;
		pre = new StringBuffer();
		suffix = new StringBuffer();
		result = new StringBuffer();
		try {
			_out = new FileOutputStream(filename,true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void appendPrefix(String name,String uri){
		pre.append("@Prefix "+name+": <"+uri+">  .\n");
	}
	public void appendPrefix(Map<String, String> nsMap){
		Iterator<Entry<String, String>> it = nsMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> ent = it.next();
			String prefix = ent.getKey();
			String uri = ent.getValue();
			pre.append("@Prefix "+prefix+": <"+uri+">  .\n");
		}
	}
	public void appendAdd(String Content){
		result.append("Add {");
		result.append(Content+"");
		result.append("}  .\n");
	}
	public void appendDelete(String Content){
		result.append("Delete {  ");
		result.append(Content+"");
		result.append("}  .\n");
	}
	public void appendBind(String Content){
		result.append("Bind  ");
		result.append(Content+" .\n");
	}
	public void appendCut(String Content){
		result.append("Cut  ");
		result.append(Content+" .\n");
	}
	public void appendUpdateList(String Content){
		result.append("UpdateList  ");
		result.append(Content+" .\n");
	}
	
//	public void print(Hashtable<String, String> Pre, String[] Add, String[] Delete, String[] Cut, String[] Bind, String[] UpdateList){
//		
//	}
	public void print(String fileName){
		try {
			_out = new FileOutputStream(fileName,true);
			_out.write(pre.toString().getBytes());
			_out.write("\n".getBytes());
			_out.write(result.toString().getBytes());
			_out.write("\n".getBytes());
			_out.write(suffix.toString().getBytes());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		PatchProducer pp = new PatchProducer();
		pp.appendPrefix("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		pp.appendPrefix("schema","http://schema.org");
		pp.appendPrefix("profile","http://ogp.me/ns/profile#");
		pp.appendPrefix("ex","<http://example.org/vocab#");
		
		pp.appendDelete("<#> profile:first_name \"Tim\"");
		pp.appendAdd(" <#> profile:first_name \"Timothy\" ;\n"+
				"profile:image <https://example.org/timbl.jpg> .");
		pp.appendBind("?workLocation <#> / schema:workLocation");
		pp.appendCut("?workLocation");
		pp.appendUpdateList("<#> ex:preferredLanguages 1..2 ( \"fr-CH\" )");
		
		pp.print("ddd");
	}
	
	
	
	
	

}
