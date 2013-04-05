package org.publicmain.chatengine;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import org.publicmain.common.MSG;

public abstract class Kanal extends Observable {
	protected Object 	referenz;

	protected TreeSet<MSG> messages = new TreeSet<MSG>();

	public abstract boolean add(MSG nachricht);


	/**Gibt die letzten x Nachrichten aus einem Kanal als Set zur�ck
	 * @param count Die gew�nschte Anzahl von Nachrichten
	 * @return Set der x-Letzen Nachrichten
	 */
	public Set<MSG> getLastMSGs(int count) {
		if (messages.size() >= count) {
			Iterator<MSG> x = messages.descendingIterator();
			MSG last = messages.last();
			while (x.hasNext() && (count > 0)) {
				count--;
				last = ((MSG) x.next());
			}
			return messages.tailSet(last, true);
		}
		return messages;
	}
	
	/**
	 * L�scht alle Nachrichten aus dem Kanal.
	 */
	public void purgeMSGs(){
		messages.clear();
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((referenz == null) ? 0 : referenz.hashCode());
		return result;
	}
	

	public boolean equals(Object obj) {
		return referenz.equals(obj);
		
	}

	public Object getReferenz() {
		return referenz;
	}

	public boolean is(Object vergleich) {
		return (this.referenz.equals(vergleich));
	}
	
	
	
}
