package clientTorrent.Message;


public class NotInterested extends Message {
	
	public NotInterested() {
		
		lengthPrefix = 1;
		messageID = 3;
		payload = null;
	}

}
