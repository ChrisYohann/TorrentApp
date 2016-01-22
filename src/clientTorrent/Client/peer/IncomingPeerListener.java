package clientTorrent.Client.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Set;

import opentracker.OpentrackerScript;

import java.net.InetAddress;
import java.net.InetSocketAddress ;

public class IncomingPeerListener implements Runnable {
	
	private PeerManager parent ;
	
	public IncomingPeerListener(PeerManager father){
		parent = father ;
	}

	@Override
	public void run() {
		//TODO : Mettre a jour la liste des peers sinon c'est useless
		//TODO : Choix des pairs moins aléatoire
		while(parent.getActivePeers().size()< 2 && !parent.getTorrent().isComplete()){
			Set<String> keys = parent.getTorrent().getPeers().keySet() ;
			String[] array_string = keys.toArray(new String[0]);
			Random r = new Random();
			if(keys.size() > 0){
			int i = r.nextInt(keys.size());
			try {
				InetAddress peer_address = InetAddress.getByName(array_string[i]);
				if(parent.getTorrent().getPeers() == null)
					return ;
				int peer_port = (int)parent.getTorrent().getPeers().get(array_string[i]);
				if(!parent.isAlreadyConnected(peer_address)){
					PeerNew new_peer = new PeerNew(peer_address,peer_port,parent.getFileDisk(),parent);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
			if(Thread.currentThread().isInterrupted()){
				return ;
			}
		}	
		

	}

}
