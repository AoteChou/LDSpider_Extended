package com.ontologycentral.ldspider.http;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.Callbacks;

import com.aote.lodspider.callback.SearchBySPARQL;
import com.aote.lodspider.corrections.Correction;
import com.aote.lodspider.relevance.Relevance;
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
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.hooks.content.ContentHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.hooks.sink.Provenance;
import com.ontologycentral.ldspider.hooks.sink.Sink;
import com.ontologycentral.ldspider.http.robot.Robots;
import com.ontologycentral.ldspider.queue.SpiderQueue;
import com.sun.org.apache.bcel.internal.generic.LUSHR;

public class LookupThread extends Thread {
	Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	SpiderQueue _q;
	ContentHandler _contentHandler;
	Sink _content;
	Callback _links;
	FetchFilter _ff, _blacklist;
	
	StatementCountingCallback _stmtCountingCallback;
	
	Robots _robots;
//	Sitemaps _sitemaps;
	
	ErrorHandler _eh;
	ConnectionManager _hclient;
	
	int _no;

	static AtomicInteger _overall200FetchesWithRDF = new AtomicInteger(0);
	static AtomicInteger _overall200Fetches = new AtomicInteger(0);

	public LookupThread(ConnectionManager hc, SpiderQueue q, ContentHandler handler, Sink content, Callback links, Robots robots, ErrorHandler eh, FetchFilter ff, FetchFilter blacklist, int no) {
		_hclient = hc;
		_q = q;
		_contentHandler = handler;
		_content = content;
		_links = links;
		_robots = robots;
		_ff = ff;
		_blacklist = blacklist;
		_eh = eh;
		
		_no = no;
		
		_stmtCountingCallback = new StatementCountingCallback();
		
		setName("LT-"+_no);
	}
	
