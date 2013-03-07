
public class KnotenKanal extends Kanal{
	
	public KnotenKanal(long node) {
		this.referenz=node;
	}

	@Override
	public boolean add(MSG nachricht) {
		if((nachricht.getSender()==(long)referenz)||nachricht.getEmpfänger()==(long)referenz){
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}

	@Override
	public boolean is(Object vergleich) {
		return (referenz==vergleich);
	}

}
