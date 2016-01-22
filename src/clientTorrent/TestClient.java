	package clientTorrent;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class TestClient {
	

	  public static void main(String[] args) {

	    Socket socket;
	    DataInputStream userInput;
	    PrintStream theOutputStream;

	    try {
	      InetAddress serveur = InetAddress.getByName(args[0]);
	      socket = new Socket(serveur, 6969);

	      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	      PrintStream out = new PrintStream(socket.getOutputStream());

	      out.println("Bienvenue à vous");
	      System.out.println(in.readLine());

	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }

}
