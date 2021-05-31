package servent.message;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = 3586102505319194978L;

	public UpdateMessage(int senderPort, String senderIp, int receiverPort,  String receiverIp, String text) {
		super(MessageType.UPDATE, senderPort, senderIp, receiverPort, receiverIp, text);
	}
}
