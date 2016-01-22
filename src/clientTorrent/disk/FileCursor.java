package clientTorrent.disk;

import java.io.RandomAccessFile;

public class FileCursor {
	
    private Long fileOffset;
    private Long pieceOffset;
    private RandomAccessFile file;

    public FileCursor(RandomAccessFile file, Long fileOffset, Long pieceOffset) {
        this.file = file;
        this.fileOffset = fileOffset;
        this.pieceOffset = pieceOffset;
    }

    public Long getFileOffset() {
        return fileOffset;
    }

    public Long getPieceOffset() {
        return pieceOffset;
    }

    public RandomAccessFile getFile() {
        return file;
    }

}
