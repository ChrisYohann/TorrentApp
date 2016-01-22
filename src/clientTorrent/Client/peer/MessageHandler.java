package clientTorrent.Client.peer;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;

import clientTorrent.Client.peer.PeerNew.BufferState;
import clientTorrent.Message.BitfieldType;
import clientTorrent.Message.Cancel;
import clientTorrent.Message.Have;
import clientTorrent.Message.KeepAlive;
import clientTorrent.Message.Message;
import clientTorrent.Message.Piece;
import clientTorrent.Message.Request;
import clientTorrent.Message.Unchoke;
import clientTorrent.disk.CompletedException;
import clientTorrent.disk.CorruptedException;

public class MessageHandler implements MessageListener {
	
	public static final byte messageID_MAX = 9 ;
	public static final byte messageID_MIN = 0 ;
	public static final int message_MAXLENGTH = (1<<14) + 13 ;
	public static final int message_MINLENGTH = 0 ;
	

	private PeerManager parent ;
	public int nombre_mess = 0;

	public MessageHandler(PeerManager father) {
		parent = father ;
	}

	@Override
	public void partialMessage(ByteBuffer wrapped, PeerNew peer) {
		PeerNew.BufferState remaining = peer.getBufferState();

		byte IDmessage = remaining.messageID;

		// Pour l'instant je vais faire que les Piece, à supposer que c'est
		// elles qui bug
		switch (IDmessage) {

		case 7:
			int index = remaining.index;
			int offset = remaining.offset;
			int longueur = remaining.length;
			byte[] block = new byte[longueur];
			
			if(block.length > wrapped.remaining()){
				System.out.println("Block Length : "+block.length+" Buffer Remaining : "+wrapped.remaining());
				System.out.println("The piece will be cut. Preparing partial Message");
				PeerNew.BufferState state = peer.new BufferState(IDmessage,index,offset+wrapped.remaining(),block.length-wrapped.remaining());
				peer.setBufferState(state);
				block = new byte[wrapped.remaining()];
				wrapped.get(block, 0, wrapped.remaining());
				System.out.println("Piece : Index = " + index + " Begin : " + offset + " Length : " + wrapped.remaining() );
			} else {
			wrapped.get(block, 0, block.length);
			peer.setBufferState(null);
			}

			
			System.out.println("Partial piece from a previous Message");
			System.out.println("Piece : Index = " + index + " Begin : " + offset+" Length :"+longueur);
			try {
				peer.getFileDisk().write(index, offset, block);
			} catch (CompletedException complete) {
				// si le hash est bon
				System.out.println("Piece " + index + " Hash Test passed.");
				peer.getCurrentPieces().remove(index);
			} catch (CorruptedException corrupted) {
				// si le hash n'est pas bon
				System.out.println("Hash failed for Piece "+index);
				Set<Block> toRequest = new TreeSet<Block>();
				PieceAlgorithm.addBlocksToRequest(index, peer.getParent().getFileDisk().getNbPieces(), peer.getParent(),
						toRequest);
				for (Block b : toRequest) {
					Message m = new Request(b.getIndexPiece(), b.getBegin(), b.getLength());
					peer.getFileMessage().offer(m);
					peer.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
					toRequest.remove(block);
				}
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void handleMessage(ByteBuffer wrapped, PeerNew peer) {
		if (peer.getBufferState() != null){
			partialMessage(wrapped,peer);
		}

		System.out.println("Handling Message");

		int lengthPrefix = -1;
		try {
			lengthPrefix = wrapped.getInt();
			System.out.println(" Length : " + lengthPrefix);
			if(lengthPrefix < message_MINLENGTH || lengthPrefix > message_MAXLENGTH ){
				System.out.println("Error : Invalid length message for Decoding.");
				try {
					peer.getSocketChannel().close();
					peer.getSelectionKey().cancel();
					return ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (BufferUnderflowException b) {
			System.out.println("Exception catchée par le buffer");
			return;
		}

		if (lengthPrefix == 0) {
			// KeepAlive
			Message message = new KeepAlive();
			peer.getFileMessage().offer(message);
			peer.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
			return;
		}

		Message message = null;
		byte messageID = (byte) wrapped.get();
		if(messageID < messageID_MIN || messageID > messageID_MAX ){
			System.out.println("Error : Invalid message ID.");
			try {
				peer.getSocketChannel().close();
				peer.getSelectionKey().cancel();
				return ;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("iD Message : " + messageID);

		try {

			byte[] integer = new byte[4];

			switch (messageID) {
			case 0:
				// choke
				peer.peer_choke(true);
				peer.getFileMessage().clear();
				break;
			case 1:
				// unchoke
				peer.peer_choke(false);
				break;
			case 2:
				// interested
				peer.peer_interested(true);
				peer.peer_choke(false);
				// peer.getParent().addInterestedPeers(peer);
				// peer.getParent().addInterestedPeers(new
				// PeerNew(peer.getSocketChannel().socket().getInetAddress(),
				// peer.getSocketChannel().socket().getPort()));
				peer.getFileMessage().offer(new Unchoke());
				peer.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
				break;

			case 3:
				// not interested
				peer.peer_interested(false);
				peer.getFileMessage().clear();
				break;

			case 4:
				// have, read pieceIndex
				int pieceIndex = wrapped.getInt();
				System.out.println("Have : Index = " + pieceIndex);
				peer.getBitfieldType().put(pieceIndex);
				peer.getParent().setBitfieldSum(pieceIndex);
				Map<Boolean, Integer> map = peer.getListHave();
				map.put(true, pieceIndex);
				peer.setMap(map);
				break;

			case 5:
				// bitfield
				byte[] bitfield = new byte[lengthPrefix - 1];
				wrapped.get(bitfield, 0, bitfield.length);
				System.out.println("Receiving Bitfield");
				peer.setBitfield(new BitfieldType(bitfield, peer.getParent().getFileDisk().getNbPieces()));
				break;

			case 6:
				// request
				int index = wrapped.getInt();
				int begin = wrapped.getInt();
				int length = wrapped.getInt();
				message = new Request(index, begin, length);
				// nombre_mess++;
				// if(nombre_mess%3 == 0) {
				// peer.peer_choke(true) ;
				// peer.getFileMessage().offer(new Choke());
				//
				// }
				System.out.println("Request : Index = " + index + " Begin : " + begin + " Length : " + length);
				if (peer.getFileDisk().isAvaiable(index, begin, length)) {
					peer.getFileMessage()
							.offer(new Piece(index, begin, length, peer.getFileDisk().read(index, begin, length)));
					peer.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
				}
				// peer.getParent().addDlRate(peer.getConnectedClient(), index);
				break;

			case 7:
				// piece
				index = wrapped.getInt();
				begin = wrapped.getInt();
				byte[] block = new byte[lengthPrefix - 9];
				if(block.length > wrapped.remaining()){
					System.out.println("Block Length : "+block.length+" Buffer Remaining : "+wrapped.remaining());
					System.out.println("The piece will be cut. Preparing partial Message");
					PeerNew.BufferState state = peer.new BufferState(messageID,index,begin+wrapped.remaining(),block.length-wrapped.remaining());
					peer.setBufferState(state);
					block = new byte[wrapped.remaining()];
					wrapped.get(block, 0, wrapped.remaining());
					System.out.println("Piece : Index = " + index + " Begin : " + begin + " Length : " + wrapped.remaining() );
				} else {
				wrapped.get(block, 0, block.length);
				System.out.println("Piece : Index = " + index + " Begin : " + begin + " Length : " + (lengthPrefix - 9));
				}
				try {
					peer.getFileDisk().write(index, begin, block);
				} catch (CompletedException complete) {
					// si le hash est bon
					System.out.println("Piece " + index + " Hash Test passed.");
					peer.getCurrentPieces().remove(index);
					parent.offerAll(new Have(index));
					
				} catch (CorruptedException corrupted) {
					// si le hash n'est pas bon
					System.out.println("Hash failed for Piece "+index);
					Set<Block> toRequest = new TreeSet<Block>();
					PieceAlgorithm.addBlocksToRequest(index, peer.getParent().getFileDisk().getNbPieces(),
							peer.getParent(), toRequest);
					for (Block b : toRequest) {
						Message m = new Request(b.getIndexPiece(), b.getBegin(), b.getLength());
						peer.getFileMessage().offer(m);
						peer.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
						toRequest.remove(b);
					}
				}
				break;

			case 8:
				// cancel
				index = wrapped.getInt();
				begin = wrapped.getInt();
				length = wrapped.getInt();
				System.out.println("Cancel : Index = " + index + " Begin : " + begin + " Length : " + length);
				message = new Cancel(index, begin, length);
				peer.getFileMessage().remove(message);
				break;
			}

		} catch (BufferUnderflowException b) {
			System.out.println("Fin de la fonction : exception");
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (wrapped.hasRemaining())
			handleMessage(wrapped, peer);
		return;

	}

}
