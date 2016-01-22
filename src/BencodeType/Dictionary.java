package BencodeType;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import Bencode.*;
import opentracker.OpentrackerScript;


public class Dictionary {
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	protected TreeMap<String,Object> contenu ;
	
	
	public Dictionary(){
		this.contenu = new TreeMap<String,Object>();		
	}
	
	public Dictionary(Map<String,Object> maps){
		this.contenu = new TreeMap<String,Object>(maps);
	}
	
	public void setContenu(TreeMap<String,Object> content){
		this.contenu = content ;
	}
	
	public TreeMap<String,Object> getContenu(){
		return this.contenu ;
	}
	

	
	public String toString(){
		Set keys = this.contenu.keySet();
		String arbre = "\tDictionnaire"+"["+keys.size()+"] : \n";
		Iterator it = keys.iterator() ;
		
		while(it.hasNext()){
				String key = (String)it.next();
				if(!key.equals("pieces") && !key.equals("peers")){
				Object value = this.contenu.get(key) ;
				
				if(value instanceof byte[]){
					String chaine = "";
					try {
						chaine = new String((byte []) value,"UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					arbre += "\t\t"+key+" : "+chaine+" \n";
				} else {
					arbre += "\t\t"+key+" : "+value.toString()+" \n";
				}
				
				} else {
					Object value = this.contenu.get(key) ;
					if(key.equals("peers")){

						arbre += "\t\t"+key+" : "+bytetoIPv4((byte[]) value )+" \n";
						//arbre += "\t\t"+key+" : ..." ;

					}
					
					else {arbre += "\t\t"+key+" : ..." ;
					//arbre += "\t\t"+key+" : "+bytesToHex((byte[]) value )+" \n";
					}
				}
				
		}
		return arbre ;
	}
	/**
	 * Converts a byte array in a Hexadecimal readable String
	 * @param bytes
	 * Bytes you want to convert in a String
	 * @return A string in a Hexadecimal format
	 */
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 3];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	/**
	 * Converts Tracker's response peers field into a (Ip address,port number) couple.
	 * @param peers List of Peers, Binary Encoded
	 * @return Map of Peers with <IP_Address,Port Number>
	 */
	public static Map getPeersList(byte[] peers){
		Map maps = new HashMap<String,Integer>();
		String result ;
		try{
			int i ;
			for (i=0 ; i<=peers.length-6 ; i=i+6){
				result ="";
				for(int j=i ; j<i+5 ; j++){
					if(j<i+3){
						result+= (256+peers[j])%256+".";
					} else if(j<i+4){
						result+= (256+peers[j])%256;
					}
					else {
						int port_number = (((256+peers[j])%256)<<8)+(256+peers[j+1])%256 ;
						//if(!result.equals(OpentrackerScript.getLocalAddress("wlan0")) && !result.equals(OpentrackerScript.getLocalAddress("eth0")))
							maps.put(result, port_number);
					}
				}
			}
	
		} catch(Exception n){
			n.printStackTrace();
		}
		return maps;
	}
	
	public static String bytetoString(byte[] peers) {
		String result= "" ;
		for (int i=0; i<peers.length; i++) {
			result += (256+peers[i])%256;
		}
		return result;
	}
	/**
	 * Converts binary peers list in a readable String of IPv4 Addresses
	 * @param peers List of peers, Binary Encoded
	 * @return List of peers
	 */
	public static String bytetoIPv4(byte[] peers) {
		String result="\n\t\t";
		try{
			int i ;
			for (i=0 ; i<=peers.length-6 ; i=i+6){
				for(int j=i ; j<i+5 ; j++){
					if(j<i+3){
						result+= (256+peers[j])%256+".";
					} else if(j<i+4){
						result+= (256+peers[j])%256;
					}
					else {
						int port_number = (((256+peers[j])%256)<<8)+(256+peers[j+1])%256 ;
						result+= ":"+port_number+"\n\t\t";
					}
				}
			}
	
		} catch(Exception n){
			n.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Encode the dictionary in Bencode
	 * @param path The path where the torrent will be saved
	 * @param os   The OutputStream. Can be a FileOutputStream or a BufferedArrayOutputStream
	 * @throws Exception
	 * @see BencodeList
	 */
	public void doEncode(String path,OutputStream os) throws Exception{
		Set keys = this.contenu.keySet();
		Iterator it = keys.iterator() ;
		String result = "" ;
		try{
		
		os.write("d".getBytes());
		while(it.hasNext()){
				String key = (String)it.next();
				Object value = this.contenu.get(key) ;
				
				result  = key.length()+":"+key;
				os.write(result.getBytes());
				
				if(value instanceof Integer || value instanceof Long){					
					result = "i"+value.toString()+"e";
					os.write(result.getBytes());
				}
				else if(value instanceof String){
					String chaine = (String)value ;
					result = chaine.length()+":"+chaine;
					os.write(result.getBytes());
					
				} else if(value instanceof byte[]){
					result = String.valueOf(((byte[]) value).length)+":";
					os.write(result.getBytes());
					os.write((byte []) value);
						
				} else if(value instanceof Dictionary){
					
					((Dictionary) value).doEncode(path,os);
				} else if(value instanceof BencodeList){
					
					 ((BencodeList) value).doEncode(path,os) ;
					 
				}
				else {
					
					System.out.print("Warning : The dictionary should not contain "+ value.getClass().getName()+" elements");
				}
				
		}
		os.write("e".getBytes());	
		//os.close();
		} catch (Exception f){
			f.printStackTrace();
		}
		
	}
	
	public Object getAttribute(String attribute){
		Set keys = contenu.keySet();
		if(!keys.contains(attribute)) {
			return null;
		}
		
		return contenu.get(attribute);
	}
	

}