	public void run() {
		_log.info("starting thread ...");
		
		if (!(!CrawlerConstants.URI_LIMIT_ENABLED || (_overall200FetchesWithRDF.get() < CrawlerConstants.URI_LIMIT_WITH_NON_EMPTY_RDF))) {
			_log.info("URI limit reached. Stopping...");
			return;
		}
		
		int i = 0;

		URI lu = _q.poll();

		_log.fine("got " + lu);
		
		while (lu != null) {
			
			if (!(!CrawlerConstants.URI_LIMIT_ENABLED || (_overall200FetchesWithRDF.get() < CrawlerConstants.URI_LIMIT_WITH_NON_EMPTY_RDF))) {
				_log.info("URI limit reached. Stopping...");
				break;
			}
				
			
			setName("LT-"+_no+":"+lu.toString());
			
			_q.addSeen(lu);
			
			i++;
			long time = System.currentTimeMillis();
			
//				URI lu = _q.obtainRedirect(u);

			long time1 = System.currentTimeMillis();
			long time2 = time1;
			long time3 = time1;
			long bytes = -1;
			int status = 0;
			String type = null;
			
//			List<URI> li = _sitemaps.getSitemapUris(lu);
//			if (li != null && li.size() > 0) {
//				_log.info("sitemap surprisingly actually has uris " + li);
//			}
			
			Header[] headers = null;
			
			if (!_blacklist.fetchOk(lu, 0, null)) {
				_log.info("access denied per blacklist for " + lu);
				_eh.handleStatus(lu, CrawlerConstants.SKIP_SUFFIX, null, 0, -1);
			} else if (!_robots.accessOk(lu)) {
				_log.info("access denied per robots.txt for " + lu);
				_eh.handleStatus(lu, CrawlerConstants.SKIP_ROBOTS, null, 0, -1);
			} else {
				time2 = System.currentTimeMillis();

				HttpGet hget = new HttpGet(lu);
				hget.setHeaders(CrawlerConstants.HEADERS);
				
				try {
					HttpResponse hres = _hclient.connect(hget);

					HttpEntity hen = hres.getEntity();

					status = hres.getStatusLine().getStatusCode();

					Header ct = hres.getFirstHeader("Content-Type");
					if (ct != null) {
						type = hres.getFirstHeader("Content-Type").getValue();
					}
					
					_log.info("lookup on " + lu + " status " + status + " " + getName());

					if (status == HttpStatus.SC_OK) {				
						if (hen != null) {
							if (_ff.fetchOk(lu, status, hen) && _contentHandler.canHandle(type)) {
								//change: move this ahead to add redirect first, so the redirect URI can get relative corrections
								headers = hres.getAllHeaders();

								Header hloc = hres.getFirstHeader("Content-Location");
								URI to = null;
								if (hloc != null) {
									to = new URI(hloc.getValue());
									
									// handle local redirects
									if (!to.isAbsolute()) {
										to = lu.resolve(hloc.getValue());
									}

									_q.setRedirect(lu, to, status);	
								}
								
								//C search by sparql first
								new SearchBySPARQL().Search(lu.toString(), type);
								//CEND
								InputStream is = hen.getContent();
								Callback contentCb = _content.newDataset(new Provenance(lu, hres.getAllHeaders(), status));
								Callbacks cbs = new Callbacks(new Callback[] { contentCb, _links, _stmtCountingCallback.reset() } );
								//TODO: maybe can do without contentCB if SPARQL find something
								_contentHandler.handle(lu, type, is, cbs);
								is.close();
								
								_overall200Fetches.incrementAndGet();
								
								if (_stmtCountingCallback.getStmtCount() > 0)
									_overall200FetchesWithRDF.incrementAndGet();
								
								//System.out.println("done with " + lu);
								if (hloc != null && to != null) {
									_eh.handleRedirect(lu, to, status);
									_q.addSeen(to);
								}
								
								
							} else {
								_log.info("disallowed via fetch filter " + lu + " type " + type);
								_eh.handleStatus(lu, CrawlerConstants.SKIP_MIMETYPE, null, 0, -1);
								hget.abort();
								hen = null;
								status = 0;
							}
						} else {
							_log.info("HttpEntity for " + lu + " is null");
						}
					} else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY || status == HttpStatus.SC_SEE_OTHER || status == HttpStatus.SC_TEMPORARY_REDIRECT) { 
						// treating all redirects the same but shouldn't: 301 -> rename context URI, 302,307 -> keep original context URI, 303 -> spec inconclusive
						Header[] loc = hres.getHeaders("location");
						String path = loc[0].getValue();
						_log.info("redirecting (" + status + ") to " + path);
						URI to = new URI(path);
						
						// handle local redirects
						if (!to.isAbsolute()) {
							to = lu.resolve(path);
						}

						// set redirect from original uri to new uri
						_q.setRedirect(lu, to, status);
						_eh.handleRedirect(lu, to, status);
	
						headers = hres.getAllHeaders();
					}

					if (hen != null) {
						bytes = hen.getContentLength();
					}
					hget.abort();
				} catch (Throwable e) {
					hget.abort();
					_log.warning("Exception " + e.getClass().getName() + " " + lu);
					_eh.handleError(lu, e);
				}
				
				time3 = System.currentTimeMillis();
				
				if (status != 0) {
					_eh.handleStatus(lu, status, headers, (time3-time2), bytes);
				}
				
				_log.fine(lu + " " + (time1-time) + " ms before lookup, " + (time2-time1) + " ms to check if lookup is ok, " + (time3-time2) + " ms for lookup");
			}

			lu = _q.poll();
		}
		
		_log.info("finished thread after fetching " + i + " uris; " + getOverall200Fetches() + " in all threads overall until now (" + getOverall200FetchesWithNonEmptyRDF() + " with non-empty RDF).");
	}
	
	public static int getOverall200FetchesWithNonEmptyRDF() {
		return _overall200FetchesWithRDF.get();
	}
	public static int getOverall200Fetches() {
		return _overall200Fetches.get();
	}
	


}