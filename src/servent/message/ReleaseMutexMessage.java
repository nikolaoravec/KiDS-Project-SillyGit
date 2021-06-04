package servent.message;

public class ReleaseMutexMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;
	private String messageTextReturn;
	


	public ReleaseMutexMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, int chordId, String messageTextReturn) {
		
		super(MessageType.RELEASE_MUTEX, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chordId));
		
		this.messageTextReturn = messageTextReturn;
	
	}
	
	public String getMessageTextReturn() {
		return messageTextReturn;
	}
	
	
}
	
