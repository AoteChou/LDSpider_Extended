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
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import com.aote.lodspider.util.XMLParser;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CorrectionParser {
	public static Logger _log = Logger.getLogger("CorrectionParser");

	public static Collection<Correction> parse() throws Exception {
		Collection<Correction> corrections = new ArrayList<Correction>();
		// read xml config file
		String configFilePath = "src/main/java/com/aote/lodspider/config/CrawlerConfig.xml";
		Hashtable<String, String> configMap = XMLParser.parse(configFilePath);

		String uri = configMap.get("CorrectionURI");
		InputStream in = readFromURI(uri);
		if (in != null) {
			corrections = parse(readFromURI(uri));
		}
		// corrections = parse(new FileInputStream("Corrections/corrections"));
		if (corrections.size() <= 0) {
			throw new Exception("no correction in triple store!");
		}
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
				count += 1;
				correction_CountMap.put(sub.toString(), count);
			} else if (pre.toString().equals(
					"http://localhost/corrections#publishedDate")) {
				Date publishedDate;
				try {
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					publishedDate = df.parse(obj.toString());
					correction.setPublishedDate(publishedDate);
					count += 1;
					correction_CountMap.put(sub.toString().replace("\\", ""), count);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (pre.toString().equals(
					"http://localhost/corrections#sourceURI")) {
				try {
					correction.setSourceURI(new URI(obj.toString()));
					count += 1;
					correction_CountMap.put(sub.toString(), count);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (pre.toString().equals(
					"http://localhost/corrections#accessionPath")) {
				correction.setAccessionPath(obj.toString().replace("\\", ""));
				count += 1;
				correction_CountMap.put(sub.toString(), count);
			} else if (pre.toString().equals(
					"http://localhost/corrections#oldValue")) {
//				correction.setOldValue(obj.toString().replace("\\", ""));
				try {		
					String path = obj.toString();
					InputStream in = new FileInputStream(path);
					Model oldValue = ModelFactory.createDefaultModel();
					oldValue.read(in, "");
					correction.setOldValue(oldValue);
					correction.setOldValuePath(path);
				} catch (Exception e) {
					_log.warning("reading oldvalue error");
					_log.warning(e.toString());
				}
				count += 1;
				correction_CountMap.put(sub.toString(), count);
			} else if (pre.toString().equals(
					"http://localhost/corrections#newValue")) {
//				correction.setNewValue(obj.toString().replace("\\", ""));
				try {					
					String path = obj.toString();
					InputStream in = new FileInputStream(path);
					Model newValue = ModelFactory.createDefaultModel();
					newValue.read(in, "");
					correction.setNewValue(newValue);
					correction.setNewValuePath(path);
				} catch (Exception e) {
					_log.warning("reading oldvalue error");
				}
				count += 1;
				correction_CountMap.put(sub.toString(), count);
			}

			// Check if correction has been fully set
			if (count.equals(getSizeByCorrectionType(correction.getType()))) {
				switch (correction.getType()) {
				case SUBSTITUTION:
					correction = new Substitution(correction);
					break;
				case DELETION:
					correction = new Deletion(correction);
					break;
				case ADDITION:
					correction = new Addition(correction);
					break;
				default:
					break;
				}
				System.out.println(correction.toString());
				correctionMap.put(sub.toString(), correction);
			}

		}

		return correctionMap.values();
	}

	public static int getSizeByCorrectionType(Type type) {
		switch (type) {
		case SUBSTITUTION:
			return 6;
		case DELETION:
			return 5;
		case ADDITION:
			return 5;
		default:
			return 6;
		}
	}
	public static void parseRDFXMLIntoModel(){
		InputStream in;
		try {
			in = new FileInputStream("Corrections/4_old");
			Model model = ModelFactory.createDefaultModel();
			model.read(in, "");
			StmtIterator stmtIterator = model.listStatements();
			while (stmtIterator.hasNext()) {
				Statement statement = (Statement) stmtIterator.next();
				String sub = statement.getSubject().toString();
				String pre = statement.getPredicate().toString();
				String obj = statement.getObject().toString();
				
				System.out.println(sub+"\n"+pre+"\n"+obj);
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static InputStream readFromURI(String uri) {
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
			// Header ct = response.getFirstHeader("Content-Type");
			// if (ct != null) {
			// System.out.println(response.getFirstHeader("Content-Type")
			// .getValue());
			// }
			HttpEntity httpEntity = response.getEntity();
			in = httpEntity.getContent();
		} catch (ClientProtocolException e) {
			// e.printStackTrace();
			_log.fine(e.getMessage());
		} catch (IOException e) {
			// e.printStackTrace();
			_log.fine(e.getMessage());
		}
		return in;
	}

	public static void main(String[] args) {

		try {
			CorrectionParser.parseRDFXMLIntoModel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
