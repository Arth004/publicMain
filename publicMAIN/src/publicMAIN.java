import java.io.IOException;

import javax.swing.JOptionPane;

import org.publicmain.common.Config;
import org.publicmain.common.LogEngine;
import org.publicmain.gui.GUI;
import org.publicmain.gui.startWindow;
import org.publicmain.sql.LocalDBConnection;

/**
 * @author ATRM
 * 
 */

public class publicMAIN {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(Config.getConfig().getLock()){
			if(Config.getConfig().getAlias() == null){
				
				startWindow sw = startWindow.getStartWindow();
				while(!sw.isGoPushed()){	//TODO: Diese Variante zu pr�fen ob startWindow "fertig" ist gef�llt mir (noch) nicht -> aber gerade keine andere idee ist ja schon sp�t :-)
					try {
						Thread.sleep((long)1000);
					} catch (InterruptedException e) {
						LogEngine.log("Fehler im warteThreat: " + e.getMessage(), LogEngine.ERROR);
					}
				}
				//TODO: hier darauf warten bis nutzer daten eingegeben und "submit" gedr�ckt hat. also zum Beispiel ein boolean im Startwindwo �berpr�fen
				//TODO: Will werte bei Submit-Click im Startwindow in config speichern...wie? ;-)
				
				//TODO: Warum l�uft das prog nicht direkt weiter wenn der DB-Server aus ist? Selbst wenn dieser Aufruf hier in nem extra Threat ist geht�s nicht.
				//Du hattest das ja nie in einen Thread gepackt sonder lediglich in ein Runnable und dann die run Methode selbst ausgef�hrt anstatt DAS von einem Thread machen zu lassen
				//habe das im DB Construktor korrigiert ... den Rest schau ich mir sp�ter an viel Spa� beim planschen  ;)
				LocalDBConnection.getDBConnection(); 
				
				

				GUI.getGUI();
			}else {
				LocalDBConnection.getDBConnection(); //TODO: Warum l�uft das prog nicht direkt weiter wenn der DB-Server aus ist? Selbst wenn dieser Aufruf hier in nem extra Threat ist geht�s nicht.
				GUI.getGUI();
			}
			//Mir gef�lltnicht das hier die Controlle weggegeben wird. Es sollte ein strukturierte und Kontrollierter Start formuliert werden der die Module in der richtigen Reihenfolge startet
			
		}
		else{
			JOptionPane.showMessageDialog(null, "publicMAIN konnte nicht gestartet werden weil bereits eine Instanz der Software l�uft");
			LogEngine.log("publicMAIN konnte nicht gestartet werden weil bereits eine Instanz der Software l�uft", LogEngine.ERROR);
		}
		
	}
}
