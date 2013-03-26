package org.publicmain.nodeengine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.publicmain.chatengine.ChatEngine;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.Node;





/**
 * Die NodeEngine ist f�r die Verbindungen zu anderen Nodes zust�ndig.
 * Sie verwaltet die bestehenden Verbindungen, sendet Nachichten und Datein 
 * und ist f�r das Routing zust�ndig 
 */
public class NodeEngine {
	protected static final long CONNECTION_TIMEOUT = 1000;
	private static volatile NodeEngine ne;
	private Node meinNode;
	private ChatEngine ce;
	
	private ServerSocket server_socket;
	private ConnectionHandler root_connection;
	private MulticastSocket multi_socket;
	
	public List<ConnectionHandler> connections;
	private List<Node> allNodes;
	private Map<Integer,List<Node>> routing;
	
	
	private boolean isOnline;
	private boolean isRoot;

	private Thread multicastRecieverBot;
	private Thread connectionsAcceptBot;

	private final InetAddress group = InetAddress.getByName("230.223.223.223");
	private final int multicast_port = 6789;
	private final int MAX_CLIENTS = 5;
	


	
	public NodeEngine(ChatEngine parent) throws IOException {
		allNodes =new ArrayList<Node>();
		connections=new ArrayList<ConnectionHandler>();
		routing = new HashMap<Integer, List<Node>>();
		
		ne=this;
		ce=parent;
		
		server_socket = new ServerSocket(0);
		multi_socket = new MulticastSocket(multicast_port);
		multi_socket.joinGroup(group);
		multi_socket.setLoopbackMode(true);
		multi_socket.setTimeToLive(10);

		meinNode=Node.getMe();
		allNodes.add(meinNode);
		
		isOnline=false;

		LogEngine.log("Multicast Socket ge�ffnet",this,LogEngine.INFO);
		
		
		connectionsAcceptBot = new Thread(new Runnable() {
			public void run() {
				while (isOnline&&connections.size()<=MAX_CLIENTS) {
					System.out.println("Listening on Port:" + server_socket.getLocalPort());
					try {
						ConnectionHandler tmp = new ConnectionHandler(server_socket.accept());
						connections.add(tmp);
						routing.put(connections.indexOf(tmp),new ArrayList<Node>());
						tmp.send(new MSG(allNodes,MSGCode.REPORT_ALLNODES));
						LogEngine.log(tmp);
					} catch (IOException e) {
						LogEngine.log(e);
					}
				}
			}
		});
		
		
		multicastRecieverBot=new Thread(new Runnable() {
			public void run() {
				while(true){
					byte[] buff = new byte[65535];
					DatagramPacket tmp = new DatagramPacket(buff, buff.length);
					try {
						multi_socket.receive(tmp);
						MSG nachricht = MSG.getMSG(tmp.getData());
						LogEngine.log(this,"multicastRecieve",nachricht);
						handle(nachricht,-2);
					} catch (IOException e) {
						LogEngine.log(e);
					}
				}
			}
		});
		multicastRecieverBot.start();
		
		send(new MSG(meinNode, MSGCode.ROOT_DISCOVERY), -2);
		Thread selbstZumRootErkl�rer = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(CONNECTION_TIMEOUT);
				} catch (InterruptedException e) {
				}
				if(!isOnline){
					isOnline=true;
					isRoot=true;
					connectionsAcceptBot.start();
				}
			}
		});
		selbstZumRootErkl�rer.start();
		
		
	}

	public static NodeEngine getNE(){
		return ne;
	}
	


	
	/**
	 * isConnected() gibt "true" zur�ck wenn die laufende Nodeengin hochgefahren und mit anderen Nodes verbunden oder root ist,
	 * "false" wenn nicht.
	 */
	public	boolean	isOnline(){
		return isOnline;
	}
	
	/**
	 * isRoot() gibt "true" zur�ck wenn die laufende Nodeengin Root ist und
	 * "false" wenn nicht.
	 */
	public boolean	isRoot (){
		return ((root_connection==null)||!root_connection.isConnected());
	}
	
	
	/**
	 * getMe() gibt das eigene NodeObjekt zur�ck
	 */
	public Node getME (){
		return meinNode;					//TODO:nur zum test
	}
	
	/**
	 * getNodes() gibt ein NodeArray zur�ck welche alle verbundenen
	 * Nodes beinhaltet.
	 */
	public List<Node> getNodes (){
		System.out.println(allNodes);
		return allNodes;					//TODO:nur zum test
	}
	
	
	private void updateNodes(){
		if(isRoot){
		allNodes.clear();
		allNodes.add(getME());
			for(List<Node>x :routing.values()){
				allNodes.addAll(x);
			}
		}
		else root_send(new MSG(null,MSGCode.POLL_ALLNODES));
	}
	
	private void updateChilds(){
		for(ConnectionHandler x : connections){
			x.send(new MSG(null,MSGCode.POLL_CHILDNODES));
		}
	}


	/**
	 * Gibt ein StringArray aller vorhandenen Groups zur�ck
	 *
	 */
	public String[] getGroups	(){
		String[] grouparray = {"public","GruppeA", "GruppeB"};
		return grouparray;					//TODO:to implement
	}
	
	
	private void root_send(MSG msg) {
		root_connection.send(msg);
	}
	
	/**
	 * versendet Daten vom Typ MSG an ZielNodes oder git diese an send_file() weiter.
	 * pr�ft Dateigr��e (wenn < 5MB aufruf der send_file() sonst als Msg-Type Data)
	 * send_file() wird sowohl das Ziel als auch die Daten mitgegeben. D.h., dass das
	 * MSG-Paket hier in File und destination geteilt.
	 */
	public void sendmutlicast (MSG nachricht){
		byte[] buf = MSG.getBytes(nachricht);
		LogEngine.log(this,"sende",nachricht);
		try {
			multi_socket.send(new DatagramPacket(buf,buf.length,group,6789));
		} catch (IOException e) {
			LogEngine.log(e);
		}
	}
	
	public void sendtcp(MSG nachricht){
		if(!isRoot)root_connection.send(nachricht);
		for (ConnectionHandler x : connections) x.send(nachricht);
	}
	
	/**
	 * versendet Datein �ber eine TCP-Direktverbindung
	 * wird nur von send() aufgerufen nachdem festgestellt wurde, dass nachicht > 5MB
	 */
	public void send_file (String destination){		
		// bekommt ziel und FILE �bergeben
		
	}
	
	
	
	
	

	/**
	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird ausschlie�lich von den ConnectionHandlern aufgerufen um empfange Pakete verarbeiten zu lassen.
	 * 
	 * @param paket neue 
	 * @param index index der Quelle <ul>	<li>-2 MulticastSocket
	 * 									<li>-1 RootConnectionSocket
	 * 									<li>0> ChildSocketIndex
	 * 							</ul>
	 */
	public void handle(MSG paket, int index) { // Muss Thread-Safe sein damit die ConnHandlers direkt damit arbeiten k�nnen.
		LogEngine.log(this,"handling ["+index+"]",paket);
		switch (paket.getTyp()){
		case GROUP:
			if(!isRoot&&index!=-1)root_connection.send(paket);
			for (int j = 0; j < connections.size(); j++) {
				if(j!=index)
					connections.get(j).send(paket);
			}
			ce.put(paket);
			break;
		case SYSTEM:
			switch(paket.getCode()){
			case NODE_UPDATE:
				LogEngine.log(this, "NODE_UPDATE auf "+index, paket);
				if(index==-2)allNodes.add((Node)paket.getData());
				if(index>=0){
					routing.get(index).add((Node) paket.getData());
					send(paket,-1);
				}
				break;
			case ROOT_REPLY:
				if(!isOnline){
					try {
						connectTo((Node)paket.getData());
					} catch (IOException e) {
						LogEngine.log(e);
					}
				}
				break;
			case ROOT_DISCOVERY:
				if(isRoot&&isOnline) sendDiscoverReply((Node)paket.getData());
				break;
				
			case POLL_ALLNODES:
				if(index>=0&&index<connections.size())connections.get(index).send(new MSG(allNodes,MSGCode.REPORT_ALLNODES));
				else LogEngine.log("POLL_ALLNODES von komischer Qulle bekommen:"+index, this, LogEngine.WARNING);
				break;
			case REPORT_ALLNODES:
				if(index==-1){
					allNodes=(List<Node>)paket.getData();
					allNodes.add(meinNode);
				}
				else LogEngine.log("REPORT_ALLNODES von komischer Qulle bekommen:"+index, this, LogEngine.WARNING);
				break;
			case POLL_CHILDNODES:
				if(index==-1){
				List<Node> tmp=new ArrayList<Node>();
				for(List<Node> x : routing.values()) tmp.addAll(x);
				root_send(new MSG(tmp,MSGCode.REPORT_CHILDNODES));
				}
				else LogEngine.log("POLL_CHILDNODES von komischer Qulle bekommen:"+index, this, LogEngine.WARNING);
				break;
			case REPORT_CHILDNODES:
				if(index>=0&&index<connections.size()){
					routing.get(index).clear();
					routing.get(index).addAll((List<Node>) paket.getData());
				}
				else LogEngine.log("REPORT_CHILDNODES von komischer Qulle bekommen:"+index, this, LogEngine.WARNING);
				break;
			}
			break;
		case DATA:
			break;
		default:
				//ce.put(paket);
		}
		
	}
	
	private void sendDiscoverReply(Node quelle) {
		// TODO Auto-generated method stub
		byte[] data=MSG.getBytes(new MSG(getBestNode(), MSGCode.ROOT_REPLY));
		LogEngine.log("sending Replay to " + quelle.toString(),this,LogEngine.INFO);
/*		DatagramPacket discover =new DatagramPacket(data,data.length,group,multicast_port);//�ber Multicast
		try {
			multi_socket.send(discover);
		} catch (IOException e) {
			LogEngine.log(e);
		}
	*/
		for(InetAddress x : quelle.getSockets()){
			DatagramPacket discover =new DatagramPacket(data,data.length,x,multicast_port);//�ber Unicast
			try {
				multi_socket.send(discover);
			} catch (IOException e) {
				LogEngine.log(e);
			}
			
		}

		
	}


	private Node getBestNode() {
		// TODO Intelligente Auswahl des am besten geeigneten Knoten mit dem sicher der Neue Verbinden darf.
		
		return meinNode;
	}


	/** Stelle Verbindung mit diesem <code>NODE</code> her!!!!
	 * @param knoten der Knoten
	 * @throws IOException 
	 */
	private void connectTo(Node knoten) throws IOException {

		Socket tmp_socket = null;
		for (InetAddress x : knoten.getSockets()) {
			try {
				tmp_socket = new Socket(x.getHostAddress(),
						knoten.getServer_port());

			} catch (UnknownHostException e) {
				LogEngine.log(e);
			} catch (IOException e) {
				LogEngine.log(e);
			}
			if (tmp_socket != null && tmp_socket.isConnected())
				break;
		}
		if (tmp_socket != null) {
			root_connection = new ConnectionHandler(tmp_socket);
			send(new MSG(getME()),-1);
			send(new MSG(getME()),-2);
			isOnline = true;

		}
	}
	
	private void send(MSG paket, int socket){
		switch (socket) {
		case -2:
			byte[] data=MSG.getBytes(paket);
			try {
			if(data.length<64000){
				DatagramPacket udp_paket =new DatagramPacket(data,data.length,group,multicast_port);
				multi_socket.send(udp_paket);
			}else LogEngine.log("Paket zu gro� f�r UDP",this,LogEngine.ERROR);
			} catch (IOException e) {
				LogEngine.log(e);
			}
			break;
		case -1:
			if(!isRoot&&root_connection!=null)root_connection.send(paket);
			break;
		default:
			connections.get(socket).send(paket);
		}
	}

	public int getServer_port(){
		return server_socket.getLocalPort();
	}

}
