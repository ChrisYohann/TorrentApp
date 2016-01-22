package clientTorrent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;

import Bencode.Decoding;
import Bencode.Encoding;
import Bencode.ForLogger;
import BencodeType.Dictionary;

import com.trolltech.qt.gui.QWidget;

public class Resume {

	private static Torrent torrent ;
	private static String path = "Resume";// Lieu de sauvegarde des fichier .resume
	private static String name;
	private static byte[] hash ;
	private static String LOGGEUR="verbose";
	private static Integer INT_LOGGEUR=0 ; // 0 pour nothing 
//										  1 pour info
//										  2 pour verbose
//								_		  3 pour debbug

	
	private static byte[] getHash (String filepath) throws IOException {
		
		byte[] response = null ;
		Decoding decodeur = new Decoding();
		decodeur.readFile(filepath);
		Dictionary dico = decodeur.getDictionary() ;
		response = (byte[]) dico.getContenu().get("pieces");		
		return response ;
	}
	

	
	
	
	public static void resume(QWidget window) throws IOException {
		if (!existTorrent()) {
			return ;
		}
		
		File fichier = new File(path);
		File[] liste = fichier.listFiles();
		Dictionary dico = new Dictionary();
		Decoding decodeur = new Decoding();
		
		for (int i = 0; i < liste.length; i++) {
			
		
		decodeur.readFile(liste[i].getAbsolutePath());
		if(ForLogger.visibility(LOGGEUR, INT_LOGGEUR)){
		System.out.println(liste[i].getPath());
		System.out.println("File exit :"+ liste[i].exists());
		}
		dico = decodeur.getDictionary() ;
		String dl= String.valueOf(dico.getContenu().get("downloaded"));
		String path_file= new String((byte []) dico.getContenu().get("file_path_file"),"UTF-8");
		String path_torrent=  new String((byte []) dico.getContenu().get("file_path_torrent"),"UTF-8");

		if(ForLogger.visibility(LOGGEUR, INT_LOGGEUR)){
		System.out.println("torrent path: " + path_torrent);
		System.out.println("file path: " + path_file.toString());
		}
		/*QWidget torrent_widget = new TorrentWidget(
				new Torrent(path_torrent),window);
		window.layout().addWidget(torrent_widget);*/
		}
	    
		
		
	}
	
	

