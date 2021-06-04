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
	private final String relativePath, extension;
	private final int versionOfFIle;

	public TellGetMessage(int senderPort, String senderIp, int receiverPort, String receiverIp, File file,
			String fileName, String extension, String content, String relativePath, int chordId, int versionOfFIle) {
		super(MessageType.TELL_GET, senderPort, senderIp, receiverPort, receiverIp, String.valueOf(chordId));
		this.fileName = fileName;
		this.extension = extension;
		this.content = content;
		this.relativePath = relativePath;
		this.file = file;
		this.versionOfFIle = versionOfFIle;
	}
	
	public String getExtension() {
		return extension;
	}
	
	public int getVersionOfFIle() {
		return versionOfFIle;
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
