package org.publicmain.common;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.publicmain.nodeengine.NodeEngine;




/**Diese Klasse repr�sentiert unser Datenpaket
 * ggf. von Serializable auf Externalized umstellen wenn Dateneingespart werden m�ssen da nicht alle Paktearten alle Felder ben�tigen
 * @author tkessels
 *
 */
public class MSG implements Serializable,Comparable<MSG>{
	private static Integer id_counter=0;
	private static final long serialVersionUID = -2010661171218754968L;

	//Typisierung
	private final NachrichtenTyp typ;
	private MSGCode code;
	//Quelle und Eindeutigkeit
	private final long sender;
	private long timestamp;
	private final int id;
	//Optionale Datenfelder f�r beispielsweise Empf�nger
	private long empf�nger;
	private String group;
	//Payload
	private Object data;

	private MSG(NachrichtenTyp typ) {
		synchronized (id_counter) {
			this.id=id_counter;
			MSG.id_counter++;
		}
		this.typ=typ;
		this.timestamp=System.currentTimeMillis();
		this.sender= NodeEngine.getNE().getMe().getNodeID();
	}

	public MSG(Object payload, MSGCode code){
		this(NachrichtenTyp.SYSTEM);
		this.code = code;
		this.data = payload;
	}

	public MSG(Node daNode){
		this(NachrichtenTyp.SYSTEM);
		this.code=MSGCode.NODE_UPDATE;
		this.data=daNode;
	}

	public MSG(String group,String text){
		this(NachrichtenTyp.GROUP);
		this.group=group.toLowerCase();
		this.data=text;
	}

	public MSG(long user, String text){
		this(NachrichtenTyp.PRIVATE);
		this.empf�nger=user;
		this.data=text;
	}


	public MSG(File datei, long nid) throws IOException {
		this(NachrichtenTyp.DATA);
		if(!datei.isFile())throw new IOException("Verzeichnisse werden nicht unterst�tzt.");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		GZIPOutputStream zip = new GZIPOutputStream(bout);
		BufferedOutputStream bos = new BufferedOutputStream(zip);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(datei));
		byte[] cup=new byte[64000];
		int leng=-1;
		while((leng=bis.read(cup))!=-1){
				bos.write(cup,0,leng);
		}
		bos.flush();
		zip.finish();
		bos.close();
		data=bout.toByteArray();
		System.out.println(((byte[])data).length);
		setEmpf�nger(nid);
		group=datei.getName();
	}
	
	public void save(File datei) throws IOException{
		if(typ==NachrichtenTyp.DATA&&data!=null&&data instanceof byte[]&&((byte[])data).length>0){
			System.out.println(((byte[])data).length);

			ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) data);
			GZIPInputStream zip = new GZIPInputStream(bais);
			BufferedInputStream bis = new BufferedInputStream(zip);
			
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(datei));
			byte[] cup=new byte[64000];
			int leng=-1;
			while((leng=bis.read(cup))!=-1){
				bos.write(cup,0,leng);
			}
			bos.flush();
			bos.close();
		}
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
	
	public void reStamp() {
		timestamp=System.currentTimeMillis();
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
	public void setEmpf�nger(long value) {
		empf�nger=value;
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

	@Override
	public int compareTo(MSG o) {
		if (this.getTimestamp() != o.getTimestamp())	return (this.getTimestamp() > o.getTimestamp()) ? 1 : -1;
			else if (this.getSender() != o.getSender())	return (this.getSender() > o.getSender()) ? 1 : -1;
			else if (this.getId() != o.getId())			return (this.getId() - o.getId());
			return 0;
		
	}
	
	
}
