package servent.message;

public class AskPullMessage extends BasicMessage {

	private static final long serialVersionUID = -8558031124520315033L;

	public AskPullMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, String text) {
		super(MessageType.ASK_GET, senderPort, senderIp, receiverPort, receiverIp, text);
	}
}
