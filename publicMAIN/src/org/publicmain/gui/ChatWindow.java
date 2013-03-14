package org.publicmain.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;

//import com.nilo.plaf.nimrod.NimRODLookAndFeel;

/**
 * @author ATRM
 *
 */

public class ChatWindow extends JPanel implements ActionListener, Observer{

	// Deklarationen:
	private String name;
	private JButton sendenBtn;
	private JTextArea msgTextArea;
	private JScrollPane jScrollPane;
	private JTextField eingabeFeld;
	private String gruppe;
	private long user;
	
	
	private GUI gui;
	
	/**
	 * Erstellt Content und macht Layout f�r das Chatpanel
	 */
	private void doWindowbuildingstuff(){
		//Layout f�r ChatWindow (JPanel) festlegen auf BorderLayout:
		this.setLayout(new BorderLayout());
	
		// Initialisierungen:
		this.sendenBtn = new JButton("send");
		this.msgTextArea = new JTextArea(10,30);
		this.jScrollPane = new JScrollPane(msgTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.eingabeFeld = new JTextField();
		
		msgTextArea.setEditable(false);
		msgTextArea.setLineWrap(true);
		
		eingabeFeld.setDocument(new SetMaxText(200));
		
		sendenBtn.addActionListener(this);
		eingabeFeld.addActionListener(this);
		
		this.add(jScrollPane, BorderLayout.CENTER);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(eingabeFeld, BorderLayout.CENTER);
		panel.add(sendenBtn, BorderLayout.EAST);
		this.add(panel, BorderLayout.SOUTH);
		
		this.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void focusGained(FocusEvent arg0) {
				//Focus auf eingabeFeld setzen:
				eingabeFeld.requestFocusInWindow();
			}
		});
		
		this.setVisible(true);
	}
	
	public ChatWindow(long uid, String username){
		this.user=uid;
		this.name=username;
		this.gui = GUI.getGUI();
		doWindowbuildingstuff();
	}
	
	public ChatWindow(String gruppenname){
		gruppe=gruppenname;
		this.name=gruppenname;
		this.gui = GUI.getGUI();
		doWindowbuildingstuff();
	}
	
	/**
	 * @return String f�r Tab..
	 */
	public String getChatWindowName(){
		return this.name;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(!eingabeFeld.getText().equals("")){

		//Pr�fen ob die Eingabe mit "/" beginnt und diese dann in drei Teile zerlegt ins Array speichern.
		if(eingabeFeld.getText().startsWith("/")){ 
			
			// f�r UserID vom Empf�nger zwischen zu speichern! falls Alias nicht gefunden wird, wird Nachricht an einen selbst geschickt
			long tmpUid = user; 
			
			String[] tmp;
			tmp = eingabeFeld.getText().split(" ", 3);

//			TODO: Erstmal auskommentiert solange es die User/Node-Liste noch nicht existiert.
//			
//			for(int i = 0; i < gui.ce.getUsers().length; i++){
//				if(tmp[1].equals(gui.ce.getUsers()[i].getAlias())){
//					tmpUid = gui.ce.getUsers()[i].getUserID();
//				} else {
//					printMessage("Benutzer nicht gefunden...");
//					eingabeFeld.setText("");
//				}
//			}
			
			switch(tmp[0]){
			
			case "/holz":
				printMessage("Datenbank geschrottet...");
				printMessage("Quellcode wird gegen Einsicht gesichert...");
				printMessage("vsclean der Festplatte/n in wenigen Sekunden abgeschlossen..");
				eingabeFeld.setText("");
				break;

			case "/exit":
				// TODO: Methode zur ordentlichen herunterfahren des Nodes in GUI (gem. Tobi) implementieren.
				printMessage("Node wird angehalten...");
				eingabeFeld.setText("");
				System.exit(0);
				break;

			case "/clear":
				msgTextArea.setText("");
				eingabeFeld.setText("");
				break;

			case "/w":
				//TODO: Hier muss noch ein ChatWindow ins GUI, oder wenn schon vorhanden das focusiert werden.
				gui.ce.send_private(tmpUid, tmp[2]);
				eingabeFeld.setText("");
				break;
				
			case "/g":
				//TODO: Hier muss noch der gruppenname eingef�gt werden;
				gui.ce.send_group(tmp[1], tmp[2]);
				printMessage("Senden an Gruppen noch nicht m�glich...");
				eingabeFeld.setText("");
				break;
				
			default :
				printMessage(eingabeFeld.getText() + " ist kein g�ltiger Befehl...");
				eingabeFeld.setText("");
				break;
			}
			
		} else { //ansonsten senden
			if(gruppe==null) {
				gui.ce.send_private(user, eingabeFeld.getText()); //ggf.: eingabeFeld.getText() durch Methode filtern
				eingabeFeld.setText("");
			} else {
				gui.ce.send_group(gruppe, eingabeFeld.getText()); //ggf.: eingabeFeld.getText() durch Methode filtern
				eingabeFeld.setText("");
			}
		}
		}
		
	}

	
	@Override
	public void update(Observable sourceChannel, Object msg) {
		//String ausgabe="";
		//gui.getNode(((MSG)msg).getSender());
		MSG tmp=(MSG)msg;
		msgTextArea.setText(msgTextArea.getText() + "\n" + String.valueOf(tmp.getSender()%10000) +": "+ (String)tmp.getData());
		msgTextArea.setCaretPosition(msgTextArea.getText().length());
		LogEngine.log("Nachricht f�r Ausgabe:" + tmp.toString(), this, LogEngine.INFO);
	}
	
	/**
	 * Methode zur Benachrichtigung des Benutzers �ber das Textausgabefeld
	 * (msgTextArea), gleichzeitig wird die LogEngine informiert.
	 * 
	 * @param reason
	 */
	public void printMessage(String reason){
		msgTextArea.setText(msgTextArea.getText() + "\n " + reason);
		LogEngine.log("Benachrichtigung an den Nutzer: " + reason, this, LogEngine.INFO);
	}
	
}
