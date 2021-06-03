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
				String[] messageText = clientMessage.getMessageText().split(",");

				int hash = Integer.parseInt(messageText[0]);
				int version = Integer.parseInt(messageText[1]);
				Map<Integer, File> valueMap = AppConfig.chordState.getValueMap();
				String relativeGLobal = "";

				if (AppConfig.chordState.isKeyMine(hash)) {
					File toReturn = null;
					if (valueMap.containsKey(hash)) {
						File toFind = valueMap.get(hash);
						File storage = new File(AppConfig.STORAGE_PATH);
						File[] files = storage.listFiles();
						System.out.println(files.toString());
						int max = -1;
						for (int i = 0; i < files.length; i++) {

							// String fileName = getFileName(files[i]);
							String absolutePath1 = getFileNameWithoutVersion(files[i]);

							// System.out.println("abs putanja ya fajl u storidxzu " + absolutePath1);
							String absolutePath2 = storage.getAbsolutePath() + "\\";

							if (absolutePath2.length() < absolutePath1.length()) {
								String relative = absolutePath1.substring(absolutePath2.length());

								// System.out.println("relativna putanja ya fajl u storidxzu " + relative);

								if (ChordState.chordHash(relative) == hash) {

									if (!files[i].isDirectory()) {
										if (version == -1) {
											int versionOfFile = getFileVersion(files[i]);
											System.out.println("verzija fajla " + versionOfFile);
											if (versionOfFile > max) {
												max = versionOfFile;
												toReturn = files[i];
												relativeGLobal = relative;
											}
										} else {
											int versionOfFile = getFileVersion(files[i]);
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

					} else {

					}
					if (toReturn != null) {
						TellGetMessage tellGetMessage = new TellGetMessage(AppConfig.myServentInfo.getListenerPort(),
								AppConfig.myServentInfo.getIpAddress(), askGetMessage.getSenderPort(),
								askGetMessage.getSenderIp(), toReturn, toReturn.getName(),
								getFileContent(toReturn), relativeGLobal);

						MessageUtil.sendMessage(tellGetMessage);
					} else {
						System.out.println("Fajl nije pronadjen");
					}
				} else {

					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(hash);

					AskGetMessage agm = new AskGetMessage(askGetMessage.getSenderPort(), askGetMessage.getSenderIp(),
							nextNode.getListenerPort(), nextNode.getIpAddress(), String.valueOf(hash + "," + version));
					MessageUtil.sendMessage(agm);

				}

			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Got ask get with bad text: " + clientMessage.getMessageText());
			}

		} else {
			AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
		}

	}

	private String getFileNameWithoutVersion(File file) {

		String name = file.getAbsolutePath();
		int lastIndexOfUderscore = name.lastIndexOf("_");
		if (lastIndexOfUderscore == -1) {
			return name.substring(0, name.length());
		}
		String ret = name.substring(0, lastIndexOfUderscore);

		return name.substring(0, lastIndexOfUderscore);
	}

	private Integer getFileVersion(File file) {
		String name = file.getAbsolutePath();
		int lastIndexOf = name.lastIndexOf(".");
		int lastIndexOfUderscore = name.lastIndexOf("_");
		if (lastIndexOf == -1) {
			return Integer.parseInt(name.substring(lastIndexOfUderscore + 1, name.length()));

		}
		if (lastIndexOfUderscore == -1) {
			return -1;
		}
		return Integer.parseInt(name.substring(lastIndexOfUderscore + 1, lastIndexOf));
	}

	public String getFileContent(File file) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			return stringBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}