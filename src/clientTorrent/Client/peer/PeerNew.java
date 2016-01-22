package clientTorrent.Client.peer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import BencodeType.Dictionary;
import clientTorrent.Message.*;
import clientTorrent.disk.Disk;

public class PeerNew {

	public PeerManager getParent() {
		return parent;
	}

	public void setParent(PeerManager parent) {
		this.parent = parent;
	}

	private static boolean AM_CHOKING = true;
	private static boolean AM_INTERESTED = false;
	private static boolean PEER_CHOKING = true;
	private static boolean PEER_INTERESTED = false;

	private PeerManager parent;
	private InetAddress ip_address;
	private PeerNew connectedclient;

	private SocketChannel channel;
	private int port;
	private SelectionKey selectionkey;
	private MessageHandler listener;
	private MessageSender sender;
	private Disk filedisk;
	private BlockingQueue<Message> file_messages = new LinkedBlockingQueue<Message>();
	private BitfieldType bitfield;

	private Map<Boolean, Integer> have_liste = new HashMap<>();
	private Set<Integer> currentPieces = new HashSet<Integer>();

	public class BufferState{
		public byte messageID ;
		public int index ;
		public int offset ;
		public int length ;

		public BufferState(byte id,int indexe, int begin, int longueur){
			messageID = id ;
			index = indexe ;
			offset = begin ;
			length = longueur ;
		}
	}

	private BufferState bufferstate ;

	// We decide to start the connection
	public PeerNew(InetAddress ip, int port, Disk fichier, PeerManager father) {
		System.out.println("Initiate a connection");
		try {
			channel = SocketChannel.open();
			channel.connect(new InetSocketAddress(ip, port));
			this.ip_address = ip;
			this.port = port;
			this.filedisk = fichier;
			this.parent = father;
			this.bitfield = new BitfieldType(parent.getFileDisk().getNbPieces());
			Handshake.sendHandshake(father.getTorrent(), channel);

			// connectedclient = new PeerNew(channel.socket().getInetAddress(),
			// channel.socket().getPort(),this);
			if (channel.isConnected()) {
				byte[] handshake = Handshake.receivingHandShake(channel);
				if (Handshake.infoHashIsOk(parent.getTorrent(), Handshake.extractInfoHashFromHandShake(handshake))) {
					channel.configureBlocking(false);
					selectionkey = parent.register(channel, SelectionKey.OP_READ, this);
					
					if(!parent.isResume()){
					Message bitefilde = new Bitfield(filedisk.getBitfieldFromFile());
					file_messages.offer(bitefilde);
					} 
					else  {
						Message bitefilde = new Bitfield(filedisk.getBitfieldFromFile2(parent.getResumeplace(), parent.getTorrent().getFilepathtorrent()));
						file_messages.offer(bitefilde);
					}
					Message interested = new Interested();
					AM_INTERESTED = true;
					
					file_messages.offer(interested);
					selectionkey.interestOps(SelectionKey.OP_WRITE);
					if(!parent.add(this)){
						System.out.println("Already connected to this Peer. Closing connection.");
						channel.close();
						selectionkey.cancel();
						return ;
					}
				} else {
					System.out.println("Info Hash is not Ok, Aborting connection");
					channel.close();

				}
			} else {
				System.out.println("On est plus connectés");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			parent.getTorrent().getPeers().remove(ip.toString());
		}

	}

	// Connection from an Incoming Peer through SocketChannel
	public PeerNew(SocketChannel socket2, int port, Disk fichier,
			PeerManager father) {
		System.out.println("Receive a connection from channel");
		ip_address = ((InetSocketAddress) socket2.socket()
				.getRemoteSocketAddress()).getAddress();

		channel = socket2;

		this.port = port;
		this.filedisk = fichier;
		this.parent = father;
		// connectedclient = new PeerNew(channel.socket().getInetAddress(),
		// channel.socket().getPort(),this);

		this.bitfield = new BitfieldType(parent.getFileDisk().getNbPieces());

		try {
			byte[] handshake = Handshake.receivingHandShake(channel);
			selectionkey = parent.register(channel, SelectionKey.OP_READ, this);

			if (Handshake.infoHashIsOk(parent.getTorrent(), Handshake.extractInfoHashFromHandShake(handshake))) {
				Handshake.sendHandshake(parent.getTorrent(), channel);
				if(!parent.add(this)){
					System.out.println("Already connected to this Peer. Closing connection.");
					channel.close();
					selectionkey.cancel();
					return ;
				}
				Message bitefilde = new Bitfield(filedisk.getBitfieldFromFile());
				file_messages.offer(bitefilde);
				selectionkey.interestOps(SelectionKey.OP_WRITE);
			} else {
				System.out.println("Info Hash is not Ok, Aborting connection");
				channel.close();

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PeerNew(InetAddress ip, int port, PeerNew peer) {
		this.ip_address = ip;
		this.port = port;
		this.connectedclient = peer;

	}

	public int getPort() {
		return this.port;
	}

	public PeerNew getConnectedClient() {
		return this.connectedclient;
	}

	public Disk getFileDisk() {
		return filedisk;
	}



	public SocketChannel getSocketChannel() {
		return channel;
	}

	public InetAddress getAdress() {
		return ip_address;
	}

	public BitfieldType getBitfieldType() {
		return bitfield;
	}

	public BlockingQueue<Message> getFileMessage() {
		return file_messages;
	}

	public Set<Integer> getCurrentPieces() {
		return currentPieces;
	}

	public void setBitfield(BitfieldType bite_type) {
		bitfield = bite_type;
	}

	public void am_choke(boolean value) {
		AM_CHOKING = value;
	}

	public void peer_choke(boolean value) {
		PEER_CHOKING = value;
	}

	public void am_interested(boolean value) {
		AM_INTERESTED = value;
	}

	public void peer_interested(boolean value) {
		PEER_INTERESTED = value;
	}

	public SelectionKey getSelectionKey() {
		return selectionkey;
	}

	public void setSelectionKey(SelectionKey selection) {
		selectionkey = selection;
	}

	public void setMap(Map<Boolean, Integer> map) {
		have_liste = map;
	}

	public Map<Boolean, Integer> getListHave() {
		return have_liste;

	}

	public boolean is_interested() {
		return PEER_INTERESTED;
	}

	public boolean am_choked() {
		return PEER_CHOKING;
	}

	public BufferState getBufferState(){
		return bufferstate ;
	}

	public void setBufferState(BufferState state){
		bufferstate = state ;
	}

}
