package me.crossle.vaircast;

import android.util.Log;

import me.crossle.vaircast.NanoHTTPD.Response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Map;

public class Nano extends NanoHTTPD {

	public Nano(int port) {
	  super(port);
    
  }
	
	public void start() {
		try {
	    super.start();
    } catch (IOException e) {
	    e.printStackTrace();
    }
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		String uri = session.getUri();
		 Map<String, String>  map = session.getHeaders();
		 Iterator iter = map.entrySet().iterator(); 
		 while (iter.hasNext()) {
		    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
		    String key = entry.getKey();
		    String val = entry.getValue();
		    Log.e("====sss====", key+"==="+val);
		} 
		File file = new File(uri);
	
		Response helloResponse = null;
    try {
	    helloResponse = new Response(Status.OK, "video/mp4", new FileInputStream(file));
    } catch (FileNotFoundException e) {
	    e.printStackTrace();
    }
    helloResponse.addHeader("Accept-Ranges", "bytes");
	  return helloResponse;
	}
}
