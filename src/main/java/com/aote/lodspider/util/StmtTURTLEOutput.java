package com.aote.lodspider.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javafx.stage.Stage;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.rdfxml.xmloutput.impl.BaseXMLWriter;
import com.hp.hpl.jena.vocabulary.RDFSyntax;

/**
 * Writes out statments in RDF/XML format
 */
public class StmtTURTLEOutput {
	/**
	 * write statements in TURTLE
	 * @param statement
	 */
	public String getTURTLEStatements(Statement statement, Map<String, String> nsMap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		Model temp = ModelFactory.createDefaultModel();
		temp.setNsPrefixes(nsMap);
		temp.add(statement);
		temp.write(baos, "TURTLE");
		
		//delete the prefix part and return the result
		return baos.toString().replaceAll("@prefix[\\s\\S]*?>\\s*\\.", "").trim();
	}
	

}
