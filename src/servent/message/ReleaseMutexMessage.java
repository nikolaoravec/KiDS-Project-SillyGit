package servent.message;

public class ReleaseMutexMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;
	private String messageTextReturn;
	private String fileName;
	private int newVersion;

	public ReleaseMutexMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, int chordId,
			String messageTextReturn) {

		super(MessageType.RELEASE_MUTEX, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chordId));

		this.messageTextReturn = messageTextReturn;
		this.fileName = "";
		this.newVersion = -1;

	}

	public ReleaseMutexMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, int chordId,
			String messageTextReturn, String fileName, int newVersion) {
		super(MessageType.RELEASE_MUTEX, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chordId));

		this.messageTextReturn = messageTextReturn;
		this.fileName = fileName;
		this.newVersion = newVersion;
	}

	public String getMessageTextReturn() {
		return messageTextReturn;
	}

	public String getFileName() {
		return fileName;
	}

	public int getNewVersion() {
		return newVersion;
	}

}
