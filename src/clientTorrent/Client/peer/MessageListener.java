package clientTorrent.Client.peer;

import java.io.InputStream;

import clientTorrent.Message.*;
import java.nio.* ;

public interface MessageListener{
	
	public void handleMessage(ByteBuffer wrapped, PeerNew peer);
	
	public void partialMessage(ByteBuffer wrapped,PeerNew peer);

}
