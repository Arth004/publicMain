
public class KnotenKanal extends Kanal{
	
	
	public KnotenKanal(long node) {
		super(node);
	}

	@Override
	public boolean check(MSG nachricht) {
		if((nachricht.getSender()==(long)identifier)||nachricht.getEmpf�nger()==(long)identifier){
			setChanged();
			notifyObservers(nachricht);
			return true;
		}
		return false;
	}

}
