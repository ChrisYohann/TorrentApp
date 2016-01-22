package opentracker;

import java.io.*;

public class StreamDisplay implements Runnable {
	private final InputStream input ;
	
	public StreamDisplay(InputStream is){
		this.input = is ;
	}
	
	 private BufferedReader getBufferedReader(InputStream is) {
	        return new BufferedReader(new InputStreamReader(is));
	    }

	 @Override
	    public void run() {
	        BufferedReader br = getBufferedReader(input);
	        String ligne = "";
	        try {
	            while ((ligne = br.readLine()) != null) {
	                System.out.println(ligne);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
}






