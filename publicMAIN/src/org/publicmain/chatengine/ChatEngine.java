package org.publicmain.chatengine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.publicmain.common.Config;
import org.publicmain.common.FileTransferData;
import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;
import org.publicmain.gui.GUI;
import org.publicmain.nodeengine.NodeEngine;
import org.publicmain.sql.LocalDBConnection;

/**
 * @author ATRM
 * 
 */

public class ChatEngine{

	private static ChatEngine ce;
	private NodeEngine ne;
	private Set<Long> ignored;
	private long userID;
	private String alias;

	private Set<GruppenKanal> group_channels;
	private Set<KnotenKanal> private_channels;
	private KnotenKanal default_channel;

	// Verteilt eingehende Messages auf die Kan�le
	private Thread msgSorterBot = new Thread(new MsgSorter());
	// Wartungsthread f�r die NodeEngine
	// private Thread neMaintenance;
	private BlockingQueue<MSG> inbox;
	// private Set<String> allGroups=new HashSet<String>();
	private Set<String> myGroups = new HashSet<String>();
	
	/**
	 * Liefert die laufende Instanz der ChatEngine
	 * 
	 * @return: ChatEngine
	 */
	public static ChatEngine getCE() {
		return ce;
	}

	/**
	 * TODO: Kommentieren!
	 * 
	 * @throws IOException
	 */
	public ChatEngine() throws IOException {
		ce = this;
		// <<<<<<<< Tempor�r >>>>>>>>
		this.userID = (long) (Math.random() * Long.MAX_VALUE);
//		setUserID(Config.getConfig().getUserID());

		setAlias(System.getProperties().getProperty("user.name"));
//		setAlias(Config.getConfig().getAlias());

		this.ne = new NodeEngine(this);

		group_channels = new HashSet<GruppenKanal>();
		private_channels = new HashSet<KnotenKanal>();
		default_channel = new KnotenKanal(ne.getNodeID());
		ignored = Collections.synchronizedSet(new HashSet<Long>());
		inbox = new LinkedBlockingQueue<MSG>();

		// tempor�re Initialisierung der GruppenListe mit default Groups
		ne.getGroups().addAll(Arrays.asList(new String[] { "public" }));
		msgSorterBot.start();
	}

	/**
	 * Findet zu einer definierten NodeID zugeh�rigen Node in der Liste
	 * 
	 * @param nid, NodeID
	 * @return Node-Objekt zu angegebenem NodeID
	 */
	public Node getNodeForNID(long nid) {
		return ne.getNode(nid);
	}

	public Node getNodeForUID(long uid) {
		return ne.getNodeForUID(uid);
	}

	/**
	 * Findet zu einem bestimmten <code>alias</code>, falls eindeutig, den
	 * {@link Node} und liefert diesen zur�ck. <br>
	 * <b>Diese Methode ist nur f�r Befehlseingaben vorgesehen!</b>
	 * 
	 * @param
	 * @return {@link Node}
	 */
	public Node getNodeforAlias(String alias) {
		Set<Node> tmp = new HashSet<Node>();
		alias = alias.toLowerCase();
		for (Node x : getUsers()) {
			if (x.getAlias().toLowerCase().startsWith(alias)) {
				tmp.add(x);
			}
		}
		if (tmp.size() == 1) {
			return ((Node) tmp.toArray()[0]);
		}
		return null;
	}

	/**
	 * Diese Methode liefert die eigene NodeID
	 * 
	 * @return
	 */
	public long getMyNodeID() {
		return ne.getNodeID();
	}

	/**
	 * Getter f�r die <code>UserID</code>.
	 * 
	 * @return userID
	 */
	public long getUserID() {
		return userID;
	}

