package servent.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.ServentInfo;
import servent.message.GiveMyFilesAcceptedMessage;
import servent.message.GiveMyFilesMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class GiveMyFilesHandler implements MessageHandler {

	private Message clientMessage;
	private Map<Integer, ArrayList<File>> mapValueToAdd;
	private int chordId;

	public GiveMyFilesHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.GIVEMYFILES) {

			GiveMyFilesMessage giveMyFilesMessage = (GiveMyFilesMessage) clientMessage;

			mapValueToAdd = new HashMap<>(giveMyFilesMessage.getValueMap());

			String storage = AppConfig.STORAGE_PATH + File.separator;

			for (Entry<Integer, ArrayList<File>> valueEntry : mapValueToAdd.entrySet()) {
				ArrayList<File> file = valueEntry.getValue();
				ArrayList<File> filesToAdd = new ArrayList<File>();
				for (File f : file) {

					String relativePath = f.getAbsolutePath().replace("\\", "/");
					String[] path = relativePath.split("/");
					String relative = path[path.length - 1];

					File newFile = new File(storage + relative);

					try {
						newFile.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					String content = AppConfig.fileConfig.getFileContent(f);
					AppConfig.fileConfig.setFileContent(newFile, content);
					filesToAdd.add(newFile);
				}
				AppConfig.chordState.getValueMap().put(valueEntry.getKey(), filesToAdd);

			}

			chordId = Integer.parseInt(giveMyFilesMessage.getMessageText());

			ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
			GiveMyFilesAcceptedMessage giveMyFilesAcceptedMessage = new GiveMyFilesAcceptedMessage(
					AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(),
					nextNode.getListenerPort(), nextNode.getIpAddress(), chordId);
			MessageUtil.sendMessage(giveMyFilesAcceptedMessage);

		}

	}

}
