package servent.message;

import app.ServentInfo;

public class ReleaseMutexMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;
	


	public ReleaseMutexMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, int chordId) {
		
		super(MessageType.RELEASE_MUTEX, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chordId));
	
	
	}
	
	
}
	
