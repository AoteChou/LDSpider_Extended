package com.aote.lodspider.callback;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.jena.riot.RiotException;

import com.aote.lodspider.corrections.Correction;
import com.aote.lodspider.relevance.Relevance;
import com.aote.lodspider.relevance.Relevance_URI;
import com.aote.lodspider.relevance.RelevanceFactory;
import com.aote.lodspider.relevance.Relevance_Domain;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class SearchBySPARQL {
	
	OutputStream _out;
	public final Logger _log = Logger.getLogger(this.getClass().getName());
	
	public SearchBySPARQL(){
		try {
			_out = new FileOutputStream("SPARQL_Result", true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void Search(String uri, String contentType){
		//C
		Model model = ModelFactory.createDefaultModel();
		try {
			if (contentType.contains("rdf+xml")) {
				model.read(uri.toString(), uri,"RDF/XML");
			}else if (contentType.contains("text/plain")) {
				model.read(uri.toString(), uri,"N-TRIPLE");
			}else if (contentType.contains("n3")) {
				model.read(uri.toString(), uri,"N3");
			}else if (contentType.contains("turtle")) {
				model.read(uri.toString(), uri,"TURTLE");
			}			
		} catch (RiotException e) {
			_log.warning("[cannot parse file]:"+uri);
			return;
		}
		if (model == null) {
			return;
		}
//		StmtIterator stmtIterator = model.listStatements();
//		while( stmtIterator.hasNext()){
//			System.out.println(stmtIterator.nextStatement().toString());
//		}
		//find 
		List<Correction> corrections =  new ArrayList<Correction>();
		Relevance r = RelevanceFactory.getRelevance();
		try {
			corrections = r.getRelatedCorrections(new URI(uri));
			while (corrections == null) {
				System.out.println("waiting for the result: " + uri);
				Thread.sleep(500);
				corrections = r.getRelatedCorrections(new URI(uri));
			}
			System.out.println("get correction for "+ uri);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		if (uri.equals("http://www.uniprot.org/uniprot/P37231.rdf")) {
////			System.out.println(Relevance._relevances.toString());
//			try {
//				System.out.println(r.getRelatedCorrections(new URI(uri)));
//			} catch (URISyntaxException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		for (Correction correction : corrections) {
			searchForOldvalue(model, correction, uri);
		}
		//CEND
	}
	
	/**
	 * execute SPARQL select query
	 * @param queryStirng 
	 * @param model the model being queried
	 * @return
	 */
	public ResultSet SPARQL_SELECT(String queryString, Model model){
		return SPARQL_SELECT(queryString, model, null);
	}
	/**
	 * execute SPARQL select query
	 * @param queryStirng 
	 * @param model the model being queried
	 * @param baseURI
	 * @return
	 */
	public ResultSet SPARQL_SELECT(String queryString, Model model, String baseURI){
		Query query = QueryFactory.create(queryString, baseURI);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet rs = qexec.execSelect();
		rs = ResultSetFactory.copyResults(rs);
		return rs;
	}
	
	/**
	 * search for the statements that match SPARQL pattern and with oldValue
	 * @param model_Target
	 * @param correction
	 */
	public void searchForOldvalue(Model model_Target, Correction correction, String baseURI){
		
		ResultSet rs = SPARQL_SELECT(correction.getAccessionPath(), model_Target, baseURI);
		while(rs.hasNext()){
			QuerySolution qs = rs.nextSolution();
			Iterator<String> varNames = qs.varNames();
			//TODO: now only support for one column
			if(varNames.hasNext()){
				RDFNode node = qs.get(varNames.next());
				String result = "";
				//TODO: add support for the resource
				if (node.isLiteral()) {
					result = ((Literal)node).getLexicalForm();
				} else if (node.isResource()) {
					result = ((Resource) node).getURI();
				}
				if (result.equals(correction.getOldValue())) {
					//print 所在的statement
					String info = new Date()+": find correction in "+baseURI +"\n";
					info +="-----------------------\n";
					info +=correction.toString();
					info +="-----------------------\n";
					try {
						_out.write(info.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
