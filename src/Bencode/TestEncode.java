package Bencode;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TestEncode {

	public static void main(String[] args) throws Exception {
//		 Encoding canal = new Encoding("create_torrent.json");		
		Decoding decodeur = new Decoding();
//		//decodeur.readFile("test_torrent.torrent");
//		System.out.println(canal.getMetadata());
		decodeur.readFile("DCAFD8B6094739E16BFE454760D4C74920F79615.dat");
//	    //canal.setMetadata(decodeur.getDictionary());
//		
//	    canal.doEncode("fichier_sortie_toto.torrent");
		


}
}
