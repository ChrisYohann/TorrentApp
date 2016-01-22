package clientTorrent.Message;


public class Interested extends Message {
	
	public Interested() {
		
		lengthPrefix = 1;
		messageID = 2;
		payload = null;
	}
	

}
