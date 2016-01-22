package clientTorrent.Client.peer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import opentracker.OpentrackerScript;
import clientTorrent.Torrent;
import clientTorrent.Message.Message;
import clientTorrent.disk.Disk;
import clientTorrent.disk.FileDisk;

public class PeerManager implements Runnable {

	public static final int KEEP_ALIVE_IDLE_MINUTES = 2;
	private Torrent torrent;
	private ServerSocketChannel serverSocket;
	private Selector selector;
	private Map<String, Integer> peers_list;
	private Disk destination_file;
	private List<PeerNew> active_peers = new LinkedList<PeerNew>();
	private HashMap<PeerNew, Integer> interested_peers = new HashMap();
	private HashMap<PeerNew, Date> unchocked_peers = new HashMap();
	private IncomingPeerListener connection_listener;
	private int[] bitfield_sum;
	private MessageListener handler;
	private MessageSender sender;
	private PieceAlgorithm algo;
	private ExecutorService executor = Executors.newFixedThreadPool(3);
	public boolean continuer = true;
	private UnchockAlgorithm algounchoke;
	private boolean resume = false ;
	private String resumeplace ;

	public String getResumeplace() {
		return resumeplace;
	}

	public void setResumeplace(String resumeplace) {
		this.resumeplace = resumeplace;
	}

	private ExecutorService executorAlgoUnchocked = Executors
			.newSingleThreadExecutor();

	public PeerManager(Torrent tor) {

		this.algounchoke = new UnchockAlgorithm(this);
		this.torrent = tor;
		try {

			selector = Selector.open();
			serverSocket = ServerSocketChannel.open();
			InetSocketAddress hostAddress = new InetSocketAddress(OpentrackerScript.getLocalAddress("wlan0"), 0);
			serverSocket.bind(hostAddress);
			torrent.setPort(((InetSocketAddress) serverSocket.getLocalAddress()).getPort());
			serverSocket.configureBlocking(false);
			int ops = serverSocket.validOps();
			SelectionKey selectKy = serverSocket.register(selector, ops, this);

		} catch (IOException e) {
			e.printStackTrace();
		}
		destination_file = new FileDisk(torrent);
		try {
			destination_file.init();
			int nb_pieces = destination_file.getNbPieces();
			bitfield_sum = new int[nb_pieces];
		} catch (IOException e) {
			e.printStackTrace();
		}
		peers_list = torrent.getPeers();
		connection_listener = new IncomingPeerListener(this);
		handler = new MessageHandler(this);
		sender = new MessageSender();
		algo = new PieceAlgorithm(this);

	}

	public PeerManager(Torrent tor, boolean vrai, String place) {

		this.resume=vrai;
		this.resumeplace=place;
		this.algounchoke = new UnchockAlgorithm(this);
		this.torrent = tor;
		try {

			selector = Selector.open();
			serverSocket = ServerSocketChannel.open();
			InetSocketAddress hostAddress = new InetSocketAddress(OpentrackerScript.getLocalAddress("wlan0"), 0);
			serverSocket.bind(hostAddress);
			torrent.setPort(((InetSocketAddress) serverSocket.getLocalAddress()).getPort());
			serverSocket.configureBlocking(false);
			int ops = serverSocket.validOps();
			SelectionKey selectKy = serverSocket.register(selector, ops, this);

		} catch (IOException e) {
			e.printStackTrace();
		}
		destination_file = new FileDisk(torrent);
		try {
			destination_file.init();
			int nb_pieces = destination_file.getNbPieces();
			bitfield_sum = new int[nb_pieces];
		} catch (IOException e) {
			e.printStackTrace();
		}
		peers_list = torrent.getPeers();
		connection_listener = new IncomingPeerListener(this);
		handler = new MessageHandler(this);
		sender = new MessageSender();
		algo = new PieceAlgorithm(this);

	}
	
	public synchronized void addInterestedPeers(PeerNew peer) {

		if (!interested_peers.containsKey(peer))
			interested_peers.put(peer, 0);
		else
			System.out.println("DEJA PRESENT");

	}

	public synchronized void addDlRate(PeerNew peer, Integer DL_rate) {

		if (!interested_peers.containsKey(peer))
			try {
				throw new Exception("The peer must be interested");
			} catch (Exception e) {
				e.printStackTrace();
			}

		int dl = interested_peers.get(peer);
		dl += DL_rate;
		interested_peers.remove(peer);
		interested_peers.put(peer, dl);

	}

	public synchronized void addUnchockedPeers(PeerNew peer) {

		if (!unchocked_peers.containsKey(peer))
			unchocked_peers.put(peer, new Date());
		else
			System.out.println("DEJA PRESENT");

	}

	public void setPeersList(Map<String, Integer> maps) {
		this.peers_list = maps;
	}

	public Map<String, Integer> getPeersList() {
		return peers_list;
	}

	public HashMap<PeerNew, Integer> getInterested_peers() {
		return interested_peers;
	}

	public synchronized void setInterested_peers(HashMap<PeerNew, Integer> interested_peers) {
		this.interested_peers = interested_peers;
	}

