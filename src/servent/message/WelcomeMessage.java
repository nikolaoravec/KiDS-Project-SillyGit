package servent.message;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<Integer, Integer> values;
	
	public WelcomeMessage(int senderPort, String senderIp, int receiverPort,String receiverIp, Map<Integer, Integer> values) {
		super(MessageType.WELCOME, senderPort, senderIp,  receiverPort, receiverIp);
		
		this.values = values;
	}
	
	public Map<Integer, Integer> getValues() {
		return values;
	}
}
