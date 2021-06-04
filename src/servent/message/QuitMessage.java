package servent.message;

public class QuitMessage extends BasicMessage {

	/**
	 * 	Send this messge to every Node to update their SuccessorTable
	 */
	private static final long serialVersionUID = 1245393882568495655L;

	public QuitMessage(int senderPort, String senderIp, int receiverPort, String receiverIp,
			int chordId) {
		super(MessageType.QUIT, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chordId));
	}

}