	public HashMap<PeerNew, Date> getUnchocked_peers() {
		return unchocked_peers;
	}

	public synchronized void setUnchocked_peers(HashMap<PeerNew, Date> unchocked_peers) {
		this.unchocked_peers = unchocked_peers;
	}

	public synchronized void addUnchocked_peers(PeerNew peer) {

		if (!unchocked_peers.containsKey(peer))
			unchocked_peers.put(peer, new Date());
		else
			System.out.println("DEJA PRESENT");

	}

	public synchronized void removeUnchocked_peers(PeerNew peer) {

		unchocked_peers.remove(peer);

	}


	public boolean isResume() {
		return resume;
	}

	public void setResume(boolean resume) {
		this.resume = resume;
	}

	public synchronized List<PeerNew> getActivePeers() {
		return active_peers ;
	}

	public synchronized void setBitfieldSum(int index) {
		bitfield_sum[index]++;
	}

	public synchronized SelectionKey register(SocketChannel channel, int validOps, PeerNew obj) throws Exception {

		SelectionKey selectionkey = null;
		try {
			channel.configureBlocking(false);
			selector.wakeup();
			selectionkey = channel.register(selector, validOps, obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return selectionkey;

	}

	public Disk getFileDisk() {
		return destination_file;
	}

	public synchronized boolean add(PeerNew peer) {
		if(!isAlreadyConnected(peer.getAdress())){
			active_peers.add(peer);
			return true ;
		}
		return false ;
	}
	
	public synchronized boolean offerAll(Message message){
		boolean result = true ;
		for(PeerNew peer : active_peers){
			result = result && peer.getFileMessage().offer(message);
			peer.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
		}
		return result ;
	}

	public Torrent getTorrent() {
		return torrent;
	}

	public ServerSocketChannel getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(ServerSocketChannel serverSocket) {
		this.serverSocket = serverSocket;
	}

	public boolean isAlreadyConnected(InetAddress peer_address) {
		for (PeerNew peer : active_peers) {
			if (peer!= null && peer.getAdress().equals(peer_address))
				return true;
		}

		return false;
	}
	
	public void check_uselessPeers() throws IOException{
		Iterator<PeerNew> it = active_peers.iterator() ;
		while (it.hasNext()){
				PeerNew peer = it.next() ;
			if(peer != null && peer.getBitfieldType().hasCompleteTorrent()){
				System.out.println("Closing connection with Remote peer "+peer.getAdress().getHostAddress());
				peer.getSocketChannel().close();
				peer.getSelectionKey().cancel();
				it.remove();	
			}
		}
	}

	@Override
	public void run() {

		// executor.execute(algounchoke);
		executor.execute(connection_listener);
		executor.execute(algo);
		while (continuer) {
			try {
				int keys_number = selector.select(100);
				if (keys_number > 0) {
					System.out.println("Number of keys :" + keys_number);
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iter = selectedKeys.iterator();

					while (iter.hasNext()) {

						SelectionKey ky = iter.next();
						if(ky.isValid())
						System.out.println("Keys Ready Ops :" + ky.readyOps() + " InterestOps :" + ky.interestOps());
						if (ky.isValid() && ky.isAcceptable()) {
							SocketChannel client;
							try {
								client = serverSocket.accept();
								
									if (client != null) {
										client.configureBlocking(false);

										PeerNew peer = new PeerNew(client, client.socket().getPort(), destination_file,
												this);
										System.out.println("Accepted new connection from client: " + client);
									}

							} catch (IOException e) {
								e.printStackTrace();
							}

						}
						if (ky.isValid() && ky.isReadable()) {
							SocketChannel client = (SocketChannel) ky.channel();
							ByteBuffer buffer = ByteBuffer.allocate(1 << 19);
							PeerNew pair = (PeerNew) ky.attachment();
							try {
								client.configureBlocking(false);
								int bytesread = client.read(buffer);
								if (bytesread > 0) {
									System.out.println("Bytes Read : " + bytesread);
									buffer.flip();
									handler.handleMessage(buffer, pair);
									if (!client.isOpen()) {
										active_peers.remove(pair);
									}
								} else if(bytesread == -1){
									System.out.println("EOF. Remote peer has closing the connection.");
									ky.channel().close();
									ky.cancel();
									active_peers.remove(pair);
								}
							} catch (IOException e) {
								e.printStackTrace();
								ky.channel().close();
								ky.cancel();
								active_peers.remove(pair);
							}
						}
						if (ky.isValid() && ky.isWritable()) {
							SocketChannel client = (SocketChannel) ky.channel();
							PeerNew pair = (PeerNew) ky.attachment();
							client.configureBlocking(false);
							try {
								sender.send(client, pair);
								if (!client.isOpen()) {
									ky.channel().close();
									ky.cancel();
									active_peers.remove(pair);
								}

							} catch (Exception e) {
								e.printStackTrace();
								ky.channel().close();
								ky.cancel();
								active_peers.remove(pair);
							}
						}
						iter.remove();
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			torrent.setInfo();
			
			if(destination_file.isComplete()){
				try {
					check_uselessPeers();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if(!continuer){
			executor.shutdownNow();
		}
	}

}
