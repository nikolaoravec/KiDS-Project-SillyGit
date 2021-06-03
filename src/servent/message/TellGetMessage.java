package servent.message;

import java.io.File;
import java.util.List;

import app.ServentInfo;

public class TellGetMessage extends BasicMessage {

	private static final long serialVersionUID = -6213394344524749872L;
	// private final List<File> files;
	private final File file;
	private final String fileName;
	private final String content;
	private final String relativePath;

	public TellGetMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, File file,
			String fileName, String content, String relativePath) {
		super(MessageType.TELL_GET, senderPort, senderIp, receiverPort, receiverIp, "");
		this.fileName = fileName;
		this.content = content;
		this.relativePath = relativePath;
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public String getContent() {
		return content;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public File getFile() {
		return file;
	}

}
