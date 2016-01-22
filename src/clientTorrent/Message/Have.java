package clientTorrent.Message;

import java.nio.ByteBuffer;


public class Have extends Message {
	
	private int index ;
	
	public Have(int pieceIndex) {
		index = pieceIndex ;
		lengthPrefix = 5;
		messageID = 4;
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(pieceIndex);
		payload = buffer.array();
		
	}
	
	public int getIndex(){
		return index ;
	}
	
	

}
