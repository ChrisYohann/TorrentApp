package clientTorrent;

import java.io.IOException;

import Bencode.CreateTorrent;

public class TestUpload {

	public static void main(String[] args) throws IOException {
		
		Torrent t1,t2 = null ;
			String torrent_src = CreateTorrent.getTextFile();
			String torrent_dest = CreateTorrent.getTextFile();
			t1 = new Torrent(torrent_src,torrent_dest);
			Thread th1 = new Thread(t1);
			th1.start();
			torrent_src = CreateTorrent.getTextFile();
			torrent_dest = CreateTorrent.getTextFile();
			t2 = new Torrent(torrent_src,torrent_dest);
			Thread th2 = new Thread(t2);
			th2.start();
			
			
			
	}

}
