package clientTorrent.Client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import clientTorrent.Torrent;
import clientTorrent.Message.EtatConnexion;

public class Peer {

	protected Torrent torrent;
	protected boolean can_accept=false;
    private EtatConnexion etat;

	public Peer() {

	}

	public Peer(Torrent torrent) {
		this.torrent = torrent;
	}

	public void setTorrent(Torrent torrent) {
		this.torrent = torrent;
	}

	public Torrent getTorrent() {
		return torrent;
	}
	
	public boolean getCanAccept(){
		return can_accept;
	}
	
	public void setCanAccept(boolean response){
		can_accept=response;
	}

    public EtatConnexion getEtat() {
		return etat;
	}

	public byte[] handShake(Torrent torrent) throws Exception {
		String str = "19BitTorrent protocol";
		byte handshake[];
		handshake = str.getBytes();
		
		byte[] hash = torrent.getInfoHash();
		byte[] peerid = torrent.getPeerId();
		byte[] message = new byte[69];

		int j = 0;
		for (int i = 0; i < handshake.length; i++) {
			message[j] = handshake[i];
			j++;
		}

		for (int z = 0; z < 8; z++) {
			message[j] = 1;
			j++;
		}

		for (int z = 0; z < hash.length; z++) {
			message[j] = hash[z];
			j++;
		}
		for (int p = 0; p < peerid.length; p++) {
			message[j] = peerid[p];
			j++;
		}

		System.out.println("mess :" + new String(message));
		System.out.println("peerid :" + new String(peerid));
		return message;

	}

	public void sendHandshake(Torrent torrent, Socket sock) throws Exception {

		ByteArrayInputStream fis = null;
		BufferedInputStream bis = null;
		OutputStream os = null;
		ServerSocket servsock = null;
//		Socket socket = null;
		try {
//			servsock = new ServerSocket(0);
				System.out.println("Waiting...");
				try {
//					socket = servsock.accept();
//					System.out.println("Accepted connection : " + sock);
					// send file;
					byte[] mybytearray = new byte[69];
					mybytearray = handShake(torrent);
					fis = new ByteArrayInputStream(mybytearray);
					bis = new BufferedInputStream(fis);
					bis.read(mybytearray, 0, mybytearray.length);
					os = sock.getOutputStream();
					System.out.println("Sending (" + mybytearray.length
							+ " bytes)");
					os.write(mybytearray, 0, mybytearray.length);
					os.flush();
					System.out.println("Done.");
				} finally {
					if (bis != null)
						bis.close();
				}
			
		} finally {
		}

	}

	public byte[] receivingHandShake(Socket sock)
			throws IOException {

		int bytesRead;
		int current = 0;
		DataOutputStream out = null;
		DataInputStream in = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
//		Socket socket = null;
		byte[] mybytearray = null;
		try {
			
//			System.out.println("Connection to server");

			mybytearray = new byte[69];
			out = new DataOutputStream(new BufferedOutputStream(
					sock.getOutputStream()));
			in = new DataInputStream(new BufferedInputStream(
					sock.getInputStream()));
			in.read(mybytearray);
//			System.out.println(new String(mybytearray));

			// do {
			// bytesRead = is.read(mybytearray, current,
			// (mybytearray.length - current));
			// if (bytesRead >= 0)
			// current += bytesRead;
			// } while (bytesRead > -1);
			//
			// bos.write(mybytearray, 0, current);
			// bos.flush();
			// System.out.println("File "
			// + (String) torrent.getDictionnary().getAttribute("name")
			// + " downloaded (" + current + " bytes read)");
		} finally {
			if (fos != null)
				fos.close();
			if (bos != null)
				bos.close();

//			out.close();
//			in.close();

			// if (sock != null)
			// sock.close();
		}

		return mybytearray;

	}

	public byte[] extractInfoHashFromHandShake(byte[] handshake) {
		byte[] info = new byte[20];
		int j = 0;
		for (int i = 29; i < 49; i++) {
			info[j] = handshake[i];
			j++;
		}

		return info;
	}

	public byte[] extractPeerIdFromHandShake(byte[] handshake) {
		byte[] info = new byte[20];
		int j = 0;
		for (int i = 49; i < 69; i++) {
			info[j] = handshake[i];
			j++;
		}

		return info;
	}

	public void infoHashIsOk(Torrent torrent,byte[] infohash) throws Exception {
		if (infohash.equals(torrent.getInfoHash())){
			can_accept=true;
			
		}			

		return ;
	}
	
	

}
