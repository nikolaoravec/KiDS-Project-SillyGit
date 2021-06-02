package servent.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import app.AppConfig;
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
			try {
				String[]  messageText = clientMessage.getMessageText().split(",");
				
				int hash = Integer.parseInt(messageText[0]);
				int version = Integer.parseInt(messageText[1]);
				
				if (AppConfig.chordState.isKeyMine(hash)) {
					List<File> retList = new ArrayList<>();
					Map<Integer, File> valueMap = AppConfig.chordState.getValueMap(); 
					int value = -1;
					
					if (valueMap.containsKey(hash)) {
						File storage = new File(AppConfig.STORAGE_PATH);
						File[] files = storage.listFiles();
						int max = 0;
						for (int i = 0; i < files.length; i++) {
							String fileName = getFileName(files[i]);
							
							if(files[i].isDirectory() && (AppConfig.chordState.chordHash(files[i].getName()) == hash)) {
								retList = (List<File>) Arrays.asList(files[i].listFiles()).stream().filter(e -> !e.isDirectory());
								
							}
//							if (chordHash(fileName) == hashFileName) {
//								int tmpVersion = getFileVersion(files[i]);
//								if (version == -1) {
//									if (tmpVersion > max) {
//										max = tmpVersion;
//										toReturn = files[i];
//									}
//								} else {
//									if (version == tmpVersion)
//										toReturn = files[i];
//								}
//							}
					}
					}
					// vraticemo vrvt neki nas objekat koji ce opisivati fajl ili folder
					TellGetMessage tgm = new TellGetMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), 
							clientMessage.getSenderPort(),
							clientMessage.getSenderIp(),
							retList);
					MessageUtil.sendMessage(tgm);
				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(hash);
					AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							String.valueOf(hash+","+version));
					MessageUtil.sendMessage(agm);
				}
				
				
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Got ask get with bad text: " + clientMessage.getMessageText());
			}

		} else {
			AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
		}

	}
		
	private String getFileName(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return file.getName().substring(0, name.length() - 2); // empty extension
		}
		return name.substring(0, lastIndexOf - 2);
	}

}