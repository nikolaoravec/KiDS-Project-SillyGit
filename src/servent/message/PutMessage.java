package servent.message;

public class PutMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;

	public PutMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, int key, int value) {
		super(MessageType.PUT, senderPort, senderIp, receiverPort, receiverIp, key + ":" + value);
	}
}
