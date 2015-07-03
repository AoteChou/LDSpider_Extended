package com.aote.lodspider.corrections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CorrectionParser {
	
	public void parse(InputStream source){
		
		Model model = ModelFactory.createDefaultModel();
		model.read(source, "");
		StmtIterator stmtIterator = model.listStatements();
		Map<String, Correction> correctionMap = new HashMap<String, Correction>();
		while( stmtIterator.hasNext()){
			Statement nextStatement = stmtIterator.next();
			Resource sub = nextStatement.getSubject();
			Resource pre = nextStatement.getPredicate();
			RDFNode obj = nextStatement.getObject();
			
			Correction correction = correctionMap.get(sub.toString());
			if( correction == null){
				correction = new Correction();
				correctionMap.put(sub.toString(), correction);
			}
			if (pre.toString().equals("http://localhost/corrections#type")) {
				correction.setType(Type.valueOf(obj.toString().toUpperCase()));
//				System.out.println("type:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#publishedDate")){
				Date publishedDate;
				try {
					DateFormat df = new SimpleDateFormat("YYYY-MM-DD");
					publishedDate = df.parse(obj.toString());
					correction.setPublishedDate(publishedDate);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				System.out.println("publishedDate:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#sourceURI")){
				try {
					correction.setSourceURI(new URI(obj.toString()));
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				System.out.println("sourceURI:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#accessionPath")){
				correction.setAccessionPath(obj.toString());
//				System.out.println("accessionPath:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#oldValue")){
				correction.setOldValue(obj.toString());
//				System.out.println("oldValue:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#newValue")){
				correction.setNewValue(obj.toString());
//				System.out.println("newValue:"+obj.toString());
			}
			
		}
	}
		

	public static void main(String[] args) {
		CorrectionParser c = new CorrectionParser();
		try {
			InputStream in = new FileInputStream("Corrections/corrections");
//			InputStream in = new URL("http://localhost:8080/marmotta/resource?uri=http%3A%2F%2Flocalhost%2FCorrections%2F3&amp;format=application/rdf%2Bxml").openStream();
			c.parse(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
