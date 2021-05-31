package servent.message;

public class SorryMessage extends BasicMessage {

	private static final long serialVersionUID = 8866336621366084210L;

	public SorryMessage(int senderPort, String senderIp, int receiverPort, String receiverIp) {
		super(MessageType.SORRY,  senderPort, senderIp, receiverPort, receiverIp);
	}
}
