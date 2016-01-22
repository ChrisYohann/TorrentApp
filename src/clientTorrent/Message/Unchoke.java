package clientTorrent.Message;


public class Unchoke extends Message {
	
	public Unchoke() {
		lengthPrefix = 1;
		messageID = 1;
		payload = null;
	}

}
