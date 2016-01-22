package clientTorrent.disk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import Bencode.Decoding;
import BencodeType.Dictionary;
import clientTorrent.Torrent;

public class FileDisk implements Disk {

	private Torrent torrent;
	private List<Piece> pieces = new ArrayList<Piece>();
	List<RandomAccessFile> files = new LinkedList<RandomAccessFile>();
	private String filepath;
	private int nb_pieces;
	private int lastPieceLength;

	public FileDisk(Torrent torrent) {
		this.torrent = torrent;
		this.filepath = torrent.getFilePath();
	}
	
	public FileDisk(){
		
	}

	@Override
	public synchronized void init() throws IOException {
		int pieceNumber = 0;

		// Init pieces

		Dictionary dico = torrent.getDictionnary();
		Dictionary info_dico = (Dictionary) dico.getAttribute("info");
		byte[] pieces_concat = (byte[]) info_dico.getAttribute("pieces");
		Long piece_length = ((Long) info_dico.getAttribute("piece length"));
		int nombre_pieces = pieces_concat.length / 20;
		int last_piece_size = ((Long) (torrent.getSize() - (nombre_pieces - 1)
				* piece_length)).intValue();
		for (int i = 0; i <= pieces_concat.length - 20; i = i + 20) {
			byte[] sha1 = new byte[20];
			System.arraycopy(pieces_concat, i, sha1, 0, 20);
			Piece piece = new Piece(sha1);
			if (pieceNumber != nombre_pieces - 1) {
				piece.setLength(((Long) info_dico.getAttribute("piece length"))
						.intValue());
			} else {
				piece.setLength(last_piece_size);
			}
			if (torrent.isComplete()) {
				if (pieceNumber != nombre_pieces - 1) {
					piece.addPieceBlock(0, piece.getLength());
				} else {
					piece.addPieceBlock(0, last_piece_size);
				}
			}
			pieces.add(piece);
			if (i == pieces_concat.length - 20)
				lastPieceLength = piece.getLength();
			pieceNumber++;
		}
		
		nb_pieces = pieceNumber ;
		
        //Fichiers
        if (torrent.isSingleFile()) {
            File file_dest = new File(filepath);
            RandomAccessFile raf = new RandomAccessFile(file_dest, "rw");
            raf.setLength(torrent.getSize());
            files.add(raf);
        } else {
        		File saveDirectory = new File(filepath,torrent.getNom());
                saveDirectory.mkdir();
        		TreeMap<String,Long> fichiers = (TreeMap)torrent.getFiles();
        		Set keys = fichiers.keySet();
        		Iterator it = keys.iterator() ;
        		
        		while(it.hasNext()){
        			String key = String.valueOf(it.next());
        			 RandomAccessFile raf = new RandomAccessFile(key, "rw");
        			 int length = ((Long) fichiers.get(key)).intValue(); 
                     raf.setLength(length);
                     files.add(raf);
        			
        		}    
            }
              
        /*Lien pieces fichiers*/
        Iterator<RandomAccessFile> fileIterator = files.iterator();
        Iterator<Piece> pieceIterator = pieces.iterator();

        Long fileOffset = 0l;
        Long pieceOffset = 0l;

        Piece piece = pieceIterator.next();
        RandomAccessFile file = fileIterator.next();

        while (piece != null && file != null) {

            piece.addCurseur(new FileCursor(file, fileOffset, pieceOffset));

            Long pieceFreeBytes = piece.getLength() - pieceOffset;
            Long fileMissingBytes = file.length() - fileOffset;
            

            if (pieceFreeBytes < fileMissingBytes) {
                fileOffset += pieceFreeBytes;
                if (pieceIterator.hasNext()) {
                    piece = pieceIterator.next();
                } else {
                    piece = null;
                }
                pieceOffset = 0l;
            } else if (pieceFreeBytes > fileMissingBytes) {
                pieceOffset += fileMissingBytes;
                if (fileIterator.hasNext()) {
                    file = fileIterator.next();
                } else {
                    file = null;
                }
                fileOffset = 0l;
            } else {
                fileOffset = 0l;
                pieceOffset = 0l;
                if (fileIterator.hasNext()) {
                    file = fileIterator.next();
                } else {
                    file = null;
                }
                if (pieceIterator.hasNext()) {
                    piece = pieceIterator.next();
                } else {
                    piece = null;
                }
            }
        }
        
        resume();
        
        //System.out.println("Bitfield :"+Dictionary.bytesToHex(getBitfieldFromFile()));
		return ;
	}

