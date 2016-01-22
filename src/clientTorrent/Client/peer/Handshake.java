package clientTorrent.Client.peer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import BencodeType.Dictionary;
import clientTorrent.TextUtil;
import clientTorrent.Torrent;

public class Handshake {
	
	public static byte[] handShake(Torrent torrent) throws Exception {
		String str = "BitTorrent protocol";
		byte handshake[];
		handshake = str.getBytes();
		
		byte[] hash = torrent.getInfoHash();
		byte[] peerid = torrent.getPeerId();
		byte[] message = new byte[68];
		
		message[0] = 19 ;
		
		int j = 1;
		for (int i = 0; i < handshake.length; i++) {
			message[j] = handshake[i];
			j++;
		}

		for (int z = 0; z < 8; z++) {
			message[j] = 0;
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

	public static void sendHandshake(Torrent torrent, Socket sock) throws Exception {

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
	
	

	public static byte[] receivingHandShake(Socket sock)
			throws IOException {

		int bytesRead;
		int current = 0;
		DataOutputStream out = null;
		DataInputStream in = null;
		FileInputStream fis = null;
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
			System.out.println("Receiving Handshake from :"+((InetSocketAddress)sock.getRemoteSocketAddress()).getAddress());
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
	
	public static void sendHandshake(Torrent torrent,SocketChannel channel) throws Exception{
		System.out.println("Sending Handshake at "+System.currentTimeMillis());
//			socket = servsock.accept();
//			System.out.println("Accepted connection : " + sock);
			// send file;
			byte[] mybytearray = new byte[68];
			mybytearray = handShake(torrent);
			ByteBuffer wrapped = ByteBuffer.wrap(mybytearray);
			channel.write(wrapped);
	}
	
	public static byte[] receivingHandShake(SocketChannel channel) throws IOException{
		
		int bytesRead;
		int current = 0;
			try {
				Thread.sleep(1000); 	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ByteBuffer wrapped = ByteBuffer.allocate(68);
			channel.read(wrapped);
			wrapped.flip();
			
			System.out.println("Receiving Handshake from at "+System.currentTimeMillis()+":"+((InetSocketAddress)channel.socket().getRemoteSocketAddress()).getAddress());
			

		return wrapped.array();
		
	}

	public static byte[] extractInfoHashFromHandShake(byte[] handshake) {
		byte[] info = new byte[20];
		int j = 0;
		for (int i = 28; i < 48; i++) {
			info[j] = handshake[i];
			j++;
		}

		return info;
	}

	public static byte[] extractPeerIdFromHandShake(byte[] handshake) {
		byte[] info = new byte[20];
		int j = 0;
		for (int i = 48; i < 68; i++) {
			info[j] = handshake[i];
			j++;
		}

		return info;
	}

	public static boolean infoHashIsOk(Torrent torrent,byte[] infohash) throws Exception {
		System.out.println("InfoHash received : "+ Dictionary.bytesToHex(infohash));
		System.out.println("InfoHash expected :"+ Dictionary.bytesToHex(torrent.getInfoHash()));
		return TextUtil.bytesCompare(infohash, torrent.getInfoHash());
					
	}


	

}
