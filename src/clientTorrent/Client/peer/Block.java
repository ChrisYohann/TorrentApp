package clientTorrent.Client.peer;

public class Block implements Comparable<Block> {
	
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

	@Override
	public int compareTo(Block other_block) {
		if(((Integer)this.indexPiece).compareTo(other_block.indexPiece) != 0){
			return ((Integer)this.indexPiece).compareTo(other_block.indexPiece) ;
		} else if (((Integer)this.begin).compareTo(other_block.begin) != 0)  {
			return ((Integer)this.begin).compareTo(other_block.begin);
		} else {
			return ((Integer)this.length).compareTo(other_block.length);
		}
	}
	
	@Override
	public boolean equals(Object o){
		return ((Block)o).indexPiece == this.indexPiece && ((Block)o).begin == this.begin && ((Block)o).length == this.length ;
	}

}
