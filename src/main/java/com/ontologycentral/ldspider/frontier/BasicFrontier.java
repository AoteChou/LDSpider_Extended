package com.ontologycentral.ldspider.frontier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BasicFrontier extends Frontier {
	Set<URI> _data;
	
	public BasicFrontier() {
		super();
		_data = Collections.synchronizedSet(new HashSet<URI>());
	}
	
	public synchronized void add(URI u) {
		u = process(u);
		if (u != null) {
			_data.add(u);
			//added to record urls added
			try {
				OutputStream _out = new FileOutputStream("FrontierQueue", true);
				_out.write((new Date()+"  new URL:  "+u.toString()+"\n\n").getBytes("utf-8"));
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
			//end
		}
	}
	
	public Iterator<URI> iterator() {
		return _data.iterator();
	}

	public void removeAll(Collection<URI> c) {
		_data.removeAll(c);
	}
	
	public void reset() {
		_data.clear();
	}
	
	public String toString() {
		return _data.toString();
	}
}