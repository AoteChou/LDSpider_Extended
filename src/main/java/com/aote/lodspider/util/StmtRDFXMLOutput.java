package com.aote.lodspider.util;

import java.io.PrintWriter;
import java.util.Map;

import javafx.stage.Stage;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
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
public class StmtRDFXMLOutput {
	public StmtRDFXMLOutput() {
		setStringBuffer(new StringBuffer());
		space =" ";
	}

	private String space;
	private StringBuffer stringBuffer;
	private Map<String, String> ns;
	private Model currentModel;

	// @Override protected void writeBody
	// ( Model model, PrintWriter pw, String base, boolean inclXMLBase )
	// {
	// setSpaceFromTabCount();
	// writeRDFHeader( model, pw );
	// writeRDFStatements( model, pw );
	// writeRDFTrailer( pw, base );
	// pw.flush();
	// }
	//
	// private void setSpaceFromTabCount()
	// {
	// space = "";
	// for (int i=0; i < tabSize; i += 1) space += " ";
	// }
	//
	// protected void writeSpace( PrintWriter writer )
	// { stringBuffer.append( space ); }

	// private void writeRDFHeader(Model model, PrintWriter writer)
	// {
	// String xmlns = xmlnsDecl();
	// stringBuffer.append( "<" + rdfEl( "RDF" ) + xmlns );
	// if (null != xmlBase && xmlBase.length() > 0)
	// stringBuffer.append( "\n  xml:base=" + substitutedAttribute( xmlBase ) );
	// stringBuffer.append("\n"+ " > " );
	// }

//	public void writeRDFStatements(Model model) {
//		ResIterator rIter = model.listSubjects();
//		while (rIter.hasNext())
//			writeRDFStatements(model, rIter.nextResource());
//	}

	// protected void writeRDFTrailer( PrintWriter writer, String base )
	// { stringBuffer.append("\n"+ "</" + rdfEl( "RDF" ) + ">" ); }

	/**
	 * write statements in the model with specific subject
	 * @param model
	 * @param subject
	 */
	public void writeRDFStatements(Model model, Resource subject) {
		setNs(model.getNsPrefixMap());
		currentModel = model;
		stringBuffer.setLength(0);
		StmtIterator sIter = model
				.listStatements(subject, null, (RDFNode) null);
		writeDescriptionHeader(subject);
		while (sIter.hasNext())
			writePredicate(sIter.nextStatement());
		writeDescriptionTrailer(subject);
	}
	
	public void writeRDFStatements(Model model, Statement statement) {
		setNs(model.getNsPrefixMap());
		currentModel = model;
		stringBuffer.setLength(0);
		writeDescriptionHeader(statement.getSubject());
		writePredicate(statement);
		writeDescriptionTrailer(statement.getSubject());
	}
	/**
	 * write statements in the model with specific subject
	 * and replace predicate and object with the newValue String
	 * @param model
	 * @param subject
	 */
	public void writeRDFStatements(Model model, Resource subject, String newValue) {
		setNs(model.getNsPrefixMap());
		currentModel = model;
		stringBuffer.setLength(0);
		writeDescriptionHeader(subject);
		stringBuffer.append(newValue);
		writeDescriptionTrailer(subject);
	}
	
	protected void writeDescriptionHeader(Resource subject) {
		getStringBuffer().append("\n"+space + "<rdf:Description ");
		writeResourceId(subject);
		getStringBuffer().append( ">");
	}

	protected void writePredicate(Statement stmt) {
		final Property predicate = stmt.getPredicate();
		final RDFNode object = stmt.getObject();

		getStringBuffer().append(
				"\n"+space + space + "<" + currentModel.getNsURIPrefix(predicate.getNameSpace()) 
				+ ":"
						+ predicate.getLocalName());

		if (object instanceof Resource) {
			getStringBuffer().append(" ");
			writeResourceReference(((Resource) object));
			getStringBuffer().append("/>"+"\n");
		} else {
			writeLiteral((Literal) object);
			getStringBuffer().append(
					 "</" + currentModel.getNsURIPrefix(predicate.getNameSpace())  + ":"
							+ predicate.getLocalName() + ">"+"\n");
		}
	}

	// @Override protected void unblockAll()
	// { blockLiterals = false; }
	//
	// private boolean blockLiterals = false;
	//
	// @Override protected void blockRule( Resource r ) {
	// if (r.equals( RDFSyntax.parseTypeLiteralPropertyElt )) {
	// // System.err.println("Blocking");
	// blockLiterals = true;
	// } else
	// logger.warn("Cannot block rule <"+r.getURI()+">");
	// }

	protected void writeDescriptionTrailer(Resource subject) {
		getStringBuffer().append( space + "</ rdf:Description >"+"\n");
	}

	protected void writeResourceId(Resource r) {
		if (r.isAnon()) {
			getStringBuffer().append("rdf:nodeID" + "=" + r.getURI());
		} else {
			getStringBuffer().append("rdf:about" + "=" + r.getURI());
		}
	}

	protected void writeResourceReference(Resource r) {
		if (r.isAnon()) {
			getStringBuffer().append("rdf:nodeID" + "=" + r.getURI());
		} else {
			getStringBuffer().append("rdf:about" + "=" + r.getURI());
		}
	}

	protected void writeLiteral(Literal l) {
		String lang = l.getLanguage();
		String form = l.getLexicalForm();
		if (Util.isLangString(l)) {
			getStringBuffer().append(" xml:lang=\"" + lang + "\"");
		} else if (l.isWellFormedXML()) {
			// RDF XML Literals inline.
			getStringBuffer().append(
					" rdf:parseType" + "=\"" + "Literal" + "\">");
			getStringBuffer().append(form);
			return;
		} else {
			// Datatype (if not xsd:string and RDF 1.1)
			String dt = l.getDatatypeURI();
			if (!Util.isSimpleString(l))
				getStringBuffer().append(" rdf:datatype" + "=\"" + dt + "\"");
		}
		// Content.
		getStringBuffer().append(">");
		getStringBuffer().append(Util.substituteEntitiesInElementContent(form));
	}
	
	
	public String getResult(){
		return stringBuffer.toString();
	}

	public StringBuffer getStringBuffer() {
		return stringBuffer;
	}

	public void setStringBuffer(StringBuffer stringBuffer) {
		this.stringBuffer = stringBuffer;
	}

	public Map<String, String> getNs() {
		return ns;
	}

	public void setNs(Map<String, String> ns) {
		this.ns = ns;
	}

}
