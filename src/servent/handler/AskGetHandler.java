package servent.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.AskGetMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellGetMessage;
import servent.message.util.MessageUtil;

public class AskGetHandler implements MessageHandler {

	private Message clientMessage;

	public AskGetHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ASK_GET) {
			System.out.println("usao sam u get");
			try {
				AskGetMessage askGetMessage = (AskGetMessage) clientMessage;
				String[] messageText = askGetMessage.getMessageText().split(",");

				int hash = Integer.parseInt(messageText[0]);
				int version = Integer.parseInt(messageText[1]);
				int chordId = Integer.parseInt(messageText[2]);

				String relativeGLobal = "";

				if (AppConfig.chordState.isKeyMine(hash)) {
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
										System.out.println("verzija fajla " + versionOfFile);
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
					System.out.println(toReturn.getAbsolutePath());

					if (toReturn != null) {
						int versionOfFile = AppConfig.fileConfig.getFileVersion(toReturn);
						ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
						TellGetMessage tellGetMessage = new TellGetMessage(AppConfig.myServentInfo.getListenerPort(),
								AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(),
								nextNode.getIpAddress(), toReturn,
								AppConfig.fileConfig.getFileNameWithoutVersion(toReturn.getName()),
								AppConfig.fileConfig.getFileContent(toReturn), relativeGLobal, chordId, versionOfFile);

						MessageUtil.sendMessage(tellGetMessage);
					} else {
						System.out.println("Fajl nije pronadjen");
					}
				} else {

					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(hash);

					AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(),
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