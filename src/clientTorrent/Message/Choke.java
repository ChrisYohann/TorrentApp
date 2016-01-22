package clientTorrent.Message;


public class Choke extends Message {
	
	public Choke() {
		
		lengthPrefix = 1;
		messageID = 0;
		payload = null;
	}

}
