package servent.message;

public class NewNodeMessage extends BasicMessage {

	private static final long serialVersionUID = 3899837286642127636L;

	public NewNodeMessage(int senderPort, String senderIp, int receiverPort, String receiverIp) {
		super(MessageType.NEW_NODE, senderPort, senderIp, receiverPort, receiverIp);
	}
}
