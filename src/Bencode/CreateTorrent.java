package Bencode;

import java.awt.Component;
import java.io.File;
import java.nio.file.FileSystem;
import java.util.*;
import org.apache.commons.io.* ;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import Bencode.*;
import clientTorrent.Torrent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CreateTorrent {
	
	public static String getTextFile(){
		  JFileChooser fc=new JFileChooser();
		  fc.setDialogTitle("Torrent Application");
		  fc.setApproveButtonText("Select");
		  int returnVal=fc.showOpenDialog(new JFrame());
		  if (returnVal == JFileChooser.APPROVE_OPTION) {
		    File file = fc.getSelectedFile();
		    return file.getAbsolutePath();
		  }
		  return null ;
		}
	
	public static String getDirectory(){
		JFileChooser choose = new JFileChooser();
		choose.setCurrentDirectory(new File("."));
		choose.setDialogTitle("Choose a path where to save the file");
		choose.setApproveButtonText("Select");
		choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		choose.setAcceptAllFileFilterUsed(false);
		if (choose.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
			
		}
		return choose.getSelectedFile().getAbsolutePath();
	}
	
	public static Torrent create_torrent(String filepath,String torrent_dest,String tracker,String comment) throws Exception{
		Encoding canal = new Encoding();
		JSONObject obj = new JSONObject();
		
		String[] announce_list = tracker.split(";");
		obj.put("announce",announce_list[0]);
		
		JSONArray tab = new JSONArray();
		for(int i=1 ; i<announce_list.length ; i++){
			JSONArray list = new JSONArray();
			list.add(announce_list[i]);
			tab.add(list);
		}
		obj.put("announce-list", tab);
		obj.put("comment",comment);
		obj.put("encoding", "utf-8");
		obj.put("created by", "YJMY-ISSC");
		JSONObject obj2 = new JSONObject();
		obj2.put("filepath", filepath);
		String name = FilenameUtils.getName(filepath);
		obj2.put("name",name);
		obj.put("info", obj2);
		BencodeType.Dictionary dico = Encoding.get_from_json(obj);
		canal.setMetadata(dico);
		try {
			canal.doEncode(torrent_dest+System.getProperty("file.separator")+name+".torrent");
			System.out.println("Torrent created in : "+torrent_dest+System.getProperty("file.separator")+name+".torrent");
			Torrent torrent = new Torrent(torrent_dest+System.getProperty("file.separator")+name+".torrent",filepath);
			return torrent;
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null ;
	}

	public static void main(String[] args) {
		Encoding canal = new Encoding();
		JSONObject obj = new JSONObject();
		Scanner sc = new Scanner(System.in);
		System.out.println("Creation d'un nouveau torrent :");
		System.out.println("Announce :");
		String str = sc.nextLine() ;
		obj.put("announce", str);
		
		int elements = 0 ;
		boolean mismatch = true ;
		while(mismatch){
			try{
				System.out.println("Nombre d'élements dans announce-list :");
				elements = sc.nextInt();
				sc.nextLine();
				mismatch = false ;
			} catch(InputMismatchException e){
			System.out.println("L'élement doit être un entier");
			sc.next();
			sc.nextLine();
			}
		}
		JSONArray tab = new JSONArray();
		for(int i=0 ; i<elements ; i++){
			JSONArray list = new JSONArray();
			System.out.println("announce-list "+i+" :");
			str = sc.nextLine();
			list.add(str);
			tab.add(list);
		}
		obj.put("announce-list", tab);
		System.out.println("comment :");
		str = sc.nextLine();
		obj.put("comment",str);
		System.out.println("created by :");
		str = sc.nextLine();
		obj.put("created by",str);
		obj.put("encoding", "utf-8");
		JSONObject obj2 = new JSONObject();
		System.out.println("Veuillez sélectionner un fichier");
		str = getTextFile();
		obj2.put("filepath", str);
		String name = FilenameUtils.getName(str);
		obj2.put("name",name);
		obj.put("info", obj2);
		BencodeType.Dictionary dico = Encoding.get_from_json(obj);
		System.out.println(dico);
		canal.setMetadata(dico);
		System.out.println("Destination");
		String directory = getDirectory();
		try {
			canal.doEncode(directory+"/"+name+".torrent");
			System.out.println("Torrent created in : "+directory+"/"+name+".torrent");
			Torrent torrent = new Torrent(directory+"/"+name+".torrent",str);	        
	        Thread t = new Thread(torrent);
	        t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
