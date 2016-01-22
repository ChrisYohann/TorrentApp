package clientTorrent.Message;

import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import clientTorrent.Client.Client;

public class Ecoute implements Runnable {
	
	private Client client;

	@Override
	public void run() {
		
		try {
			
			EtatConnexion etat = client.getEtat();
			Socket sock = etat.getSock();
			InputStream is = sock.getInputStream();
			int index;
			int begin;
			int length;
			byte[] integer = new byte[4];
			
			while (true) {
				
				// On lit lengthPrefix
				is.read(integer, 0, 4);
				ByteBuffer wrapped = ByteBuffer.wrap(integer);
				int lengthPrefix = wrapped.getInt();
				
				// Cas de keep alive
				if (lengthPrefix == 0) {
					etat.receiveKeepAlive();
				}
				
				// On lit messageID
				byte messageID = (byte) is.read();
				
				switch (messageID) {
				case 0:
					// choke
					etat.receiveChoke();
					break;
				case 1:
					// unchoke
					etat.receiveUnchoke();
					break;
				case 2:
					// interested
					etat.receiveInterested();
					break;
				case 3:
					// not interested
					etat.receiveNotInterested();
					break;
				case 4:
					// have, read pieceIndex
					is.read(integer, 0, 4);
					wrapped = ByteBuffer.wrap(integer);
					int pieceIndex = wrapped.getInt();
					etat.receiveHave(pieceIndex);
					break;
				case 5:
					// bitfield
					byte[] bitfield = new byte[lengthPrefix - 1];
					is.read(bitfield, 0, lengthPrefix - 1);
					etat.receiveBitfield(bitfield);
					break;
				case 6:
					// request
					is.read(integer, 0, 4);
					wrapped = ByteBuffer.wrap(integer);
					index = wrapped.getInt();
					is.read(integer, 0, 4);
					wrapped = ByteBuffer.wrap(integer);
					begin = wrapped.getInt();
					is.read(integer, 0, 4);
					wrapped = ByteBuffer.wrap(integer);
					length = wrapped.getInt();
					etat.receiveRequest(index, begin, length);
					break;
				case 7:
					// piece
					is.read(integer, 0, 4);
					wrapped = ByteBuffer.wrap(integer);
					index = wrapped.getInt();
					is.read(integer, 0, 4);
					wrapped = ByteBuffer.wrap(integer);
					begin = wrapped.getInt();
					byte[] block = new byte[lengthPrefix - 9];
					is.read(block, 0, lengthPrefix - 9);
					etat.receivePiece(index, begin, block);
					break;
				case 8:
					// cancel
					is.read(integer, 0, 4);
					wrapped = ByteBuffer.wrap(integer);
					index = wrapped.getInt();
					is.read(integer, 0, 4);
					wrapped = ByteBuffer.wrap(integer);
					begin = wrapped.getInt();
					is.read(integer, 0, 4);
					wrapped = ByteBuffer.wrap(integer);
					length = wrapped.getInt();
					etat.receiveCancel(index, begin, length);
					break;
				}
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	}

}
