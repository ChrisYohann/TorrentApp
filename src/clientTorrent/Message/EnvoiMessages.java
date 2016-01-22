package clientTorrent.Message;

import java.util.PriorityQueue;

public class EnvoiMessages implements Runnable {
	
	private PriorityQueue<Message> aEnvoyer;

	@Override
	public void run() {
		
		// On envoie les messages par ordre de priorité (par exemple tout avant piece car trop volumineux)
		
	}

}
