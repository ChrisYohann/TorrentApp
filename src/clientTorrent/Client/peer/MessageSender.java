package clientTorrent.Client.peer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import clientTorrent.Message.KeepAlive;
import clientTorrent.Message.Message;
import java.net.Socket;

public class MessageSender {

	public MessageSender() {

	}

	public void send(SocketChannel channel, PeerNew peer) throws Exception {
		System.out.println("Debut Sender");

		int messages_sent = 0 ;
		while(peer.getFileMessage().size()>0 && messages_sent < 32){
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}			
			Message message;
			try {
				message = peer.getFileMessage().poll(
						PeerManager.KEEP_ALIVE_IDLE_MINUTES,
						TimeUnit.MINUTES);
				if (message == null) {
					/*KeepAlive keep_alive = new KeepAlive();
					keep_alive.send(peer.getSocketChannel());*/
					break ;


					} else {
						if(peer.am_choked() && message.isRequest()){
							System.out.println("On est choked !!!");
							break ;
						}
							
							
						int bytesWritten = message.send(peer.getSocketChannel());
						System.out.println("Bytes written : " + bytesWritten);
						if (message.isPiece() && bytesWritten < message.getPayload().length + 5) {
							System.out.println("Remote side of TCP Buffer is full");
							//channel.close();
						}
						messages_sent++;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
	}
		System.out.println("Fin Sender");
		if (peer.getFileMessage().size() == 0) {
			peer.getSelectionKey().interestOps(SelectionKey.OP_READ);
		} else {
			peer.getSelectionKey().interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

		}
		
	}
	}
