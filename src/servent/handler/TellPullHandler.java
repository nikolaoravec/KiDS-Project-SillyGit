package servent.handler;

import java.io.File;
import java.io.IOException;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellPullMessage;
import servent.message.util.MessageUtil;

public class TellPullHandler implements MessageHandler {

	private Message clientMessage;

	public TellPullHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		try {

			if (clientMessage.getMessageType() == MessageType.TELL_GET) {

				TellPullMessage tellGetMessage = (TellPullMessage) clientMessage;

				int chordId = Integer.parseInt(tellGetMessage.getMessageText());

				if (AppConfig.myServentInfo.getChordId() == chordId) {

					String storage = AppConfig.WORK_ROUTE_PATH + File.separator;
					File newFile = new File(storage + tellGetMessage.getFileName() + tellGetMessage.getExtension());

					try {
						newFile.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					AppConfig.chordState.getFileVersions().put(tellGetMessage.getFileName(), tellGetMessage.getVersionOfFIle());
					AppConfig.fileConfig.setFileContent(newFile, tellGetMessage.getContent());
					System.out.println("Pull succesfull!");
					AppConfig.releaseBothMutex();
				
				} else {
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
					TellPullMessage tgm = new TellPullMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getIpAddress(), nextNode.getListenerPort(), nextNode.getIpAddress(),
							tellGetMessage.getFile(), tellGetMessage.getFileName(), tellGetMessage.getExtension(),tellGetMessage.getContent(),
							tellGetMessage.getRelativePath(), chordId, tellGetMessage.getVersionOfFIle());

					MessageUtil.sendMessage(tgm);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
