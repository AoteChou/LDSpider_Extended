package com.ontologycentral.ldspider.frontier;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RankedFrontier extends Frontier {
	
	// Keeps an "eternal" list of URIs for the rank to take into account
	// all hops. Maybe a different implementation could save RAM.
	Map<String, Integer> _data;
	Set<URI> _unscheduledUris;
	Set<String> _domainVisited;

	Object lock = new Object();
	
	OutputStream _out, _out2;
	
	public RankedFrontier() {
		super();
		_data = Collections.synchronizedMap(new HashMap<String, Integer>());
		_unscheduledUris = Collections.synchronizedSet(new HashSet<URI>());
		_domainVisited = Collections.synchronizedSet(new HashSet<String>());
		try {
			_out = new FileOutputStream("FrontierQueue");
			_out2 = new FileOutputStream("FrontierQueue_Domain");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
 	}

	public void add(URI u) {
		u = process(u);
		
		if (u != null) {
			Integer count;
			_unscheduledUris.add(u);
			
			synchronized(lock) {
				count = _data.get(u.toString());
				if (count == null) {
					count = 1;
				} else {
					count++;
				}
				_data.put(u.toString(), count);
			}
			try {
				
				_out.write((new Date()+"  new URL:  "+u.toString()+"\n\n").getBytes("utf-8"));
				_out.write("____________________\n".getBytes());
				for (URI uri : _unscheduledUris) {
					
						_out.write(uri.toString().getBytes());
						_out.write(("  [Count:"+ _data.get(uri.toString())+"]").getBytes());
						_out.write("\n".getBytes());
			
				}
				_out.write("_______________\n".getBytes());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String domain = u.getHost();
			if(!_domainVisited.contains(domain)){
				_domainVisited.add(domain);
				try {
					_out2.write((new Date()+"  new domain:  "+domain+"\n\n").getBytes("utf-8"));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			_log.fine("added " + u);
		}
	}

	public void remove(URI u) {
		_data.remove(u.toString());
		_unscheduledUris.remove(u);
	}

	public Iterator<URI> iterator() {
		final List<URI> li = new ArrayList<URI>();

		li.addAll(_unscheduledUris);

		Collections.sort(li, new DescendingCountComparatorAlph<URI>(_data));
		
		return new Iterator<URI>() {
			Iterator<URI> it = li.iterator();
			URI currentUri;

			public boolean hasNext() {
				return it.hasNext();
			}

			public URI next() {
				return currentUri = it.next();
			}

			public void remove() {
				_data.remove(currentUri.toString());
				_unscheduledUris.remove(currentUri);
			}
		};
	}
	
	public void clearCount(URI uri){
		_data.remove(uri);
	}

	public void removeAll(Collection<URI> c) {
		for (URI u : c) {
			remove(u);
		}
	}

	public void reset() {
		_unscheduledUris.clear();
	}
}