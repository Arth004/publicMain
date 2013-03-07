import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;



/**
 * @author tkessels
 *
 */
public class ChatEngine extends Observable{
	private static ChatEngine ce;
	public NodeEngine ne;
	public LogEngine log;
//	private GUI gui;
	private Set<Node> nodes;
	private List<GruppenKanal> group_channels;
	private List<KnotenKanal> private_channels;
	
	private Thread msgSorterBot;//verteilt eingehende MSGs
	private Thread neMaintenance;//warted die NE
	
	
	private BlockingQueue<MSG> inbox;
	
	
	
	/**Liefert die Instanz der CE
	 * @return
	 */
	public static synchronized ChatEngine getCE() throws Exception{
		if(ce==null) ce=new ChatEngine();
		return ce;
	}

	
	private ChatEngine(){
		ne=NodeEngine.getNE();
		group_channels=new ArrayList<GruppenKanal>();
		private_channels=new ArrayList<KnotenKanal>();
		nodes=new HashSet<Node>();
		nodes.addAll(Arrays.asList(ne.getNodes()));
		inbox=new LinkedBlockingQueue<MSG>();
	}
	
	
	/**Weisst die ChatEngine an einen <code>text</code> an den Nutzer mit der entsprechen <code>uid</code> zu schicken. 
	 * @param uid UID des Empf�ngers
	 * @param text Nachricht
	 */
	public void send_private(long uid, String text){
		
		//TODO: CODE HERE
	}
	
	/**Weisst die ChatEngine an einen <code>text</code> an eine gruppe <code>group</code> zu schicken.
	 * @param group Gruppenbezeichnung
	 * @param text Nachricht
	 */
	public void send_group(String group, String text){
		//TODO: CODE HERE	
	}
	
	/**Weisst die ChatEngine an einen <code>datei</code> an einen Nutzer mit der entsprechenden <code>uid</code> zu schicken.
	 * @param uid UID des Empf�ngers
	 * @param datei Datei
	 * @return id des Dateitransfers f�r sp�tere R�ckfragen
	 */
	public int send_file(long uid, File datei){
		//TODO: CODE HERE
		return 0;
	}
	
	/** Gibt den Zustand der �bertragung einer Datei an
	 * @param file_transfer_ID
	 * @return <ul>	<li><code>-1</code> Dateitransfer nicht m�glich</li>
	 * 				<li><code>-2</code> Benutzer lehnt transfer ab</li>
	 * 				<li><code>0</code> - <code>100</code> Vortschritt der Daten�bertragung in Prozent 
	 */
	public int file_transfer_status(int file_transfer_ID){
		
		//TODO: CODE HERE
		return 0;
	}
	
	/**Fragt ein Array alle User ab 
	 * @return Array aller verbundener Nodes
	 */
	public	Node[]	getUsers(){
		return (Node[]) nodes.toArray();
	}
	
	/** tritt einer Gruppe bei
	 * @param gruppen_name Gruppennamen sind CaseInSensitiv und bestehen aus alphanumerischen Zeichen
	 */
	public void group_join(String gruppen_name){
		/*Lege KAnal an 
		 * informiere NodeEngine �ber neue gruppe wenn noch nicht vorhanden so das ander nodes diese anzeigen
		 * vielleicht machen wirdas auch einfach indem wir ein group announce paket forgen
		*/
		//TODO: CODE HERE
	}
	
	/**verl�sst eine gruppe wieder
	 * @param gruppen_name Gruppennamen sind CaseInSensitiv und bestehen aus alphanumerischen Zeichen
	 */
	public void group_leave(String gruppen_name){
		
		//TODO: CODE HERE		
	}
	
	/**Liefert eine Liste der verf�gbaren Gruppenstrings
	 * @return Array der verf�gbaren Gruppenstrings
	 */
	public	String[]	group_list(){
		//TODO: CODE HERE		
		return null;
	}
	
	/** Bittet die ChatEngine um ein Fileobjekt zur Ablage der empfangenen Datei
	 * wird von der NodeEnginge aufgerufen und soll an die GUI weiterleiten
	 * @return abstraktes Fileobjekt zu speicherung einer Datei. 
	 * 	Null Wenn der Nutzer den Empfang ablehnt 
	 */
	public	File	request_File(){
		//TODO: CODE HERE
		return GUI.getGUI().request_File();
	}
	
	/**Veranlasst das Nachrichten vom user mit der <code>uid</code> nicht mehr angezeigt werden.
	 * @param uid
	 */
	public	void	ignore_user(long uid){
		//TODO: CODE HERE
	}
	
	/**Ver�ndert den Anzeigenamen des Nutzers
	 * @param alias neuer Anzeigename [a-zA-Z0-9]{12} 
	 */
	public	void	set_alias(String alias){
		//TODO: CODE HERE
	}
	
	/**Liefert den aktuellen Anzeigenamen
	 * @return aktueller Anzeigename
	 */
	public	String	get_alias(){
		return ne.getME().getAlias();
	}
	
	
	/** Meldet einen Nachrichten-Listener an einem Gruppen - Nachrichten Kanal an 
	 * @param chatPanel Das abonierende Fenster
	 * @param gruppen_name zu abonierender Gruppen Kanal
	 */
	public void add_MSGListener(Observer chatPanel,String gruppen_name){
		//gibt es den Kanal schon
		group_channels.contains(new GruppenKanal(gruppen_name));
		
		//neues CW an Kanal anmelden

		
		/*for (Kanal x : channels) {
			if(x.is(gruppen_name)){
				x.addObserver(chatPanel);
				return;
			}
		}
		
		GruppenKanal tmp =new GruppenKanal(gruppen_name);
		channels.add(tmp);*/
		
	}
	
	
	/** Meldet einen Nachrichten-Listener an einem privaten - Nachrichten Kanal an 
	 * @param chatPanel Das abonierende Fenster
	 * @param gruppen_name zu abonierender Gruppen Kanal
	 */
	public void	add_MSGListener(Observer chatPanel,long UID){
		//TODO:Code Here		
	}
	/** Entefert ein Chatpannel aus allen Kan�len
	 * @param chatPanel
	 */
	public	void	remove_MSGListener(Observer chatPanel){
		//TODO:Code Here		
	}
	
	
	/**Wir von der NodeEngine aufgerufen um f�r den User interressante Nachrichten an die ChatEngine zu �bermitteln
	 * @param nachricht Die neue Nachricht
	 */
	public void put(MSG nachricht){
		inbox.add(nachricht);
	}
	

	
	
	
	public static void main(String[] args) {
		
	}

	

}


