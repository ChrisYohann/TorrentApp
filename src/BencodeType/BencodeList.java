package BencodeType;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.* ;


public class BencodeList {
	protected ArrayList liste ;
	
	
	public BencodeList(){
		this.liste = new ArrayList();
	}
	
	public ArrayList getListe(){
		return this.liste ;
	}
	
	public void setListe(ArrayList li){
		this.liste = li ;
	}
	
	public void doEncode(String path,OutputStream os){
		Iterator it = liste.iterator() ;
	String result = "" ;
	try{	
			os.write("l".getBytes());
			while(it.hasNext()){
			Object value = it.next();
						
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
				System.out.print("Warning : The List should not contain "+ value.getClass().getName()+" elements");
			}
			
	}
			os.write("e".getBytes());
	} catch (Exception f){
		f.printStackTrace();
	}
	
}
	public static List getAnnounceList(BencodeList liste) throws UnsupportedEncodingException{
		ArrayList announce_list = liste.getListe() ;
		ArrayList result = new ArrayList();
		Iterator it = announce_list.iterator() ;
			while(it.hasNext()){
				Object value = it.next();
				if (value instanceof BencodeList){
					result.addAll(getAnnounceList((BencodeList) value));
				} else {
					result.add(new String((byte[]) value,"UTF-8"));
				}			
			}		
		return result ;
	}


	public String toString(){
		String arbre = "Liste["+liste.size()+"] : \n";
		Iterator it = liste.iterator() ;
		
		while(it.hasNext()){
				Object value = it.next();
				if(value instanceof byte[]){
					String chaine = "";
					try {
						chaine = new String((byte []) value,"UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					arbre += "\t\t"+chaine+" \n";
				} else {
					arbre += "\t\t"+value.toString()+" \n";
				}		
		}
		return arbre ;
	}
	
}
