package clientTorrent.Client.peer;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import clientTorrent.Message.Choke;
import clientTorrent.Message.Unchoke;

public class UnchockAlgorithm implements Runnable {

	private PeerManager manager;
	int time = 0;
	int time2 = 0;
	boolean first_time = true;
	boolean first_time2 = true;

	public UnchockAlgorithm( PeerManager manager) {
		this.manager = manager;
	}

	public synchronized ArrayList<PeerNew> setToList(Map<PeerNew, Integer> set) {
		ArrayList<PeerNew> list = new ArrayList<PeerNew>();

		for (PeerNew peer_new : set.keySet()) {
			if (!manager.getUnchocked_peers().containsKey(peer_new))
				list.add(peer_new);
		}

		return list;
	}

	@Override
	public void run() {

		while (true) {

			System.out.println("DANS LE RUN DE UNCHOCK ALOG");
			other_unchocked();
			HashMap<PeerNew, Integer> map = manager.getInterested_peers();
			int size = map.size();
			if (size != 0) {

				Comparateur comp = new Comparateur(map);
				TreeMap<PeerNew, Integer> map_triee = new TreeMap(comp);
				map_triee.putAll(map);
				int i = 0;
				for (PeerNew peer_new : map_triee.keySet()) {
					peer_new.getFileMessage()
							.offer(new Unchoke());
					peer_new.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
					manager.addUnchocked_peers(peer_new);

					i++;
					if (i == 3 || i == (size - 1))
						break;
				}

				if (!first_time && time == 0) {
					ArrayList<PeerNew> liste = setToList(map);

					int max = liste.size();
					if (max != 0) {
						int nombreAleatoire = (int) (Math.random() * ((max)));
						PeerNew random = liste.get(nombreAleatoire);
						random.getFileMessage()
								.offer(new Unchoke());
						random.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
						
					}

				}

			}

			try {
				Thread.sleep(10000);
				time = (time + 10) % 3;
			} catch (InterruptedException e) {
				System.out.println("problème d'attente");
			}

			first_time = false;

		}

	}

	// TO MODIFY
	public synchronized void other_unchocked() {
		
		System.out.println("DANS LE RUN DE UNCHOCK ALOG NUMERO 2");

		HashMap<PeerNew, Date> map = manager.getUnchocked_peers();
		int size = map.size();
		if (size != 0) {

			Comparateur_Date comp = new Comparateur_Date(map);
			TreeMap<PeerNew, Date> map_triee = new TreeMap<PeerNew, Date>(comp);
			map_triee.putAll(map);
			int i = size;
			for (PeerNew peer_new : map_triee.keySet()) {
				if (size < 4) {

				} else {
					peer_new.getFileMessage()
							.offer(new Choke());
					peer_new.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
					manager.removeUnchocked_peers(peer_new);
				}
				i--;
				if (i == size - 3)
					break;
			}

			ArrayList<PeerNew> liste = setToList(manager.getInterested_peers());

			int max = liste.size();
			if (max != 0) {
				int nombreAleatoire = (int) (Math.random() * ((max)));
				PeerNew random = liste.get(nombreAleatoire);
				random.getFileMessage()
						.offer(new Unchoke());
				random.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
			}

			if (!first_time2 && time2 == 0) {

				try {
					Thread.sleep(10000);
					time2 = (time2 + 10) % 2;
				} catch (InterruptedException e) {
					System.out.println("problème d'attente");
				}

			}

			else {
				
				HashMap<PeerNew, Date> map2 = manager.getUnchocked_peers();
				int size2 = map2.size();
				if (size2 != 0) {

					Comparateur_Date comp2 = new Comparateur_Date(map2);
					TreeMap<PeerNew, Date> map_triee2 = new TreeMap<PeerNew, Date>(comp2);
					map_triee.putAll(map2);
					int i2 = size2;
					for (PeerNew peer_new : map_triee2.keySet()) {
						if (size2 < 5) {

						} else {
							peer_new.getFileMessage()
									.offer(new Choke());
							peer_new.getSelectionKey().interestOps(SelectionKey.OP_WRITE);

							manager.removeUnchocked_peers(peer_new);
						}
						i2--;
						if (i2 == size - 4)
							break;
					}
				}
				
				
				try {
					Thread.sleep(10000);
					time2 = (time2 + 10) % 2;
				} catch (InterruptedException e) {
					System.out.println("problème d'attente");
				}
			}
		}

		first_time2 = false;

	}

	class Comparateur implements Comparator {

		Map tuple;

		public Comparateur(HashMap map) {
			this.tuple = map;
		}

		// ce comparateur ordonne les éléments dans l'ordre décroissant
		@Override
		public int compare(Object o1, Object o2) {
			// TODO Auto-generated method stub
			if ((int) tuple.get(o1) >= (int) tuple.get(o2)) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	class Comparateur_Date implements Comparator {

		Map<PeerNew, Date> tuple;

		public Comparateur_Date(HashMap<PeerNew, Date> map) {
			this.tuple = map;
		}

		// ce comparateur ordonne les éléments dans l'ordre décroissant
		@Override
		public int compare(Object o1, Object o2) {
			// TODO Auto-generated method stub
			if (tuple.get(o1).before(tuple.get(o2))) {
				return -1;
			} else {
				return 1;
			}
		}
	}

}