	public static void createResume(Torrent torrent) {

		
		Encoding canal = new Encoding();
		JSONObject resume = new JSONObject();
		name = torrent.getNom();
		resume.put("file_path_torrent", torrent.getFilepathtorrent());
		resume.put("name", name);
		Long dl = torrent.getDownloaded();
		Long size = torrent.getSize();
		resume.put("downloaded", dl);
		resume.put("size", size);
		resume.put("file_path_file", torrent.getFilePath());
		resume.put("peers", torrent.getListPeers());
		BencodeType.Dictionary dico = Encoding.get_from_json(resume);
		try {
			info_dictionary(dico);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		canal.setMetadata(dico);
		try {
			canal.doEncode(path + System.getProperty("file.separator") + name
					+ ".resume");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		Encoding canal = new Encoding();
//		JSONObject resume = new JSONObject();
//		name = torrent.getNom();
//		resume.put("file_path_torrent", torrent.getFilepathtorrent());
//		resume.put("name", name);
//		Long dl = torrent.getDownloaded();
//		Long size = (torrent.getSize());
//		resume.put("downloaded", dl);
//		resume.put("size", size);
//		resume.put("file_path_file", torrent.getFilePath());
//		resume.put("peers", torrent.getListPeers());
//		resume.put("trackeradress", torrent.getTracker().getAnnounce());
//		resume.put("trackeradressannonce", torrent.getTracker().getList_annonce());		
//		BencodeType.Dictionary dico = Encoding.get_from_json(resume);
//		canal.setMetadata(dico);
//		try {
//			canal.doEncode(path + System.getProperty("file.separator") + name
//					+ ".resume");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}

	public static boolean existTorrent() {
		File fichier = new File(path);
		File[] liste;
		boolean resultat = false;
		if (fichier.isDirectory()) {
			liste = fichier.listFiles();;
			if (liste != null && liste.length != 0)
				resultat = true;

		}
		return resultat;
	}

	public static ArrayList<Torrent> resumeTorrents() throws IOException {
		ArrayList<Torrent> list = new ArrayList<>();
		
		if (!existTorrent()) {
			return list;
		}
		File fichier = new File(path);
		File[] liste = fichier.listFiles();
		for (int i = 0; i < liste.length; i++) {

			if(ForLogger.visibility(LOGGEUR, INT_LOGGEUR)){
			System.out.println("Liste : " + liste[i]);
			}
			list.add(createTorrent(liste[i]));
		}
		
		return list;	

	}
	
	public static Torrent createTorrent(File file) throws IOException{
			Dictionary dico = new Dictionary();
			Decoding decodeur = new Decoding();
			decodeur.readFile(file.getAbsolutePath());

			if(ForLogger.visibility(LOGGEUR, INT_LOGGEUR)){
			System.out.println(file.getPath());
			System.out.println("File exit :"+ file.exists());
			}
			dico = decodeur.getDictionary() ;
			String name = new String((byte []) dico.getContenu().get("name"),"UTF-8");
			String dl= String.valueOf(dico.getContenu().get("downloaded"));
			String size = String.valueOf(dico.getContenu().get("size"));
			String path_file= new String((byte []) dico.getContenu().get("file_path_file"),"UTF-8");
			String path_torrent=  new String((byte []) dico.getContenu().get("file_path_torrent"),"UTF-8");
			HashMap<String, Integer> peers_list = (HashMap) Dictionary
					.getPeersList((byte[]) dico.getContenu().get("peers"));
			

			
			System.out.println("torrent path: " + path_torrent);
			System.out.println("file path: " + path_file.toString());
			
			Torrent tor;
			if(size.equals(dl)) {
		    tor = new Torrent(path_torrent, path_file);
		    
			}
			else {
				
				tor = new Torrent(path_torrent, path_file, true,path+"/"+name+".resume");
//				tor.setResumeplace(path+"/"+name+".resume");
				tor.setDownloaded(Long.valueOf(dl));
			    tor.setPeers(peers_list);
			}
		    
		    return tor;
	
		
	}
	
	
	public static void info_dictionary(Dictionary dico) throws Exception{
	    String filepath = (String)(dico.getContenu().get("file_path_file"));
		FileChannel fc ;
			try { 
					FileInputStream fichier = new FileInputStream(new File(filepath));
					fc = fichier.getChannel() ;
					List<Byte> liste = new ArrayList<Byte>();
					long pieces_size = pieces_size(fc.size());
					ByteBuffer buffer = ByteBuffer.allocate((int) pieces_size);
					int bytes_read = fc.read(buffer);
					while(bytes_read != -1){
						buffer.flip();
						byte[] to_SHA1 = new byte[bytes_read];
						for(int i = 0 ; i < bytes_read ; i++){
							to_SHA1[i] = buffer.get() ; 
						}						
						MessageDigest md = MessageDigest.getInstance("SHA-1");
						byte[] hashed_message = md.digest(to_SHA1);
						for (int i = 0 ; i< hashed_message.length ; i++){
							liste.add(hashed_message[i]);
						}
						buffer.clear();
						bytes_read = fc.read(buffer);
					}
					   byte[] pieces = new byte[liste.size()];
					   
					   for (int i=0 ; i<liste.size() ; i++){
						   pieces[i]=liste.get(i);
					   }
					   
					dico.getContenu().put("piece length", (int) pieces_size);
					dico.getContenu().put("pieces", pieces) ;
					dico.getContenu().put("length", fc.size());
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

}
