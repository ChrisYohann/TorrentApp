package clientTorrent.Message;

public class Bitfield extends Message {
	
	public Bitfield(byte[] bitfield) {
		
		lengthPrefix = 1 + bitfield.length;
		messageID = 5;
		payload = bitfield;
	}

}