	/**
	 * Getter f�r den Anzeigenamen (Alias) zur�ck.
	 * 
	 * @return userID
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Setter f�r den Anzeigenamen (Alias) des eigenen Benutzers.
	 * 
	 * @param alias, neuer Anzeigename [a-zA-Z0-9]{12}
	 */
	public void setAlias(String alias) {
		this.alias = alias;
		if (ne != null && ne.isOnline()) {
			ne.updateAlias();
		}
	}

	/**
	 * Weisst die ChatEngine an einen <code>text</code> an den Nutzer mit der
	 * entsprechen <code>uid</code> zu schicken.
	 * 
	 * @param uid
	 * @param text
	 */
	public void send_private(long uid, String text) {
		MSG tmp = new MSG(ce.getNodeForUID(uid).getNodeID(), text);
		put(tmp);
		ne.sendtcp(tmp);
	}

	/**
	 * Weisst die ChatEngine an einen <code>text</code> an eine gruppe
	 * <code>group</code> zu schicken.
	 * 
	 * @param group, Gruppenbezeichnung
	 * @param text, Nachricht
	 */
	public void send_group(String group, String text) {
		MSG tmp = new MSG(group, text);
		put(tmp);
		ne.sendtcp(tmp);
	}
	
	/**
	 * Weisst die ChatEngine an eine <code>Datei</code> an einen Nutzer mit der
	 * entsprechenden <code>uid</code> zu schicken.
	 * 
	 * @param datei, Datei
	 * @param uid, UID des Empf�ngers
	 * 
	 * @return id des Dateitransfers f�r sp�tere R�ckfragen
	 */
	public void send_file(File datei, long uid) {
		Node tmp_node = getNodeForUID(uid);
		if (tmp_node == null) {
			GUI.getGUI().info(
					"User currently offline. Unable to transmit File.", uid, 2);
		} else {
			ne.send_file(datei, tmp_node.getNodeID());
		}
	}

	/**NOT IMPLEMENTED YET
	 * Gibt den Zustand der �bertragung einer Datei an
	 * 
	 * @param file_transfer_ID
	 * @return <ul>
	 *         <li><code>-1</code> Dateitransfer nicht m�glich</li>
	 *         <li><code>-2</code> Benutzer lehnt transfer ab</li>
	 *         <li><code>0</code> - <code>100</code> Vortschritt der
	 *         Daten�bertragung in Prozent
	 */
	public int file_transfer_status(int file_transfer_ID) {
		// TODO: CODE HERE
		return 0;
	}

	/**
	 * Fragt ein Array alle User ab
	 * 
	 * @return Array aller verbundener Nodes
	 */
	public Set<Node> getUsers() {
		return ne.getNodes();
	}

	/**
	 * Beitritt zu einer Gruppe
	 * 
	 * @param gruppen_name
	 *            Gruppennamen sind CaseInSensitiv und bestehen aus
	 *            alphanumerischen Zeichen
	 */
	public void group_join(String gruppen_name) {
		synchronized (myGroups) {
			if (myGroups.add(gruppen_name)) {
				ne.joinGroup(Arrays.asList(gruppen_name), null);
			}
		}
	}

	/**
	 * Verl�sst eine Gruppe wieder
	 * 
	 * @param gruppen_name, Gruppennamen sind CaseInSensitiv und
	 *            			bestehen aus alphanumerischen Zeichen
	 */
	public void group_leave(String gruppen_name) {
		synchronized (myGroups) {
			if (myGroups.remove(gruppen_name)) {
				ne.leaveGroup(Arrays.asList(gruppen_name), null);
			}
		}
	}

	/**
	 * Liefert eine Liste der verf�gbaren Gruppenstrings
	 * 
	 * @return Array der verf�gbaren Gruppenstrings
	 */
	public Set<String> getAllGroups() {
		synchronized (ne.getGroups()) {
			return ne.getGroups();
		}
	}

