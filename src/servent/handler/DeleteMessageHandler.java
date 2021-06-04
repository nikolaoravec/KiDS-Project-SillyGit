package servent.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.AddMessage;
import servent.message.DeleteMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReleaseMutexMessage;
import servent.message.util.MessageUtil;

public class DeleteMessageHandler implements MessageHandler {

	private Message clientMessage;

	public DeleteMessageHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.DELETE) {
			DeleteMessage addMessage = (DeleteMessage) clientMessage;

			try {
				int hash = addMessage.getHashFileName();
				Integer chordId = Integer.parseInt(addMessage.getMessageText());

				if (AppConfig.chordState.isKeyMine(hash)) {

					File storage = new File(AppConfig.STORAGE_PATH + File.separator);
					File[] files = storage.listFiles();

					for (int i = 0; i < files.length; i++) {

						String absolutePath1 = AppConfig.fileConfig.getFileNameWithoutVersion(files[i].getAbsolutePath());
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
							 nextNode.getListenerPort(), nextNode.getIpAddress(), chordId);

					MessageUtil.sendMessage(releaseMutexMessage);

				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(hash);
					DeleteMessage dm = new DeleteMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							hash, chordId);
					MessageUtil.sendMessage(dm);
			
				}
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Got put message with bad text: " + clientMessage.getMessageText());
			}

		}else

	{
		AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
	}

}

}
