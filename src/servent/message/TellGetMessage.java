package servent.message;

import java.io.File;
import java.util.List;

public class TellGetMessage extends BasicMessage {

	private static final long serialVersionUID = -6213394344524749872L;
	private final List<File> files;

	public TellGetMessage(int senderPort, String senderIp, int receiverPort,  String receiverIp, List<File> files) {
		super(MessageType.TELL_GET, senderPort, senderIp, receiverPort, receiverIp, "");
		this.files = files;
	}
	
	public List<File> getFiles() {
		return files;
	}
}
