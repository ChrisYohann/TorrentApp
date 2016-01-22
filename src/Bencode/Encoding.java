package Bencode;
import BencodeType.* ;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.*;

public class Encoding {
	protected Dictionary metadata ;
	protected JSONObject json_file ;
	
	public Encoding(){
		this.metadata = new Dictionary();
		this.json_file = null ;
	}
	
	public Encoding(String filepath) throws Exception{
		this.json_file = load_from_file(filepath);
		this.metadata = this.get_from_json(json_file);
	}
	
	public void setMetadata(Dictionary dico){
		this.metadata = dico ;
	}
	
	public Dictionary getMetadata(){
		return this.metadata ;
	}

	public JSONObject load_from_file(String path) throws Exception{
	try {
		InputStream is = new FileInputStream(path) ;
	
	try{
	String jsonTxt = IOUtils.toString(is);
	JSONParser parser = new JSONParser();
	Object obj = parser.parse(jsonTxt);
	JSONObject obj_json = (JSONObject) obj ;

	return obj_json ;	
	
	}catch(IOException e){
		throw new IOException(e.getMessage());
	}
	
	} catch(FileNotFoundException f ) {
		throw new FileNotFoundException(f.getMessage());
	}
}  
	
	public static Dictionary get_from_json(JSONObject obj){
		
		
		
		Dictionary dico = new Dictionary();
		
		Set keys = obj.keySet() ;
		System.out.println(keys.size());
		Iterator it = keys.iterator() ;	
		while(it.hasNext()){
			String key = (String)it.next();
			Object value = obj.get(key);
			
			if(value.getClass().equals(JSONObject.class)){
				dico.getContenu().put(key ,get_from_json((JSONObject) value));
			
			} else if(value.getClass().equals(JSONArray.class)) {
				dico.getContenu().put(key, get_list_from_json((JSONArray) value));
			} else {
				dico.getContenu().put(key, value);
			}			
		}
		
		try {
			
			if(keys.contains("info")){
				if(!keys.contains("creation date")){
					dico.getContenu().put("creation date", System.currentTimeMillis()/1000);
				}
				info_dictionary(dico);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
						
		return dico;
	}
	
	public static void info_dictionary(Dictionary dico) throws Exception{
		Dictionary info_file = (Dictionary)dico.getContenu().get("info") ;
	    String filepath = (String)(info_file.getContenu().get("filepath"));
		FileChannel fc ;
			try { 
					FileInputStream fichier = new FileInputStream(new File(filepath));
					fc = fichier.getChannel() ;
					List<Byte> liste = new ArrayList<Byte>();
					long pieces_size = pieces_size(fc.size());
					ByteBuffer buffer = ByteBuffer.allocate((int) pieces_size);
					Long piece_field_length = (((fc.size()/pieces_size + 1)*20)); 
					ByteBuffer buffer_piece = ByteBuffer.allocate(piece_field_length.intValue());
					int bytes_read = fc.read(buffer);
					while(bytes_read != -1){
						buffer.flip();
						byte[] to_SHA1 = new byte[bytes_read];
						for(int i = 0 ; i < bytes_read ; i++){
							to_SHA1[i] = buffer.get() ; 
						}						
						MessageDigest md = MessageDigest.getInstance("SHA-1");
						byte[] hashed_message = md.digest(to_SHA1);
						/*for (int i = 0 ; i< hashed_message.length ; i++){
							liste.add(hashed_message[i]);
						}*/
						buffer_piece.put(hashed_message);
						buffer.clear();
						bytes_read = fc.read(buffer);
					}
					  /* byte[] pieces = new byte[liste.size()];
					   
					   for (int i=0 ; i<liste.size() ; i++){
						   pieces[i]=liste.get(i);
					   }*/
						
					   buffer_piece.flip();
					   byte[] pieces = new byte[buffer_piece.limit()];
					   buffer_piece.get(pieces);
					   
					   	   
					info_file.getContenu().put("piece length", (int) pieces_size);
					info_file.getContenu().put("pieces", pieces) ;
					info_file.getContenu().put("length", fc.size());
					info_file.getContenu().remove("filepath");
					fc.close();
					
			} catch(Exception e){
				e.printStackTrace();
			}
		return ;
	}
	
	private static long pieces_size(long size) {
		if (size == 0){
			return 0 ;
		}
		float nb =(float)(Math.log(size/1200)/Math.log(2));
		int puissance = (int)Math.round(nb);
		return (long) 1<< puissance ;
		
	}

	public static BencodeList get_list_from_json(JSONArray json_list){
		BencodeList liste = new BencodeList();
		Iterator it = json_list.iterator();
		while(it.hasNext()){
			Object value = it.next();
			
			if(value.getClass().equals(JSONObject.class)){
				liste.getListe().add(get_from_json((JSONObject) value)) ; 
			
			} else if(value.getClass().equals(JSONArray.class)) {
				liste.getListe().add(get_list_from_json((JSONArray) value));
			} else {
				liste.getListe().add(value);
			}					
		}
		return liste ;
	}
	
	public void doEncode(String path) throws Exception{
		FileOutputStream os = new FileOutputStream(new File(path));
		this.metadata.doEncode(path,os) ;
		os.close();
	}
}
