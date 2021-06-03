package servent.message;

import java.util.ArrayList;

public class CommitMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;

	private final String fileName, content, extension;
	private final int hashFileName, version;
	private final boolean isDir;
	private final ArrayList<String> children;

	public CommitMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, int hashFileName,
			String fileName, String content, String extension, int version, boolean isDir, ArrayList<String> children,
			int chordId) {

		super(MessageType.COMMIT, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chordId));
		this.fileName = fileName;
		this.content = content;
		this.extension = extension;
		this.isDir = isDir;
		this.children = children;
		this.hashFileName = hashFileName;
		this.version = version;
	}

	public int getVersion() {
		return version;
	}

	public int getHashFileName() {
		return hashFileName;
	}

	public ArrayList<String> getChildren() {
		return children;
	}

	public boolean isDir() {
		return isDir;
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
