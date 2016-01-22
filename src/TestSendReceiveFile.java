import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import clientTorrent.Torrent;
import clientTorrent.Client.Server;

public class TestSendReceiveFile {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// server
		if (args[0] == "0") {
		
			JFileChooser choose = new JFileChooser();
			choose.setCurrentDirectory(new File("."));		
			choose.setDialogTitle("Choose a torrent file");
			choose.setApproveButtonText("Select");
			FileFilter filter = new FileNameExtensionFilter("file torrent", "torrent");
			choose.setFileFilter(filter);
			choose.addChoosableFileFilter(filter);
			choose.setFileSelectionMode(JFileChooser.APPROVE_OPTION);
			choose.setAcceptAllFileFilterUsed(false);
			 if (choose.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) { 
			      
			      }
			 
			 Torrent torrent = new Torrent(String.valueOf(choose.getSelectedFile()));
			 torrent.getTracker().performRequest("started");
			 
			 Server server = new Server();
			 server.sendFile(6969, String.valueOf(choose.getSelectedFile()));

		}// client
		else if (args[0] == "1") {
			
			
			
		} else
			throw new Exception(
					"First argument must be 0 ( for server ) or 1 ( for client)");
	}
	
	

}
