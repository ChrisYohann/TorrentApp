package clientTorrent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import com.trolltech.qt.QSignalEmitter;

import Bencode.*;
import BencodeType.*;
import BencodeType.Dictionary;
import clientTorrent.Client.*;
import clientTorrent.Client.peer.PeerManager;

public class Torrent extends QSignalEmitter implements Runnable {

    public static final String REGULAR = "regular";
	public static final String STARTED = "started";
	public static final String STOPPED = "stopped";
	public static final String COMPLETED = "completed";
	static final int COMPACT =  1;
	static final int NO_COMPACT = 0 ;

	private String name ;
	private String filepath ;
	private String filepathtorrent;
	private String resumeplace;
	private String comment;
	private Client client ;
	private Server serveur ;
    private Dictionary dictionnaire ;
    private Tracker tracker ;
    private byte[] peerId ;
    private byte[] list_peers ;
	private int port=6881 ;
	private long size ;
	private long uploaded=0 ;
	private long downloaded=0 ;
	private long previouslydownloaded = 0 ;
	private long left ;
	private boolean compact ;
	private boolean no_peer_id ;
	private boolean single_file = true ;
	private String event ;
	private Map<String,Integer> peers ;
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	private ScheduledExecutorService periodicexecute = Executors.newSingleThreadScheduledExecutor();
	private PeerManager manager ;
	public Signal1<Integer> valueChanged = new Signal1<Integer>();
	public Signal1<String>  stringChanged = new Signal1<String>() ;
	private long previoustime = System.currentTimeMillis() ;
	private long bitrate = 0 ;

	public Torrent(){
		this.dictionnaire = new Dictionary();
		this.setPeerId();
		this.tracker = new Tracker();
	}


	public Torrent(String file) throws IOException{
		Decoding decodeur = new Decoding();
		decodeur.readFile(file);
		this.dictionnaire = decodeur.getDictionary() ;
		if(dictionnaire.getContenu().containsKey("announce")){
			String tracker_address = new String((byte[]) dictionnaire.getContenu().get("announce"),"UTF-8");
			if(dictionnaire.getContenu().get("announce-list") != null){
			List announce_list = BencodeList.getAnnounceList((BencodeList)dictionnaire.getContenu().get("announce-list"));
			this.tracker = new Tracker(tracker_address,announce_list,this);
			} else {
				this.tracker = new Tracker(tracker_address,this);
			}
		} else {
			this.tracker = new Tracker();
		}
	    this.setPeerId();
	    this.setName(dictionnaire);
	    this.setSize(dictionnaire);
	    this.setComment(dictionnaire);
	    this.setFilepathtorrent(file);
	    this.setFilePath();
	    this.left = this.size ;
	    manager  = new PeerManager(this) ;
//	    client = new Client(this);
//	    serveur = new Server(this);
//	    manager  = new PeerManager(this) ;

	}


	public Torrent(String torrent_file, String file_dest) throws IOException {
		Decoding decodeur = new Decoding();
		decodeur.readFile(torrent_file);
		this.dictionnaire = decodeur.getDictionary() ;
		if(dictionnaire.getContenu().containsKey("announce")){
			String tracker_address = new String((byte[]) dictionnaire.getContenu().get("announce"),"UTF-8");
			List announce_list = BencodeList.getAnnounceList((BencodeList)dictionnaire.getContenu().get("announce-list"));

			this.tracker = new Tracker(tracker_address,announce_list,this);
		} else {
			this.tracker = new Tracker();
		}
	    this.setPeerId();
	    this.setName(dictionnaire);
	    this.setSize(dictionnaire);
	    this.setComment(dictionnaire);
	    this.setFilepathtorrent(torrent_file);
	    this.setFilePath(file_dest);
		manager = new PeerManager(this) ;
	    //this.setComplete();
	    this.setLeft();
//	    client = new Client(this);
//	    serveur = new Server(this);


	}

