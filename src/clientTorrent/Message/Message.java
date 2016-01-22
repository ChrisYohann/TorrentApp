package clientTorrent.Message;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import BencodeType.Dictionary;
import clientTorrent.TextUtil;


public class Message {
	protected int index = 0 ;
	protected int begin = 0 ;
	protected int length = 0 ;

	// int car 4 octets
	protected int lengthPrefix;

	protected byte messageID;

	protected byte[] payload;

	public int send(SocketChannel channel) throws Exception{
		System.out.println("Sending :"+this.getClass().getName()+" message");
		if(isPiece() || isRequest()){
			System.out.println("Index : "+index+" Begin : "+begin+" Length : "+length);
		}

		if(this.getPayload() != null){
			//System.out.println("Payload :"+Dictionary.bytesToHex(getPayload()));
			//System.out.println("Payload size : "+payload.length);
			ByteBuffer wrapped = ByteBuffer.allocate(5+payload.length);

			//System.out.println("LengthPrefix : "+lengthPrefix);
			wrapped.putInt(lengthPrefix);
			wrapped.put(messageID);
			wrapped.put(payload);
			wrapped.flip();
			int bytesWritten = channel.write(wrapped);
			return bytesWritten ;
		} else {
			// Cas de KEEP ALIVE
			if (messageID == -1) {
			  return channel.write(ByteBuffer.wrap(TextUtil.intToByte4(lengthPrefix)));

			}
			ByteBuffer wrapped = ByteBuffer.allocate(5);


			// Autres messages
			wrapped.putInt(lengthPrefix);
			wrapped.put(messageID);
			wrapped.flip();

			//channel.write(wrapped);
			int bytesWritten = channel.write(wrapped);
			wrapped.clear();

			return bytesWritten ;
		}

	}

	public byte getMessageID(){
		return messageID ;
	}

	public byte[] getPayload(){
		return payload ;
	}

	public int getIndex(){
		return index ;
	}

	public int getLengthPrefix(){
		return lengthPrefix ;
	}

	public boolean isPiece(){
		return false ;
	}

	public boolean isCancel(){
		return false ;
	}

	public boolean isRequest(){
		return false ;
	}

	@Override
	public boolean equals(Object o){
		if(isPiece() || isCancel()){
            boolean boule = ((Message)o).index == this.index && ((Message)o).begin == this.begin && ((Message)o).length == this.length ;
			return boule ;
		} else {
			return this.equals(o);
		}
	}


}