	/**
	 * Bittet die ChatEngine um ein Fileobjekt zur Ablage der empfangenen Datei
	 * wird von der NodeEnginge aufgerufen und soll an die GUI weiterleiten.
	 * 
	 * @param parameterObject
	 *            TODO
	 * @param filename
	 *            TODO
	 * 
	 * @return, abstraktes Fileobjekt zu speicherung einer Datei oder "null"
	 *          wenn der Nutzer den Empfang ablehnt
	 */
	public File request_File(FileTransferData parameterObject) {
		return GUI.getGUI().request_File(parameterObject);
	}

	/**
	 * Veranlasst das Nachrichten vom Benutzer mit der <code>uid</code> nicht
	 * mehr angezeigt werden. Die Pr�fung ob der Nutzer vorhanden ist muss durch
	 * die GUI realisiert werden.
	 * 
	 * @param uid
	 */
	public boolean ignore_user(long uid) {
		if (uid != userID) {
			return ignored.add(getNodeForUID(uid).getNodeID());
		}
		return false;
	}

	/**
	 * Veranlasst das Nachrichten vom Benutzer mit der <code>uid</code> wieder
	 * angezeigt werden. Hier wird gepr�ft ob der Benutzer �berhaupt in der
	 * <code>ignored</code> ist. Wenn ja wird das Long aus der HashSet gel�scht
	 * und "true" zur�ckgeliefert anderenfalls wird mit "false" das Fehlen des
	 * Eintrages signalisiert
	 * 
	 * @param uid
	 */
	public boolean unignore_user(long uid) {
		return ignored.remove(getNodeForUID(uid).getNodeID());
	}

	/**
	 * Meldet einen Nachrichten-Listener an einem Gruppen - Nachrichten Kanal
	 * an.
	 * 
	 * @param chatPanel, das abonierende Fenster
	 * @param gruppen_name, zu abonierender Gruppen Kanal
	 */
	public void add_MSGListener(Observer chatPanel, String gruppen_name) {
		for (Kanal cur : group_channels) {
			if (cur.is(gruppen_name)) {
				cur.addObserver(chatPanel);
				return;
			}
		}
		GruppenKanal tmp = new GruppenKanal(gruppen_name);
		tmp.addObserver(chatPanel);
		group_channels.add(tmp);
		group_join(gruppen_name);
	}

	/**
	 * Meldet einen Nachrichten-Listener an einem privaten - Nachrichten Kanal
	 * an.
	 * 
	 * @param chatPanel, das abonierende Fenster
	 * @param gruppen_name, zu abonierender Gruppen Kanal
	 */
	public void add_MSGListener(Observer chatPanel, long uid) {
		long nid = ce.getNodeForUID(uid).getNodeID();
		for (KnotenKanal cur : private_channels) {
			if (cur.is(nid)) {
				cur.addObserver(chatPanel);
				return;
			}
		}
		KnotenKanal tmp = new KnotenKanal(nid);
		tmp.addObserver(chatPanel);
		private_channels.add(tmp);
	}
	
	/**
	 * Den Default-Kanal f�r eingehende, nicht zuordenbare Nachrichten registrieren.
	 * TODO: Kommentar pr�fen!
	 * 
	 * @param gui
	 */
	public void register_defaultMSGListener(Observer gui) {
		default_channel.addObserver(gui);
	}

	/**
	 * Entfernt ein Chatpannel aus allen Kan�len.
	 * 
	 * @param chatPanel
	 */
	public void remove_MSGListener(Observer chatPanel) {
		Set<Kanal> empty = new HashSet<Kanal>();

		for (Kanal x : group_channels) {
			x.deleteObserver(chatPanel);
			 // Wenn der Kanal leer ist.
			if (x.countObservers() == 0) {
				empty.add(x);
				group_leave((String) x.referenz);
			}
		}
		group_channels.removeAll(empty);

		empty.clear();
		for (Kanal x : private_channels) {
			x.deleteObserver(chatPanel);
			 // Wenn der Kanal leer ist.
			if (x.countObservers() == 0) {
				empty.add(x);
			}
		}
		private_channels.removeAll(empty);

	}