	public Torrent(String torrent_file, String file_dest, Boolean resume, String resumeplace) throws IOException {
		Decoding decodeur = new Decoding();
		decodeur.readFile(torrent_file);
		this.dictionnaire = decodeur.getDictionary() ;
		if(dictionnaire.getContenu().containsKey("announce")){
			String tracker_address = new String((byte[]) dictionnaire.getContenu().get("announce"),"UTF-8"); 
			if(dictionnaire.getContenu().get("announce-list") != null){
			List announce_list = BencodeList.getAnnounceList((BencodeList)dictionnaire.getContenu().get("announce-list"));

			this.tracker = new Tracker(tracker_address,announce_list,this);
//			tracker.setList_annonce((byte[])dictionnaire.getContenu().get("announce-list"));
			} else {
				this.tracker = new Tracker(tracker_address,this);
			}
		} else {
			this.tracker = new Tracker();
		}
	    this.setPeerId();
	    this.setName(dictionnaire);
	    this.setSize(dictionnaire);
	    this.setComment(dictionnaire);
	    this.setFilepathtorrent(torrent_file);
	    this.setFilePath(file_dest);
//	    client = new Client(this);
//	    serveur = new Server(this);
	    this.resumeplace=resumeplace;
	    manager = new PeerManager(this,true,resumeplace) ;

	}

	private void setSize(Dictionary dictionnaire2) {
		 Dictionary info_dico = (Dictionary)dictionnaire.getContenu().get("info") ;
		 Object files = info_dico.getAttribute("files");
		 long longueur = 0 ;
		 if(files != null){
			 single_file = false ;
			 Iterator it = ((BencodeList)files).getListe().iterator() ;
			 while (it.hasNext()){
				 Object value = it.next() ;
				 longueur += (long)((Dictionary)value).getAttribute("length") ;
			 }
			this.size = longueur ;
		 }
		 this.size = (Long)info_dico.getAttribute("length");


	 }

