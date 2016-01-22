package clientTorrent.Client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import clientTorrent.Torrent;

public class Server extends Peer implements Runnable {

	private ServerSocket servsock;
	private Socket socketclient;
	private int port;

	public Server() {

	}

	public Server(Torrent torrent) {

		super(torrent);
		this.port = torrent.getPort();
		try {
			servsock = new ServerSocket(0);
			torrent.setPort(servsock.getLocalPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void git(Torrent torrent) {
		super.setTorrent(torrent);
	}

	public Torrent getTorrent() {
		return super.getTorrent();
	}
	
	public void sendHandshake(Socket sock) throws Exception{
		super.sendHandshake(torrent,sock);
	}
	
	public byte[] receivingHandShake(Socket sock) throws IOException {
		return super.receivingHandShake(sock);
	}

	public void sendFile(Integer porte, String file_to_send) throws IOException {

		FileInputStream fis = null;
		BufferedInputStream bis = null;
		OutputStream os = null;
		Socket sock = null;
		try {
			while (true) {
				System.out.println("Waiting...");
				try {
					sock = servsock.accept();	
					System.out.println("Accepted connection : " + sock + ", Port : " +sock.getPort() + ", IP: "+ sock.getInetAddress());
					byte[] hand = receivingHandShake(sock);
					try {
						String one = new String(super.extractInfoHashFromHandShake(hand));
						System.out.println("HAND equals:" + one.equals(new String(torrent.getInfoHash())));
					} catch (Exception e) {
						e.printStackTrace();
					}
					File myFile = new File(getTorrent().getFilePath());	
					byte[] mybytearray = new byte[(int) myFile.length()];
					System.out.println("AVANT INPUT");
					fis = new FileInputStream(myFile);
					bis = new BufferedInputStream(fis);
					System.out.println("APRES INPUT");
					bis.read(mybytearray, 0, mybytearray.length);
					System.out.println("APRES READ");
					os = sock.getOutputStream();
					System.out.println("APRES GETOUT");
					System.out.println("Sending " + getTorrent().getFilePath()
							+ "(" + mybytearray.length + " bytes)");
					os.write(mybytearray, 0, mybytearray.length);
					os.flush();
					System.out.println("Done.");
					torrent.setUploaded(mybytearray.length);
				} finally {
					if (bis != null)
						bis.close();
					if (os != null)
						os.close();
					if (sock != null)
						sock.close();
				}
			}
		} finally {
			if (servsock != null)
				servsock.close();
		}
	}

	@Override
	public void run() {

//		while(servsock.isClosed()) {
//			
//		}
//		
//		if (!super.getCanAccept()) {
//			System.out.println("ABORDING!");
//			try {
//				if (servsock != null && !servsock.isClosed())
//				servsock.close();
//			} catch (IOException e) {
//				return;
//			}
//			return;
//		} else {
//			if (servsock == null || servsock.isClosed()) {
//				System.out.println("ABORDING!");
//				return;
//			}
//
//		}

		while (getTorrent().getDownloaded() > 0) {

			try {
				sendFile(port, getTorrent().getFilePath());
			} catch (IOException e) {
				e.printStackTrace();
			}

			return;

		}
	}

}
