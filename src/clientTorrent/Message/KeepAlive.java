package clientTorrent.Message;


public class KeepAlive extends Message {
	
	public KeepAlive() {
		
		lengthPrefix = 0;
		messageID = -1;
		payload = null;
		
	}
	
	

}
