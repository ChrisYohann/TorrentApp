package Bencode;
import java.io.File ;
import java.io.FileInputStream ;
import java.nio.* ;
import java.nio.channels.FileChannel ;
import java.util.ArrayList ;
import java.util.Iterator;
import java.util.Set;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import BencodeType.*;

public class Decoding{
	
	public Decoding(){
		this.fichier_decode = new Dictionary();
	}
	
	protected Dictionary fichier_decode ;
	
	public Dictionary getDictionary(){
		return this.fichier_decode ;
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    return true;
	}

	public void readFile(String path) throws IOException{
		FileChannel fc ;
		try{
			File file = new File(path);
			System.out.println(path);
			FileInputStream fichier = new FileInputStream(file) ;
			fc = fichier.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate((int)fc.size());
			int bytesread = fc.read(buffer) ;
			String number = "";
		      
			while (bytesread != -1){
				buffer.flip();
				char caractere = (char) buffer.get();
				fichier_decode = parseBencodeDictionary(buffer);
				System.out.println(fichier_decode);
	
				buffer.clear();
                bytesread = fc.read(buffer);
			}			
			fc.close();


		} catch(IOException f){
			throw new IOException(f.getMessage());
		}
		

	}

	public static Dictionary parseBencodeDictionary(ByteBuffer buffer) throws UnsupportedEncodingException{
		String number = "";		
		Dictionary dico = new Dictionary();
		ArrayList liste = new ArrayList();
		while (buffer.hasRemaining()){
			
			char caractere = (char) buffer.get();
			
			if(isInteger(String.valueOf(caractere))){
				number += String.valueOf(caractere) ;
			} else {
				switch(caractere){
				
				case 'd': liste.add(parseBencodeDictionary(buffer)) ;
						  break ;
				
				case 'l': liste.add(parseBencodeList(buffer)) ;
						  break ;
				
				case ':': liste.add(parseBencodeString(buffer,number));
						  number = "" ;
						  break ;
				case 'i': liste.add(parseBencodeLong(buffer)) ;
						  break ;
				
				case 'e':  
						  for (int i = 0 ; i<liste.size() ; i=i+2){
							 dico.getContenu().put(new String((byte []) liste.get(i),"UTF-8"),liste.get(i+1));							 
						  }
						  return dico ;
						 
				default : 
						break ;						
				}
			}					
		}
		return dico;
	}

	public static Long parseBencodeLong(ByteBuffer buffer) {
		String number = "";
		
		while (buffer.hasRemaining()){
			char caractere = (char) buffer.get();
			
			if(isInteger(String.valueOf(caractere))){
				number += String.valueOf(caractere) ;
			} else{
				try{
				return Long.parseLong(number);
				} catch(NumberFormatException e){
					System.out.println(Integer.MAX_VALUE);
					e.printStackTrace();
				}
			}			
		}		
		return 0L ;
	}

	public static BencodeList parseBencodeList(ByteBuffer buffer)throws UnsupportedEncodingException{
		String number = "";
		BencodeList list_bencode = new BencodeList();
		
		while (buffer.hasRemaining()){
			char caractere = (char) buffer.get();
			
			if(isInteger(String.valueOf(caractere))){
				number += String.valueOf(caractere) ;
			} else {
				switch(caractere){
				
				case 'd': list_bencode.getListe().add(parseBencodeDictionary(buffer)) ;
						  break ;
				
				case 'l': list_bencode.getListe().add(parseBencodeList(buffer)) ;
						  break ;
				
				case ':': list_bencode.getListe().add(parseBencodeString(buffer,number));
						  break ;
				
				case 'i': list_bencode.getListe().add(parseBencodeLong(buffer)) ;
						  break ;
				
				case 'e': return list_bencode ;			
				
				default : 
						break ;
				
				}
			}					
		}
		return list_bencode;
		
	}
	
	public static byte[] parseBencodeString(ByteBuffer buffer,String number){
		
		int nombre = Integer.parseInt(number) ;
		  number = "";
		  ByteBuffer chaine = ByteBuffer.allocate(nombre);
		  for (int i=0 ; i<nombre ; i++){
		  chaine.put(buffer.get()) ;				  
		}
		  chaine.rewind();
		  byte[] link = chaine.array();
		  return link ;
		
	}

}
