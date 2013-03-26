package org.publicmain.common;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.publicmain.nodeengine.NodeEngine;

public class Node implements Serializable {

	private static Node me;

	private long nodeID;
	private long userID;
	private String alias;
	private List<InetAddress> sockets;
	private String hostname;
	private int server_port;
	//private boolean isRoot;
	
	private Node() {
		Random myrnd = new Random();
		nodeID = myrnd.nextLong();
		userID = myrnd.nextLong(); //noch zuf�llig sp�ter aus config
		alias = System.getProperties().getProperty("user.name");
		sockets=getMyIPs();
		server_port = NodeEngine.getNE().getServer_port();
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public static Node getMe(){
		if(me==null)me=new Node();
		return me;
	}

	public long getNodeID() {
		return nodeID;
	}

	public void setNodeID(long nodeID) {
		this.nodeID = nodeID;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public List<InetAddress> getSockets() {
		return sockets;
	}


	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
/*
	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	*/
	/**Erzeugt eine Liste aller lokal vergebenen IP-Adressen mit ausnahme von Loopbacks und IPV6 Adressen
	 * @return Liste aller lokalen IPs
	 */
	public static List<InetAddress> getMyIPs() {
		List<InetAddress> addrList = new ArrayList<InetAddress>();
		try {
			for (InetAddress inetAddress : InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())) { //Finde alle IPs die mit meinem hostname assoziert sind und 
			if (inetAddress.getAddress().length==4)addrList.add(inetAddress);									 //f�ge die meiner liste hinzu die IPV4 sind also 4Byte lang
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return addrList;
	}

	public int getServer_port() {
		return server_port;
	}
	
	@Override
	public String toString() {
		// 
		return alias+"@"+hostname;
	}

	

}
