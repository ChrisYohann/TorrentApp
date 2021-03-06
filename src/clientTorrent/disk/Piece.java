package clientTorrent.disk;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import clientTorrent.TextUtil;

public class Piece {

    private byte[] sha1;
    private int length;
    private List<FileCursor> files = new ArrayList<FileCursor>();

    private class PieceBlock {

        public PieceBlock(int begin, int longueur) {
            this.begin = begin;
            this.length = longueur;
        }
        public int begin;
        public int length;

        public String toString() {
            return "Block index :" + begin + " longueur :" + length;
        }
    }
    private Set<PieceBlock> blocks = new TreeSet<PieceBlock>(new Comparator<PieceBlock>() {

        public int compare(Piece.PieceBlock o1, Piece.PieceBlock o2) {
            int beginDiff = o1.begin - o2.begin;
            if (beginDiff != 0) {
                return beginDiff;
            }
            return o1.length - o2.length;
        }
    });

    public Piece(byte[] sha1) {
        this.sha1 = sha1;
    }

    public void addCurseur(FileCursor curseur) {
        files.add(curseur);
    }

    public int getCompleted() {
        Integer completed = 0;

        for (PieceBlock block : blocks) {
            completed += block.length;
        }
        return completed;
    }

    public void setLength(int piece_size) {
        this.length = piece_size;
    }

    public int getLength() {
        return length;
    }

    public boolean isCompleted() {
        return getCompleted() == length;
    }

    public void write(int begin, byte[] block) throws IOException, CompletedException, CorruptedException {

        int filePieceIndex = findFilePieceIndex(begin);
        FileCursor filePiece = files.get(filePieceIndex);

        int writtenBytes = 0;
        while (writtenBytes < block.length) {
            RandomAccessFile raf = filePiece.getFile();
            Long seek = filePiece.getFileOffset() + ((begin + writtenBytes) - filePiece.getPieceOffset());
            raf.seek(seek);

            int byteToWrite = block.length - writtenBytes;
            Long byteboutdefichier = raf.length() - seek;

            Long byteAvaiableToWrite = byteToWrite < byteboutdefichier ? byteToWrite : byteboutdefichier;
            raf.write(block, writtenBytes, byteAvaiableToWrite.intValue());
            writtenBytes += byteAvaiableToWrite.intValue();
            
            //On est en fin de fichier et il reste encore des octets a ecrire
            if (byteAvaiableToWrite.equals(byteboutdefichier) && writtenBytes < block.length) {
                filePiece = files.get(filePieceIndex+1);
            }
        }

        addPieceBlock(begin, block.length);

        if (isCompleted()) {
            if (!checkSha1()) {
                blocks.clear();
                throw new CorruptedException();
            } else {
            	throw new CompletedException();
            }
        }
    }

    public byte[] read(int begin, int length) throws IOException {

        if (!isAvaiable(begin, length)) {
            throw new EOFException("Piece not available " + "begin: " + begin + " length: " + length);
        }
        int filePieceIndex = findFilePieceIndex(begin);
        FileCursor filePiece = files.get(filePieceIndex);
        byte[] block = new byte[length];

        int readBytes = 0;
        while (readBytes < length) {
            RandomAccessFile raf = filePiece.getFile();
            Long seek = filePiece.getFileOffset() + ((begin + readBytes) - filePiece.getPieceOffset());
            raf.seek(seek);

            int byteToRead = length - readBytes;
            Long byteAvaiableInThisFile = raf.length() - seek;

            Long byteAvaiableToRead = byteToRead < byteAvaiableInThisFile ? byteToRead : byteAvaiableInThisFile;
            raf.readFully(block, readBytes, byteAvaiableToRead.intValue());
            readBytes += byteAvaiableToRead.intValue();

            if (byteAvaiableToRead.equals(byteAvaiableInThisFile) && readBytes < length) {
            	System.out.println("byte available to read "+byteAvaiableToRead +"\nbyte available in this file "+byteAvaiableInThisFile
            			+"\n readBytes "+readBytes+"\n length "+length);
                filePiece = files.get(filePieceIndex+1);
            }
        }

        return block;
    }


    private int findFilePieceIndex(int begin) {
        int i = 0;
        for (i = 0; i < files.size() - 1; i++) {
            if (files.get(i).getPieceOffset() <= begin && files.get(i + 1).getPieceOffset() > begin) {
                return i;
            }
        }

        return i;
    }

    public void addPieceBlock(int begin, int length) {

        PieceBlock newPieceBlock = new PieceBlock(begin, length);
        blocks.add(newPieceBlock);
        
        Iterator<PieceBlock> iterator = blocks.iterator();
        PieceBlock prev = iterator.next();

        Collection<PieceBlock> blocksToBeRemoved = new LinkedList<PieceBlock>();

        while (iterator.hasNext()) {
            PieceBlock p = iterator.next();
            if (prev.begin + prev.length >= p.begin) {

                p.length = Math.max(p.length + (p.begin - prev.begin), prev.length);
                p.begin = prev.begin;
                blocksToBeRemoved.add(prev);
            }
            prev = p;
        }

        for (PieceBlock pb : blocksToBeRemoved) {
            blocks.remove(pb);
        }
    }

    public boolean isAvaiable(int begin, int length) {
        for (PieceBlock block : blocks) {
            if (begin >= block.begin && length <= block.length) {
                return true;
            } else if (begin + length < block.begin) {
                return false;
            }
        }

        return false;
    }

    public boolean checkSha1() throws IOException {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        byte[] pieceBuffer = read(0, length);
        byte[] sha1Digest = md.digest(pieceBuffer);
        return TextUtil.bytesCompare(sha1, sha1Digest);
    }

    public void clear() {
        blocks.clear();
    }

    public int available(int begin) {
        for (PieceBlock pb : blocks) {
            if (pb.begin <= begin && (pb.begin + pb.length) > begin) {
                return (pb.begin + pb.length) - begin;
            }
        }

        return 0;
    }
}
