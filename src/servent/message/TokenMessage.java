package servent.message;

public class TokenMessage extends BasicMessage {

	private static final long serialVersionUID = 2084490973699262440L;

	public TokenMessage(int senderPort, String senderIp, int receiverPort, String receiverIp) {
		super(MessageType.TOKEN, senderPort, senderIp, receiverPort, receiverIp);
	}
}
