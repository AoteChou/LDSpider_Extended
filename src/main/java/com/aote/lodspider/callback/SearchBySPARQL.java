package com.aote.lodspider.callback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.jena.riot.RiotException;

import com.aote.lodspider.corrections.Correction;
import com.aote.lodspider.corrections.Type;
import com.aote.lodspider.matching.Matching;
import com.aote.lodspider.matching.MatchingFactory;
import com.aote.lodspider.patch.PatchProducer;
import com.aote.lodspider.relevance.Relevance;
import com.aote.lodspider.relevance.Relevance_URI;
import com.aote.lodspider.relevance.RelevanceFactory;
import com.aote.lodspider.relevance.Relevance_Domain;
import com.aote.lodspider.util.StmtRDFXMLOutput;
import com.aote.lodspider.util.StmtTURTLEOutput;
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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class SearchBySPARQL {

	OutputStream _out;
	public final Logger _log = Logger.getLogger(this.getClass().getName());
	Matching _m, _exactM, _approachingM;

	public SearchBySPARQL() {
		try {
			_out = new FileOutputStream("SPARQL_Result", true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		_exactM = MatchingFactory.getExactMatching();
		_approachingM = MatchingFactory.getApproachingMatching();
	}

	public void Search(String uri, String contentType) {
		_log.info("SPARQL:" + uri + "\n");
		// C
		Model model = ModelFactory.createDefaultModel();
		try {
			if (contentType.contains("rdf+xml")) {
				model.read(uri.toString(), uri, "RDF/XML");
			} else if (contentType.contains("text/plain")) {
				model.read(uri.toString(), uri, "N-TRIPLE");
			} else if (contentType.contains("n3")) {
				model.read(uri.toString(), uri, "N3");
			} else if (contentType.contains("turtle")) {
				model.read(uri.toString(), uri, "TURTLE");
			}else if (contentType.contains("html")) {
				model.read(uri.toString(), uri, null);
			}
		} catch (RiotException e) {
			_log.warning("[cannot parse file]:" + uri);
			return;
		} 
		if (model == null) {
			return;
		}
		// StmtIterator stmtIterator = model.listStatements();
		// while( stmtIterator.hasNext()){
		// System.out.println(stmtIterator.nextStatement().toString());
		// }
		// find
		List<Correction> corrections = new ArrayList<Correction>();
		Relevance r = RelevanceFactory.getRelevance();
		try {
			corrections = r.getRelatedCorrections(new URI(uri));
			while (corrections == null) {
				System.out.println("waiting for the result: " + uri);
				Thread.sleep(500);
				corrections = r.getRelatedCorrections(new URI(uri));
			}
			System.out.println("get correction for " + uri);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (Correction correction : corrections) {
			if (correction.getType() == Type.SUBSTITUTION) {
				searchForSubstitution(model, correction, uri);
			} else if (correction.getType() == Type.ADDITION) {
				searchForAddition(model, correction, uri);
			} else if (correction.getType() == Type.DELETION) {
				searchForDeletion(model, correction, uri);
			}
		}
		// CEND
	}

	/**
	 * execute SPARQL select query
	 * 
	 * @param queryStirng
	 * @param model
	 *            the model being queried
	 * @return
	 */
	public ResultSet SPARQL_SELECT(String queryString, Model model) {
		return SPARQL_SELECT(queryString, model, null);
	}

	/**
	 * execute SPARQL select query
	 * 
	 * @param queryStirng
	 * @param model
	 *            the model being queried
	 * @param baseURI
	 * @return
	 */
	public ResultSet SPARQL_SELECT(String queryString, Model model,
			String baseURI) {
		Query query = QueryFactory.create(queryString, baseURI);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet rs = qexec.execSelect();
		rs = ResultSetFactory.copyResults(rs);
		return rs;
	}

	public Model SPARQL_CONSTRUCT(String queryString, Model model,
			String baseURI) {
		Query query = QueryFactory.create(queryString, baseURI);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		Model m = qexec.execConstruct();

		return m;
	}

	/**
	 * search for the statements that match SPARQL pattern and with oldValue
	 * 
	 * @param model_Target
	 * @param correction
	 */
	public void searchForSubstitution(Model model_Target,
			Correction correction, String baseURI) {

		Model m = SPARQL_CONSTRUCT(correction.getAccessionPath(), model_Target,
				baseURI);
		//if no result
		if (m.size() <= 0) {
			return;
		}
		StmtIterator stmtIterator = m.listStatements();
		List<Statement> stmtList = stmtIterator.toList();
		// get oldValue
		Model oldValue_Model = correction.getOldValue();
		StmtIterator oldValue_stmt = oldValue_Model.listStatements();
		List<Statement> oldValue_stmtList = oldValue_stmt.toList();
		// if no oldvalue
		if (oldValue_stmtList.size() < 0) {
			return;
		}
		RDFNode oldObjNode = oldValue_stmtList.get(0).getObject();

		// get newValue
		Model newValue_Model = correction.getNewValue();
		StmtIterator newValue_stmt = newValue_Model.listStatements();
		List<Statement> newValue_stmtList = newValue_stmt.toList();
		// if no newValue
		if (newValue_stmtList.size() < 0) {
			return;
		}
		RDFNode newObjNode = newValue_stmtList.get(0).getObject();

		for (Statement statement : stmtList) {

			RDFNode currentObjNode = statement.getObject();

			String currentObjValue = "";
			String oldObjValue = "";
			String newObjValue = "";
			if (currentObjNode.isLiteral() && oldObjNode.isLiteral()) {
				currentObjValue = ((Literal) currentObjNode).getLexicalForm();
				oldObjValue = ((Literal) oldObjNode).getLexicalForm();
				newObjValue = ((Literal) newObjNode).getLexicalForm();
				_m = _approachingM;
			} else if (currentObjNode.isResource() && oldObjNode.isResource()) {
				// TODO: add support for the resource
				currentObjValue = ((Resource) currentObjNode).getURI();
				oldObjValue = ((Resource) oldObjNode).getURI();
				newObjValue = ((Resource) newObjNode).getURI();
				_m = _exactM;
			} else {
				// current and old value have different type
				continue;
			}
			if (_m.ifmatch(currentObjValue, oldObjValue) && !currentObjValue.equals(newObjValue)) {
				String patchFileName = "Patches/Patch"
						+ UUID.randomUUID().toString();

				PatchProducer pp = new PatchProducer();

				// old value may be in many place
				StmtTURTLEOutput t = new StmtTURTLEOutput();

				Map<String, String> nsMap = m.getNsPrefixMap();
				pp.appendPrefix(nsMap);
				pp.appendDelete(t.getTURTLEStatements(statement, nsMap));

				pp.appendAdd(t.getTURTLEStatements(
						statement.changeObject(newObjNode), nsMap));
				pp.print(patchFileName);

				printInfo(baseURI, correction, patchFileName);
			}

		}
		// while(rs.hasNext()){
		// QuerySolution qs = rs.nextSolution();
		// Iterator<String> varNames = qs.varNames();
		// //TODO: now only support for one column
		// // ArrayList<RDFNode> nodeList = new ArrayList<RDFNode>();
		// // while(varNames.hasNext()){
		// // RDFNode node = qs.get(varNames.next());
		// // nodeList.add(node);
		// // }
		// //TODO: check type
		// RDFNode sub = qs.get("s");
		// Property pre = qs.get
		// RDFNode obj = qs.get("o");
		// RDFNode oldvalue = qs.get("oldvalue");
		// // if (nodeList.size() < 4) {
		// // _log.warning("the length of result should be 4!");
		// // return;
		// // }
		// String result = "";
		// if (oldvalue.isLiteral()) {
		// result = ((Literal)oldvalue).getLexicalForm();
		// } else if (oldvalue.isResource()) {
		// //TODO: add support for the resource
		// result = ((Resource) oldvalue).getURI();
		// }
		// if (result.equals(correction.getOldValue())) {
		// String patchFileName = "Patch"+new Date().toString();
		//
		// PatchProducer pp = new PatchProducer();
		//
		//
		// //old value may be in many place
		// Statement oldStatement =
		// ResourceFactory.createStatement(sub.asResource(),
		// (Property)pre.asResource(), obj);
		// pp.appendDelete(PrintUtil.print(oldStatement));
		// pp.appendAdd(sub.asNode().toString()+" "+pre.toString()+" "+obj.toString());
		// pp.print();
		//
		// printInfo(baseURI, correction, patchFileName);
		// }
		// }
	}

	/**
	 * search for if there is match information(position) for Deletion
	 * 
	 * @param model_Target
	 * @param correction
	 * @param baseURI
	 */
	public void searchForDeletion(Model model_Target, Correction correction,
			String baseURI) {

		Model m = SPARQL_CONSTRUCT(correction.getAccessionPath(), model_Target,
				baseURI);
		//if no result
		if (m.size() <= 0) {
			return;
		}
		StmtIterator stmtIterator = m.listStatements();
		List<Statement> stmtList = stmtIterator.toList();
		// get oldValue
		Model oldValue_Model = correction.getOldValue();
		StmtIterator oldValue_stmt = oldValue_Model.listStatements();
		List<Statement> oldValue_stmtList = oldValue_stmt.toList();
		// if no oldvalue
		if (oldValue_stmtList.size() < 0) {
			return;
		}
		RDFNode oldObjNode = oldValue_stmtList.get(0).getObject();
		for (Statement statement : stmtList) {
			RDFNode oldvalue = statement.getObject();

			RDFNode currentObjNode = statement.getObject();

			String currentObjValue = "";
			String oldObjValue = "";
			if (currentObjNode.isLiteral() && oldObjNode.isLiteral()) {
				currentObjValue = ((Literal) currentObjNode).getLexicalForm();
				oldObjValue = ((Literal) oldObjNode).getLexicalForm();
				_m = _approachingM;
			} else if (currentObjNode.isResource() && oldObjNode.isResource()) {
				// TODO: add support for the resource
				currentObjValue = ((Resource) currentObjNode).getURI();
				oldObjValue = ((Resource) oldObjNode).getURI();
				_m = _exactM;
			} else {
				// current and old value have different type
				continue;
			}

			if (_m.ifmatch(currentObjValue, oldObjValue)) {
				String patchFileName = "Patches/Patch"
						+ UUID.randomUUID().toString();

				PatchProducer pp = new PatchProducer();

				// StmtRDFXMLOutput o = new StmtRDFXMLOutput();
				// o.writeRDFStatements(m,statement);

				StmtTURTLEOutput t = new StmtTURTLEOutput();

				Map<String, String> nsMap = m.getNsPrefixMap();
				pp.appendPrefix(nsMap);
				pp.appendDelete(t.getTURTLEStatements(statement, nsMap));
				pp.print(patchFileName);

				printInfo(baseURI, correction, patchFileName);
			}

		}
	}

	/**
	 * search for if there is match information(position) for Addition
	 * 
	 * @param model_Target
	 * @param correction
	 * @param baseURI
	 */
	public void searchForAddition(Model model_Target, Correction correction,
			String baseURI) {
		Model m = SPARQL_CONSTRUCT(correction.getAccessionPath(), model_Target,
				baseURI);
		//if no result
		if (m.size() <= 0) {
			return;
		}
		// get newValue
		Model newValue_Model = correction.getNewValue();
		StmtIterator newValue_stmt = newValue_Model.listStatements();
		List<Statement> newValue_stmtList = newValue_stmt.toList();
		// if no newValue
		if (newValue_stmtList.size() < 0) {
			return;
		}
		//whether the new value is already there
		for (Statement statement : newValue_stmtList) {
			if (m.contains(statement)) {
				return;
			}
		}
		
		 String patchFileName = "Patches/Patch" + UUID.randomUUID().toString();


		PatchProducer pp = new PatchProducer();

		StmtTURTLEOutput t = new StmtTURTLEOutput();

		Map<String, String> nsMap = m.getNsPrefixMap();
		pp.appendPrefix(nsMap);
		Statement[] statements = newValue_stmtList.toArray(new Statement[newValue_stmtList.size()]);
		pp.appendAdd(t.getTURTLEStatements(statements, nsMap));
		pp.print(patchFileName);

		printInfo(baseURI, correction, patchFileName);
		
	}

	private void printInfo(String uri, Correction correction,
			String patchFileName) {
		// TODO: print the whole statements
		String info = new Date() + ": find defect in " + uri + "\n";
		info += "-----------------------\n";
		info += correction.toString();
		info += "Patch: " + patchFileName + "\n";
		info += "-----------------------\n";
		try {
			_out.write(info.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
