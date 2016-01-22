package clientTorrent.Message;

import java.nio.ByteBuffer;


public class Piece extends Message {
	

	
	public Piece(int index, int begin,int length, byte[] block) {
			this.index = index ;
			this.begin = begin ;
			this.length = length ;
			lengthPrefix = 9 + block.length;
			messageID = 7;
			ByteBuffer buffer = ByteBuffer.allocate(8 + block.length);
			buffer.putInt(0, index);
			buffer.putInt(4, begin);
			for (int i = 0; i < block.length; i++)
				buffer.put(8 + i, block[i]);
			payload = buffer.array();
			 
	}
	
	public boolean isPiece(){
		return true ;
	}

}
