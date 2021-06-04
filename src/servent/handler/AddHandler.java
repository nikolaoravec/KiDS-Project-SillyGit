package servent.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AddMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReleaseMutexMessage;
import servent.message.util.MessageUtil;

public class AddHandler implements MessageHandler {

	private Message clientMessage;

	public AddHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ADD) {
			AddMessage addMessage = (AddMessage) clientMessage;

			try {
				int hash = addMessage.getHashFileName();
				Integer chordId = Integer.parseInt(addMessage.getMessageText());

				if (AppConfig.chordState.isKeyMine(hash)) {

					String storage = AppConfig.STORAGE_PATH + File.separator;
					if (!addMessage.isDir()) {

						String relativePath = addMessage.getRelativePath().replace("\\", "/");
						String[] splitPath = relativePath.split("/");

						if (splitPath.length > 1) {
							for (int i = 0; i < splitPath.length - 1; i++) {
								File dir = new File(storage + splitPath[i]);
								if (!dir.exists()) {
									dir.mkdir();
								}
								storage += splitPath[i] + File.separator;
							}
						}
						File newFile = new File(storage + addMessage.getFileName() + "_0" + addMessage.getExtension());

						try {
							newFile.createNewFile();
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						AppConfig.fileConfig.setFileContent(newFile, addMessage.getContent());

						ArrayList<File> list = new ArrayList<File>();
						list.add(newFile);
						AppConfig.chordState.getValueMap().put(hash, list);

					} else {

						String relativePath = addMessage.getRelativePath().replace("\\", "/");
						String[] splitPath = relativePath.split("/");

						File newFile = new File(storage + splitPath[splitPath.length - 1]);

						if (!newFile.exists()) {
							newFile.mkdir();
						}

						ArrayList<File> list = new ArrayList<File>();
						list.add(newFile);
						AppConfig.chordState.getValueMap().put(hash, list);

						// ArrayList<Integer> childrenHash = new ArrayList<>();
						//
						// for (String child : children) {
						// childrenHash.add(chordHash(child));
						// }
						//
						// childrenHashes.put(hashFileName, childrenHash);
					}
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
					ReleaseMutexMessage releaseMutexMessage = new ReleaseMutexMessage(
							AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
							nextNode.getListenerPort(), nextNode.getIpAddress(), chordId, "Add seccesfull!");

					MessageUtil.sendMessage(releaseMutexMessage);

				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(hash);
					AddMessage pm = new AddMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							hash, addMessage.getFileName(), addMessage.getContent(), addMessage.getExtension(),
							addMessage.getRelativePath(), addMessage.isDir(), addMessage.getChildren(), chordId);
					MessageUtil.sendMessage(pm);
				}
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Got put message with bad text: " + clientMessage.getMessageText());
			}

		} else {
			AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
		}

	}

}
