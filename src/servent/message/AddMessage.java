package servent.message;

public class AddMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;

	private final String fileName;
	private final String content;
	private final String extension;

	public AddMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, int hashFileName,
			String fileName, String content, String extension) {
		super(MessageType.ADD, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(hashFileName));
		this.fileName = fileName;
		this.content = content;
		this.extension = extension;
	}

	public String getFileName() {
		return fileName;
	}

	public String getContent() {
		return content;
	}

	public String getExtension() {
		return extension;
	}
	
	
}
