package org.publicmain.common;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.publicmain.nodeengine.NodeEngine;




/**Diese Klasse repr�sentiert unser Datenpaket
 * ggf. von Serializable auf Externalized umstellen wenn Dateneingespart werden m�ssen da nicht alle Paktearten alle Felder ben�tigen
 * @author tkessels
 *
 */
public class MSG implements Serializable{
	private static Integer id_counter=0;
	private static final long serialVersionUID = -2010661171218754968L;

	//Typisierung
	private NachrichtenTyp typ;
	private MSGCode code;
	//Quelle und Eindeutigkeit
	private long sender;
	private long timestamp;
	private int id;
	//Empf�nger
	private long empf�nger;
	private String group;
	//Payload
	private Object data;

	private MSG() {
		synchronized (id_counter) {
			this.id=id_counter;
			MSG.id_counter++;
		}
		this.timestamp=System.currentTimeMillis();
		this.sender= NodeEngine.getNE().getME().getNodeID();
	}

	public MSG(Object payload, MSGCode code){
		this();
		this.typ=NachrichtenTyp.SYSTEM;
		this.code = code;
		this.data = payload;
	}

	public MSG(Node daNode){
		this();
		this.typ=NachrichtenTyp.SYSTEM;
		this.code=MSGCode.NODE_UPDATE;
		this.data=daNode;
	}

	public MSG(String group,String text){
		this();
		this.typ=NachrichtenTyp.GROUP;
		this.group=group.toLowerCase();
		this.data=text;
	}

	public MSG(long user, String text){
		this();
		this.typ=NachrichtenTyp.PRIVATE;
		this.empf�nger=user;
		this.data=text;
	}


	public long getEmpf�nger() {
		return empf�nger;
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

	public MSGCode getCode() {
		return code;
	}

	public Object getData() {
		return data;
	}
	
	public String getGroup() {
		return group;
	}

	public int getId() {
		return id;
	}


	
	
	@Override
	public String toString() {
		return "MSG [" + (typ != null ? "typ=" + typ + ", " : "")
				+ (code != null ? "code=" + code + ", " : "") + "sender="
				+ sender + ", timestamp=" + timestamp + ", id=" + id
				+ ", empf�nger=" + empf�nger + ", "
				+ (group != null ? "group=" + group + ", " : "")
				+ (data != null ? "data=" + data : "") + "]";
	}

	public static byte[] getBytes(MSG x){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream obout = new ObjectOutputStream(bos);
			obout.writeObject(x);
			obout.flush();
		} catch (IOException e) {
			LogEngine.log(e);
		}
		return bos.toByteArray();
	}
	
	public static MSG getMSG(byte[] data){
		try {
			ObjectInputStream obin=new ObjectInputStream( new ByteArrayInputStream(data));
			MSG tmp = (MSG)obin.readObject();
			return tmp;
			
		} catch (Exception e) {
			LogEngine.log(e);
		}
		return null;
	}
	
	public static MSG createReply(MSG x) {
		if (x!=null&&x.typ==NachrichtenTyp.SYSTEM) {
			MSG reply=new MSG();
			reply.typ=NachrichtenTyp.SYSTEM;
			reply.empf�nger=x.sender;
			
			switch(x.code) {
				case ECHO_REQUEST:
					reply.code=MSGCode.ECHO_RESPONSE;
					reply.data=x.getTimestamp();
					break;
				case NODE_LOOKUP:
					reply.code=MSGCode.NODE_LOOKUP_REPLY;
					reply.data=NodeEngine.getNE().getME();
					break;
			}
			return reply;
		}
		throw new IllegalArgumentException();
	}
}
