package com.ontologycentral.ldspider.hooks.content;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.nx.util.NxUtil;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

import com.aote.lodspider.corrections.Correction;
import com.aote.lodspider.relevance.Relevance_URI;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;


/**
 * Handles RDF/XML documents.
 * 
 * @author RobertIsele
 */
public class ContentHandlerRdfXml implements ContentHandler {

	private final Logger _log = Logger.getLogger(this.getClass().getName());
	
	public static final String[] MIMETYPES = { "application/rdf+xml", "application/xml" };
	
	public boolean canHandle(String mime) {
		for (String ct : MIMETYPES) {
			if (mime.contains(ct)) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean handle(URI uri, String mime, InputStream source, Callback callback) {
		try {
//			RDFXMLParser r =
			//CHANGE:judge the relevance
//					Model model = ModelFactory.createDefaultModel();
//					model.read(source, uri.toString());
//					model.write(new FileOutputStream("justGiveATry",true));
					new RDFXMLParser(source, true, true, uri.toString(), callback, new Resource(NxUtil.escapeForNx(uri.toString())));
//			RDFXMLParser r = new RDFXMLParser(source, true, false, uri.toString());
//			while (r.hasNext()) {
//				Node[] nx = r.next();
//				
//				callback.processStatement(nx);
//				
//				//_log.info("processing " + Nodes.toN3(nx));
//			}
			//, callback, new Resource(uri.toString()))
			
			return true;
		} catch (ParseException e) {
			_log.log(Level.INFO, "Could not parse document", e);
			return false;
		} catch (IOException e) {
			_log.log(Level.WARNING, "Could not read document", e);
			return false;
		}
	}
	
	public String[] getMimeTypes() {
		return MIMETYPES;
	}

}
