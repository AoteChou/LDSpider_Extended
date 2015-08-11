package com.aote.lodspider.relevance;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.riot.RiotException;
import org.semanticweb.yars.nx.namespace.Namespace;
import org.semanticweb.yars.nx.parser.NxParser;

import com.aote.lodspider.corrections.Correction;
import com.aote.lodspider.corrections.CorrectionParser;
import com.aote.lodspider.exception.URIParseException;
import com.aote.lodspider.util.XMLParser;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.util.FileManager;
import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.http.ConnectionManager;

public class Relevance_URI extends Relevance{
	
	private final Logger _log = Logger.getLogger(this.getClass().getName());	
	
	public Relevance_URI(){
		super();
	}
	public Relevance_URI(double dividingScore){
		super(dividingScore);
	}
	
	public synchronized List<Correction> getRelatedCorrections(URI uri){
//		Boolean flag = _flags.get(uri);
//		while(flag == null || flag == false){
//			try {
//				System.out.println("W  "+uri);
//				wait();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		CountDownLatch countDownLatch = _flags.get(uri);
//		try {
//			countDownLatch.await();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		_log.info("get"+ uri.toString());
		return _relevances.get(uri.toString());
	}

	/**
	 * 
	 * @param uri the resource Uri
	 */
	public synchronized void setRelatedCorrections(URI uri){
		if (_relevances.get(uri.toString()) == null){
			List<Correction> result = new ArrayList<Correction>();
//			CountDownLatch countDownLatch = new CountDownLatch(1);
//			_flags.put(uri, countDownLatch);
			_relevances.put(uri.toString(), result);
			for (Correction correction : _corrections) {
				double score = 0;
				try {
					score = calculateRelevance(uri, correction);
				} catch (URIParseException e) {
					//for uri fail to be parsed, just skip all corrections
					return;
				}
				if(  score >= dividingScore){
//					System.out.println(calculateRelevance(uri, correction));
					result.add(correction);
					_log.info("add correction to[score: "+score+"] :"+ uri.toString());
				}
			}
//			countDownLatch.countDown();
//			System.err.println("N  "+uri);
		}
	}
	
	
	private double calculateRelevance(URI uri, Correction correction) throws URIParseException{
		//TODO:getnamespace and calculate
		//For corrections there are two options:
		//1.get namespaces from source uri
		//2.get namespaces from the result of SPARQL
//		return (Math.random())*2;
		List<String> nameSpaceList_URI = getNamespaces(uri);
		List<String> nameSpaceList_Correction = getNamespaces(correction.getSourceURI(),true);
		
		int size_URI = nameSpaceList_URI.size();
		int size_Correction = nameSpaceList_Correction.size();
		if (size_URI == 0 || size_Correction == 0) {
			return 0;
		}
		int sameNum = 0;
		
		
		for (int i = 0; i < size_Correction; i++) {
			String nameSpaceItem = nameSpaceList_Correction.get(i);
			if (nameSpaceList_URI.contains(nameSpaceItem)) {
				sameNum ++;
			}
		}
		
		double result = 0;
		if (size_Correction != 0) {
			//calculate the ratio of same nameSpace In nameSpaces of URI
			result = (double)sameNum/(double)size_Correction;
		}
		
		return result;
	}
	
	public List<String> getNamespaces(URI uri) throws URIParseException{
		return getNamespaces(uri, false);
	}
	
	private List<String> getNamespaces(URI uri, Boolean cache) throws URIParseException{
		
		if (!cache) {
			Model model = ModelFactory.createDefaultModel();
			try {
				InputStream content = dealWithURI(uri);
				if (content != null) {
					model.read(content, uri.toString());
				}
			} catch (Exception e) {
//				_log.warning("[unsupported input]:"+uri);
				throw new URIParseException();
			} 
			
			NsIterator listNameSpaces = model.listNameSpaces();
			return listNameSpaces.toList();
		}
		
		
		if(_nameSpaces.get(uri) == null){
			
			Model model = ModelFactory.createDefaultModel();
			try {
				InputStream content = dealWithURI(uri);
				if (content != null) {
					model.read(content, uri.toString());
				}
			} catch (Exception e) {
//				_log.warning("[unsupported input]:"+uri);
				throw new URIParseException();
			} 
			
			NsIterator listNameSpaces = model.listNameSpaces();
//			while (listNameSpaces.hasNext())
//			{
//		 	 System.out.println("Namespace from iterator: " + listNameSpaces.next());
//			}
			_nameSpaces.put(uri, listNameSpaces.toList());
		}
		
		return _nameSpaces.get(uri);
	}
	
	/**
	 * request for rdf/xml and return inputstream(LD-spider can read from uri but cannot deal with 303 redirect)
	 * @param uri
	 * @return response content
	 * @throws URIParseException
	 */
	private InputStream dealWithURI(URI uri) throws URIParseException {
		try {
			URLConnection con = uri.toURL().openConnection();
			con.setRequestProperty("Accept", "application/rdf+xml, application/xml");
			con.connect();
			// Cast to a HttpURLConnection
			if ( con instanceof HttpURLConnection)
			{
			   HttpURLConnection httpCon = (HttpURLConnection) con;

			   int code = httpCon.getResponseCode();
			   if(code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == HttpURLConnection.HTTP_SEE_OTHER || code == HttpURLConnection.HTTP_OK){
//				   URI redirectPath = httpCon.getURL().toURI();
//				   return redirectPath;
				   return httpCon.getInputStream();
			   } 
			   
			}else
			{
			   _log.fine("error - not a http request!["+uri+"]" );
			}
			return null;
		} catch (Exception e) {
			throw new URIParseException();
		} 
	}

	public static void main(String[] args) {
		
		Relevance_URI relevance = new Relevance_URI();
		try {
			List<String> namespaces = relevance.getNamespaces(new URI("http://localhost:8080/marmotta/resource?uri=http://localhost/Corrections"));
			for (String string : namespaces) {
				System.out.println(string);
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	catch (URIParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(Relevance.readFromURI("http://purl.uniprot.org/uniprot/P37231"));
		
		
	}

}
