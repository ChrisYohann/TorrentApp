package clientTorrent.Message;

import java.net.Socket;

import clientTorrent.Client.Client;


public class EtatConnexion {
	
	private Socket sock;
	
	private Client client;
	
	private BitfieldType bitfield;
	
	private boolean am_choking = true;
	private boolean am_interested = false;
	private boolean peer_choking = true;
	private boolean peer_interested = false;
	
	public EtatConnexion(Client peer) {
		client = peer;
	}
	
	public Socket getSock() {
		return sock;
	}
	
	public void setSock(Socket sock) {
		this.sock = sock;	
	}
	
	public void receiveKeepAlive() {
		
	}
	
	public void receiveChoke() {
		peer_choking = true;
	}
	
	public void receiveUnchoke() {
		peer_choking = false;
	}
	
	public void receiveInterested() {
		peer_interested = true;
	}
	
	public void receiveNotInterested() {
		peer_interested = false;
	}
	
	public void receiveHave(int pieceIndex) {
		
		// on vérifie si on a la piece, si non, intéressé
		if (!bitfield.contains(pieceIndex))
			am_interested = true;
		
	}
	
	public void receiveBitfield(byte[] bitfield) {
		
		
	}
	
	public void receiveRequest(int index, int begin, int length) {
		
		// on verifie que am_choking est false
		if (am_choking)
			return;
		
		// on envoie la piece en question
		
		
	}
	
	public void receivePiece(int index, int begin, byte[] block) {
		
	}
	
	public void receiveCancel(int index, int begin, int length) {
		
	}
	
}
