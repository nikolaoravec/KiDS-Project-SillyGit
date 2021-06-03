package servent.message;

import java.util.ArrayList;

import app.ServentInfo;

public class AddMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;

	private final String fileName;
	private final String content;
	private final String extension;
	private final String relativePath;
	private final boolean isDir;
	private final ArrayList<String> children;

	public AddMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, int hashFileName,
			String fileName, String content, String extension, String relativePath, boolean isDir,ArrayList<String> children) {
		
		super(MessageType.ADD, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(hashFileName));
		this.fileName = fileName;
		this.content = content;
		this.extension = extension;
		this.relativePath = relativePath;
		this.isDir = isDir;
		this.children = children;
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
	
	public String getRelativePath() {
		return relativePath;
	}
	
	
}
