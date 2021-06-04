package servent.message;

public class GiveMyFilesAcceptedMessage extends BasicMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3340319887475550968L;

	public GiveMyFilesAcceptedMessage(int senderPort, String senderIp, int receiverPort, String receiverIp,
			int chordId) {
		super(MessageType.GIVE_ACCEPTED, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chordId));

	}

}
