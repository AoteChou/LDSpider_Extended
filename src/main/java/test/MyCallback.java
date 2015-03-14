package test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.CallbackRDFXMLOutputStream;

public class MyCallback implements Callback{
	private static Logger _log = Logger.getLogger(MyCallback.class.getName());

	OutputStream _out;
	
	MyCallback(OutputStream out){
		_out = out;
	}

	@Override
	public void startDocument() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endDocument() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processStatement(Node[] nx) {
		//check length of triple
		if (nx.length < 3) {
			_log.warning("length should be at least 3!");
		}
		
		//check predicate
		if (nx[1] instanceof Resource) {
			_log.warning("perdicate should be resource!");
		}
				
		String subjID = nx[0].toString();
		String predID = nx[1].toString();
		String objID = nx[2].toString();
		
		
		//if (subjID.contains("Scorpion_toxinL/defesin") || objID.contains("Scorpion_toxinL/defesin")) {
		if (objID.contains("2015-01-01")) {
		
			//System.out.println("Matching found!"+new Date());
			try {
				_out.write(("Matching found!"+new Date()+"\n").getBytes("utf-8"));
				_out.write((subjID+"  "+predID+"  "+objID+"\n").getBytes("utf-8"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(subjID+"  "+predID+"  "+objID);
		
		
	}

}
