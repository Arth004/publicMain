import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.util.Date;


public class MSG implements Serializable{
	private long empf�nger;
	private long sender;
	private long timestamp;
	private NachrichtenTyp typ;
	private byte code;
	private Object data;
	

	private static final long serialVersionUID = -2010661171218754968L;

	public long getEmpf�nger() {
		return empf�nger;
	}

	public void setEmpf�nger(long empf�nger) {
		this.empf�nger = empf�nger;
	}

	public long getSender() {
		return sender;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public NachrichtenTyp getTyp() {
		return typ;
	}

	public byte getCode() {
		return code;
	}

	public Object getData() {
		return data;
	}

	
	public MSG() {
		timestamp=System.currentTimeMillis();
		empf�nger=0;
		sender= NodeEngine.getNE().getME().getNodeID();
		sender=0;
		typ=NachrichtenTyp.SYSTEM;
		data="Echo Request";
		code=1;
	}
	

	public byte[] toByteArray() {
		byte[] buffer=new byte[1];
		try {
			ByteArrayOutputStream toBytes=new ByteArrayOutputStream();
			ObjectOutputStream toStream= new ObjectOutputStream(toBytes);
			
			toStream.writeObject(empf�nger);
			toStream.writeObject(sender);
			toStream.writeObject(timestamp);
			toStream.writeObject(typ);
			toStream.writeObject(code);
			toStream.writeObject(data);
			
			
			
			buffer=toBytes.toByteArray();
		} catch (IOException e) {
			System.err.println("Konnte Nachricht:" + this.toString() + "nicht in Bytes umwandeln"   );
		}
		return buffer;
	}
	
	public MSG(DatagramPacket x) throws ClassNotFoundException, IOException{
		this(x.getData());
	}
	
	public MSG(byte[] data) throws ClassNotFoundException, IOException{
			ByteArrayInputStream toBytes=new ByteArrayInputStream(data);
			ObjectInputStream toStream= new ObjectInputStream(toBytes);
			
			empf�nger= (long) toStream.readObject();
			sender = (long) toStream.readObject();
			timestamp = (long) toStream.readObject();
			typ= (NachrichtenTyp) toStream.readObject();
			code= (byte) toStream.readObject();
			data= (byte[]) toStream.readObject();
	}

	@Override
	public String toString() {
		return "Message [empf�nger=" + empf�nger + ", sender=" + sender
				+ ", timestamp=" + timestamp + ", typ=" + typ + ", code="
				+ code + ", data=" + data + "]";
	}

}
