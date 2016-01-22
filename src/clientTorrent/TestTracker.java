package clientTorrent;

import java.io.IOException;
import java.util.Set;

import Bencode.*;

public class TestTracker {

	public static void main(String[] args) throws Exception {
			Torrent torrent = new Torrent("Under.the.Dome.S03E12.HDTV.x264-LOL.mp4.torrent");
			Thread t = new Thread(torrent);
			t.start();

			

	}

}
