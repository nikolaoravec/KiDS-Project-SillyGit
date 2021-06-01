package servent.message;

import java.io.File;
import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<Integer, File> values;
	
	public WelcomeMessage(int senderPort, String senderIp, int receiverPort,String receiverIp, Map<Integer, File> values) {
		super(MessageType.WELCOME, senderPort, senderIp,  receiverPort, receiverIp);
		
		this.values = values;
	}
	
	public Map<Integer, File> getValues() {
		return values;
	}
}
