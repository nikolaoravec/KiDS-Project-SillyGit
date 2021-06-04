package servent.handler;

import java.io.File;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.CommitMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReleaseMutexMessage;
import servent.message.util.MessageUtil;

public class CommitHandler implements MessageHandler {

	private Message clientMessage;

	public CommitHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.COMMIT) {
			CommitMessage commitMessage = (CommitMessage) clientMessage;

			try {
				int hash = commitMessage.getHashFileName();
				Integer chordId = Integer.parseInt(commitMessage.getMessageText());
				int version = commitMessage.getVersion();
				String messageText = "";


				if (AppConfig.chordState.isKeyMine(hash)) {
					if (AppConfig.chordState.getValueMap().containsKey(hash)) {
						File storage = new File(AppConfig.STORAGE_PATH + File.separator);
						File[] files = storage.listFiles();
						int max = 0;
						for (int i = 0; i < files.length; i++) {

							String absolutePath1 = AppConfig.fileConfig
									.getFileNameWithoutVersion(files[i].getAbsolutePath());
							String absolutePath2 = storage.getAbsolutePath() + "\\";
							String relative = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);

							if (!relative.equals("")) {

								if (ChordState.chordHash(relative) == hash) {

									int versionOfFile = AppConfig.fileConfig.getFileVersion(files[i]);

									if (versionOfFile > max) {
										max = versionOfFile;

									}
								}
							}
						}

						if (max > version) {
							// logika za konflikt

						}
						File oldFile = new File(AppConfig.STORAGE_PATH + File.separator + commitMessage.getFileName()
								+ "_" + max + commitMessage.getExtension());

						String oldContent = AppConfig.fileConfig.getFileContent(oldFile);

						if (!oldContent.trim().equals(commitMessage.getContent().trim())) {
							int newVersion = max + 1;
							File commitFile = new File(AppConfig.STORAGE_PATH + File.separator
									+ commitMessage.getFileName() + "_" + newVersion + commitMessage.getExtension());
							AppConfig.fileConfig.setFileContent(commitFile, commitMessage.getContent());
							AppConfig.chordState.getFileVersions().put(commitFile.getName(), newVersion);

						}

						messageText = "Fajl je commitovan";
					} else {
						messageText = "Fajl ne postoji ili je izbrisan";
						
					}
					
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
					ReleaseMutexMessage releaseMutexMessage = new ReleaseMutexMessage(
							AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
							nextNode.getListenerPort(), nextNode.getIpAddress(), chordId);

					MessageUtil.sendMessage(releaseMutexMessage);
				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(hash);
					CommitMessage pm = new CommitMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							hash, commitMessage.getFileName(), commitMessage.getContent(), commitMessage.getExtension(),
							commitMessage.getVersion(), commitMessage.isDir(), commitMessage.getChildren(), chordId);
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
