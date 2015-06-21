package com.ontologycentral.ldspider.seen;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.algebra.evaluation.function.datetime.Seconds;

import com.ontologycentral.ldspider.CrawlerConstants;

public class HashSetSeen implements Seen {

	Set<URI> _set;
	//CHANGE:add last seen time
	Map<URI, Date> _lastSeenTime;
	Object lock = new Object();

	public HashSetSeen() {
		_set = Collections.synchronizedSet(new HashSet<URI>());
		//CHANGE
		_lastSeenTime = Collections.synchronizedMap(new HashMap<URI, Date>());
	}

	public boolean hasBeenSeen(URI u) {
		//CHANGE
		if (_set.contains(u)) {
			Date lastCheckTime = _lastSeenTime.get(u);
			//calculate the diff in seconds
			//consider that haven't been seen when it's over doublecheck interval
			return (new Date().getTime() - lastCheckTime.getTime())/1000 < CrawlerConstants.URIDOUBLECHECKINTERVAL; 
		}
		return false;
	}

	public boolean add(Collection<URI> uris) {
		boolean result = _set.addAll(uris);
		//CHANGE
		Date currentTime = new Date();
		for (URI uri : uris) {
			synchronized(lock) {
				_lastSeenTime.put(uri, currentTime);
			}			
		}
		return result;
		
	}

	public boolean add(URI uri) {
		boolean result = _set.add(uri);
		//CHANGE
		synchronized(lock) {
			Date currentTime = new Date();
			_lastSeenTime.put(uri, currentTime);
		}
		return result;
	}

	public void clear() {
		_set.clear();

	}

}
