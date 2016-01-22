package clientTorrent.disk;


import java.io.IOException;

public interface Disk {

    public void init() throws IOException;

    public void write(int index, int begin, byte[] block) throws IOException, CompletedException, CorruptedException;

    public byte[] read(int index, int begin, int length) throws IOException;

    public Long getCompleted();

    public boolean isCompleted(int index);
    
    public boolean isComplete();
    
    public int getDownloaded(int index);

    public boolean isAvaiable(int index, int begin, int length);

    public long available(int index, int begin);

    public long available(int index, int begin, long maxLength);
    
    public byte[] getBitfieldFromFile();
    
    public byte[] getBitfieldFromFile2(String filepath, String filepathtorrent) throws IOException ;

    public int getLength(int index);
    
    public int getNbPieces();

    public int getLastPieceLength();

    public void close();
}
