package com.aote.lodspider.corrections;


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
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CorrectionParser {
	public static Collection<Correction> parse() {
		Collection<Correction> corrections = new ArrayList<Correction>();
		// TODO: read from config file
		String uri = "http://localhost:8080/marmotta/export/download?context=http://localhost:8080/marmotta/context/default&format=application/xml";
		InputStream in = readFromURI(uri);
		if(in != null){	
			corrections = parse(readFromURI(uri));
		}
//			corrections = parse(new FileInputStream("Corrections/corrections"));
		return corrections;
	}

	public static Collection<Correction> parse(InputStream source) {

		Model model = ModelFactory.createDefaultModel();
		model.read(source, "");
		StmtIterator stmtIterator = model.listStatements();
		Map<String, Correction> correctionMap_temp = new HashMap<String, Correction>();
		Map<String, Correction> correctionMap = new HashMap<String, Correction>();
		Map<String, Integer> correction_CountMap = new HashMap<String, Integer>();
		
		while (stmtIterator.hasNext()) {
			Statement nextStatement = stmtIterator.next();
			Resource sub = nextStatement.getSubject();
			Resource pre = nextStatement.getPredicate();
			RDFNode obj = nextStatement.getObject();

			Correction correction = correctionMap_temp.get(sub.toString());
			Integer count = correction_CountMap.get(sub.toString());
			// initiate new instance for new sub
			// except node "http://localhost/Corrections" which holds all
			// corrections
			if (correction == null && count == null) {
				correction = new Correction();
				count = new Integer(0);
				correctionMap_temp.put(sub.toString(), correction);
				correction_CountMap.put(sub.toString(), count);
			}
			if (pre.toString().equals("http://localhost/corrections#type")) {
				correction.setType(Type.valueOf(obj.toString().toUpperCase()));
				count+=1;
				correction_CountMap.put(sub.toString(), count);
			} else if (pre.toString().equals(
					"http://localhost/corrections#publishedDate")) {
				Date publishedDate;
				try {
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					publishedDate = df.parse(obj.toString());
					correction.setPublishedDate(publishedDate);
					count+=1;
					correction_CountMap.put(sub.toString(), count);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (pre.toString().equals(
					"http://localhost/corrections#sourceURI")) {
				try {
					correction.setSourceURI(new URI(obj.toString()));
					count+=1;
					correction_CountMap.put(sub.toString(), count);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (pre.toString().equals(
					"http://localhost/corrections#accessionPath")) {
				correction.setAccessionPath(obj.toString());
				count+=1;
				correction_CountMap.put(sub.toString(), count);
			} else if (pre.toString().equals(
					"http://localhost/corrections#oldValue")) {
				correction.setOldValue(obj.toString());
				count+=1;
				correction_CountMap.put(sub.toString(), count);
			} else if (pre.toString().equals("http://localhost/corrections#newValue")) {
				correction.setNewValue(obj.toString());
				count+=1;
				correction_CountMap.put(sub.toString(), count);
			}
			
			//Check if correction has been fully set
			if(count.equals(6)){
				System.out.println(correction.toString());
				correctionMap.put(sub.toString(), correction);
			}

		}

		return correctionMap.values();
	}

	public static InputStream readFromURI(String uri){
		InputStream in = null;
		try {
			HttpGet httpget = new HttpGet(uri);
			// httpget.setHeaders(CrawlerConstants.HEADERS);
			HttpContext context = new BasicHttpContext();
			HttpResponse response;
			response = new DefaultHttpClient().execute(httpget, context);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				throw new IOException(response.getStatusLine().toString());
			// HttpUriRequest currentReq = (HttpUriRequest)
			// context.getAttribute(
			// ExecutionContext.HTTP_REQUEST);
			// HttpHost currentHost = (HttpHost) context.getAttribute(
			// ExecutionContext.HTTP_TARGET_HOST);
			// String currentUrl = (currentReq.getURI().isAbsolute()) ?
			// currentReq.getURI().toString() : (currentHost.toURI() +
			// currentReq.getURI());
//			Header ct = response.getFirstHeader("Content-Type");
//			if (ct != null) {
//				System.out.println(response.getFirstHeader("Content-Type")
//						.getValue());
//			}
			HttpEntity httpEntity = response.getEntity();
			in = httpEntity.getContent();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return in;
	}
	public static void main(String[] args) {

		CorrectionParser.parse();

	}

}
