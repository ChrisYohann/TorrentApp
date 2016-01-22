package clientTorrent.Message;

public class BitfieldType {
	
	// index des pieces 0123...
	private byte[] bitfield;
	
	private int nbPieces;
	
	public BitfieldType(int nbPieces) {
		bitfield = new byte[nbPieces/8 + (nbPieces%8 == 0 ? 0:1)];
		this.nbPieces = nbPieces;
	}
	
	public BitfieldType(byte[] bite, int nbPieces){
		bitfield = bite ;
		this.nbPieces = nbPieces;
	}
	
	public boolean contains(int index) {
		int mask = 1 << (index/8 + 1) * 8 - index - 1;
		int i = index/8;
		if ((bitfield[i] & mask) == 0)
			return false;
		return true;
	}
	
	public void put(int index) {
		int val = 1 << (index/8 + 1) * 8 - index - 1;
		int i = index/8;
		bitfield[i] |= val;
	}
	
    public boolean isComplete() {
		for (int i = 0; i < 16; i++) {
			if (!contains(i))
				return false;
		}
		return true;
	}

	public byte[] getBitfield(){
		return this.bitfield ;
	}

	public void setBitfield(byte[] bitfieldFromFile) {
		this.bitfield = bitfieldFromFile ;
		
	}
	
	public boolean hasCompleteTorrent(){
		boolean result = true ;
		int i = 0 ;
		while(result && i< nbPieces){
			result = result && contains(i);
			i++ ;			
		}		
		return result ;
	}

}