	 private void setName(Dictionary dictionnaire2) {
		 Dictionary info_dico = (Dictionary)dictionnaire.getContenu().get("info") ;
		 try {
			name = new String((byte []) info_dico.getContenu().get("name"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	 }

	 public String getNom(){
		 return this.name ;
	 }

	 public synchronized long getDownloaded(){
		 return this.downloaded ;
	 }

	 public long getUploaded(){
		 return this.uploaded ;
	 }

	 public long getLeft(){
		 return this.left ;
	 }

	 public byte[] getListPeers(){
		 return this.list_peers;
	 }

	 public String getFilepathtorrent() {
		return this.filepathtorrent;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(Dictionary dico) throws UnsupportedEncodingException {
		this.comment = new String((byte[]) dico.getContenu().get("comment"),"utf-8");
	}


	public void setFilepathtorrent(String filepathtorrent) {
		this.filepathtorrent = filepathtorrent;
	}


	 public synchronized void setDownloaded(long down){
		 this.downloaded = down ;
		 setLeft();
	 }

	 public synchronized void setUploaded(long up){
		 this.uploaded = up ;
	 }

	 public synchronized void setLeft(){
		 this.left = this.size-this.downloaded;
	 }

	 public synchronized void setInfo(){
		 long timenow = System.currentTimeMillis() ;
		 long delay = timenow-previoustime ;
		 bitrate = (timenow == previoustime && delay < 1000)?bitrate:(downloaded - previouslydownloaded)/(delay)*1000;

		 String result = sizeformatter(this.downloaded)+" / "+sizeformatter(this.size)+"   (<i>Uploaded : "+sizeformatter(this.uploaded)+"</i>)"+"      "+sizeformatter(bitrate)+"/s";

		 if(delay > 1000){
	     previouslydownloaded = downloaded ;
	     previoustime = timenow ;
		 stringChanged.emit(result);
		 valueChanged.emit(((Long)downloaded).intValue());
		 }
	 }

	 public void setListPeers(byte[] list){
		 this.list_peers=list;
	 }

	 public long getSize(){
		 return this.size;
	 }

	 public boolean isComplete(){
		 if(this.size == this.downloaded){
			 return true ;
		 }
		 return false ;
	 }

	 public void setComplete(){
		 this.downloaded = this.size ;
	 }

	 public void setFilePath(){
		 JFileChooser choose = new JFileChooser();
			choose.setCurrentDirectory(new File("."));
			choose.setDialogTitle("Choose a path where to save the file");
			choose.setApproveButtonText("Select");
			choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			choose.setAcceptAllFileFilterUsed(false);
			if (choose.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {

			}
			this.filepath = choose.getSelectedFile() + "/"
			+ this.getNom();

	 }

	 public void setFilePath(String path){
		 this.filepath = path ;
	 }

	 public String getFilePath(){
		 return this.filepath ;
	 }

			 
	public String getResumeplace() {
		return resumeplace;
	}


	public void setResumeplace(String resumeplace) {
		this.resumeplace = resumeplace;
	}

	public void setPeerId(){
	peerId = new byte[20];
    Random random = new Random(System.currentTimeMillis());
    random.nextBytes(peerId);
    System.arraycopy("-JYY-0001".getBytes(), 0, peerId, 0, 8);
	 }

	 public byte[] getPeerId(){
		 return this.peerId ;
	 }

	
	 public PeerManager getManager() {
		return manager;
	}


	public void setManager(PeerManager manager) {
		this.manager = manager;
	}


	public int getPort(){
		 return this.port ;
	 }

	 public synchronized Map<String,Integer> getPeers(){
		 return peers;
	 }

	 public void setPeers(Map<String,Integer> peers_map){
		 this.peers = peers_map ;
	 }

	 public void setPort(int porte){
		 this.port = porte ;
	 }

	 public Tracker getTracker() {
		 return tracker;
	 }

	 public static String sizeformatter(long value){
		 if(value < 1024){
			 return value + " bytes" ;
		 } else if((value/1024)<=1024){
			 long ko = value/1024 ;
			 return ko + " Kb" ;
		 } else if(value/(1024*1024) <= 1024){
			 double mo = value/(1024*1024.0);
			 return String.format("%6.2f", mo) + " Mb" ;
		 } else{
			 double go = value/(1024*1024*1024.0);
			 return String.format("%6.2f", go) + " Gb" ;
		 }
	 }

	 public Map<String,Long> getFiles(){

		if(!single_file){
			Map<String,Long> result = new TreeMap<String,Long>();
			BencodeList files = (BencodeList)((Dictionary)dictionnaire.getAttribute("info")).getAttribute("files") ;
			ArrayList liste_fichiers = files.getListe() ;
			for(Object item : liste_fichiers){
				long size_file = (Long)((Dictionary)item).getAttribute("length");
				String file_name = "";
				ArrayList path = ((BencodeList)((Dictionary)item).getAttribute("path")).getListe();
				for (Object subpath : path){
						try {
							file_name += new String((byte[])subpath,"UTF-8")+System.getProperty("file.separator");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
				}
				file_name = file_name.substring(0, file_name.length()-1);
				result.put(file_name, size_file);
			}
			return result ;
		}


		return null;

	 }

	 public boolean isSingleFile(){
		 return single_file ;
	 }


	 public byte[] getInfoHash() throws Exception{
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try {
				if(dictionnaire.getContenu().containsKey("info")){
					Dictionary info_dico = (Dictionary)dictionnaire.getContenu().get("info") ;
					info_dico.doEncode("", output);
					byte[] to_SHA1 = output.toByteArray() ;
					MessageDigest md = MessageDigest.getInstance("SHA-1");
					byte[] hashed_message = md.digest(to_SHA1);
					return hashed_message;


				}
			} catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}

	 public Dictionary getDictionnary(){
		 return this.dictionnaire;
	 }

	 public void setDictionnary(Dictionary dico){
		 this.dictionnaire=dico;
	 }

	  public synchronized void setValue()
	    {
	        	int value = (int)this.getDownloaded();
	            valueChanged.emit(value);
	    }

	public void interrupt() throws Exception{
		tracker.performRequest(STOPPED);
		manager.continuer = false ;
		executor.shutdownNow();
		periodicexecute.shutdownNow();
	}
	
	public void doResume() throws Exception {
		Dictionary dico = dictionnaire ;
		dico.getContenu().put("filepath", this.getFilePath());
		OutputStream outstream = new FileOutputStream(new File("./resume/"+this.getNom()+".resume"));
		dico.doEncode("", outstream);
		return ;
	}
	
	
	public void run() {
			try {
				tracker.performRequest(STARTED);
//			    periodicexecute.scheduleAtFixedRate(this.tracker, 0,tracker.getInterval(),TimeUnit.SECONDS);
				executor.execute(manager);

			} catch (Exception e) {
				e.printStackTrace();
			}
	}


	



}