    private synchronized void resume() throws IOException {
         long resumed = 0;
         for (Piece p : pieces) {        	 
             p.addPieceBlock(0, p.getLength());
             if (!p.checkSha1()) {
                 p.clear();
             } else {
                 resumed += p.getLength();
             }
         }
         torrent.setDownloaded(resumed);
		
	}

	public synchronized void write(int index, int begin, byte[] block) throws IOException, CompletedException, CorruptedException {
        Piece piece = pieces.get(index);
        if (!piece.isCompleted()) {
        	torrent.setDownloaded(torrent.getDownloaded()+block.length);
            piece.write(begin, block);
        }
        
    }

    public synchronized byte[] read(int index, int begin, int length) throws IOException {
        Piece piece = pieces.get(index);
        torrent.setUploaded(torrent.getUploaded()+length);
        return piece.read(begin, length);
    }

    public synchronized Long getCompleted() {
        Long completed = 0l;
        for (Piece p : pieces) {
            completed += p.getCompleted();
        }
        return completed;
    }

    public synchronized boolean isCompleted(int index) {
        return pieces.get(index).isCompleted();
    }

    public synchronized int getDownloaded(int index) {
        return pieces.get(index).getCompleted();
    }

    public synchronized boolean isAvaiable(int index, int begin, int length) {
        return pieces.get(index).isAvaiable(begin, length);
    }

    public synchronized int getLength(int index) {
        return pieces.get(index).getLength();
    }

    public synchronized void close() {
        for (RandomAccessFile file : files) {
            try {
                file.close();
            } catch (IOException ex) {
            }
        }
    }
    
    public synchronized boolean isComplete(){
    	return getCompleted() == torrent.getSize() ;
    }

    public synchronized long available(int index, int begin) {
        return available(index, begin, Long.MAX_VALUE);
    }
    
    public synchronized byte[] getBitfieldFromFile() {
        byte[] bitField = new byte[(pieces.size() >> 3) + ((pieces.size() & 0x7) != 0 ? 1 : 0)];
        for (int i = 0; i < pieces.size(); i++) {
            bitField[i >> 3] |= (pieces.get(i).isCompleted() ? 0x80 : 0) >> (i & 0x7);
        }
        return bitField;
    }

    public synchronized long available(int index, int begin, long length) {

        int pieceLength = pieces.get(0).getLength();

        boolean continuer = true;
        long avaiable = 0;
        while (continuer) {
            Piece p = pieces.get(index);
            int pieceAvaiable = p.available(begin);

            avaiable += pieceAvaiable;
            index++;
            begin = 0;

            if (pieceAvaiable != p.getLength() - begin ||
                    pieceAvaiable >= length) {
                continuer = false;
            }
        }

        return avaiable;
    }
    
    public synchronized int getNbPieces(){
    	return nb_pieces ;
    }

	public int getLastPieceLength() {
		return lastPieceLength;
	}

	public  synchronized byte[] getBitfieldFromFile2(String filepath,
			String filepathtorrent) throws IOException {
		
//		System.out.println("ICI" + filepath);
		byte[] byteFiled = getHash(filepath,false);
		
		int size = byteFiled.length / 20;
		int debut = 0;
		byte[] bitField = new byte[(size >> 3) + ((size & 0x7) != 0 ? 1 : 0)];
		for (int i = 0; i < size; i++) {

			bitField[i >> 3] |= (hashEquals(filepath, filepathtorrent, debut) ? 0x80
					: 0) >> (i & 0x7);
			debut += 20;
		}
		
//		System.out.println("BITFILED EST : " + Dictionary.bytesToHex(bitField));
		
		return bitField;
	}

	public static boolean hashEquals(String filepath, String filepathtorrent,
			int debut) throws IOException {

		byte[] byteFiled = getHash(filepath,false);
		ByteBuffer buffer = ByteBuffer.wrap(byteFiled, debut, 20);
		byte[] faux = new byte[20];
		buffer.get(faux);
		String yo = Dictionary.bytesToHex(faux);

		byte[] byteFiled2 = getHash(filepathtorrent,true);
		ByteBuffer buffer2 = ByteBuffer.wrap(byteFiled2, debut, 20);
		byte[] faux2 = new byte[20];
		buffer2.get(faux2);

		String yo2 = Dictionary.bytesToHex(faux2);

		if (yo.equals(yo2))
			return true;

		return false;

	}

	private static byte[] getHash(String filepath,boolean torrent) throws IOException {

		byte[] response = null;
		Decoding decodeur = new Decoding();
		decodeur.readFile(filepath);
		Dictionary dico = decodeur.getDictionary();
		if(torrent)
			dico = (Dictionary)dico.getContenu().get("info");
		response = (byte[]) dico.getContenu().get("pieces");
		return response;
	}

}