package servent.message;

public class DeleteMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;
	private final Integer hashFileName;
	

	public DeleteMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, int hashFileName,int chordId) {
		
		super(MessageType.DELETE, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chordId));
		this.hashFileName = hashFileName;
	}
	
	
	public Integer getHashFileName() {
		return hashFileName;
	}
	
}
