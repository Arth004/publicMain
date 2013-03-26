package org.publicmain.nodeengine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.NachrichtenTyp;
import org.publicmain.common.Node;
import org.publicmain.gui.GUI;



/**Wir eine Facade f�r unsere Sockets um Messeges zu empfangen und zu versenden
 * @author Kaddi
 *
 */
public class ConnectionHandler {
	private static final int NOT_CONNECTED = 0;
	private static final int CONNECTED = 1;
	private static final int CHATMODE = 2;
	private static final int DATAMODE = 2;
	
	private Socket line;
	private ObjectOutputStream line_out;
	private ObjectInputStream line_in;
	private Thread pakets_rein_hol_bot;
	private Node connectedWith;
	private Set<Node> childs;
	private NodeEngine ne;
	private int zustand=NOT_CONNECTED;
	
	
	public ConnectionHandler(Socket underlying) throws IOException{
		ne=NodeEngine.getNE();
		childs = new HashSet<Node>(); 
		pakets_rein_hol_bot = new Thread(new reciever());
		line = underlying;
		line_out=new ObjectOutputStream(new BufferedOutputStream(line.getOutputStream()));
		send(new MSG(ne.getME()));
		//line_out.flush();
		line_in=new ObjectInputStream(new BufferedInputStream(line.getInputStream()));
		zustand=CONNECTED;
		LogEngine.log("Verbindung", this, LogEngine.INFO);
		pakets_rein_hol_bot.start();
	}

	/**Verschickt ein MSG-Objekt �ber den Soket.
	 * @param paket Das zu versendende Paket
	 * @throws IOException Wenn es zu einem Fehler beim senden auf dem TCP-Socket kommt
	 */
	public void send(MSG paket) throws IOException{
			line_out.writeObject(paket);
			line_out.flush();
	}
	
	class reciever implements Runnable
	{
		public void run() 
		{
			while(zustand==CHATMODE&&line.isConnected())
			{
				try 
				{
					MSG tmp = (MSG) line_in.readObject();
					if (tmp.getCode()==MSG.NODE_UPDATE){
						childs.add((Node)tmp.getData());
						if(connectedWith==null)connectedWith=(Node)tmp.getData();
					}
					ne.handle(tmp,getIndexOfME());
				} 
				catch (ClassNotFoundException|IOException e) 
				{
					LogEngine.log(e);
				} 
			}
		}		
	}
	
	public Node getConnectionPartner(){
		while(connectedWith==null)
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LogEngine.log(e);
			}
		return connectedWith;
	}

	public boolean isConnected() {
		return line.isConnected();
	}
	private int getIndexOfME(){
		return NodeEngine.getNE().connections.indexOf(this);
	}
}
