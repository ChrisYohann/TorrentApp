package clientTorrent.Client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
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
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import clientTorrent.Torrent;
import opentracker.OpentrackerScript;

public class Client extends Peer implements Runnable {

	private Torrent torrent;
	private ServerSocket socketserver ;
	private Socket socketclient;
	private int port ;

	public Client(Torrent torrent) {
		this.torrent = torrent;
	}

	public void setTorrent(Torrent torrent) {
		this.torrent = torrent;
	}

	public Torrent getTorrent() {
		return torrent;
	}

	public Socket getSocker() {
		return socketclient;
	}

	public void setSocket(Socket socket) {
		this.socketclient = socket;
	}
	
	public void sendHandshake(Socket sock) throws Exception{
		super.sendHandshake(torrent,sock);
	}
	
	public byte[] receivingHandShake(Socket sock) throws IOException {
		return super.receivingHandShake(sock);
	}

	/* Fonction dégueulasse, juste pour cette fois */
	public void download() {
		String addresse_ip = "";
		int porte = 0;
		System.out.println("Peers :"+torrent.getPeers());
		Set keys = torrent.getPeers().keySet();
		for (Object key : keys) {
			addresse_ip = String.valueOf(key);
			porte = (int) torrent.getPeers().get(key);

			try {
				startDownload(InetAddress.getByName(addresse_ip), porte);
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public void startDownload(InetAddress serv_adress, Integer port)
			throws IOException {

		int bytesRead;
		int current = 0;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ReadableByteChannel channel_in = null;
		FileChannel channel_out = null;
		Socket sock = null;
		try {
			System.out.println("Adresse distante :"
					+ serv_adress.getHostAddress() + " Port :" + port);
			sock = new Socket(serv_adress, port);
			
			System.out.println("Connection to server");
			
			try {
				sendHandshake(sock);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Avant le InputStream");

			//byte[] mybytearray = new byte[torrent.getSize()];
			InputStream is = sock.getInputStream();

			System.out.println("Apres le InputStream");
			channel_in = Channels.newChannel(is);
			channel_out = new FileOutputStream(torrent.getFilePath()).getChannel();
			
			 ByteBuffer bytebuf = ByteBuffer.allocate(2<<22);

	         int bytesCount;
	         while ((bytesCount = channel_in.read(bytebuf)) > 0) { 
	        	current+= bytesCount ;
	        	torrent.setDownloaded(current);
	        	torrent.setValue();
	        	torrent.setInfo();
	            bytebuf.flip();
	            while(bytebuf.hasRemaining()){
	            channel_out.write(bytebuf);  
	            }
	            bytebuf.clear();
	         }
	         
			System.out.println("File "
					+ (String) torrent.getNom()
					+ " downloaded (" + current + " bytes read)");
		} finally {
			if (channel_in != null)
				channel_in.close();
			if (channel_out != null)
				channel_out.close();
			if (fos != null)
				fos.close();
			if (bos != null)
				bos.close();
			if (sock != null)
				sock.close();
		}

	}

	@Override
	public void run() {

		if (!torrent.isComplete()) {
			download();
		}
		try {
			torrent.getTracker().performRequest("completed");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
