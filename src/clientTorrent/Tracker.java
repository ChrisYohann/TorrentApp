package clientTorrent;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import Bencode.Decoding;
import BencodeType.Dictionary;

public class Tracker implements Runnable {

	private String announce;
	private HttpURLConnection connexion;
	private List announce_list;
	private Long interval;
	private Long minInterval;
	private String trackerId;
	private Long complete;
	private Long incomplete;
	private Torrent torrent;
	private byte[] list_annonce; 
	

	public byte[] getList_annonce() {
		return list_annonce;
	}

	public void setList_annonce(byte[] list_annonce) {
		this.list_annonce = list_annonce;
	}

	public String getAnnounce() {
		return announce;
	}

	public void setAnnounce(String announce) {
		this.announce = announce;
	}

	public List getAnnounce_list() {
		return announce_list;
	}

	public void setAnnounce_list(List announce_list) {
		this.announce_list = announce_list;
	}

	public Tracker() {

	}

	public Tracker(String tracker_address, List announce_list,Torrent torrent) {
		this.announce = tracker_address;
		this.announce_list = announce_list;
		this.torrent = torrent;
	}
	
	public Tracker(String tracker_address,Torrent torrent){
		this.announce = tracker_address;
		this.torrent = torrent;
	}

	public long getInterval(){
		return this.interval ;
	}
	
	public String setTrackerRequest(String event) throws Exception {
		String url_request = "";
		url_request += announce + "?";
		url_request += "info_hash=" + TextUtil.urlEncode(torrent.getInfoHash()) + "&";
		url_request += "peer_id=" + TextUtil.urlEncode(torrent.getPeerId())
				+ "&";
		url_request += "port="+torrent.getPort()+"&";
		url_request += "uploaded=" + 550706386546.+"&";
		url_request += "downloaded="+torrent.getDownloaded()+"&";
		url_request += "left="+torrent.getLeft()+"&";
		url_request += "compact=" + Torrent.COMPACT + "&";
		if(!event.equals(Torrent.REGULAR)){
		url_request += "event=" + event ;
		}
		return url_request;

	}

	public synchronized void performRequest(String event) throws Exception {

		
		String url = this.setTrackerRequest(event);
		URL obj = new URL(url);
		this.connexion = (HttpURLConnection) obj.openConnection();
		connexion.setRequestMethod("GET");

		int responseCode = connexion.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedInputStream data = new BufferedInputStream(
				connexion.getInputStream());
		byte[] byte_data = to_bytearray(data);
		ByteBuffer buffer = ByteBuffer.wrap(byte_data);
		buffer.get();
		Dictionary dico = Decoding.parseBencodeDictionary(buffer);
		buffer.clear();
		System.out.println(dico);

		if (dico.getContenu().containsKey("complete"))
			this.complete = new Long((Long) dico.getContenu()
					.get("complete"));
		if (dico.getContenu().containsKey("incomplete"))
			this.incomplete = new Long((Long) dico.getContenu().get(
					"incomplete"));
		if (dico.getContenu().containsKey("interval"))
			this.interval = new Long((Long) dico.getContenu()
					.get("interval"));
		if (dico.getContenu().containsKey("trackerId"))
			this.trackerId = (String) dico.getContenu().get("trackerId");
		if (dico.getContenu().containsKey("peers")) {
			byte[] list = (byte[]) dico.getContenu().get("peers");
			HashMap<String, Integer> peers_list = (HashMap) Dictionary
					.getPeersList(list);
			torrent.setListPeers(list);			
			torrent.setPeers(peers_list);
			//System.out.println("TAILLE LISTE :" + peers_list.size());
		}
		 

	}

	public String toString() {
		String result = "";
		result += "announce : " + announce + "\n";
		result += "announce-list : \n";
		for (Object tracker : announce_list) {
			result += tracker.toString() + "\n";
		}

		return result;
	}

	public static byte[] to_bytearray(InputStream data) throws Exception {
		byte[] array = new byte[data.available()];
		data.read(array);
		return array;
	}
	
	public Torrent getTorrent(){
		return torrent;
	}

	@Override
	public void run() {
		try {
			this.performRequest(Torrent.REGULAR);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof InterruptedException){
				Thread.currentThread().interrupt();
				return ;
			}
		}
		
	}

}
