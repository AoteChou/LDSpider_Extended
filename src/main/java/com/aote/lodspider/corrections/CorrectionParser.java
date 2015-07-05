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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CorrectionParser {
	public static Collection<Correction> parse(){
		Collection<Correction> corrections = new ArrayList<Correction>();
		try {
			//TODO: read from config file
			corrections = parse(new FileInputStream("Corrections/corrections"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return corrections;
	}
	
	public static Collection<Correction> parse(InputStream source){
		
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
			//initiate new instance for new sub 
			//except node "http://localhost/Corrections" which holds all corrections
			if( correction == null && !sub.toString().equals("http://localhost/Corrections")){
				correction = new Correction();
				correctionMap.put(sub.toString(), correction);
			}
			if (pre.toString().equals("http://localhost/corrections#type")) {
				correction.setType(Type.valueOf(obj.toString().toUpperCase()));
				System.out.println("type:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#publishedDate")){
				Date publishedDate;
				try {
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					publishedDate = df.parse(obj.toString());
					correction.setPublishedDate(publishedDate);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("publishedDate:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#sourceURI")){
				try {
					correction.setSourceURI(new URI(obj.toString()));
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("sourceURI:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#accessionPath")){
				correction.setAccessionPath(obj.toString());
				System.out.println("accessionPath:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#oldValue")){
				correction.setOldValue(obj.toString());
				System.out.println("oldValue:"+obj.toString());
			}else if(pre.toString().equals("http://localhost/corrections#newValue")){
				correction.setNewValue(obj.toString());
				System.out.println("newValue:"+obj.toString());
			}
			
		}
		
		return correctionMap.values();
	}
	
	public void tryj(InputStream source){
		
		Model model = ModelFactory.createDefaultModel();
		model.read(source, "");
		StmtIterator stmtIterator = model.listStatements();
		StringBuffer queryString = new StringBuffer("prefix : <http://localhost/corrections#>");
		queryString.append("SELECT ?c WHERE {");
		queryString.append("?p :type ?c");
		queryString.append("}");
		try {
			Query query = QueryFactory.create(queryString.toString());
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			ResultSet rs = qexec.execSelect();
			while(rs.hasNext()){
				QuerySolution qs = rs.nextSolution();
				Iterator<String> varNames = qs.varNames();
				//TODO: now only support for one column
				if(varNames.hasNext()){
					RDFNode node = qs.get(varNames.next());
					System.out.println(node.toString());
				}

			}
			
//			ResultSetFormatter.out(rs);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}		

	public static void main(String[] args) {
		CorrectionParser c = new CorrectionParser();
		try {
			InputStream in = new FileInputStream("Corrections/corrections");
//			InputStream in = new URL("http://localhost:8080/marmotta/resource?uri=http%3A%2F%2Flocalhost%2FCorrections%2F3&amp;format=application/rdf%2Bxml").openStream();
//			c.tryj(in);
//			CorrectionParser.parse();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
