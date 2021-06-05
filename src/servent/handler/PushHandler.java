package servent.handler;

import java.io.File;

import app.AppConfig;
import app.ServentInfo;
import servent.message.CommitMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PushMessage;
import servent.message.ReleaseMutexMessage;
import servent.message.util.MessageUtil;

public class PushHandler implements MessageHandler {

	private Message clientMessage;

	public PushHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.PUSH) {
			PushMessage pushMessage = (PushMessage) clientMessage;

			String fileName = pushMessage.getFileName();
			String extension = pushMessage.getExtension();
			String content = pushMessage.getContent();
			Integer chordId = Integer.parseInt(pushMessage.getMessageText());
			int hash = pushMessage.getHashFileName();
			int version = pushMessage.getVersion();

			if (AppConfig.myServentInfo.getChordId() == chordId) {
				System.err.println("No file with that name in conflict!");
				AppConfig.releaseBothMutex();

			} else {
				if (AppConfig.chordState.isKeyMine(hash)) {

					int newVersion = version + 1;
					File commitFile = new File(
							AppConfig.STORAGE_PATH + File.separator + fileName + "_" + newVersion + extension);

					AppConfig.fileConfig.setFileContent(commitFile, content);
					AppConfig.chordState.getValueMap().get(hash).add(commitFile);

					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
					ReleaseMutexMessage releaseMutexMessage = new ReleaseMutexMessage(
							AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
							nextNode.getListenerPort(), nextNode.getIpAddress(), chordId, "Push command succesfull!",
							fileName, newVersion);

					MessageUtil.sendMessage(releaseMutexMessage);

				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(hash);
					PushMessage pm = new PushMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							hash, fileName, content, extension, version, false, null,
							chordId);
					MessageUtil.sendMessage(pm);
				}
			}
		}
	}

}
