package servent.message;

public class ConflictMessage extends BasicMessage {

	private static final long serialVersionUID = -8558031124520315033L;
	private final int hash, version;
	private String content, fileName, extension;
	private boolean conflict;

	public ConflictMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, int chordId, int hash,
			String content, String extension, String fileName, int version, boolean conflict) {
		super(MessageType.CONFLICT, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chordId));
		this.hash = hash;
		this.content = content;
		this.fileName = fileName;
		this.version = version;
		this.conflict = conflict;
		this.extension = extension;
	}
	
	public String getExtension() {
		return extension;
	}
	
	public boolean isConflict() {
		return conflict;
	}
	
	public int getHash() {
		return hash;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getVersion() {
		return version;
	}
	
	


	
}