	/**
	 * Wird von der NodeEngine aufgerufen um f�r den User interressante
	 * Nachrichten an die ChatEngine zu �bermitteln.
	 * 
	 * @param nachricht, die neue Nachricht
	 */
	public void put(MSG nachricht) {
		if (!ignored.contains(nachricht.getSender())) {
			inbox.add(nachricht);
			
		}
	}

	/**
	 * TODO: Kommentar
	 * 
	 */
	private final class MsgSorter implements Runnable {
		public void run() {
			while (true) {
				try {
					MSG tmp = inbox.take();
					LogEngine.log("msgSorterBot", "sorting", tmp);
					if (tmp.getTyp() == NachrichtenTyp.GROUP) {
						for (Kanal x : group_channels)
							if (x.add(tmp))
								break;
					} else if (tmp.getTyp() == NachrichtenTyp.PRIVATE) {
						System.out.println("PRIVATE MSG");
						boolean msgAssigned = false;
						for (KnotenKanal y : private_channels) {
							if (y.add(tmp)) {
								msgAssigned = true;
								break;
							}
						}
						// Kein CW angemeldet um die Nachricht aufzunehmen sende
						// es an GUI via DEFAULT CHANNEL
						if (!msgAssigned)default_channel.add(tmp);
					}
				} catch (InterruptedException e) {
					// Unterbrochen beim Warten...
					// hmmm ist das Schlimm?
				}
			}
		}
	}

	/**
	 * Die eigenen Gruppenmigliedschaften holen.
	 * 
	 */
	public Set<String> getMyGroups() {
		synchronized (myGroups) {
			return myGroups;
		}
	}

	/**
	 * Den eigenen Benutzeralias �ndern.
	 * 
	 */
	public void updateAlias(String newAlias) {
		setAlias(newAlias);
		ne.updateAlias();
	}

	/**
	 * Verschiedene Debug-Funktionen zum testen usw. diese werden sp�ter
	 * aus dem Programm entfernt.
	 * 
	 */
	public void debug(String command, String parameter) {
		switch (command) {
		case "alias":
			setAlias(parameter);
			break;
		case "disconnect":
			shutdown();
			break;

		case "ping":
			final Node tmp_ping = ce.getNodeforAlias(parameter);
			if (tmp_ping != null) {
				new Thread(new Runnable() {
					public void run() {
						GUI.getGUI().info(
								"Ping(" + tmp_ping.getAlias() + "):"
										+ ne.pathPing(tmp_ping), null, 0);
					}
				}).start();
			}
			break;

		case "info":
			Node nodeforAlias = ce.getNodeforAlias(parameter);
			if (nodeforAlias != null) {
				Map<String, String> tmp = nodeforAlias.getData();
				for (String x : tmp.keySet()) {
					GUI.getGUI().info(x + ":" + tmp.get(x), null, 0);
				}
			}
			break;

		default:
			ne.debug(command, parameter);
			break;
		}

	}

	/**
	 * Geordnetes Herunterfahren und Abmelden von pM vom Netzwerk.
	 * 
	 */
	public void shutdown() {
		ne.disconnect();
	}

	/**
	 * Pr�fen ob nid auf der ignored-Liste steht und ein entsprechendes boolean
	 * zur�ckliefern.
	 * 
	 * @param nodeID
	 * @return
	 */
	public boolean is_ignored(long nodeID) {
		return ignored.contains(nodeID);
	}

	/**
	 * Benutzer �ber einen anstehenden Dateitransfers informieren.
	 * 
	 * @param tmp
	 */
	public void inform(FileTransferData tmp) {
		String str = tmp.receiver.getAlias()
				+ ((tmp.accepted) ? " accept " : " declined ")
				+ "receiving File:\"" + tmp.datei.getName() + "\"";
		GUI.getGUI().info(str, tmp.receiver.getUserID(), 0);

	}
}
