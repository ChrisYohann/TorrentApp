package clientTorrent.Message;

import java.nio.ByteBuffer;


public class Cancel extends Message {

	public Cancel(int index, int begin, int length) {
		this.index = index ;
		this.begin = begin ;
		this.length = length ;
			
		lengthPrefix = 13;
		messageID = 8;
		ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(0, index);
		buffer.putInt(4, begin);
		buffer.putInt(8, length);
		payload = buffer.array();
		
	}
	
	public boolean isCancel(){
		return true ;
	}

}
