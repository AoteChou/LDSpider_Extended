package com.aote.lodspider.corrections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CorrectionParser {
	
	public void parse(InputStream source){
		
		try {
			Model model = ModelFactory.createDefaultModel();
			model.read(source, "");
			StmtIterator stmtIterator = model.listStatements();
			while( stmtIterator.hasNext()){
				System.out.println(stmtIterator.next());
			}
			model.write(new FileOutputStream("justGiveATry",true));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		CorrectionParser c = new CorrectionParser();
		try {
			InputStream in = new FileInputStream("Corrections/correction3");
			c.parse(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
