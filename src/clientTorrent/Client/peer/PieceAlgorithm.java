package clientTorrent.Client.peer;
import clientTorrent.Message.*;

import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class PieceAlgorithm implements Runnable {
	
	/*private class Block {
		
		private int indexPiece;
		private int begin;
		private int length;
		
        public Block(int indexPiece, int begin, int length) {
            this.indexPiece = indexPiece;
        	this.begin = begin;
            this.length = length;
        }
        
        public int getIndexPiece() {
        	return indexPiece;
        }
        
        public int getBegin() {
        	return begin;
        }
        
        public int getLength() {
        	return length;
        }

    }*/
	
	static final int MAX_PIECES = 3;
	
	private PeerManager manager;
	private int[] rarest_first;
	private Set<Integer> rarest_pieces; // int = index de piece
	private Set<Integer> non_requested_pieces;
	private Set<Block> blocks_to_request; // blocks prioritaires
	private long nbPieces;
	
	public PieceAlgorithm(PeerManager manager) {
		
		this.manager = manager;
		rarest_first = new int[manager.getFileDisk().getBitfieldFromFile().length * 8];
		rarest_pieces = new HashSet<Integer>();
		non_requested_pieces = new HashSet<Integer>();
		blocks_to_request = new TreeSet<Block>();
		nbPieces = manager.getFileDisk().getNbPieces();
		BitfieldType bitfield = new BitfieldType((int)nbPieces);
		bitfield.setBitfield(manager.getFileDisk().getBitfieldFromFile());
		for (int i = 0; i < nbPieces; i++) {
			if (!bitfield.contains(i))
				non_requested_pieces.add(i);
		}
	}
	
	public synchronized Set<Block>  getBlockToRequest(){
		return blocks_to_request ;	
	}
	
	// On ajoute les occurrences des pieces a partir d'un bitfield
	public void addOccurrence(byte[] bitfield) {
		
		// On verifie que le bitfield est de la bonne taille
		if (bitfield.length * 8 != rarest_first.length)
			return;
		
		// On ajoute les occurrences correspondant au bitfield
		for (int i = 0; i < bitfield.length; i++) {
			for (int j = 0; j < 8; j++) {
				rarest_first[i*8 + j] += (bitfield[i] >> (7-j)) % 2;
			}
		}
	}
	
	// Initialisation de rarest_first
	public void rarestFirst() {
		
		for (PeerNew active_peer : manager.getActivePeers()) {
			addOccurrence(active_peer.getBitfieldType().getBitfield());
		}
	}
	
	// On remplit l'ensemble des pieces les plus rares
	public void rarestPieces() {
		
		// On parcourt rarest_first pour obtenir le nombre d'occurrences minimal parmi les pieces qu'on n'a pas
		int nbMin = rarest_first[0];
		for (int i : non_requested_pieces) {
			if (rarest_first[i] < nbMin)
				nbMin = rarest_first[i];
		}
		
		// On remplit le tableau
		for (int i : non_requested_pieces) {
			if (rarest_first[i] == nbMin)
				rarest_pieces.add(i);
		} 
	}
	
	// On vide rarest_first et rarest_pieces
	public void clear() {
		
		// rarest_first
		for (int i = 0; i < rarest_first.length; i++)
			rarest_first[i] = 0;
		
		// rarest_pieces
		rarest_pieces.clear();
		
	}
	
	// On renvoie l'index choisi (rarest first)
	public int indexPieceRarestFirst() {
		
		// Index min et max de piece
		int indexMin = 0;
		int indexMax = (int) (nbPieces - 1);
		
		// Choix d'un index
		int index;
		do {
			index = (int) (Math.random() * (indexMax - indexMin + 1)) + indexMin;
		} while (!rarest_pieces.contains(index) && !non_requested_pieces.contains(index));
		
		return index;
	}
	
	// On renvoie l'index choisi (random first)
	public int indexPieceRandomFirst() {
		
		// Index min et max de piece
		int indexMin = 0;
		int indexMax = (int) (nbPieces - 1);
		
		// Choix d'un index
		int index;
		do {
			index = (int) (Math.random() * (indexMax - indexMin + 1)) + indexMin;
		} while (!non_requested_pieces.contains(index));
		
		return index;
	}
	
	// ajout de blocks prioritaires
	public static void addBlocksToRequest(int pieceIndex, long nbPieces, PeerManager manager, Set<Block> blocks_to_request) {
		
		int pieceLength;
		int nbBlocks;
		Block block;
		int begin = 0;
		int blockSize = (int) Math.pow(2,  14);
		
		// Cas de la derniere piece
		if (pieceIndex == nbPieces - 1)
			pieceLength = manager.getFileDisk().getLastPieceLength();
		
		// Cas normal
		else
			pieceLength = manager.getFileDisk().getLength(pieceIndex);
		
		// Nombre de blocks
		if ((pieceLength % blockSize) == 0)
			nbBlocks = pieceLength / blockSize;
		else
			nbBlocks = pieceLength / blockSize + 1;
		
		// On boucle sur le nombre de blocks et on les rajoute au set
		for (int i = 0; i < nbBlocks; i++) {
			if (i == nbBlocks - 1)
				block = new Block(pieceIndex, begin, pieceLength - begin);
			else
				block = new Block(pieceIndex, begin, blockSize);
			
			blocks_to_request.add(block);
			begin += blockSize;
		}
		
	}
	
	@Override
	public void run() {
		
		int index;
		int downloadedPieces;
		Message m;
		
		// On boucle tant qu'il y a des pieces qui ne sont pas requestees
		while (!non_requested_pieces.isEmpty()) {
			
			BitfieldType bitfield = new BitfieldType((int)nbPieces);
			bitfield.setBitfield(manager.getFileDisk().getBitfieldFromFile());
			downloadedPieces = 0;
			for (int i = 0; i < nbPieces; i++) {
				if (bitfield.contains(i))
					downloadedPieces++;
			}
			
			// Choix d'une piece a requester
			if (downloadedPieces < 4)
				index = indexPieceRandomFirst();
			else {
				// init : on recalcule les index de pieces rares
				clear();
				rarestFirst();
				rarestPieces();
				index = indexPieceRarestFirst();
			}
			
			
			
			// On choisit au hasard un PeerNew qui possede la piece
			int nbPeers = manager.getActivePeers().size();
			if (nbPeers == 0)
				continue;
			// Index min et max
			int indexMin = 0;
			int indexMax = nbPeers - 1;
			// Choix d'un peer qui possede la piece
			int indexPeer = -1;
			do {
				indexPeer = (int) (Math.random() * (indexMax - indexMin + 1)) + indexMin;
			} while (!manager.getActivePeers().get(indexPeer).getBitfieldType().contains(index) || !(manager.getActivePeers().get(indexPeer).getCurrentPieces().size() < 3)|| manager.getActivePeers().get(indexPeer).am_choked());
			
			System.out.println("Index choisi : "+indexPeer);
			PeerNew peer = manager.getActivePeers().get(indexPeer);
			
			peer.getCurrentPieces().add(index);
			//System.out.println(peer.getCurrentPieces());
			// On ajoute les blocks prioritaires
			addBlocksToRequest(index, nbPieces, manager, blocks_to_request);
			Iterator<Block> it = blocks_to_request.iterator();			
			while (it.hasNext()){
				Block block = it.next();
				// Creation des request messages a envoyer
				m = new Request(block.getIndexPiece(), block.getBegin(), block.getLength());
				//System.out.println("Piece Algorithm Index = "+block.getIndexPiece()+" Begin : "+block.getBegin()+" Length : "+block.getLength());
				peer.getFileMessage().offer(m);
				peer.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
				it.remove();
			}
			non_requested_pieces.remove(index);
			
		}
		
		// End Game Mode : tous les blocks ont ete requestes
		

	}
	

}


