package clientTorrent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestSocket extends Thread {
	
	public boolean client ;
	public Socket socketclient;
	
	public static void main(String[] args){
		
		try{
			ServerSocket socket = new ServerSocket(6969);
			System.out.println("Debut serveur socket");
			while(true){
				Socket client = socket.accept();
				TestSocket test = new TestSocket(client);
				test.start();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
		
		public TestSocket(Socket socck){
			socketclient=socck;
		}
		
		public void run(){
			reception();
		}
		
		public void reception(){
			try{
				
				String mess="";
				System.out.println("Connexion avec le client : " + socketclient.getInetAddress());
				BufferedReader in = new BufferedReader(new InputStreamReader(socketclient.getInputStream()));
				PrintStream out = new PrintStream(socketclient.getOutputStream());
				mess = in.readLine();
				System.out.println("Message reçu : " + mess);
				out.println("Bonjour " + mess);
				socketclient.close();
				
				
			} catch (Exception e){
				e.printStackTrace();		}
		}
		
	}
	


