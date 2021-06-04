package servent.handler;

import java.io.File;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReleaseMutexMessage;
import servent.message.RemoveMessage;
import servent.message.util.MessageUtil;

public class RemoveMessageHandler implements MessageHandler {

	private Message clientMessage;

	public RemoveMessageHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.REMOVE) {
			RemoveMessage removeMessage = (RemoveMessage) clientMessage;

			try {
				int hash = removeMessage.getHashFileName();
				Integer chordId = Integer.parseInt(removeMessage.getMessageText());

				if (AppConfig.chordState.isKeyMine(hash)) {

					File storage = new File(AppConfig.STORAGE_PATH + File.separator);
					File[] files = storage.listFiles();

					for (int i = 0; i < files.length; i++) {

						String absolutePath1 = AppConfig.fileConfig
								.getFileNameWithoutVersion(files[i].getAbsolutePath());
						String absolutePath2 = storage.getAbsolutePath() + "\\";
						String relative = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);

						if (!relative.equals("")) {
							if (ChordState.chordHash(relative) == hash) {
								boolean ret = files[i].delete();
							}
						}
					}

					AppConfig.chordState.getValueMap().remove(hash);
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
					ReleaseMutexMessage releaseMutexMessage = new ReleaseMutexMessage(
							AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
							nextNode.getListenerPort(), nextNode.getIpAddress(), chordId, "Delete succesfull!");

					MessageUtil.sendMessage(releaseMutexMessage);

				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(hash);
					RemoveMessage dm = new RemoveMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							hash, chordId);
					MessageUtil.sendMessage(dm);

				}
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Got put message with bad text: " + clientMessage.getMessageText());
			}

		} else

		{
			AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
		}

	}

}
