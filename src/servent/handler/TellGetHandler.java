package servent.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import app.AppConfig;
import app.ServentInfo;
import mutex.TokenMutex;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellGetMessage;
import servent.message.util.MessageUtil;

public class TellGetHandler implements MessageHandler {

	private Message clientMessage;

	public TellGetHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		try {

			if (clientMessage.getMessageType() == MessageType.TELL_GET) {

				TellGetMessage tellGetMessage = (TellGetMessage) clientMessage;

				int chordId = Integer.parseInt(tellGetMessage.getMessageText());

				if (AppConfig.myServentInfo.getChordId() == chordId) {

					String storage = AppConfig.WORK_ROUTE_PATH + File.separator;
					File newFile = new File(storage + tellGetMessage.getFileName());

					try {
						newFile.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					AppConfig.chordState.getFileVersions().put(tellGetMessage.getFileName(), tellGetMessage.getVersionOfFIle());
					AppConfig.fileConfig.setFileContent(newFile, tellGetMessage.getContent());
					AppConfig.releaseBothMutex();
				
				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
					TellGetMessage tgm = new TellGetMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							tellGetMessage.getFile(), tellGetMessage.getFileName(), tellGetMessage.getContent(),
							tellGetMessage.getRelativePath(), chordId, tellGetMessage.getVersionOfFIle());

					MessageUtil.sendMessage(tgm);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
