package servent.handler;

import java.io.File;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.AskPullMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReleaseMutexMessage;
import servent.message.TellPullMessage;
import servent.message.util.MessageUtil;

public class AskPullHandler implements MessageHandler {

	private Message clientMessage;

	public AskPullHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ASK_GET) {

			System.out.println("usao sam u get");
			try {
				AskPullMessage askGetMessage = (AskPullMessage) clientMessage;
				String[] messageText = askGetMessage.getMessageText().split(",");
				String messageTextReturn = "";

				int hash = Integer.parseInt(messageText[0]);
				int version = Integer.parseInt(messageText[1]);
				int chordId = Integer.parseInt(messageText[2]);

				String relativeGLobal = "";
				// System.out.println("da li je moj kljuc " +
				// AppConfig.chordState.isKeyMine(hash));
				if (AppConfig.chordState.isKeyMine(hash)) {
					if (AppConfig.chordState.getValueMap().containsKey(hash)) {
						File toReturn = null;

						File storage = new File(AppConfig.STORAGE_PATH);
						File[] files = storage.listFiles();
						int max = -1;
						for (int i = 0; i < files.length; i++) {

							String absolutePath1 = AppConfig.fileConfig
									.getFileNameWithoutVersion(files[i].getAbsolutePath());
							String absolutePath2 = storage.getAbsolutePath() + "\\";
							String relative = AppConfig.fileConfig.getRelativePath(absolutePath1, absolutePath2);

							if (!relative.equals("")) {

								if (ChordState.chordHash(relative) == hash) {

									if (!files[i].isDirectory()) {
										if (version == -1) {
											int versionOfFile = AppConfig.fileConfig.getFileVersion(files[i]);

											if (versionOfFile > max) {
												max = versionOfFile;
												toReturn = files[i];
												relativeGLobal = relative;
											}
										} else {
											int versionOfFile = AppConfig.fileConfig.getFileVersion(files[i]);
											if (versionOfFile == version) {
												toReturn = files[i];
												relativeGLobal = relative;
											}
										}
									} else {
										// ako pulujemo direktorijum
									}

								}
							}
						}

						if (toReturn != null) {
							int versionOfFile = AppConfig.fileConfig.getFileVersion(toReturn);
							ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
							TellPullMessage tellGetMessage = new TellPullMessage(
									AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
									nextNode.getListenerPort(), nextNode.getIpAddress(), toReturn,
									AppConfig.fileConfig.getFileNameWithoutVersion(toReturn.getName()),
									AppConfig.fileConfig.getFileExtension(toReturn),
									AppConfig.fileConfig.getFileContent(toReturn), relativeGLobal, chordId,
									versionOfFile);

							MessageUtil.sendMessage(tellGetMessage);
						} else {
							messageTextReturn = "Fajl ne postoji ili je izbrisan";
							ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
							ReleaseMutexMessage releaseMutexMessage = new ReleaseMutexMessage(
									AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
									nextNode.getListenerPort(), nextNode.getIpAddress(), chordId, messageTextReturn);

							MessageUtil.sendMessage(releaseMutexMessage);
						}
					} else {
						ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
						ReleaseMutexMessage releaseMutexMessage = new ReleaseMutexMessage(
								AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
								nextNode.getListenerPort(), nextNode.getIpAddress(), chordId,
								"File not found to pull!");

						MessageUtil.sendMessage(releaseMutexMessage);
					}
				} else {

					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(hash);

					AskPullMessage agm = new AskPullMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							String.valueOf(hash + "," + version + "," + chordId));
					MessageUtil.sendMessage(agm);

				}

			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Got ask get with bad text: " + clientMessage.getMessageText());
			}

		} else {
			AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
		}

	}

}