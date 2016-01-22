package opentracker;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class OpentrackerScript {
	
	static String script_launch = "./src/opentracker/launch_opentracker.sh" ;

	public static String getLocalAddress(String network_interface) throws SocketException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			if (iface.getName().equals(network_interface)) {
				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
						return addr.getHostAddress();
					}
				}
			}
		}
		return null;
	}
	
	public static void main(String args[]){
		System.out.println("Lancement de Opentracker...");
		try {
			String command = script_launch+" "+getLocalAddress("wlan0")+" "+10000+" "+10000 ;
			Process p = Runtime.getRuntime().exec(command);
			StreamDisplay flux_sortie = new StreamDisplay(p.getInputStream());
			StreamDisplay flux_erreur = new StreamDisplay(p.getErrorStream());
			new Thread(flux_sortie).start();
			new Thread(flux_erreur).start();
			p.waitFor();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}

}
