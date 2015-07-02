package com.aote.lodspider.corrections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
		while( stmtIterator.hasNext()){
			Statement nextStatement = stmtIterator.next();
			Resource sub = nextStatement.getSubject();
			Resource pre = nextStatement.getPredicate();
			RDFNode obj = nextStatement.getObject();
			if (pre.toString().equals("http://localhost/corrections#type")) {
				System.out.println("type:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#publishedDate")){
				System.out.println("publishedDate:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#defect")){
				System.out.println("defect:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#correction")){
				System.out.println("correction:"+obj.toString());
			}
			
		}
	}
		

	public static void main(String[] args) {
		CorrectionParser c = new CorrectionParser();
		try {
			InputStream in = new FileInputStream("Corrections/correction3");
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
